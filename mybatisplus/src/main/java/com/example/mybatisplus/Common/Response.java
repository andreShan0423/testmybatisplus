package com.example.mybatisplus.Common;

import java.util.HashMap;
import java.util.Map;

public class Response extends HashMap<String, Object> {
	//
	private static final long serialVersionUID = -8157613083634272196L;

	public Response() {
		put("code", 0);
		put("msg", "success");
	}

	public static Response error() {
		return error(1, "未知异常，请联系管理员");
	}

	public static Response error(String msg) {
		return error(1, msg);
	}

	public static Response error(int code, String msg) {
		Response r = new Response();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static Response ok(String msg) {
		Response r = new Response();
		r.put("msg", msg);
		return r;
	}

	public static Response data(Object obj) {
		Response r = new Response();
		r.put("data", obj);
		return r;
	}

	public static Response ok(Map<String, Object> map) {
		Response r = new Response();
		r.putAll(map);
		return r;
	}

	public static Response ok() {
		return new Response();
	}

	@Override
	public Response put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}