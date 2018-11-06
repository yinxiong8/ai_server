package com.tuti.ai.netty;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class ServerHandler extends ChannelInboundHandlerAdapter {

	public static boolean running = false;

//	private static final String PIC_PATH = "/Users/yinxiong/PycharmProjects/tuti-ml/data/pic/";
//	private static final String WAV_PATH = "/Users/yinxiong/PycharmProjects/tuti-ml/data/wav/";
//	private static final String MIDI_PATH = "/Users/yinxiong/PycharmProjects/tuti-ml/midi/";

	private static final String PIC_PATH = "/home/tuti-ml/data/pic/";
	private static final String WAV_PATH = "/home/tuti-ml/data/wav/";
	private static final String MIDI_PATH = "/home/tuti-ml/midi/";


	/**
	 * 客户端与服务端创建连接的时候调用
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("客户端与服务端连接开始...");
		NettyConfig.group.add(ctx.channel());
	}

	/**
	 * 客户端与服务端断开连接时调用
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("客户端与服务端连接关闭...");
		NettyConfig.group.remove(ctx.channel());
	}

	/**
	 * 服务端接收客户端发送过来的数据结束之后调用
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
		System.out.println("信息接收完毕...");
	}

	/**
	 * 工程出现异常的时候调用
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	/**
	 * 服务端处理客户端websocket请求的核心方法，这里接收了客户端发来的信息
	 */
	@Override
	public void channelRead(ChannelHandlerContext channelHandlerContext, Object info) throws Exception {
		System.out.println("我是服务端，我接受到了：" + info);
		
		if(info instanceof HttpRequest){
			HttpRequest  req = (HttpRequest ) info;
			System.out.println("HttpRequest：" + req);
		}else if(info instanceof HttpContent){
			HttpContent content = (HttpContent) info;

			System.out.println("HttpContent：" + content);

			ByteBuf buf = content.content();

			try {
				String value = buf.toString(CharsetUtil.UTF_8);
				System.out.println("---value:"+value);
				buf.release();

				if(value!=null&&!"".equals(value)){
					int index = value.indexOf("wav_url=");
					if(index>=0){
						praseContent(value, channelHandlerContext);
					}else{
						running = true;
						scanPic(value,channelHandlerContext);
					}
				}

			}catch (Exception e){

			}


		}
	}

	private byte[] getBytes(String filePath) {
		byte[] buffer = null;
		try {
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	private void scanPic(String content, ChannelHandlerContext channelHandlerContext) {

		int index = content.indexOf("pic_url=");
		content = content.substring(index);
		System.out.println("-------content:" + content);
		String[] contents = content.split("=");

		String pic_url;
		try {
			pic_url = java.net.URLDecoder.decode(contents[1], "utf-8");
			System.out.println("-------pic_url:" + pic_url);

			String jpg_name = downOssFile(pic_url,".jpg");

			System.out.println("-------jpg_name:" + jpg_name);
			if (jpg_name != null && !"".equals(jpg_name)) {

				//jpg 2 tif
				Image2Tif.image2Tif(PIC_PATH+jpg_name);


				Channel channel = channelHandlerContext.channel();

				byte[] results = "{success:true}".getBytes();
				int length = results.length;

				ByteBuf buf = Unpooled.buffer(16);

				for(int i=0; i<length; i++){
					buf.writeByte(results[i]);
				}

				writeResponse(channel,buf);

				//移动文件，tif转mid
				try{

					moveFile(PIC_PATH + jpg_name.replace(".jpg",".tif"),"/Users/yinxiong/Documents/SmartScore_Sample_Files/");

					Robot r=new Robot();
					System.out.println("-----f10------");
					r.keyPress(KeyEvent.VK_F10);

				}catch (Exception e){

				}

				//提交匹配曲库，删除文件


				return;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		channelHandlerContext.writeAndFlush(content);
	}

	private void praseContent(String content, ChannelHandlerContext channelHandlerContext) {

		int index = content.indexOf("wav_url=");
		content = content.substring(index);
		System.out.println("-------content:" + content);
		String[] contents = content.split("=");
		// JSONObject jObj = new JSONObject(content);
		// 下载wav，调用预测，生成报告，返回客户端
		// String wav_url = jObj.getString("wav_url");

		String wav_url;
		try {
			long t1 = System.currentTimeMillis();
			wav_url = java.net.URLDecoder.decode(contents[1], "utf-8");
			System.out.println("-------wav_url:" + wav_url);

			String midi_name = downOssFile(wav_url,".wav");
			System.out.println("-------midi_name:" + midi_name);
			if (midi_name != null && !"".equals(midi_name)) {

				long t2 = System.currentTimeMillis();
				
				System.out.println("time1:"+(t2-t1));
				
				predictMidi();

				String midi_path = MIDI_PATH + midi_name.replace(".wav",".mid");

				System.out.println("midi_path:" + midi_path);
				
				long t3 = System.currentTimeMillis();

				deleteWav();

				// channelHandlerContext.writeAndFlush(getBytes(midi_path));
				RequestInfo reqInfo = new RequestInfo();
				reqInfo.setInfo(getBytes(midi_path));
				System.out.println("------reqInfo:" + reqInfo);
//				channelHandlerContext.writeAndFlush(reqInfo);
				
				Channel channel = channelHandlerContext.channel();
//				writeResponse(channel,new StringBuilder("hello"));
				
//				File midi_file = new File(midi_path);
				byte[] file_bytes = getBytes(midi_path);
				int length = file_bytes.length;
				
				ByteBuf buf = Unpooled.buffer(16);
			    //写数据到buffer
			    for(int i=0; i<length; i++){
			        buf.writeByte(file_bytes[i]);
			    }
			    
				writeResponse(channel,buf);
				
				long t4 = System.currentTimeMillis();
				System.out.println("time3:"+(t4-t3));
				return;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		channelHandlerContext.writeAndFlush(content);
	}


	private void deleteWav() {
		File folder = new File(WAV_PATH);
		if (folder.isDirectory()) {
			for (File file : folder.listFiles()) {
				file.delete();
			}
		}
	}

	public static void main(String[] args) {
		String url = "http://yinxiong1.oss-cn-hangzhou.aliyuncs.com/3334_1526427435107.wav";
		try {
			url = java.net.URLDecoder.decode(url, "utf-8");
			System.out.println("---wav_url:" + url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		downOssFile(url,".wav");
	}

	private static String downOssFile(String url,String type) {
		long now = System.currentTimeMillis();
		String filename = now + type;
		String path = WAV_PATH + filename;
		if(type.contains(".jpg")){
			path = PIC_PATH + filename;
		}
		File file = new File(path);

		System.out.println("downOssFile:"+path);

		FileOutputStream fileOut = null;
		HttpURLConnection conn = null;
		InputStream inputStream = null;
		try {
			// 建立链接
			URL httpUrl = new URL(url);
			conn = (HttpURLConnection) httpUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.connect();
			inputStream = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(inputStream);
			if (!path.endsWith("/")) {
				path += "/";
			}
			// 写入到文件（注意文件保存路径的后面一定要加上文件的名称）
			fileOut = new FileOutputStream(path);
			BufferedOutputStream bos = new BufferedOutputStream(fileOut);

			byte[] buf = new byte[4096];
			int length = bis.read(buf);
			// 保存文件
			while (length != -1) {
				bos.write(buf, 0, length);
				length = bis.read(buf);
			}
			bos.close();
			bis.close();
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("抛出异常！！");
		}

		return file.exists() ? (now + type) : "";
	}

	private void predictMidi() {
		try {
			String command[] = new String[] { "/Users/yinxiong/miniconda2/bin/python",
					"/home/tuti-ml/generate_midi.py", "-d",
					"/home/tuti-ml/data/wav/", "baseline", "bin_3" };
//			String command[] = new String[] { "python",
//					"generate_midi.py", "-d",
//					"data/wav/", "baseline", "bin_3" };

			System.out.println("start");
			Process pr = Runtime.getRuntime().exec(command);

			BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			in.close();
			pr.waitFor();
			System.out.println("end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void writeResponse(Channel channel,ByteBuf buf) {

		// Decide whether to close the connection or not.
		boolean close = true;

		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (!close) {
			// There's no need to add 'Content-Length' header
			// if this is the last response.
			response.headers().set(CONTENT_LENGTH, buf.readableBytes());
		}

		// Write the response.
		ChannelFuture future = channel.writeAndFlush(response);
		System.out.println("----buf:"+ buf.readableBytes());
		System.out.println("----future:"+future);
		// Close the connection after the write operation is done if necessary.
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	private void writeResponse(Channel channel,StringBuilder responseContent) {
		// Convert the response content to a ChannelBuffer.
		ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
		
//		ByteBuf buf2 = new ByteBuf(new byte[]);
		
		responseContent.setLength(0);

		// Decide whether to close the connection or not.
		boolean close = true;

		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (!close) {
			// There's no need to add 'Content-Length' header
			// if this is the last response.
			response.headers().set(CONTENT_LENGTH, buf.readableBytes());
		}

		// Write the response.
		ChannelFuture future = channel.writeAndFlush(response);
		System.out.println("----buf:"+ buf.readableBytes());
		System.out.println("----future:"+future);
		// Close the connection after the write operation is done if necessary.
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private boolean moveFile(String fileName,String destinationFloderUrl)
	{
		File file = new File(fileName);
		File destFloder = new File(destinationFloderUrl);
		//检查目标路径是否合法
		if(destFloder.exists())
		{
			if(destFloder.isFile())
			{
				System.out.println("目标路径是个文件，请检查目标路径！");
				return false;
			}
		}else
		{
			if(!destFloder.mkdirs())
			{
				System.out.println("目标文件夹不存在，创建失败！");
				return false;
			}
		}
		//检查源文件是否合法
		if(file.isFile() &&file.exists())
		{
			String destinationFile = destinationFloderUrl+"/"+file.getName();
			if(!file.renameTo(new File(destinationFile)))
			{
				System.out.println("移动文件失败！");
				return false;
			}
		}else
		{
			System.out.println("要备份的文件路径不正确，移动失败！");
			return false;
		}
		System.out.println("已成功移动文件"+file.getName()+"到"+destinationFloderUrl);
		return true;
	}
}