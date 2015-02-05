package boc.message.bioclient;

import java.io.ByteArrayOutputStream;

public class FrugalOutputStream extends ByteArrayOutputStream {

	public byte[] buf() {
		return super.buf;
	}
	
	public int count() {
		return super.count;
	}
}
