package com.tuti.ai.netty;

import java.util.Arrays;

public class RequestInfo {

	// public static final int TYPE_PREDICT_MIDI = 1;

	private byte[] info;

	public byte[] getInfo() {
		return info;
	}

	public void setInfo(byte[] info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "RequestInfo [info=" + Arrays.toString(info) + "]";
	}
	
	

}
