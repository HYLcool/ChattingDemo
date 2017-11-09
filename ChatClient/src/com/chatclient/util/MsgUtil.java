package com.chatclient.util;

import java.util.HashMap;
import java.util.Set;

public class MsgUtil {
	
	public static String decode(String str) {
		return str.replace("%3D", "=").replace("%26", "&").replace("%22", "\"");
	}
	
	public static String encode(String str) {
		return str.replace("=", "%3D").replace("&", "%26").replace("\"", "%22");
	}
	
	public static HashMap<String, String> parseContent(String content) {
		// content string is like:
		// name1=value1[&name=value]
		HashMap<String, String> contentMap = new HashMap<>();
		String[] parts = content.split("&");
		for (String part : parts) {
			String[] nv = part.split("=");
			contentMap.put(decode(nv[0]), decode(nv[1]));
		}
		return contentMap;
	}
	
	public static String generateMsg(HashMap<String, String> map) {
		String string = "";
		Set<String> keys = map.keySet();
		for (String key : keys) {
			String value = map.get(key);
			String item = encode(key) + "=" + encode(value);
			if (!string.equals(""))
				string += "&";
			string += item;
		}
		return string;
	}
}

