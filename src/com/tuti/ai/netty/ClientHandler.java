package com.tuti.ai.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("我是客户端：" +(msg instanceof RequestInfo));
        RequestInfo info = (RequestInfo) msg;
     
        
        System.out.println("我是客户端 msg：" +new String(info.getInfo(),"utf-8"));
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("通道读取完毕！");
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(null != cause) cause.printStackTrace();
        if(null != ctx) ctx.close();
    }

}