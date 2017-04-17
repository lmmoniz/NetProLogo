package utils;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import main.NetPrologoExtension;

public class PrologConsole extends JFrame implements Serializable {
	/**
	 * 
	 */
	private final boolean DEBUG = true;
	private JTextArea jta = new JTextArea(20, 80);
	private JTextField jtf = new JTextField(80);
	
	private void debug(String s) {
		if (DEBUG) jta.append("[DEBUG] : " + s);
	}
	
	private StringBuffer parseList(StringBuffer res) {
		StringBuffer sb=new StringBuffer();
		int count = 0;
		while(count < res.length()){
			int fp = res.indexOf("'.'(");
			int par = 1, lp = fp+4;
			while(lp < res.length() && par > 0){
				if (res.charAt(lp) == '(')
					par++;
				if (res.charAt(lp) == ')')
					par--;
				lp++;
			
				
			}
		}
		
		return sb;
				
	}

	public PrologConsole() {
		add(jtf, BorderLayout.SOUTH);
		add(jta, BorderLayout.CENTER);

		jta.setLineWrap(false);
		jta.setEditable(false);
		jta.setVisible(true);
		jtf.setText("?- ");

		JScrollPane scroll = new JScrollPane(jta);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		add(scroll);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		jtf.addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			public void keyPressed(KeyEvent e) {
				StringBuffer res = new StringBuffer();
				if (jtf.getText().length() < 3)
					jtf.setText("?- ");
				else if (e.getKeyChar() == '\n') {
					String query = jtf.getText().substring(3);
					debug("Query:"+query+"  TEXT: " + jtf.getText());
					if (query.trim().equals(";"))
						res = NetPrologoExtension.runNextJPLConsole();
					else
						res = NetPrologoExtension.runQueryJPLConsole(query);
					// String res =
					// NetPrologoExtension.runQueryJPLSolutions(query);

					jta.append(query + "\n");
					jta.append("?- " + res + "\n");
					jtf.setText("?- ");

				}

			}

			public void keyReleased(KeyEvent e) {
			}
		});

	}

}