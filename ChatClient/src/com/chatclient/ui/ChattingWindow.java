package com.chatclient.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.server.UID;
import java.util.HashMap;

import javax.jws.soap.SOAPBinding.Style;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

import com.chatclient.client.Client;

public class ChattingWindow extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	// initialized width and height
	final int width = 720;
	final int height = 480;
	String appname;
	String from;
	String to;
	Client client;
	
	StyledDocument sdoc;
	
	// panel
	JPanel panel;
	
	// components
	JScrollPane scrollCmf;
	ChattingMsgField cmf;
	JTextArea inputTF;
	JButton sendBtn;
	
	public ChattingWindow(String appname, String from, String to, Client client) {
		this(appname, from, to, client, null);
	}
	
	public ChattingWindow(String appname, String from, String to, Client client, HashMap<String, String> msg) {
		this.appname = appname;
		this.from = from;
		this.to = to;
		this.client = client;
		
		this.setTitle(appname + " - " + from + ":" + to + " - Chatting");
		this.setSize(width, height);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((int)(screenSize.getWidth() - width) / 2, (int)(screenSize.getHeight() - height) / 2);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		
		// components
		panel = new JPanel(null);
		this.add(panel);
		
		sdoc = new DefaultStyledDocument();
		
		cmf = new ChattingMsgField();
		cmf.setEditable(false);
		DefaultCaret caret = (DefaultCaret) cmf.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollCmf = new JScrollPane(cmf);
		scrollCmf.setBounds(10, 10, 690, 330);
		panel.add(scrollCmf);
		
		inputTF = new JTextArea();
		inputTF.setLineWrap(true);
		inputTF.setWrapStyleWord(true);
		inputTF.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendEvent();
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					inputTF.setText("");
				}
			}
		});
		JScrollPane scrollTF = new JScrollPane(inputTF);
		scrollTF.setBounds(10, 350, 620, 90);
		panel.add(scrollTF);
		
		sendBtn = new JButton("发送");
		sendBtn.setBounds(640, 375, 60, 40);
		sendBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				sendEvent();
			}
		});
		panel.add(sendBtn);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				// default focus
				inputTF.requestFocus();
			}
			@Override
			public void windowClosing(WindowEvent e) {
				// when this window is closing, we need to tell server that
				// this user will log out
				HashMap<String, String> request = new HashMap<>();
				request.put("type", "LOGOUT");
				request.put("uid", from);
				client.sendMsg(request);
			}
		});
		if (msg != null) {
			handleReceiveMsg(msg);
		}
		new Thread(this).start();
	}
	
	public void sendEvent() {
		String content = inputTF.getText();
		if (content.isEmpty())
			return;
		// show your message on the window
//		cmf.insertDoc(content, from, true);
		
		// send message
		HashMap<String, String> request = new HashMap<>();
		request.put("type", "SENDMSG");
		request.put("uid", from);
		request.put("tuid", to);
		request.put("msg", content);
		client.sendMsg(request);
		inputTF.setText("");
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				HashMap<String, String> result = client.receiveMsg();
				if (result != null) {
					String type = result.get("type");
					if (type.equals("SUCCESS")) {
						// set the session successfully
//						JOptionPane.showMessageDialog(null, "发送成功！");
						String content = result.get("msg");
						String from = result.get("uid");
						cmf.insertDoc(content, from, true);
						JScrollBar vertical = scrollCmf.getVerticalScrollBar();
						vertical.setValue(vertical.getMaximum());
					} else if (type.equals("RECEIVE MSG")) {
						handleReceiveMsg(result);
					} else if (type.equals("LOGOUT SUCCESS")) {
					} else {
						String m = result.get("msg");
						JOptionPane.showMessageDialog(null, m, "发送失败", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handleReceiveMsg(HashMap<String, String> result) {
		String from = result.get("from");
		String msg = result.get("msg");
		cmf.insertDoc(msg, from, false);
		JScrollBar vertical = scrollCmf.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}
}
