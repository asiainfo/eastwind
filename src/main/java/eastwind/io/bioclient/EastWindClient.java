package eastwind.io.bioclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eastwind.io.common.CommonUtils;
import eastwind.io.common.Handshake;
import eastwind.io.common.Host;
import eastwind.io.common.InterfAb;
import eastwind.io.common.InvocationFuture;
import eastwind.io.common.InvocationFuturePool;
import eastwind.io.common.KryoFactory;
import eastwind.io.common.Messaging;
import eastwind.io.common.MessagingHandler;
import eastwind.io.common.MessagingHandlerManager;
import eastwind.io.common.Ping;
import eastwind.io.common.Request;
import eastwind.io.common.Response;
import eastwind.io.common.TimedIdSequence100;

public class EastWindClient {

	private String app;
	private volatile Socket socket;

	private int reconnectTime = 15 * 1000;
	private NetState netState = NetState.LOST;
	private List<NetStateListener> netStateListeners = new CopyOnWriteArrayList<>();

	private int timeout = 60 * 1000;
	private long lastReadTime = System.currentTimeMillis();
	private long lastWriteTime = System.currentTimeMillis();

	private volatile Host host;
	private InterfAb interfAb = new InterfAb();
	private volatile boolean firstConnect = false;

	private boolean shutdown = false;

	private ReentrantLock lock = new ReentrantLock();
	private Condition toConnect = lock.newCondition();
	private Condition connected = lock.newCondition();

	private ExecutorService exe = Executors.newFixedThreadPool(3);

	private InvocationFutureHandler invocationFutureHandler = new InvocationFutureHandler();
	private InvocationFuturePool invocationFuturePool = new InvocationFuturePool();
	private MessagingHandlerManager messagingHandlerManager = new MessagingHandlerManager();

	private BlockingQueue<InvocationFuture<?>> sendQueue = new LinkedBlockingQueue<>();
	private WriteThread writeThread;
	private ReadThread readThread;

	private Map<Class<?>, Object> providers = new HashMap<Class<?>, Object>();
	private ClientHandshaker clientHandshaker;

	public EastWindClient(String app) {
		this.app = app;
	}

	public EastWindClient start() {
		writeThread = new WriteThread();
		writeThread.start();
		readThread = new ReadThread();
		readThread.start();
		return this;
	}

	public void connect(Host host) {
		this.host = host;
		firstConnect = true;

		lock.lock();
		toConnect.signalAll();
		lock.unlock();
	}

