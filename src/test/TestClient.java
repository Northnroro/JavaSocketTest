package test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
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

public class TestClient {
	public static void main(String[] args) throws IOException {
		JFrame f = new JFrame("TestClient");
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
		boolean error = true;
		String ip = "";
		while (error) {
			error = false;
			try {
				ta.setText(ta.getText() + ">> finding server ip address...\n");
				URL url = new URL(
						"http://northnroro.online.gp/home/index.php?txt=true");
				Scanner web = new Scanner(url.openStream());
				ip = web.nextLine().trim();
				web.close();
			} catch (ConnectException e) {
				ta.setText(ta.getText() + ">> time out.\n");
				ta.setText(ta.getText() + ">> try to connect again.\n");
				error = true;
			}
		}
		// ta.setText(ta.getText() + ">> server found(" + ip + ").\n");
		ta.setText(ta.getText() + ">> server found.\n");
		try (Socket s = new Socket(ip, 12345);
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				Scanner in = new Scanner(s.getInputStream(), "UTF-8");) {
			ta.setText(ta.getText() + ">> connected to the server.\n");
			sb.setValue(sb.getMaximum());
			String text = "";
			while (true) {
				while (!ready.val)
					;
				synchronized (btn) {
					ready.val = false;
					btn.setEnabled(true);
				}
				text = tf.getText().trim();
				out.println("Client: " + text);
				ta.setText(ta.getText() + "Client: " + text + "\n");
				sb.setValue(sb.getMaximum());
				if ((text = in.nextLine().trim()).equals("Server: exit"))
					break;
				ta.setText(ta.getText() + text + "\n");
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