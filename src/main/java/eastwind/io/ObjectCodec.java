package eastwind.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import eastwind.io.invocation.HandlerRegistry;
import eastwind.io.invocation.MethodHandler;
import eastwind.io.model.FrameworkObjects;
import eastwind.io.model.HeadedObject;
import eastwind.io.model.Header;
import eastwind.io.model.Ping;
import eastwind.io.model.Request;
import eastwind.io.model.UniqueHolder;
import eastwind.io.serializer.Serializer;
import eastwind.io.serializer.SerializerFactoryHolder;

public class ObjectCodec extends ByteToMessageCodec<Object> {

	private static Logger logger = LoggerFactory.getLogger(ObjectCodec.class);

	private static final byte PING = 0;
	private static final byte SIMPLE = 0x55;
	private static final byte HEADED_OBJECT = 0x79;

	private SerializerFactoryHolder serializerFactoryHolder;
	private HandlerRegistry handlerRegistry;
	private TransmitSustainer transmitSustainer;

	public ObjectCodec(SerializerFactoryHolder serializerFactoryHolder, HandlerRegistry handlerRegistry,
			TransmitSustainer transmitSustainer) {
		this.serializerFactoryHolder = serializerFactoryHolder;
		this.handlerRegistry = handlerRegistry;
		this.transmitSustainer = transmitSustainer;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object message, ByteBuf out) throws Exception {
		Serializer internalSerializer = serializerFactoryHolder.getInternalSerializer();
		Serializer contentSerializer = serializerFactoryHolder.getInternalSerializer();

		if (message instanceof Ping) {
			out.writeByte(0);
		} else if (message instanceof HeadedObject) {
			logEncode(message);
			HeadedObject headedObject = (HeadedObject) message;
			Header header = headedObject.getHeader();

			ByteBuf headerBuf = ctx.alloc().buffer(64);
			headerBuf.writeByte(HEADED_OBJECT);
			headerBuf.writeMedium(0);
			headerBuf.writeShort(0);

			internalSerializer.write(header, new ByteBufOutputStream(headerBuf));

			int i = headerBuf.writerIndex();
			headerBuf.writerIndex(4);
			headerBuf.writeShort(i - 6);
			if (header.getSize() == 0) {
				headerBuf.writerIndex(i);
			} else {
				if (header.getSize() == 1) {
					contentSerializer.write(headedObject.getObj(), new ByteBufOutputStream(out));
				} else if (header.getSize() > 1) {
					headerBuf.writerIndex(i);
					headerBuf.writeShort(0);
					Object[] objs = (Object[]) headedObject.getObj();
					int[] objLens = new int[objs.length];
					ByteBufOutputStream os = new ByteBufOutputStream(out);
					for (int j = 0; j < objs.length; j++) {
						int k = out.writerIndex();
						contentSerializer.write(objs[j], os);
						objLens[j] = out.writerIndex() - k;
					}

					internalSerializer.write(objLens, new ByteBufOutputStream(headerBuf));

					int t = headerBuf.writerIndex();
					headerBuf.writerIndex(i);
					headerBuf.writeShort(t - i - 2);
					i = t;
				}

				headerBuf.writerIndex(1);
				headerBuf.writeMedium(i - 4 + out.writerIndex());
				headerBuf.writerIndex(i);
			}

			ctx.write(headerBuf);
		} else {
			logEncode(message);
			out.writeByte(SIMPLE);
			out.writeMedium(0);

			int from = out.writerIndex();
			internalSerializer.write(message, new ByteBufOutputStream(out));
			int now = out.writerIndex();
			out.writerIndex(from - 3);
			out.writeMedium(now - from);
			out.writerIndex(now);
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		Serializer internalSerializer = serializerFactoryHolder.getInternalSerializer();
		Serializer contentSerializer = serializerFactoryHolder.getInternalSerializer();

		if (in.readableBytes() > 0) {
			if (in.getByte(0) == PING) {
				in.readByte();
				out.add(FrameworkObjects.PING);
			} else {
				in.markReaderIndex();
				int model = in.readByte();
				int len = in.readMedium();
				if (in.readableBytes() < len) {
					in.resetReaderIndex();
				} else {
					if (model == SIMPLE) {
						Object obj = internalSerializer.read(null, new ByteBufInputStream(in, len));

						logDecode(obj);
						out.add(obj);
					} else if (model == HEADED_OBJECT) {
						int headerLen = in.readShort();
						HeadedObject headedObject = new HeadedObject();
						Header header = (Header) internalSerializer.read(Header.class, new ByteBufInputStream(in,
								headerLen));
						headedObject.setHeader(header);

						if (header.getSize() == 1) {
							if (header.getModel() == Header.REQUEST) {
								MethodHandler methodHandler = handlerRegistry.findHandler(header.getName());
								Class<?> cls = methodHandler.getParameterTypes()[0];
								Object obj = contentSerializer.read(cls,
										new ByteBufInputStream(in, len - headerLen - 2));
								headedObject.setObj(obj);
							} else if (header.getModel() == Header.RESPONSE) {
								@SuppressWarnings("rawtypes")
								TransmitPromise transmitPromise = transmitSustainer.get(header.getId());
								Request request = (Request) transmitPromise.getMessage();
								Object obj = contentSerializer.read(request.getReturnType(), new ByteBufInputStream(in,
										len - headerLen - 2));
								headedObject.setObj(obj);
							}
						} else if (header.getSize() > 1) {
							int sizeLen = in.readShort();
							int[] sizes = (int[]) internalSerializer.read(int[].class, new ByteBufInputStream(in,
									sizeLen));

							Object[] objs = new Object[sizes.length];
							for (int i = 0; i < sizes.length; i++) {
								MethodHandler methodHandler = handlerRegistry.findHandler(header.getName());
								Class<?> cls = methodHandler.getParameterTypes()[i];
								objs[i] = contentSerializer.read(cls, new ByteBufInputStream(in, sizes[i]));
							}
							headedObject.setObjs(objs);
						}
						logDecode(headedObject);
						out.add(headedObject);
					}
				}
			}
		}
	}

	private void logEncode(Object obj) {
		logger.info("encode:{}-{}", contentType(obj), JSON.toJSONString(obj));
	}

	private void logDecode(Object obj) {
		logger.info("decode:{}-{}", contentType(obj), JSON.toJSONString(obj));
	}

	private String contentType(Object obj) {
		if (obj instanceof HeadedObject) {
			HeadedObject ho = (HeadedObject) obj;
			switch (ho.getHeader().getModel()) {
			case Header.MESSAGE:
				return "Message";
			case Header.REQUEST:
				return "Request";
			case Header.RESPONSE:
				return "Response";
			}
		} else if (obj instanceof UniqueHolder) {
			UniqueHolder uo = (UniqueHolder) obj;
			return uo.getObj().getClass().getSimpleName();
		}
		return obj.getClass().getSimpleName();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause.getClass() != IOException.class) {
			super.exceptionCaught(ctx, cause);
		}
	}
}
