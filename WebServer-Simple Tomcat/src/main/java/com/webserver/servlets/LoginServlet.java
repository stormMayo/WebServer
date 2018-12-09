package com.webserver.servlets;
/**
 * 处理登录业务
 */
import java.io.File;
import java.io.RandomAccessFile;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

public class LoginServlet extends HttpServlet{
	public void service(HttpRequest request,HttpResponse response) {
		System.out.println("开始处理登录");
		//1 获取登录信息
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		//2 
		try (RandomAccessFile raf = new RandomAccessFile("user.dat","r");){
			boolean check=false;
			for(int i=0;i<raf.length()/100;i++) {
				//将指针移动到该条记录开头位置
				raf.seek(i*100);
				//读取用户名
				byte[] data = new byte[32];
				raf.read(data);
				String name = new String(data,"UTF-8").trim();
				//查看是否为该用户
				if(name.equals(username)) {
					//找到该用户
					//读取该用户的密码
					raf.read(data);
					String pwd = new String(data,"UTF-8").trim();
					if(pwd.equals(password)) {
						//密码正确
						check = true;	
					}
					break;
				}
			}
			if(check){
				//跳转到登录成功页面
				File file = new File("webapps/myweb/login_success.html");
				response.setEntity(file);
			}else {
				//跳转到登录失败页面
				File file = new File("webapps/myweb/login_fail.html");
				response.setEntity(file);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("处理登录结束");
	}
}
