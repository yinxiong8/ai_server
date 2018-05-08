package com.tuti.ai.netty;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

public class ClientStart {
	public static void main(String[] args) {
		// Scanner input = new Scanner(System.in);
		// Client bootstrap = new Client(8080, "127.0.0.1");
		//
		// String infoString = "";
		// while (true) {
		// infoString = input.nextLine();
		// RequestInfo req = new RequestInfo();
		//// req.setType((byte)1);
		// req.setInfo(infoString.getBytes());
		// bootstrap.sendMessage(req);
		// }

		byte[] info = "wav_url=http://yinxiong1.oss-cn-hangzhou.aliyuncs.com/1787_1524898090996.wav".getBytes();
		//
		// byte[] data = new byte[1+info.length];
		// for(int i = 0;i<data.length;i++){
		// if(i==0){
		// data[i] = (byte)1;
		// }else{
		// data[i] = info[i-1];
		// }
		//
		// }
		//
		httpPostWithJson("http://d03b202b.ngrok.io", info);
	}

	public static boolean httpPostWithJson(String url, byte[] bytes) {
		boolean isSuccess = false;

		HttpPost post = null;
		try {
			HttpClient httpClient = new DefaultHttpClient();

			// 设置超时时间
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 200000);
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 200000);

			post = new HttpPost(url);

			// 构建消息实体
			post.setEntity(new ByteArrayEntity(bytes));

			org.apache.http.HttpResponse response = httpClient.execute(post);

			// 检验返回码
			int statusCode = response.getStatusLine().getStatusCode();
			System.out.println("statusCode: " + statusCode);
			if (statusCode != HttpStatus.SC_OK) {
				System.out.println("请求出错: " + statusCode);
				isSuccess = false;
			} else {
				int retCode = 0;
				String sessendId = "";
				// 返回码中包含retCode及会话Id
				for (Header header : response.getAllHeaders()) {
					if (header.getName().equals("retcode")) {
						retCode = Integer.parseInt(header.getValue());
					}
					if (header.getName().equals("SessionId")) {
						sessendId = header.getValue();
					}
				}
			}

			System.out.println("------------------response:" + response);
			System.out.println("------------------getEntity:" + response.getEntity());

			byte[] file_bytes = EntityUtils.toByteArray(response.getEntity());

			// String result = EntityUtils.toString(response.getEntity(),
			// Charset.forName("UTF-8"));

			System.out.println("------------------file_bytes:" + file_bytes);
			
			getFile(file_bytes,"/Users/yinxiong/tools/a.mid");
		} catch (Exception e) {
			e.printStackTrace();
			isSuccess = false;
		} finally {
			if (post != null) {
				try {
					post.releaseConnection();
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return isSuccess;
	}

	public static void getFile(byte[] bfile, String file_path) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			file = new File(file_path);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bfile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
