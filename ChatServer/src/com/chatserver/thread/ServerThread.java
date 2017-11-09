package com.chatserver.thread;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.chatserver.util.DBUtil;
import com.chatserver.util.MsgUtil;
import com.chatserver.server.Server;

public class ServerThread implements Runnable {
	
	// socket
	Socket request;
	// input and output stream
	BufferedReader input;
	PrintWriter output;
	
	// return message types
	final String SUCCESS = "SUCCESS";
	
	public ServerThread(Socket request) {
		this.request = request;
		
		try {
			// initialize the input and output stream
			input = new BufferedReader(new InputStreamReader(request.getInputStream()));
			output = new PrintWriter(new OutputStreamWriter(request.getOutputStream()), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String content = "";
		try {
			while (true) {
				content = input.readLine();
				
				// parse request message
				HashMap<String, String> contentMap = MsgUtil.parseContent(content);
				// initialize return message
				HashMap<String, String> returnMsg = new HashMap<>();
				
				// handle request message
				String type = contentMap.get("type");
				if (type.equals("LOGIN")) {
					// login message
					// including username (uid), password (pwd) and client ip address (ip)
					String user = contentMap.get("uid");
					String pwd = contentMap.get("pwd");
					String ip = contentMap.get("ip") + ":" + request.getPort();
					System.out.println("Log in from: " + user + "@" + ip);
					
					// check if this user is existed
					HashMap<String, String> userInfo = DBUtil.getUserInfo(user);
					if (userInfo == null) {
						// user not existed
						returnMsg.put("type", "LOGIN ERROR");
						returnMsg.put("msg", "用户名或密码不正确！请检查后再试。");
					} else if (!pwd.equals(userInfo.get("pwd"))) {
						// incorrect password
						returnMsg.put("type", "LOGIN ERROR");
						returnMsg.put("msg", "用户名或密码不正确！请检查后再试。");
					} else if ("1".equals(userInfo.get("online"))) {
						returnMsg.put("type", "LOGIN ERROR");
						returnMsg.put("msg", "该账户已经登陆！请更换账号再试。");
					} else {
						userInfo.put("online", "1");
						userInfo.put("ip", ip);
						DBUtil.updateUserInfo(user, userInfo);
						returnMsg.put("type", SUCCESS);
					}
				} else if (type.equals("LOGOUT")) {
					// log out message
					// including username (uid)
					String user = contentMap.get("uid");
					HashMap<String, String> userInfo = DBUtil.getUserInfo(user);
					userInfo.put("online", "0");
					DBUtil.updateUserInfo(user, userInfo);
					returnMsg.put("type", "LOGOUT SUCCESS");
					output.println(MsgUtil.generateMsg(returnMsg));
					
					// close connection
					Server.clients.remove(request.getInetAddress().getHostAddress() + ":" + request.getPort());
					System.out.println("Disconnected with " + request.getInetAddress().getHostAddress() + ":" + request.getPort());
					System.out.println("Total: " + Server.clients.size());
					request.close();
					
					break;
				} else if (type.equals("CLOSE")) {
					Server.clients.remove(request.getInetAddress().getHostAddress() + ":" + request.getPort());
					System.out.println("Desconnected with " + request.getInetAddress().getHostAddress() + ":" + request.getPort());
					System.out.println("Total: " + Server.clients.size());
					request.close();
					break;
				} else if (type.equals("SIGNUP")) {
					// signup message
					// including username (uid), password (pwd)
					String user = contentMap.get("uid");
					String pwd = contentMap.get("pwd");
					System.out.println("Sign up request from: " + user);
					if (DBUtil.getUserInfo(user) != null) {
						// existed username
						returnMsg.put("type", "SIGNUP ERROR");
						returnMsg.put("msg", "用户名已被占用！");
					} else {
						// sign up successfully. update the db
						HashMap<String, String> newInfo = new HashMap<>();
						newInfo.put("pwd", pwd);
						newInfo.put("online", "0");
						newInfo.put("ip", "");
						newInfo.put("inchat", "0");
						DBUtil.updateUserInfo(user, newInfo);
						
						returnMsg.put("type", SUCCESS);
					}
				} else if (type.equals("CHECKUSER")) {
					// check if the target user is online
					// including target user (tuid)
					String targetUser = contentMap.get("tuid");
					
					HashMap<String, String> userInfo = DBUtil.getUserInfo(targetUser);
					if (userInfo == null) {
						// user inexistent
						returnMsg.put("type", "CHECK USER ERROR");
						returnMsg.put("msg", "该用户不存在！请检查后再试。");
					} else if ("1".equals(userInfo.get("online"))) {
						// target user is online
						returnMsg.put("type", SUCCESS);
						returnMsg.put("tuid", targetUser);
					} else {
						returnMsg.put("type", "OFFLINE");
						returnMsg.put("msg", "该用户已离线！");
					}
				} else if (type.equals("SENDMSG")) {
					// send message to target user
					// including username (uid), target user (tuid) and message (msg)
					String uid = contentMap.get("uid");
					String tuid = contentMap.get("tuid");
					String msg = contentMap.get("msg");
					
					HashMap<String, String> tuInfo = DBUtil.getUserInfo(tuid);
					if (tuInfo == null) {
						returnMsg.put("type", "MSG ERROR");
						returnMsg.put("msg", "用户不存在！");
					} else if ("0".equals(tuInfo.get("online"))) {
						returnMsg.put("type", "MSG ERROR");
						returnMsg.put("msg", "该用户已离线！");
					} else {
						String tip = tuInfo.get("ip");
						if (sendMsg(uid, tuid, tip, msg)) {
							returnMsg.put("type", SUCCESS);
							returnMsg.put("uid", uid);
							returnMsg.put("msg", msg);
						} else {
							returnMsg.put("type", "MSG ERROR");
							returnMsg.put("msg", "未知错误！请检查网络状态。");
						}
					}
				} else {
					// wrong type
					System.out.println(type);
					returnMsg.put("type", "TYPE ERROR");
					returnMsg.put("msg", "未知错误：type = " + type + "");
				}
				output.println(MsgUtil.generateMsg(returnMsg));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean sendMsg(String from, String to, String ip, String msg) {
		if (!Server.clients.containsKey(ip)) {
			return false;
		}
		// get the target socket
		Socket target = Server.clients.get(ip);
		// put in message
		HashMap<String, String> msgPack = new HashMap<>();
		msgPack.put("type", "RECEIVE MSG");
		msgPack.put("from", from);
		msgPack.put("msg", msg);
		
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(target.getOutputStream()), true);
			pw.println(MsgUtil.generateMsg(msgPack));
			pw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
}
