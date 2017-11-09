package com.chatclient.ui;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChattingMsgField extends JTextPane {
	public void insertDoc(String content, String from, boolean currentUser) {
		try {
			Document sdoc = this.getStyledDocument();
			MutableAttributeSet set = this.getInputAttributes();
			
			// show from whom
			this.setParagraphAttributes(set, false);
			StyleConstants.setBold(set, true);
			StyleConstants.setForeground(set, currentUser ? Color.BLUE : Color.BLACK);
//			StyleConstants.setAlignment(set, currentUser ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
			StyleConstants.setFontSize(set, 12);
			sdoc.insertString(sdoc.getLength(), from, set);
			
			// show the message
			StyleConstants.setBold(set, false);
			sdoc.insertString(sdoc.getLength(), getTime(), set);
			sdoc.insertString(sdoc.getLength(), content + "\n\n", set);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getTime() {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat(" HH:mm:ss-yyyy/MM/dd: \n");
		return format.format(calendar.getTime());
	}
}
