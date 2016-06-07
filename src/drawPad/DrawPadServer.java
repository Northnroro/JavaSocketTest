package drawPad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicBorders;

public class DrawPadServer {
	public static void main(String[] args) {
		JFrame f = new JFrame("DrawPadServer");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setAlwaysOnTop(true);
		JTextArea ta = new JTextArea(30, 30);
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setBorder(BasicBorders.getInternalFrameBorder());
		JScrollPane sp = new JScrollPane(ta);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		f.getContentPane().add(sp);
		f.pack();
		f.setVisible(true);
		ta.setText(ta.getText() + "Server Start.\n");
		try {
			@SuppressWarnings("resource")
			ServerSocket ss = new ServerSocket(12345, 10);
			Map<String, User> map = new HashMap<String, User>();
			BufferedImage img = new BufferedImage(800, 600,
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, 800, 600);
			while (true) {
				new Thread(new DrawPadServerThread(map, ss.accept(), ta,
						sp.getVerticalScrollBar(), img)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class DrawPadServerThread implements Runnable {
	private Map<String, User> m;
	private static Set<String> done = new HashSet<String>();
	private Socket s;
	private JTextArea ta;
	private JScrollBar sb;
	private BufferedImage img;
	private Integer timeDisconnect = 0;
	private boolean kicked = false;

	private static final int RESOLUTION = 3;
	private static final Color[] COLORS = { Color.BLACK, Color.WHITE,
			new Color(0x760e83), new Color(0x3344dd), new Color(0x009cfb),
			new Color(0x07c7dd), new Color(0x3aec61), new Color(0x96f3d7),
			new Color(0xffc100), new Color(0xfb5f00), new Color(0xdf2121),
			new Color(0xf4bebe), new Color(0xfb00a4), new Color(0xf209e8),
			new Color(0x39845e), new Color(0x00fbdd), new Color(0xf8f8df),
			new Color(0xbe9e6d), new Color(0xdff8f8) };

	public DrawPadServerThread(Map<String, User> m, Socket s, JTextArea ta,
			JScrollBar sb, BufferedImage img) {
		this.m = m;
		this.s = s;
		this.ta = ta;
		this.sb = sb;
		this.img = img;
	}

	@Override
	public void run() {
		Thread countDisconnect = new Thread(new Runnable() {
			@Override
			public void run() {
				String remoteIP = s.getRemoteSocketAddress() + "";
				while (true) {
					synchronized (timeDisconnect) {
						timeDisconnect++;
						if (timeDisconnect >= 300) {
							kicked = true;
							synchronized (m) {
								m.remove(remoteIP);
							}
							synchronized (done) {
								done.remove(remoteIP);
							}
						}
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		});
		String remoteIP = s.getRemoteSocketAddress() + "";
		try (PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				Scanner in = new Scanner(s.getInputStream());) {
			ta.setText(ta.getText() + "Accepted a New Client(" + remoteIP
					+ ").\n");
			sb.setValue(sb.getMaximum());
			String pixel = "";
			for (int i = 0; i < img.getHeight(); i += RESOLUTION) {
				for (int j = 0; j < img.getWidth(); j += RESOLUTION) {
					int color = 1;
					for (int k = 0; k < COLORS.length; k++) {
						if (COLORS[k].equals(new Color(img.getRGB(j, i)))) {
							color = k;
							break;
						}
					}
					pixel += (char) (color + 'A');
				}
			}
			out.println(pixel);
			countDisconnect.start();
			while (!kicked) {
				synchronized (timeDisconnect) {
					timeDisconnect = 0;
				}
				String text = "";
				User u = new User(in.nextLine());
				ta.setText(ta.getText().substring(
						Math.max(0, ta.getText().length() - 1000),
						ta.getText().length()));
				sb.setValue(sb.getMaximum());
				synchronized (m) {
					if (m.containsKey(remoteIP + "")) {
						if ((m.get(remoteIP + "").pressed || m.get(remoteIP
								+ "").pressed2)
								&& (u.pressed || u.pressed2)) {
							u.px = m.get(remoteIP + "").x;
							u.py = m.get(remoteIP + "").y;
						} else {
							u.px = -1;
							u.py = -1;
						}
					} else {
						u.px = -1;
						u.py = -1;
					}
				}
				if (u.pressed || u.pressed2) {
					Graphics2D g = img.createGraphics();
					g.setColor(COLORS[u.color]);
					int rad = u.size;
					if (u.pressed2) {
						rad = u.size * 5;
						g.setColor(Color.WHITE);
					}
					int inix = u.x - rad, iniy = u.y - rad;
					if (u.px != -1 && u.py != -1) {
						inix = u.px - rad;
						iniy = u.py - rad;
					}
					double dist = Math
							.hypot(u.x - rad - inix, u.y - rad - iniy);
					double dx = dist > 0 ? (u.x - rad - inix) / dist : 0;
					double dy = dist > 0 ? (u.y - rad - iniy) / dist : 0;
					for (int i = 0; i < dist; i++) {
						g.fillOval((int) (inix + dx * i),
								(int) (iniy + dy * i), rad * 2, rad * 2);
					}
				}
				synchronized (m) {
					m.put(remoteIP + "", u);
					String[] keys = m.keySet().toArray(new String[0]);
					for (String k : keys) {
						text += m.get(k) + "|";
					}
				}
				if (text.length() > 0)
					text = text.substring(0, text.length() - 1);
				while (true) {
					synchronized (done) {
						synchronized (m) {
							if (done.containsAll(m.keySet())) {
								done.clear();
							}
						}
						if (!done.contains(remoteIP)) {
							done.add(remoteIP);
							break;
						}
					}
				}
				out.println(text);
				Thread.sleep(30);
			}
		} catch (Exception e) {

		}
		synchronized (m) {
			m.remove(remoteIP);
		}
		synchronized (done) {
			done.remove(remoteIP);
		}
		ta.setText(ta.getText() + "Remove Client(" + s.getRemoteSocketAddress()
				+ ").\n");
		sb.setValue(sb.getMaximum());
		countDisconnect.interrupt();
	}
}
