package eastwind.io2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import eastwind.io.ObjectCodec;
import eastwind.io.serializer.Serializer;
import eastwind.io.serializer.SerializerFactoryHolder;

public class NetworkTrafficCodec extends ByteToMessageCodec<NetworkTraffic> {

	protected static Logger logger = LoggerFactory.getLogger(ObjectCodec.class);

	private static final byte SIMPLE = 0x01;
	private static final byte HEADED = 0x10;

	protected SerializerFactoryHolder serializerFactoryHolder;

	public NetworkTrafficCodec(SerializerFactoryHolder serializerFactoryHolder) {
		this.serializerFactoryHolder = serializerFactoryHolder;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, NetworkTraffic msg, ByteBuf out) throws Exception {
		logEncode(msg);
		Serializer frameworkSerializer = serializerFactoryHolder.getFrameworkSerializer();
		Serializer contentSerializer = frameworkSerializer;

		if (msg instanceof Headed) {
			Headed headed = (Headed) msg;
			ByteBuf headerBuf = null;
			if (headed.getDataLength() == 0) {
				headerBuf = out;
			} else {
				headerBuf = ctx.alloc().buffer(64);
			}
			headerBuf.writeByte(HEADED);
			headerBuf.writeMedium(0);
			headerBuf.writeShort(0);

			frameworkSerializer.write(headed, new ByteBufOutputStream(headerBuf));

			int i = headerBuf.writerIndex();
			headerBuf.writerIndex(4);
			headerBuf.writeShort(i - 6);
			headerBuf.writerIndex(i);

			if (headed.getDataLength() == 1) {
				frameworkSerializer.write(headed.getData(), new ByteBufOutputStream(out));
			} else if (headed.getDataLength() > 1) {
				headerBuf.writeShort(0);
				int[] dataLens = new int[headed.getDataLength()];
				Object[] datas = (Object[]) headed.getData();

				ByteBufOutputStream os = new ByteBufOutputStream(out);
				for (int j = 0; j < datas.length; j++) {
					int k = out.writerIndex();
					contentSerializer.write(datas[j], os);
					dataLens[j] = out.writerIndex() - k;
				}

				frameworkSerializer.write(dataLens, new ByteBufOutputStream(headerBuf));

				int t = headerBuf.writerIndex();
				headerBuf.writerIndex(i);
				headerBuf.writeShort(t - i - 2);
				i = t;
			}

			if (headerBuf != out) {
				headerBuf.writerIndex(1);
				headerBuf.writeMedium(i - 4 + out.writerIndex());
				headerBuf.writerIndex(i);

				ctx.writeAndFlush(headerBuf);
				TimeUnit.SECONDS.sleep(1);
			}
		} else {
			out.writeByte(SIMPLE);
			out.writeMedium(0);

			int from = out.writerIndex();
			frameworkSerializer.write(msg, new ByteBufOutputStream(out));
			int now = out.writerIndex();
			out.writerIndex(from - 3);
			out.writeMedium(now - from);
			out.writerIndex(now);
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		Serializer frameworkSerializer = serializerFactoryHolder.getFrameworkSerializer();
		Serializer contentSerializer = frameworkSerializer;
		
		if (in.readableBytes() > 1) {
			if (in.readableBytes() > 4) {
				in.markReaderIndex();
				int model = in.readByte();
				int len = in.readMedium();
				if (in.readableBytes() < len) {
					in.resetReaderIndex();
				} else {
					if (model == SIMPLE) {
						Object obj = frameworkSerializer.read(null, new ByteBufInputStream(in, len));

						logDecode(obj);
						out.add(obj);
					} else if (model == HEADED) {
						int headerLen = in.readShort();
						Headed headed = (Headed) frameworkSerializer.read(Headed.class, new ByteBufInputStream(in,
								headerLen));
						
						if (headed.getDataLength() == 1) {
							Object data = contentSerializer.read(null,
									new ByteBufInputStream(in, len - headerLen - 2));
							headed.setData(data);
						} else if (headed.getDataLength() > 1) {
							int sizeLen = in.readShort();
							int[] sizes = (int[]) frameworkSerializer.read(int[].class, new ByteBufInputStream(in,
									sizeLen));

							Object[] objs = new Object[sizes.length];
							
							for (int i = 0; i < sizes.length; i++) {
								objs[i] = contentSerializer.read(null, new ByteBufInputStream(in, sizes[i]));
							}
							headed.setData(objs);
						}
						logDecode(headed);
						out.add(headed);
					}
				}
			}
		}
	}

//	@Override
//	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//	}

	private void logEncode(Object obj) {
		logger.info("encode:{}-{}", contentType(obj), JSON.toJSONString(obj));
	}

	private void logDecode(Object obj) {
		logger.info("decode:{}-{}", contentType(obj), JSON.toJSONString(obj));
	}

	private String contentType(Object obj) {
		return obj.getClass().getSimpleName();
	}
}
