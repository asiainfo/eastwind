package eastwind.io;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.io.model.BusinessObject;
import eastwind.io.model.HeadedObject;
import eastwind.io.model.Header;
import eastwind.io.model.Request;
import eastwind.io.model.Response;

@Sharable
public class HeadedObjectCodec extends MessageToMessageCodec<HeadedObject, BusinessObject> {

	private static Logger logger = LoggerFactory.getLogger(HeadedObjectCodec.class);
	
	@Override
	protected void encode(ChannelHandlerContext ctx, BusinessObject msg, List<Object> out) throws Exception {
		logger.debug("{}", msg);
		HeadedObject ho = new HeadedObject();
		Header header = new Header();
		ho.setHeader(header);
		if (msg instanceof Request) {
			Request request = (Request) msg;
			header.setModel(Header.REQUEST);
			header.setId(request.getId());
			header.setBinary(request.isBinary());
			header.setName(request.getName());
			ho.setObjs(request.getArgs());
		} else if (msg instanceof Response) {
			Response response = (Response) msg;
			header.setModel(Header.RESPONSE);
			header.setId(response.getId());
			header.setBinary(response.isBinary());
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
			request.setBinary(header.isBinary());
			request.setName(header.getName());
			request.setArgs((Object[]) msg.getObj());
			out.add(request);
		} else if (model == Header.RESPONSE) {
			Response response = new Response();
			response.setId(header.getId());
			response.setBinary(header.isBinary());
			if (header.isTh()) {
				response.setTh((Throwable) msg.getObj());
			} else {
				response.setResult(msg.getObj());
			}
			out.add(response);
		}
	}

}
