package boc.message.bioclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import boc.message.common.KryoFactory;
import boc.message.common.RequestFuture;
import boc.message.common.RequestFuturePool;
import boc.message.common.RequestInvocationHandler;
import boc.message.common.Respone;
import boc.message.common.SubmitRequest;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CioClient {

	private String app;
	private volatile Socket socket;

	private int reconnectTime = 30;
	private NetState netState = NetState.LOST;
	private List<NetStateListener> netStateListeners = new CopyOnWriteArrayList<>();

	private volatile String ip;
	private volatile int port;
	private volatile boolean firstConnect = false;

	private boolean shutdown = false;

	private ReentrantLock lock = new ReentrantLock();
	private Condition toConnect = lock.newCondition();
	private Condition connected = lock.newCondition();

	private Kryo kryo = KryoFactory.getKryo();
	private ExecutorService exe = Executors.newFixedThreadPool(3);

	private BlockingQueue<RequestFuture<?>> sendQueue = new LinkedBlockingQueue<>();
	private RequestInvocationHandler requestInvocationHandler = new RequestInvocationHandler(new BioSubmitRequest());
	private RequestFuturePool requestFuturePool = new RequestFuturePool();
	private WriteThread writeThread;
	private ReadThread readThread;

	private long lastReadTime = System.currentTimeMillis();

	public CioClient(String app) {
		this.app = app;
	}

	public CioClient start() {
		writeThread = new WriteThread();
		writeThread.start();
		readThread = new ReadThread();
		readThread.start();
		return this;
	}

	public void connect(String ip, int port) {
		this.ip = ip;
		this.port = port;
		firstConnect = true;

		lock.lock();
		toConnect.signal();
		lock.unlock();
	}

	public <T> T createInvoker(Class<T> interf) {
		Object obj = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interf },
				requestInvocationHandler);
		return (T) obj;
	}

	public void addNetStateListener(NetStateListener netStateListener) {
		if (netState == netStateListener.ExecuteIfOnState()) {
			invokeListener(netStateListener);
			if (!netStateListener.oneOff()) {
				this.netStateListeners.add(netStateListener);
			}
		} else {
			this.netStateListeners.add(netStateListener);
		}
	}

	public void setReconnectTime(int reconnectTime) {
		this.reconnectTime = reconnectTime;
	}

	public boolean isConnected() {
		if (socket == null) {
			return false;
		}
		return socket.isConnected();
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
		exe.execute(new Runnable() {
			@Override
			public void run() {
				List<NetStateListener> oneOffs = new LinkedList<NetStateListener>();
				for (int i = 0; i < netStateListeners.size(); i++) {
					NetStateListener netStateListener = netStateListeners.get(i);
					invokeListener(netStateListener);
					if (netStateListener.oneOff()) {
						oneOffs.add(netStateListener);
					}
				}
				if (oneOffs.size() > 0) {
					netStateListeners.removeAll(oneOffs);
				}
			}
		});
	}

	private void connect0() {
		try {
			firstConnect = false;
			socket = new Socket(ip, port);
			netState = NetState.CONNECTED;
			System.out.println("connect ok: " + ip + ":" + port);

			lock.lock();
			connected.signalAll();
			lock.unlock();
		} catch (IOException e) {
			System.out.println("connect fail: " + ip + ":" + port);
			netState = NetState.LOST;
		}
		notifyNetStateListener();
	}

	private void write0(RequestFuture<?> rf) {
		requestFuturePool.put(rf);
		Output output = IoPut.outPut();
		output.clear();
		FrugalOutputStream fos = (FrugalOutputStream) output.getOutputStream();
		fos.reset();
		kryo.writeClassAndObject(output, rf.getRequest());
		output.flush();
		try {
			OutputStream os = socket.getOutputStream();
			int count = fos.count() + 2;

			os.write(count >> 8);
			os.write(count);
			os.write(0);
			os.write(1);

			os.write(fos.buf(), 0, fos.count());
			os.flush();
		} catch (IOException e) {
			requestFuturePool.remove(rf.getId());
			rf.fail();
			netState = NetState.LOST;
			e.printStackTrace();
			return;
		}
	}

	private class BioSubmitRequest implements SubmitRequest {

		@Override
		public void submit(RequestFuture<?> requestFuture) {
			if (netState == NetState.LOST) {
				requestFuture.fail();
				return;
			}
			requestFuturePool.put(requestFuture);
			sendQueue.add(requestFuture);
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

		fos.reset();
		for (int i = 0; i < len - 2; i++) {
			fos.write(is.read());
		}

		Input input = IoPut.inPut();
		input.setInputStream(new ByteArrayInputStream(fos.buf(), 0, fos.count()));
		Object obj = kryo.readClassAndObject(input);
		return obj;
	}

	private class WriteThread extends Thread {

		public WriteThread() {
			super("cioclient-write-thread");
		}

		@Override
		public void run() {
			for (;;) {
				if (shutdown) {
					break;
				}

				if (netState != NetState.CONNECTED) {
					if (!firstConnect) {
						lock.lock();
						try {
							toConnect.await(reconnectTime, TimeUnit.SECONDS);
						} catch (InterruptedException e) {
							continue;
						} finally {
							lock.unlock();
						}
					}

					if (ip != null && ip.trim() != "") {
						connect0();
					}

				} else {
					try {
						RequestFuture<?> rf = sendQueue.take();
						write0(rf);
					} catch (InterruptedException e) {
						continue;
					}
				}

			} // for
		} // run
	} // write thread

	private class ReadThread extends Thread {

		public ReadThread() {
			super("cioclient-read-thread");
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
				} catch (IOException e) {
					netState = NetState.LOST;
					writeThread.interrupt();
					continue;
				}

				lastReadTime = System.currentTimeMillis();
				if (obj instanceof Respone) {
					final Respone<?> respone = (Respone<?>) obj;
					exe.execute(new Runnable() {
						@Override
						public void run() {
							RequestFuture rf = requestFuturePool.remove(respone.getId());
							rf.done(respone);
						}
					});
				}
			} // for
		} // run
	} // read thread
}