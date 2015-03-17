package boc.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

import boc.message.common.Ping;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CioCodec extends ByteToMessageCodec<Object> {

	private String app;
	private Kryo kryo;

	public CioCodec(String app, Kryo kryo) {
		this.app = app;
		this.kryo = kryo;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		ByteBuf buf = ctx.alloc().buffer();
		if (msg instanceof Ping) {
			System.out.println("ping ->");
			buf.writeShort(0);
		} else {
			System.out.println(app + " send:" + JSON.toJSONString(msg));
			buf.writeShort(1);
			Output output = IoPut.outPut();
			output.clear();
			output.setOutputStream(new ByteBufOutputStream(buf));
			kryo.writeClassAndObject(output, msg);
			output.flush();
		}
		ctx.writeAndFlush(buf);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 0) {
			return;
		}
		short v = in.readShort();
		switch (v) {
		case 0:
			System.out.println("ping <-");
			out.add(Ping.instance);
			break;
		case 1:
			Input input = IoPut.inPut();
			ByteBufInputStream bbis = new ByteBufInputStream(in);
			input.setInputStream(bbis);
			Object obj = kryo.readClassAndObject(input);
			System.out.println(app + " receive:" + JSON.toJSONString(obj));
			out.add(obj);
			break;
		}
	}
}
