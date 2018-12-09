package com.webserver.core;

public class Test {

	public static void main(String[] args) {
		boolean  a = "张三,,,李四,,王五,,,,,,马六,,".split("[,]+").length == 1;
		System.out.println(a);
		

	}

}
