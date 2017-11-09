package com.chatclient.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.chatclient.client.Client;

public class LoginWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	// width and height of login window
	final int width = 350;
	final int height = 200;
	String appname;
	
	// enter key listener
	KeyAdapter enterListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				loginEvent();
			}
		}
	};
	
	// panel and components
	JPanel panel;
	
	JLabel userLabel;
	JTextField usernameTF;
	JLabel pwdLabel;
	JPasswordField pwdTF;
	
	JButton btnLogin;
	JButton btnSignup;
	
	// client
	Client client = null;
	
	public LoginWindow(String appName, Client c) {
		this.appname = appName;
		// initialize client
		if (c == null) {
			this.client = new Client();
			this.client.connect();
		} else {
			this.client = c;
		}
		
		// basic attribution of window
		this.setTitle(appName + " - 登陆");
		this.setSize(width, height);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((int)(screenSize.getWidth() - width) / 2, (int)(screenSize.getHeight() - height) / 2);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		
		// add panel
		panel = new JPanel();
		panel.setLayout(null);
		this.add(panel);
		
		// components
		userLabel = new JLabel("用户名");
		userLabel.setBounds(35, 30, 70, 25);
		panel.add(userLabel);
		
		usernameTF = new JTextField(20);
		usernameTF.setBounds(125, 30, 165, 25);
		usernameTF.setAutoscrolls(false);
		usernameTF.addKeyListener(enterListener);
		panel.add(usernameTF);
		
		pwdLabel = new JLabel("密码");
		pwdLabel.setBounds(35, 60, 70, 25);
		panel.add(pwdLabel);
		
		pwdTF = new JPasswordField(20);
		pwdTF.setBounds(125, 60, 165, 25);
		pwdTF.addKeyListener(enterListener);
		panel.add(pwdTF);
		
		btnSignup = new JButton("注册");
		btnSignup.setBounds(55, 100, 90, 30);
		btnSignup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SignupWindow(appName, client).setVisible(true);
				dispose();
			}
		});
		panel.add(btnSignup);
		
		btnLogin = new JButton("登陆");
		btnLogin.setBounds(180, 100, 90, 30);
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				loginEvent();
			}
		});
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// close the connection
				if (!client.isConnected())
					return;
				HashMap<String, String> msg = new HashMap<>();
				msg.put("type", "CLOSE");
				client.sendMsg(msg);
				client.close();
			}
		});
		
		panel.add(btnLogin);
	}
	
	public void loginEvent() {
		// send log in message to server
		String username = usernameTF.getText();
		String pwd = String.valueOf(pwdTF.getPassword());
		if (username.isEmpty() || pwd.isEmpty()) {
			JOptionPane.showMessageDialog(null, "用户名和密码不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
//		System.out.println(username + " " + pwd);
		HashMap<String, String> request = new HashMap<>();
		request.put("type", "LOGIN");
		request.put("uid", username);
		request.put("pwd", pwd);
		request.put("ip", client.getIP());
		client.sendMsg(request);
		
		// get return message
		HashMap<String, String> result = client.receiveMsg();
		if (result.get("type").equals("SUCCESS")) {
			// log in successfully
//			JOptionPane.showMessageDialog(null, "登陆成功！");
			new WaitingWindow(this.appname, client, username).setVisible(true);
			dispose();
		} else {
			String m = result.get("msg");
			JOptionPane.showMessageDialog(null, m, "登陆失败", JOptionPane.ERROR_MESSAGE);
		}
	}
}