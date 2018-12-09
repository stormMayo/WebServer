package com.webserver.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 服务端环境信息
 * @author soft01
 *
 */
public class ServerContext {
	//服务端使用的协议版本
	public static String protocol="HTTP/1.1";
	
	//服务端使用的端口号
	public static int port=8080;
	
	//服务端解析URI时使用的字符集
	public static String URIEncoding="UTF-8";
	
	//服务端线程池中线程数量
	public static int maxThreads=150;
	//请求与对应Servlet名字的映射关系
	private static Map<String,String> servletMapping = new HashMap<String,String>();
	
	static {
		init();
		initServletMapping();
	}
	/**
	 * 初始化Servlet映射
	 */
	private static void initServletMapping() {
		/*
		 * 加载conf/servlets.xml
		 * 将每个<servlet>的url作为key，className作为value
		 * 用于初始化servletMapping
		 */
		try {
			SAXReader reader = new SAXReader();
			Document doc= reader.read(new File("conf/servlet.xml"));
			Element root = doc.getRootElement();
			List<Element> list = root.elements("servlet");
			for(Element servletele:list) {
				String key = servletele.attributeValue("url");
				String value = servletele.attributeValue("className");
				servletMapping.put(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//servletMapping.put("/myweb/reg","com.webserver.servlets.RegServlet");
		//servletMapping.put("/myweb/login", "com.webserver.servlets.LoginServlet");
	}
	
	
	/**
	 * 解析conf/server.xml，将所有配置项用于初始化
	 * ServerContext对应属性
	 */
	public static  void init() {
		/*
		 * 解析conf/server.xml文件，将根标签下的子标签
		 * <Connector>中各属性的值得到，并用于初始化对应的属性
		 * 
		 * 	 *Attribute attribute(String name)
			 * 获取指定名字的属性，Attribute的每一个实例用于表示一个标签中的一个属性，
			 * 可以通过它获取该属性的名字与对应的属性值
			 * 
			 * String attributeValue(String name)
			 * 可以直接获取当前标签中指定名字的属性所对应的值
		 */
		
		try {
			SAXReader reader = new SAXReader();
			Document doc= reader.read(new File("conf/server.xml"));
			Element root= doc.getRootElement();
			Element value = root.element("Connector");
			//获取字符集
			URIEncoding = value.attributeValue("URIEncoding");

			//获取端口号
			port = Integer.parseInt(value.attributeValue("port"));
			
			//获取协议版本
			protocol = value.attributeValue("protocol");
			
			//获取线程数量
			maxThreads = Integer.parseInt(value.attributeValue("maxThreads"));
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	/**
	 * 根据请求获取对应的Servlet名字。若该请求没有对应任何Servlet则返回值为null
	 * @param uri
	 * @return
	 */
	public static String getServletName(String uri) {
		return servletMapping.get(uri);
	}
	
	
	public static void main(String[] args) {
		System.out.println(ServerContext.port);
	}

}