	@SuppressWarnings("unchecked")
	public <T> T createProvider(Class<T> interf) {
		Object obj = providers.get(interf);
		synchronized (providers) {
			if (obj == null) {
				obj = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interf },
						invocationFutureHandler);
				providers.put(interf, obj);
			}
		}
		return (T) obj;
	}

	public void registerHandler(MessagingHandler messagingHandler) {
		messagingHandlerManager.addHandler(messagingHandler);
	}

	public void addNetStateListener(NetStateListener netStateListener) {
		this.netStateListeners.add(netStateListener);
	}

	public void setReconnectTime(int reconnectTime) {
		this.reconnectTime = reconnectTime;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setClientHandshaker(ClientHandshaker clientHandshaker) {
		this.clientHandshaker = clientHandshaker;
	}

	private void invokeListener(NetStateListener netStateListener) {
		try {
			if (socket == null) {
				netStateListener.stateChanged(null, netState);
			} else {
				netStateListener.stateChanged(socket.getRemoteSocketAddress(), netState);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void notifyNetStateListener() {
		for (int i = 0; i < netStateListeners.size(); i++) {
			NetStateListener netStateListener = netStateListeners.get(i);
			invokeListener(netStateListener);
		}
	}

	private void connect0() {
		try {
			firstConnect = false;
			socket = new Socket(host.getIp(), host.getPort());
			netState = NetState.HANDSHAKING;
			lastWriteTime = System.currentTimeMillis();
			System.out.println("connect ok: " + host);
			lock.lock();
			connected.signalAll();
			lock.unlock();
		} catch (IOException e) {
			System.out.println("connect fail: " + host);
			netState = NetState.LOST;
		}
		notifyNetStateListener();
	}

	private void writeInvocation(InvocationFuture<?> rf) {
		invocationFuturePool.put(rf);
		try {
			System.out.println("->" + host + ":" + JSON.toJSONString(rf.getRequest()));
			writeObject(rf.getRequest());
		} catch (IOException e) {
			invocationFuturePool.remove(rf.getId());
			rf.fail();
			netState = NetState.LOST;
			e.printStackTrace();
			return;
		}
	}

	private void writeObject(Object object) throws IOException {
		lastWriteTime = System.currentTimeMillis();
		OutputStream os = socket.getOutputStream();
		if (object instanceof Ping) {
			os.write(0);
			os.write(4);
			os.write(0);
			os.write(0);
			os.flush();
		} else {
			Output output = IoPut.outPut();
			output.clear();
			FrugalOutputStream fos = (FrugalOutputStream) output.getOutputStream();
			fos.reset();
			Kryo kryo = KryoFactory.getLocalKryo();
			kryo.writeClassAndObject(output, object);
			output.flush();
			int count = fos.count() + 4;

			os.write(count >> 8);
			os.write(count);
			os.write(0);
			os.write(1);

			os.write(fos.buf(), 0, fos.count());
			os.flush();
		}
	}

	private Object read0() throws IOException {
		InputStream is = socket.getInputStream();
		FrugalOutputStream fos = IoPut.frugalOutputStream();
		fos.reset();

		for (int i = 0; i < 4; i++) {
			fos.write(is.read());
		}

		int len = fos.buf()[0] << 8 | fos.buf()[1];
		int v = fos.buf()[2] << 8 | fos.buf()[3];
		
		if (len == 4 && v == 0) {
			return Ping.instance;
		}
		
		fos.reset();
		for (int i = 0; i < len - 4; i++) {
			fos.write(is.read());
		}

		Input input = IoPut.inPut();
		input.setInputStream(new ByteArrayInputStream(fos.buf(), 0, fos.count()));
		Kryo kryo = KryoFactory.getLocalKryo();
		Object obj = kryo.readClassAndObject(input);
		return obj;
	}

	class InvocationFutureHandler implements InvocationHandler {

		private TimedIdSequence100 timedIdSequence100 = new TimedIdSequence100();

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(this, args);
			}
			if (netState != NetState.ACTIVE) {
				throw new RuntimeException("network is not active now");
			}
			InvocationFuture<?> invocationFuture = InvocationFuture.INVOCATION_FUTURE_LOCAL.get();
			if (invocationFuture == null) {
				invocationFuture = new InvocationFuture<Object>();
			}
			if (netState == NetState.LOST) {
				invocationFuture.fail();
			} else {
				Request request = new Request();
				request.setId(timedIdSequence100.newId());
				request.setInterf(method.getDeclaringClass().getCanonicalName());
				request.setName(method.getName());
				request.setArgs(args);

				invocationFuture.setRequest(request);
				invocationFuturePool.put(invocationFuture);
				sendQueue.add(invocationFuture);
			}

			return returnVal(method.getReturnType());
		}

		private Object returnVal(Class<?> c) {
			// boolean, byte, char, short, int, long, float, and double
			return c.isPrimitive() ? 0 : null;
		}
	}

	private class WriteThread extends Thread {

		public WriteThread() {
			super("eastwind-write-thread");
		}

		@Override
		public void run() {
			for (;;) {
				if (shutdown) {
					break;
				}

				if (netState == NetState.LOST) {
					if (!firstConnect) {
						lock.lock();
						try {
							toConnect.await(reconnectTime, TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {
							continue;
						} finally {
							lock.unlock();
						}
					}

					if (host != null) {
						connect0();
					}

				} else {
					try {
						InvocationFuture<?> rf = sendQueue.poll(timeout, TimeUnit.MILLISECONDS);
						if (rf == null) {
							if (System.currentTimeMillis() - lastWriteTime > timeout + 15000) {
								if (!socket.isClosed()) {
									try {
										socket.close();
									} catch (IOException e) {
										// close quietly
									}
								}
							} else {
								try {
									writeObject(Ping.instance);
								} catch (IOException e) {
								}
							}
						} else {
							writeInvocation(rf);
						}
					} catch (InterruptedException e) {
						continue;
					}
				}

			} // for
		} // run
	} // write thread

	private class ReadThread extends Thread {

		public ReadThread() {
			super("eastwind-read-thread");
		}

		@Override
		public void run() {
			for (;;) {
				if (shutdown) {
					break;
				}

				if (netState == NetState.LOST) {
					lock.lock();
					try {
						if (netState == NetState.LOST) {
							try {
								connected.await();
							} catch (InterruptedException e) {
								continue;
							}
						}
					} finally {
						lock.unlock();
					}
				}

				Object obj = null;
				try {
					obj = read0();
					if (obj instanceof Ping) {
					} else {
						System.out.println(host + "->:" + JSON.toJSONString(obj));
					}
				} catch (IOException e) {
					netState = NetState.LOST;
					notifyNetStateListener();
					writeThread.interrupt();
					continue;
				}

				lastReadTime = System.currentTimeMillis();
				if (obj instanceof Response) {
					handRespone((Response<?>) obj);
				} else if (obj instanceof Handshake) {
					handHandshake((Handshake) obj);
				} else if (obj instanceof Messaging) {
					handMessaging((Messaging) obj);
				}
			} // for
		} // run

		private void handHandshake(Handshake handshake) {
			Map<String, Object> in = Collections.unmodifiableMap(handshake.getAttributes());
			if (handshake.getStep() == 1) {
				interfAb.ackUuid(handshake.getApp());
				Map<String, Object> out = new HashMap<String, Object>();
				if (clientHandshaker == null) {
					// TODO
				} else {
					clientHandshaker.prepare(EastWindClient.this, in, out);
				}

				Handshake hs = new Handshake();
				hs.setApp(app);
				hs.setAttributes(out);
				hs.setStep(2);
				hs.setUuid(CommonUtils.UUID);
				try {
					netState = NetState.HANDSHAKING;
					writeObject(hs);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (handshake.getStep() == 3) {
				interfAb.ackUuid(handshake.getApp());
				if (clientHandshaker != null) {
					clientHandshaker.handshakeComplete(in);
				}
				netState = NetState.ACTIVE;
				notifyNetStateListener();
			}
		}

		private void handRespone(final Response<?> respone) {
			exe.execute(new Runnable() {
				@Override
				public void run() {
					InvocationFuture rf = invocationFuturePool.remove(respone.getId());
					rf.done(respone);
				}
			});
		}

		private void handMessaging(Messaging messaging) {
			int type = messaging.getType();
			List<MessagingHandler> handlers = messagingHandlerManager.getHandlers(type);
			for (MessagingHandler handler : handlers) {
				handler.handle(messaging);
			}
		}
	} // read thread
}