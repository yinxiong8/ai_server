package com.tuti.ai.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<RequestInfo> {

	@Override
	protected void encode(ChannelHandlerContext ctx, RequestInfo msg,
			ByteBuf out) throws Exception {

		ByteBufOutputStream writer = new ByteBufOutputStream(out);
		
		byte[] info = null;

		if (msg != null && msg.getInfo() != null ) {
			info = msg.getInfo();
			writer.write(info);
		}
		
//		if(type ==1){
//			//string
//			byte[] info = null;
//
//			if (msg != null && msg.getInfo() != null && msg.getInfo() != "") {
//				info = msg.getInfo().getBytes("utf-8");
//				writer.write(info);
//			}
//		}else if(type==2){
//			//file
//			byte[] file = msg.getFile();
//			if(file!=null){
//				writer.write(file);
//			}
//		}
	}

}