package com.tuti.ai.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

//	@Override
//	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
//			List<Object> out) throws Exception {
//		// 标记开始读取位置
//		in.markReaderIndex();
//		// 判断协议类型
//		byte type = in.readByte();
////		int type = in.readInt();
//		System.out.println("decode type:"+type);
//		RequestInfo requestInfo = new RequestInfo();
//		if(type==1){
//			// in.readableBytes()即为剩下的字节数
//			byte[] info = new byte[in.readableBytes()];
//			in.readBytes(info);
//			requestInfo.setInfo(new String(info, "utf-8"));
//		}else if(type==2){
//			// in.readableBytes()即为剩下的字节数
//			byte[] file = new byte[in.readableBytes()];
//			in.readBytes(file);
//			requestInfo.setFile(file);
//		}
//		// 最后把你想要交由ServerHandler的数据添加进去，就可以了
//		out.add(requestInfo);
//	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		// 标记开始读取位置
		in.markReaderIndex();
		// 判断协议类型
		RequestInfo requestInfo = new RequestInfo();

		byte[] info = new byte[in.readableBytes()];
		in.readBytes(info);
		requestInfo.setInfo(info);
		// 最后把你想要交由ServerHandler的数据添加进去，就可以了
		out.add(requestInfo);
	}
}