package eastwind.io2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

import com.alibaba.fastjson.JSON;

public class ObjectCodec extends ByteToMessageCodec<Object> {

	public static final byte PING = 0;
	public static final byte SIMPLE = 0x55;
	public static final byte RPC = (byte) 0xaa;

	private SelfDescribedSerializer internalSerializer = new KryoSerializer();
	private SelfDescribedSerializer contentSerializer = internalSerializer;

	@Override
	protected void encode(ChannelHandlerContext ctx, Object message, ByteBuf out) throws Exception {
		if (message instanceof Ping) {
			out.writeByte(0);
		} else if (message instanceof Request || message instanceof Response) {
			ByteBuf headerBuf = ctx.alloc().buffer();
			headerBuf.writeByte(RPC);
			headerBuf.writeMedium(0);

			int headerFrom = headerBuf.writerIndex();
			int contentFrom = out.writerIndex();
			headerBuf.writeShort(0);

			if (message instanceof Request) {
				writeRequest(message, headerBuf, out);
			} else if (message instanceof Response) {
				writeResponse(message, headerBuf, out);
			}

			int headerNow = headerBuf.writerIndex();
			headerBuf.writerIndex(headerFrom);
			int headerLen = headerNow - headerFrom;
			headerBuf.writeShort(headerLen - 2);

			int contentNow = out.writerIndex();
			headerBuf.writerIndex(headerFrom - 3);
			int contentLen = contentNow - contentFrom;
			headerBuf.writeMedium(headerLen + contentLen);
			headerBuf.writerIndex(headerNow);

			ctx.write(headerBuf);
			
			System.out.println("encode:" + JSON.toJSONString(message));
		} else {
			out.writeByte(SIMPLE);
			out.writeMedium(0);
			int from = out.writerIndex();
			internalSerializer.write(new ByteBufOutputStream(out), message);
			int now = out.writerIndex();
			out.writerIndex(from - 3);
			out.writeMedium(now - from);
			out.writerIndex(now);
		}
	}

	private void writeRequest(Object message, ByteBuf headerBuf, ByteBuf contentBuf) {
		Request request = (Request) message;
		int contentFrom = contentBuf.writerIndex();
		ByteBufOutputStream bbos = new ByteBufOutputStream(contentBuf);
		if (RequestHeader.isMessage(request.getHeader())) {
			contentSerializer.write(bbos, request.getArg());
			request.getHeader().setParamLens(new int[] { contentBuf.writerIndex() - contentFrom });
		} else if (RequestHeader.isRpc(request.getHeader())) {
			Object[] args = (Object[]) request.getArg();
			int[] paramLens = new int[args.length];
			for (int i = 0; i < args.length; i++) {
				int wi = contentBuf.writerIndex();
				contentSerializer.write(bbos, args[i]);
				paramLens[i] = contentBuf.writerIndex() - wi;
			}
			request.getHeader().setParamLens(paramLens);
		}
		internalSerializer.write(new ByteBufOutputStream(headerBuf), request.getHeader());
	}

	private void writeResponse(Object message, ByteBuf headerBuf, ByteBuf contentBuf) {
		Response response = (Response) message;
		ByteBufOutputStream bbos = new ByteBufOutputStream(contentBuf);
		contentSerializer.write(bbos, response.getResult());
		internalSerializer.write(new ByteBufOutputStream(headerBuf), response.getHeader());
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
				if (in.readableBytes() >= len) {

					if (model == RPC) {
						int headerLen = in.readShort();
						Object header = internalSerializer.read(new ByteBufInputStream(in, headerLen));
						ByteBuf contentBuf = in.slice(in.readerIndex(), len - headerLen - 2);
						in.skipBytes(contentBuf.readableBytes());
						if (header instanceof RequestHeader) {
							decodeRequest(out, header, contentBuf);
						} else if (header instanceof ResponseHeader) {
							decodeResponse(out, header, contentBuf);
						}

					} else if (model == SIMPLE) {
						Object obj = internalSerializer.read(new ByteBufInputStream(in, len));
						logDecode(obj);
						out.add(obj);
					}
				} else {
					in.resetReaderIndex();
				}
			}
		}
	}

	private void decodeResponse(List<Object> out, Object header, ByteBuf buf) {
		ResponseHeader responseHeader = (ResponseHeader) header;
		Response response = new Response();
		response.setHeader(responseHeader);
		Object content = contentSerializer.read(new ByteBufInputStream(buf));
		response.setResult(content);
		out.add(response);
		logDecode(response);
	}

	private void decodeRequest(List<Object> out, Object header, ByteBuf buf) {
		RequestHeader requestHeader = (RequestHeader) header;
		Request request = new Request();
		request.setHeader(requestHeader);
		if (RequestHeader.isMessage(requestHeader)) {
			Object content = contentSerializer.read(new ByteBufInputStream(buf));
			request.setArg(content);
		} else if (RequestHeader.isRpc(requestHeader)) {
			Object[] args = new Object[requestHeader.getParamLens().length];
			for (int i = 0; i < args.length; i++) {
				args[i] = contentSerializer.read(new ByteBufInputStream(buf, requestHeader.getParamLens()[i]));
			}
			request.setArg(args);
		}
		out.add(request);
		logDecode(request);
	}

	private void logDecode(Object obj) {
		System.out.println("decode:" + JSON.toJSONString(obj));
	}

}
