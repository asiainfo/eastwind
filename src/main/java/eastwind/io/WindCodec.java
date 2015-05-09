package eastwind.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eastwind.io.common.KryoFactory;
import eastwind.io.common.Ping;

public class WindCodec extends ByteToMessageCodec<Object> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		ByteBuf buf = ctx.alloc().buffer();
		if (msg instanceof Ping) {
			System.out.println("ping ->");
			buf.writeShort(0);
		} else {
			System.out.println(ctx.channel() + " send:" + JSON.toJSONString(msg));
			buf.writeShort(1);
			Output output = IoPut.outPut();
			output.clear();
			output.setOutputStream(new ByteBufOutputStream(buf));
			Kryo kryo = KryoFactory.getLocalKryo();
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
			Kryo kryo = KryoFactory.getLocalKryo();
			Object obj = kryo.readClassAndObject(input);
			System.out.println(ctx.channel() + " receive:" + JSON.toJSONString(obj));
			out.add(obj);
			break;
		}
	}
}
