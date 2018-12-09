package com.webserver.core;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebServer主类
 * @author soft01
 *
 */
public class WebServer {
	private ServerSocket server;
	
	//管理用于处理客户端请求的线程
	private ExecutorService threadPool;
	/**
	 * 构造方法，用来初始化服务端
	 */
	public WebServer() {
		try {
			System.out.println("正在启动服务端...");
			server = new ServerSocket(ServerContext.port);
			threadPool = Executors.newFixedThreadPool(ServerContext.maxThreads);
			System.out.println("服务端启动完毕！");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 服务端启动方法
	 */
	public void start(){
		try {
			while(true) {
				System.out.println("等待客户端.....");
				Socket socket = server.accept();
				System.out.println("客户端连接完毕！");
				
				//启动一个线程处理该客户端请求
				ClientHandler handler = new ClientHandler(socket);
				threadPool.execute(handler);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		WebServer server = new WebServer();
		server.start();
	}
}
