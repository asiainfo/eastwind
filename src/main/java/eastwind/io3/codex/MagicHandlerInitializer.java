package eastwind.io3.codex;

import io.netty.buffer.ByteBuf;

import java.util.List;

public abstract class MagicHandlerInitializer extends HandlerInitializer {

	public byte[] getMagic() {
		return null;
	}

	public List<byte[]> getMagics() {
		return null;
	}

	@Override
	public MatchState match(ByteBuf in) {
		MatchState state = match(in, getMagic());
		if (state == MatchState.MATCHED) {
			return state;
		}
		boolean matching = state == MatchState.MATCHING;
		List<byte[]> magics = getMagics();
		for (int i = 0; i < magics.size(); i++) {
			state = match(in, magics.get(i));
			if (state == MatchState.MATCHED) {
				return state;
			}
			if (state == MatchState.MATCHING && !matching) {
				matching = true;
			}
		}
		return matching ? MatchState.MATCHING : MatchState.UN_MATCHED;
	}

	private MatchState match(ByteBuf in, byte[] magic) {
		int i = 0;
		for (; i < magic.length && i < in.readableBytes(); i++) {
			if (in.getByte(i) != magic[i]) {
				return MatchState.UN_MATCHED;
			}
		}
		return i == magic.length ? MatchState.MATCHED : MatchState.MATCHING;
	}

}
