package eastwind.io3;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import eastwind.io3.model.BusinessObject;
import eastwind.io3.model.HeadedObject;
import eastwind.io3.model.Header;
import eastwind.io3.model.Request;
import eastwind.io3.model.Response;

@Sharable
public class HeadedObjectCodec extends MessageToMessageCodec<HeadedObject, BusinessObject> {

	@Override
	protected void encode(ChannelHandlerContext ctx, BusinessObject msg, List<Object> out) throws Exception {
		HeadedObject ho = new HeadedObject();
		Header header = new Header();
		ho.setHeader(header);
		if (msg instanceof Request) {
			Request request = (Request) msg;
			header.setModel(Header.REQUEST);
			header.setId(request.getId());
			header.setNamespace(request.getName());
			ho.setObjs(request.getArgs());
		} else if (msg instanceof Response) {
			Response response = (Response) msg;
			header.setModel(Header.RESPONSE);
			header.setId(response.getId());
			if (response.getTh() != null) {
				ho.setTh(response.getTh());
			} else {
				ho.setObj(response.getResult());
			}
		}
		out.add(ho);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, HeadedObject msg, List<Object> out) throws Exception {
		Header header = msg.getHeader();
		byte model = header.getModel();
		if (model == Header.REQUEST) {
			Request request = new Request();
			request.setId(header.getId());
			request.setName(header.getNamespace());
			request.setArgs((Object[]) msg.getObj());
			out.add(request);
		} else if (model == Header.RESPONSE) {
			Response response = new Response();
			response.setId(header.getId());
			if (header.isTh()) {
				response.setTh((Throwable) msg.getObj());
			} else {
				response.setResult(msg.getObj());
			}
			out.add(response);
		}
	}

}
