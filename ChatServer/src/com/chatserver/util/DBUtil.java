package com.chatserver.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DBUtil {
	private static ConcurrentHashMap<String, HashMap<String, String>> db = null;
	private static String dbPath = "dataUnsafe/udb.data";
	
	private static synchronized void readDB() {
		try {
			db = new ConcurrentHashMap<>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dbPath))));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] parts = line.trim().split(";", -1);
				HashMap<String, String> userInfo = new HashMap<>();
				userInfo.put("pwd", parts[1]);
				userInfo.put("online", parts[2]);
				userInfo.put("ip", parts[3]);
				db.put(parts[0], userInfo);
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static synchronized void updateDB() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dbPath)));
			Set<String> users = db.keySet();
			for (String user : users) {
				String pwd = db.get(user).get("pwd");
				String online = db.get(user).get("online");
				String ip = db.get(user).get("ip");
				bw.write(user + ";" + pwd + ";" + online + ";" + ip + "\n");
				bw.flush();
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized HashMap<String, String> getUserInfo(String user) {
		if (db == null)
			readDB();
		Set<String> users = db.keySet();
		if (!users.contains(user))
			return null;
		return db.get(user);
	}
	
	public static synchronized void updateUserInfo(String user, HashMap<String, String> map) {
		if (db == null)
			readDB();
		db.put(user, map);
		updateDB();
	}
}
