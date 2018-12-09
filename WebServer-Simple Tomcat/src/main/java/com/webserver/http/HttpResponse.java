package com.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.webserver.core.ServerContext;

/**
 * 响应对象
 * 一个响应应当包含三部分：
 * 状态行
 * 响应头
 * 响应正文
 * @author soft01
 *
 */
public class HttpResponse {
	/*
	 * 状态行相关信息定义
	 */
	//状态代码
	private int statusCode = 200;
	//状态描述
	private String statusReason = "OK";
	/*
	 * 响应头相关信息定义
	 */
	private Map<String,String> headers = new HashMap<String,String>();
	
	/*
	 * 响应正文相关信息定义
	 */
	//响应实体文件
	private File entity;
	
	
	private Socket socket;
	//通过Socket获取输出流，用于给客户端发送响应内容
	private OutputStream out;
	
	public HttpResponse(Socket socket) {
		
		try {
			this.socket = socket;
			this.out = socket.getOutputStream();
			String line = ServerContext.protocol+" "+statusCode+" "+statusReason;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将当前响应对象内容发送给客户端
	 */
	public void flush() {
		/*
		 * 发送状态行
		 * 发送响应头
		 * 发送响应正文
		 */
		sendStatusLine();
		sendHeaders();
		sendContent();
	}
	
	/**
	 * 发送状态行
	 */
	
	private void sendStatusLine() {

		try {
			String line = "HTTP/1.1"+" "+statusCode+" "+statusReason;
			println(line);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * 发送响应头
	 */
	private void sendHeaders() {
		try {
			//发送所有响应头
			/*
			 * key 						value
			 * Content-Type   text/html
			 * Content-Length  1234
			 */
			Set<Entry<String,String>> entrySet = headers.entrySet(); 
			for(Entry<String,String> header:entrySet) {
				String key = header.getKey();
				String value = header.getValue();
				String line = key+": "+value;
				println(line);
			}
			
			
			//单独发送CRLF表示响应头发送完毕
			println("");
		}catch(Exception e) {
			
		}
	}
	/**
	 * 发送响应正文
	 */
	private void sendContent() {
		if(entity!=null) {//先判断文件存不存在
			try (FileInputStream fis = new FileInputStream(entity);//这样写会自动关闭
				){
			byte[] data = new byte[1024*10];
			int len = -1;
			while((len=fis.read(data))!=-1) {
				out.write(data,0,len);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		}
	}
	/**
	 * 向客户端发送一个字符串，该字符串发送后会单独发送CR，LF
	 * @param line
	 */
	private void println(String line) {
		try {
			out.write(line.getBytes("ISO8859-1"));
			out.write(HttpContext.CR);//written CR
			out.write(HttpContext.LF);//written LF
		}  catch (Exception e) {
		
			e.printStackTrace();
		}
	}
	
	
	public File getEntity() {
		return entity;
	}
	/**
	 * 设置要响应给客户端的实体资源文件
	 * 再设置的同时会自动添加两个响应头
	 * Content-Type与Content-Length
	 * @param entity
	 */
	public void setEntity(File entity) {
		this.entity = entity;
		this.headers.put("Content-Length", entity.length()+"");
		/*
		 * 设置Content-Type时，要先根据文件名的后缀得到对应的值
		 */
		String fileName = entity.getName();//获取文件名
		int index = fileName.lastIndexOf(".")+1;//获取最后一个"."的下标，+1表示"."后面的第一个字母下标
		String ext = fileName.substring(index);//获取文件名的后缀
		String contentType = HttpContext.getContentType(ext);//利用HttpContext类中的静态方法获取对应的contentType
		
		
		this.headers.put("Content-Type",contentType);
	}

	public int getStatusCode() {
		return statusCode;
	}
	/**
	 * 设置状态代码
	 * 在设置状态代码的同时会将对应的状态描述默认值
	 * 自动设置好
	 * 若希望自行设置状态描述，可以单独调用对应的方法
	 * @param statusCode
	 * @see setStatusReason(String StatusReason)
	 */

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
		this.statusReason = HttpContext.getStatusReason(statusCode);
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}
	/**
	 *  向当前响应中设置一个响应头信息
	 * （后期自行重构时，还会添加获取头，以及山出头的操作）
	 * @param name
	 * @param value
	 */
	public void putHeader(String name,String value) {
		this.headers.put(name, value);
	}
	
}
