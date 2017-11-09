package com.chatserver.server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

import com.chatserver.thread.ServerThread;

public class Server {
	
	// server port
	final int PORT = 8888;
	// server socket
	ServerSocket serverSocket = null;
	// clients list
	public static HashMap<String, Socket> clients;
	
	public Server() {
		try {
			clients = new HashMap<>();
			serverSocket = new ServerSocket(PORT);
			System.out.println("Server starts at " + new Date());
			System.out.println("Server IP: " + InetAddress.getLocalHost().getHostAddress());
			System.out.println("Port: " + PORT);
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void start() {
		Socket request = null;
		ServerThread thread = null;
		try {
			while (true) {
				// receive the request
				request = serverSocket.accept();
				clients.put(request.getInetAddress().getHostAddress() + ":" + request.getPort(), request);
				// start a new thread to handle this request
				thread = new ServerThread(request);
				new Thread(thread).start();
				System.out.println("New connection from " + request.getInetAddress().getHostAddress() + ":" + request.getPort());
				System.out.println("Total: " + clients.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Server();
	}

}
