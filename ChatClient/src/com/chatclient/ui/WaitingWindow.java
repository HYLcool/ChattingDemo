package com.chatclient.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.chatclient.client.Client;

public class WaitingWindow extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	
	// width and height
	final int width = 350;
	final int height = 300;
	String appname;
	String uid;
	
	// thread
	Thread thread;
	
	// client
	Client client;
	
	// panel and components
	JPanel panel;
	
	JLabel waitLabel;
	JLabel orLabel;
	JLabel inviteLabel;
	JLabel userLabel;
	
	JTextField tuidTF;
	JButton startBtn;
	
	public WaitingWindow(String appname, Client c, String uid) {
		this.client = c;
		this.appname = appname;
		this.uid = uid;
		
		this.setTitle(this.appname + " - " + this.uid + " - 等待或发起对话");
		this.setSize(width, height);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((int)(screenSize.getWidth() - width) / 2, (int)(screenSize.getHeight() - height) / 2);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		
		// add panel
		panel = new JPanel(null);
		this.add(panel);
		
		// components
		waitLabel = new JLabel("等待其他人对您发起对话");
		waitLabel.setFont(new Font("Dialog", 0, 22));
		waitLabel.setBounds(55, 40, 310, 25);
		panel.add(waitLabel);
		
		orLabel = new JLabel("或者");
		orLabel.setFont(new Font("Dialog", 1, 18));
		orLabel.setBounds(150, 90, 310, 25);
		panel.add(orLabel);
		
		inviteLabel = new JLabel("向指定用户发起对话：");
		inviteLabel.setFont(new Font("Dialog", 0, 22));
		inviteLabel.setBounds(65, 140, 310, 25);
		panel.add(inviteLabel);
		
		userLabel = new JLabel("目标用户：");
		userLabel.setFont(new Font("Dialog", 0, 16));
		userLabel.setBounds(30, 200, 100, 25);
		panel.add(userLabel);
		
		tuidTF = new JTextField(20);
		tuidTF.setBounds(110, 195, 120, 35);
		tuidTF.setAutoscrolls(false);
		tuidTF.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					startSessionEvent();
				}
			}
		});
		panel.add(tuidTF);
		
		startBtn = new JButton("发起对话");
		startBtn.setFont(new Font("Dialog", 0, 14));
		startBtn.setBounds(240, 197, 90, 31);
		startBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				startSessionEvent();
			}
		});
		panel.add(startBtn);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// when this window is closing, we need to tell server that
				// this user will log out
				HashMap<String, String> request = new HashMap<>();
				request.put("type", "LOGOUT");
				request.put("uid", uid);
				client.sendMsg(request);
			}
		});
		thread = new Thread(this);
		thread.start();
	}
	
	public void startSessionEvent() {
		String tuid = tuidTF.getText();
		if (tuid.equals("")) {
			JOptionPane.showMessageDialog(null, "目标用户不能为空！", "请求失败", JOptionPane.ERROR_MESSAGE);
		} else if (tuid.equals(uid)) {
			JOptionPane.showMessageDialog(null, "调皮哦，不能对自己发起对话~", "请求失败", JOptionPane.ERROR_MESSAGE);
		} else {
			// start a request to check if this user is online
			HashMap<String, String> request = new HashMap<>();
			request.put("type", "CHECKUSER");
			request.put("tuid", tuid);
			client.sendMsg(request);
		}
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
						JOptionPane.showMessageDialog(null, "对话发起成功！");
						new ChattingWindow(appname, uid, result.get("tuid"), client).setVisible(true);
						break;
					} else if (type.equals("RECEIVE MSG")) {
						// receive messages from other user
						// which means somebody starts a session with you
						// here, we need to shutdown this window and open
						// the chatting window, and show this message on
						// that window
						new ChattingWindow(appname, uid, result.get("from"), client, result).setVisible(true);
						break;
					} else if (type.equals("LOGOUT SUCCESS")) {
						thread.interrupt();
					} else {
						String m = result.get("msg");
						JOptionPane.showMessageDialog(null, m, "请求失败", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		dispose();
	}
}
