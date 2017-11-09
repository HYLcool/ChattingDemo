package com.chatclient.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import com.chatclient.util.MsgUtil;

public class Client {
	// some necessary parts
	private int port = 9999;
	private Socket clientSocket;
	private BufferedReader input;
	private PrintWriter output;
	public boolean isConn;
	
	// about server
	private final String serverIP = "10.128.187.46"; // need to change
	private final int serverPort = 8888;
	
	public boolean isConnected() {
		return isConn;
	}
	
	public void connect() {
		try {
			// connect to server
			clientSocket = new Socket(serverIP, serverPort);
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			output = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
		} catch (Exception e) {
			e.printStackTrace();
			isConn = false;
			return;
		}
		isConn = true;
	}

//	@Override
//	public void run() {
//		// receive messages
//		String msg;
//		try {
//			while (true) {
//				msg = input.readLine();
//				handleMsg(msg);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public boolean sendMsg(HashMap<String, String> msgMap) {
		// send messages to server
		String msg = MsgUtil.generateMsg(msgMap);
		output.println(msg);
		output.flush();
		return true;
	}
	
	public HashMap<String, String> receiveMsg() {
		String msg = null;
		try {
			while ((msg = input.readLine()) == null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return MsgUtil.parseContent(msg);
	}
	
	public String getIP() {
		return clientSocket.getInetAddress().getHostAddress();
	}
	
	public void close() {
		try {
			input.close();
			output.close();
			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
