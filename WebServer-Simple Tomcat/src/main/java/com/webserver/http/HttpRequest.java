package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.webserver.core.ServerContext;

/**
 * 请求对象
 * HttpRequest的每一个实例用于表示客户端发送过来的一个具体的请求内容。
 * 一个请求由三个部分构成：
 * 请求行，消息头，消息正文
 * @author soft01
 *
 */
public class HttpRequest {
	/*
	 * 请求行相关消息定义
	 */
	//请求方式
	private String method;
	//请求资源路径
	private String url;
	//协议版本
	private String protocol;
	
	//url中的请求部分  url中“？”左侧内容
	private String requestURI;
	//url中的参数部分  url中“？”右侧内容
	private String queryString;
	//所有参数
	private Map<String,String> parameters = new HashMap<String,String>();
	
	
	
	/*
	 * 消息头相关信息定义
	 */
	private Map<String,String> headers = new HashMap<String,String>();
	

	/*
	 * 消息正文相关信息定义
	 */
	//对应客户端的Socket
	private Socket socket;
	
	//用于读取客户端发送过来消息的输入流
	private InputStream in;
	/**
	 * 构造方法，用来初始化HttpRequest
	 * @throws EmptyRequestException 
	 * 
	 */
	public HttpRequest(Socket socket) throws EmptyRequestException {
		try {
			this.socket = socket;
			this.in = socket.getInputStream();
			/*
		 * 1:  解析请求行
		 * 2：解析消息头
		 * 3：解析消息正文
		 */
			parseRequestLine();
			parseHeaders();
			parseContent();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 解析请求行
	 * @throws EmptyRequestException 
	 */
	private void parseRequestLine() throws EmptyRequestException {
		try {
				System.out.println("解析请求行...");
				String line;
				line = readLine();
				System.out.println("请求行："+line);
				/*
				 * 解析请求行的步骤：
				 * 1：将请求行内容按照空格拆分为三个部分
				 * 2：分别将三部分内容设置到对应的属性上
				 * method,url,protocol
				 * 
				 * 这里将来可能会抛出数组下标越界，原因在于
				 * HTTP协议中也有所提及，允许客户端连接后
				 * 发送空请求（实际就是什么也没发过来）。这
				 * 时候若解析请求行是拆分不出三项的
				 * 后面遇到再解决
				 */
				String[] data = line.split("\\s");
				if(data.length<3) {
					//空请求
					throw new EmptyRequestException();
				}
				
				method = data[0];
				url = data[1];
				//进一步解析url
				parseUrl();
				protocol = data[2];
				System.out.println("method:"+method);
				System.out.println("url:"+url);
				System.out.println("protocol:"+protocol);
				System.out.println("请求行解析完毕");
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * 进一步解析url部分
	 */
	private void parseUrl() {
		/*
		 * 首先将url进行转码，将含有的%XX内容转换为
		 * 对应的字符
		 * 例如：
		 * 原url的样子大致为：
		 * myweb/login?username=%E8%8C%83%E4%BC%A0%E5%A5%87
		 * 
		 * 经过：URLDeoder.decode(url,"UTF-8")后，得到的
		 * 字符串的样子为：
		 * myweb/login?username=范传奇
		 */
		try {
			this.url = URLDecoder.decode(url,ServerContext.URIEncoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		/*
		 * 1:先判断当前url是否含有参数部分（是否含有“?”）
		 * 若没有参数部分，直接将url赋值给requestURI
		 * 含有问好才进行下面的操作
		 * 2：按照“？”将url拆分为两部分
		 *	 	将？之前的内容设置到属性requestURI上
		 * 	将？之后的内容设置到属性queryString上
		 * 3：将queryString内容进行进一步解析
		 * 	首先按照“&”拆分出每一个参数，然后再将每个
		 * 	参数按照“=”拆分为参数名与参数值，并put到
		 * 	属性parameters这个Map中
		 */
		//按照“？“拆分为两部分
		if(url.indexOf("?")!=-1) {
			String[] data = this.url.split("\\?");
			this.requestURI = data[0];
			/*
			 * 这里根据？拆分后之所以要判断数组长度是否》1，原因在与
			 * 有的请求发过来可能如下：
			 * /myweb/reg?
			 * ?后面实际没有任何参数。（页面form表单中所有的输入域都没有
			 * 指定name属性时就会出现这样的情况）
			 * 如果不判断，可能会出现下标越界的情况
			 */
			if(data.length>1) {
				this.queryString = data[1];
				//解析参数部分
				parseParameters(queryString);
			}
		}else {
			this.requestURI = url;
		}
		System.out.println("requestURL:"+requestURI);
		System.out.println("queryString:"+queryString);
		System.out.println("parameters:"+parameters);
		
	}
	/**
	 * 解析参数部分
	 * 该内容格式应当为：name=value&name=value&
	 * @param paraLine
	 */
	
	private void parseParameters(String paraLine) {
		//拆分每个参数
		String[] paras= paraLine.split("&");
		for(String para:paras) {
		//每个参数按照“=”拆分
			String[] arr = para.split("=");
			/*
			 * 这里判断arr.length>1的原因是因为，如果
			 * 在表单中某个输入框没有输入值，那么传递
			 * 过来的数据样子会是：
			 * /myweb/reg?username=&password=123&...
			 * 像用户名这样，如果没有输入，=右面是没有
			 * 内容的，拆分后不判断数组长度会出现下标越界的情况。
			 */
			if(arr.length>1) {
				this.parameters.put(arr[0], arr[1]);
			}else {
				this.parameters.put(arr[0], null);
			}			
	}
	}
	
	/**
	 * 解析消息头
	 */
	private void parseHeaders() {
		try {
			/*
			 * 循环调用readLine方法读取每一行字符串
			 * 如果读取的字符串是空字符串则表示单独读取到了CRLF,
			 * 那么表示消息头部分读取完毕，停止循环即可。
			 * 否则读取一行字符串后应当是一个消息头的内容，
			 * 接下来将该字符串按照“：”拆分为两项，第一项是消息头的名字，
			 * 第二项为对应的值，存入到属性headers即可。
			 */
			System.out.println("解析消息头...");
			
			//循环读取
			while(true) {
				
				String line = readLine();
				//单独读取CRLF；判断是否为空字符串，若是则推出循环
				if("".equals(line)) {
					break;
				}
				//若不是则：
				String[] str = line.split("\\: ");
				String headersKey = str [0];
				String headersValue = str [1];
				headers.put(headersKey, headersValue);
			}
			System.out.println("headers:"+headers);
			//遍历输出
			Set<Entry<String,String>> entrySet= headers.entrySet();
			for(Entry<String,String> entry:entrySet) {
				String key = entry.getKey();
				String value = entry.getValue();
				System.out.println(key+":"+value);
			}
			
			
			System.out.println("消息头解析完毕");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	/**
	  * 解析消息正文
	  */
	private void parseContent() {
			System.out.println("解析消息正文...");
			/*
			 * 1：根据消息头中是否含有Content-Length来判断当前请求是否含有消息正文
			 */
			if(headers.containsKey("Content-Length")) {
				//取得消息正文的长度
				int length = Integer.parseInt(headers.get("Content-Length"));
				//2根据长度读取消息正文的数据
				
				try {
					byte[] data = new byte[length];
					in.read(data);
					
					//3 根据消息头Content-Type判断正文内容
					String ContentType = headers.get("Content-Type");
					//判断正文类型是否为form表单提交的数据
					if("application/x-www-form-urlencoded".equals(ContentType)) {
						System.out.println("开始解析post提交的form表单...");
						//消息正文字节转为字符串（原get请求地址栏“？”右侧内容）
						String line = new String(data,"ISO8859-1");
						System.out.println("post表单提交数据："+line);
						try {
							//将%XX这样的数据转码(可能出现含有中文的情况)
							line = URLDecoder.decode(line,ServerContext.URIEncoding);
							
						}catch(Exception e) {
							e.printStackTrace();
						}
						//解析参数
						parseParameters(line);
					}
					//将来还可以添加其它分支，判断其他种类数据
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			System.out.println("消息正文解析完毕");
		}
	/**
	 * 读取一行字符串，结束是以连续读取到了CRLF符号为止
	 * 返回的字符串中不含有最后读取到的CRLF
	 * @return
	 * @throws IOException
	 */
	private String readLine()throws IOException {
		//StringBuilder 默认为空字符串
		StringBuilder builder = new StringBuilder();
		int d = -1;
		//c1表示上次读取到的字符，c2表示本次读取到的字符
		char c1 = 'a',c2 = 'a';
		while((d=in.read())!=-1) {
			c2 = (char)d;
			//判断是否连续读取到了CRLF
			if(c1==HttpContext.CR&c2==HttpContext.LF) {
				break;
			}
			builder.append(c2);
			c1 = c2;
		}
		return  builder.toString().trim();
	}
	public String getMethod() {
		return method;
	}
	public String getUrl() {
		return url;
	}
	public String getProtocol() {
		return protocol;
	}
	public String getRequestURI() {
		return requestURI;
	}
	public String getQueryString() {
		return queryString;
	}
	/**
	 * 根据给定的参数名获取参数值
	 * @param name
	 * @return
	 */
	public String getParameter(String name) {
		return this.parameters.get(name);
	}
	
	
}
