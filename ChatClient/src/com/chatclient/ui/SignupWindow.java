package com.chatclient.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.tree.DefaultTreeCellEditor.EditorContainer;

import com.chatclient.client.Client;

public class SignupWindow extends JFrame {
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
					signupEvent();
				}
			}
		};
		
		// panel and components
		JPanel panel;
		
		JLabel userLabel;
		JTextField usernameTF;
		JLabel pwdLabel;
		JPasswordField pwdTF;
		JLabel pwd2Label;
		JPasswordField pwd2TF;
		
		JButton btnLogin;
		JButton btnSignup;
		
		// client
		Client client = null;
		
		public SignupWindow(String appName, Client client) {
			this.appname = appName;
			this.client = client;
			// basic attribution of window
			this.setTitle(appName + " - 注册");
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
			userLabel.setBounds(35, 10, 70, 25);
			panel.add(userLabel);
			
			usernameTF = new JTextField(20);
			usernameTF.setBounds(125, 10, 165, 25);
			usernameTF.setAutoscrolls(false);
			usernameTF.addKeyListener(enterListener);
			panel.add(usernameTF);
			
			pwdLabel = new JLabel("密码");
			pwdLabel.setBounds(35, 40, 70, 25);
			panel.add(pwdLabel);
			
			pwdTF = new JPasswordField(20);
			pwdTF.setBounds(125, 40, 165, 25);
			pwdTF.addKeyListener(enterListener);
			panel.add(pwdTF);
			
			pwd2Label = new JLabel("确认密码");
			pwd2Label.setBounds(35, 70, 120, 25);
			panel.add(pwd2Label);
			
			pwd2TF = new JPasswordField(20);
			pwd2TF.setBounds(125, 70, 165, 25);
			pwd2TF.addKeyListener(enterListener);
			panel.add(pwd2TF);
			
			btnSignup = new JButton("注册");
			btnSignup.setBounds(125, 120, 90, 30);
			btnSignup.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					signupEvent();
				}
			});
			panel.add(btnSignup);
		}
		
		public void signupEvent() {
			String username = usernameTF.getText();
			String pwd = String.valueOf(pwdTF.getPassword());
			String pwd2 = String.valueOf(pwd2TF.getPassword());
			if (username.isEmpty() || pwd.isEmpty() || pwd2.isEmpty()) {
				JOptionPane.showMessageDialog(null, "用户名或密码不能为空！");
			} else if (!pwd.equals(pwd2)) {
				JOptionPane.showMessageDialog(null, "两次输入的密码不同！请确认后再次输入。");
			} else {
				// signup
				HashMap<String, String> msg = new HashMap<>();
				msg.put("type", "SIGNUP");
				msg.put("uid", username);
				msg.put("pwd", pwd);
				client.sendMsg(msg);
				
				HashMap<String, String> result = client.receiveMsg();
				if (result.get("type").equals("SUCCESS")) {
					// return to the previous window
					JOptionPane.showMessageDialog(null, "注册成功！");
					new LoginWindow(this.appname, client).setVisible(true);;
					dispose();
				} else {
					String m = result.get("msg");
					JOptionPane.showMessageDialog(null, m, "注册失败", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
}
