package eastwind.io2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ObjectCodec extends ByteToMessageCodec<Object> {

	private static Logger logger = LoggerFactory.getLogger(ObjectCodec.class);
	
	private static final byte PING = 0;
	private static final byte SIMPLE = 0x55;
	private static final byte HEADED_OBJECT = 0x79;

	private SelfDescribedSerializer contentSerializer = new KryoSerializer();

	@Override
	protected void encode(ChannelHandlerContext ctx, Object message, ByteBuf out) throws Exception {
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

			Kryo kryo = KryoUtil.getKryo();
			Output output = KryoUtil.getOutPut();
			write(headerBuf, kryo, output, header, false);

			int i = headerBuf.writerIndex();
			headerBuf.writerIndex(4);
			headerBuf.writeShort(i - 6);
			if (header.getSize() == 0) {
				headerBuf.writerIndex(i);
			} else {
				if (header.getSize() == 1) {
					contentSerializer.write(new ByteBufOutputStream(out), headedObject.getObj());
				} else if (header.getSize() > 1) {
					headerBuf.writerIndex(i);
					headerBuf.writeShort(0);
					Object[] objs = (Object[]) headedObject.getObj();
					int[] objLens = new int[objs.length];
					ByteBufOutputStream os = new ByteBufOutputStream(out);
					for (int j = 0; j < objs.length; j++) {
						int k = out.writerIndex();
						contentSerializer.write(os, objs[j]);
						objLens[j] = out.writerIndex() - k;
					}

					write(headerBuf, kryo, output, objLens, false);

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

			Kryo kryo = KryoUtil.getKryo();
			Output output = KryoUtil.getOutPut();
			write(out, kryo, output, message, true);

			int now = out.writerIndex();
			out.writerIndex(from - 3);
			out.writeMedium(now - from);
			out.writerIndex(now);
		}
	}

	private void write(ByteBuf buf, Kryo kryo, Output output, Object obj, boolean cls) {
		output.setOutputStream(new ByteBufOutputStream(buf));
		if (cls) {
			kryo.writeClassAndObject(output, obj);
		} else {
			kryo.writeObject(output, obj);
		}
		output.flush();
	}

	private void logEncode(Object obj) {
		logger.info("encode:{}", JSON.toJSONString(obj));
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() > 0) {
			if (in.getByte(0) == PING) {
				in.readByte();
				out.add(Ping.INSTANCE);
			} else {
				in.markReaderIndex();
				int model = in.readByte();
				int len = in.readMedium();
				if (in.readableBytes() < len) {
					in.resetReaderIndex();
				} else {
					if (model == SIMPLE) {
						Kryo kryo = KryoUtil.getKryo();
						Input input = KryoUtil.getInPut();
						input.setInputStream(new ByteBufInputStream(in, len));
						Object obj = kryo.readClassAndObject(input);

						logDecode(obj);
						out.add(obj);
					} else if (model == HEADED_OBJECT) {
						int headerLen = in.readShort();
						HeadedObject headedObject = new HeadedObject();

						Kryo kryo = KryoUtil.getKryo();
						Input input = KryoUtil.getInPut();
						input.setInputStream(new ByteBufInputStream(in, headerLen));
						Header header = kryo.readObject(input, Header.class);

						headedObject.setHeader(header);
						if (header.getSize() == 1) {
							Object obj = contentSerializer.read(new ByteBufInputStream(in, len - headerLen - 2));
							headedObject.setObj(obj);
						} else if (header.getSize() > 1) {
							int sizeLen = in.readShort();

							input.setInputStream(new ByteBufInputStream(in, sizeLen));
							int[] sizes = kryo.readObject(input, int[].class);

							Object[] objs = new Object[sizes.length];
							for (int i = 0; i < sizes.length; i++) {
								objs[i] = contentSerializer.read(new ByteBufInputStream(in, sizes[i]));
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

	private void logDecode(Object obj) {
		logger.info("decode:{}", JSON.toJSONString(obj));
	}
}
