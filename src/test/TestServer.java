package test;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicBorders;

public class TestServer {
	public static void main(String[] args) {
		JFrame f = new JFrame("TestServer");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = f.getContentPane();
		c.setLayout(new BorderLayout());
		JTextArea ta = new JTextArea(6, 0);
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setBorder(BasicBorders.getInternalFrameBorder());
		JScrollPane sp = new JScrollPane(ta);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JScrollBar sb = sp.getVerticalScrollBar();
		c.add(sp, BorderLayout.CENTER);
		JPanel p = new JPanel();
		final JTextField tf = new JTextField(20);
		tf.grabFocus();
		final BooleanWrapper ready = new BooleanWrapper(false);
		JButton btn = new JButton("Send");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (e.getSource()) {
					ready.val = true;
					((JButton) e.getSource()).setEnabled(false);
					tf.grabFocus();
				}
			}
		});
		p.add(tf);
		p.add(btn);
		c.add(p, BorderLayout.SOUTH);
		f.pack();
		f.setAlwaysOnTop(true);
		f.setVisible(true);
		try (ServerSocket ss = new ServerSocket(12345);
				Socket s = ss.accept();
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				Scanner in = new Scanner(s.getInputStream(), "UTF-8");) {
			ta.setText(ta.getText() + ">> accepted a client("
					+ s.getRemoteSocketAddress() + ").\n");
			String text = "";
			while (!(text = in.nextLine().trim()).equals("Client: exit")) {
				ta.setText(ta.getText() + text + "\n");
				sb.setValue(sb.getMaximum());
				while (!ready.val)
					;
				synchronized (btn) {
					ready.val = false;
					btn.setEnabled(true);
				}
				text = tf.getText().trim();
				out.println("Server: " + text);
				out.flush();
				ta.setText(ta.getText() + "Server: " + text + "\n");
				sb.setValue(sb.getMaximum());
			}
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			System.exit(0);
		}
	}

	static class BooleanWrapper {
		public boolean val;

		public BooleanWrapper(boolean x) {
			this.val = x;
		}
	}
}