package com.tuti.ai.netty;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

//	private static final String WAV_PATH = "/Users/yinxiong/PycharmProjects/tuti-ml/data/wav/";
//	private static final String MIDI_PATH = "/Users/yinxiong/PycharmProjects/tuti-ml/midi/";
	private static final String WAV_PATH = "/home/miaopeng/wav/";
	private static final String MIDI_PATH = "/home/miaopeng/midi/";

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
		}else if(info instanceof HttpContent){
			HttpContent content = (HttpContent) info;
			ByteBuf buf = content.content();
			String value = buf.toString(CharsetUtil.UTF_8);
			buf.release();
			
			if(value!=null&&!"".equals(value)){
				praseContent(value, channelHandlerContext);
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
			wav_url = java.net.URLDecoder.decode(contents[1], "utf-8");
			System.out.println("-------wav_url:" + wav_url);

			String midi_name = downOssFile(wav_url);
			if (midi_name != null && !"".equals(midi_name)) {

				predictMidi();

				String midi_path = MIDI_PATH + midi_name;

				System.out.println("midi_path:" + midi_path);

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
		String url = "http%3A//yinxiong1.oss-cn-hangzhou.aliyuncs.com/653_1524896557595.wav";
		try {
			String wav_url = java.net.URLDecoder.decode(url, "utf-8");
			System.out.println("---wav_url:" + wav_url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// downOssFile("http://yinxiong1.oss-cn-hangzhou.aliyuncs.com/3818_1524884096773.wav?Expires=1524891106&OSSAccessKeyId=LTAIwZB2rTS4VyQQ&Signature=HDE5kiuut%2Bi1pYUDdufn2gYbctU%3D");
	}

	private String downOssFile(String url) {
		long now = System.currentTimeMillis();
		String filename = now + ".wav";
		String path = WAV_PATH + filename;
		File file = new File(path);

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

		return file.exists() ? (now + ".mid") : "";
	}

	private void predictMidi() {
		try {
			String command[] = new String[] { "/Users/yinxiong/miniconda2/bin/python",
					"/Users/yinxiong/PycharmProjects/tuti-ml/generate_midi.py", "-d",
					"/Users/yinxiong/PycharmProjects/tuti-ml/data/wav/", "baseline", "bin_3" };

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
}