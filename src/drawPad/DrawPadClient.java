package drawPad;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DrawPadClient {
	private static final int RESOLUTION = 3;
	private static final Color[] COLORS = { Color.BLACK, Color.WHITE,
			new Color(0x760e83), new Color(0x3344dd), new Color(0x009cfb),
			new Color(0x07c7dd), new Color(0x3aec61), new Color(0x96f3d7),
			new Color(0xffc100), new Color(0xfb5f00), new Color(0xdf2121),
			new Color(0xf4bebe), new Color(0xfb00a4), new Color(0xf209e8),
			new Color(0x39845e), new Color(0x00fbdd), new Color(0xf8f8df),
			new Color(0xbe9e6d), new Color(0xdff8f8) };

	public static void main(String[] args) throws InterruptedException {
		JFrame inif = new JFrame("Connecting to the server...");
		inif.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		inif.setAlwaysOnTop(true);
		TextArea inita = new TextArea(5, 50);
		inita.setEditable(false);
		inif.getContentPane().add(inita);
		inif.pack();
		inif.setVisible(true);
		boolean error = true;
		String ip = "";
		while (error) {
			error = false;
			try {
				inita.setText(inita.getText()
						+ ">> finding server ip address...\n");
				URL url = new URL(
						"http://northnroro.online.gp/home/index.php?txt=true");
				Scanner web = new Scanner(url.openStream());
				ip = web.nextLine().trim();
				web.close();
			} catch (IOException e) {
				inita.setText(inita.getText() + ">> time out.\n");
				inita.setText(inita.getText() + ">> try to connect again.\n");
				error = true;
			}
		}
		inita.setText(inita.getText() + ">> server found(" + ip + ").\n");
		Thread.sleep(1000);
		inif.dispose();
		String name = JOptionPane.showInputDialog("Please Enter Your Name", "");
		final User local = new User(name, -1, -1, false, -1, -1, false, 0, 5);
		JFrame f = new JFrame("DrawPad");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setAlwaysOnTop(true);
		Container c = f.getContentPane();
		c.setLayout(new BorderLayout());
		BufferedImage real = new BufferedImage(800, 600,
				BufferedImage.TYPE_INT_ARGB);
		BufferedImage img = new BufferedImage(800, 600,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D gr = real.createGraphics();
		gr.setColor(Color.WHITE);
		gr.fillRect(0, 0, 800, 600);
		Graphics2D g = img.createGraphics();
		JLabel imgl = new JLabel(new ImageIcon(img));
		imgl.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				local.x = e.getX();
				local.y = e.getY();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mouseMoved(e);
			}
		});
		imgl.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1)
					local.pressed = false;
				else
					local.pressed2 = false;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1)
					local.pressed = true;
				else
					local.pressed2 = true;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				local.x = -1;
				local.y = -1;
			}
		});
		c.add(imgl, BorderLayout.CENTER);
		JPanel cp = new JPanel();
		for (int i = 0; i < COLORS.length; i++) {
			JButton cb = new JButton("");
			cb.setBackground(COLORS[i]);
			cb.setOpaque(true);
			final IntegerWrapper ii = new IntegerWrapper(i);
			cb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					local.color = ii.val;
				}
			});
			cp.add(cb);
			cb.setPreferredSize(new Dimension(20, 20));
		}
		JSlider ssd = new JSlider(SwingConstants.HORIZONTAL, 1, 50, 5);
		ssd.setPreferredSize(new Dimension(100, 40));
		ssd.setMajorTickSpacing(10);
		ssd.setPaintTicks(true);
		final JTextField stfn = new JTextField(ssd.getValue() + "", 2);
		stfn.setFont(new Font("Tahoma", Font.ITALIC, 11));
		stfn.setEditable(false);
		ssd.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				stfn.setText(((JSlider) e.getSource()).getValue() + "");
				local.size = ((JSlider) e.getSource()).getValue();
			}
		});
		cp.add(ssd);
		cp.add(stfn);
		JScrollPane csp = new JScrollPane(cp);
		csp.setPreferredSize(new Dimension(100, 55));
		c.add(csp, BorderLayout.SOUTH);
		f.setResizable(false);
		f.pack();
		f.pack();
		f.setVisible(true);
		try (Socket s = new Socket(ip, 12345);
				Scanner in = new Scanner(s.getInputStream());
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);) {
			String text = in.nextLine();
			int count = 0;
			for (int i = 0; i < real.getHeight(); i += RESOLUTION) {
				for (int j = 0; j < real.getWidth(); j += RESOLUTION) {
					for (int a = 0; a < RESOLUTION; a++)
						for (int b = 0; b < RESOLUTION; b++)
							if (j + a < real.getWidth()
									&& i + b < real.getHeight())
								real.setRGB(j + a, i + b, COLORS[text
										.charAt(count) - 'A'].getRGB());
					count++;
				}
			}
			while (true) {
				out.println(local);
				g.drawImage(real, 0, 0, null);
				text = in.nextLine();
				if (text.length() > 0) {
					String[] texts = text.split("\\|");
					for (int i = 0; i < texts.length; i++) {
						User u = new User(texts[i]);
						if (u.x != -1 && u.y != -1) {
							int rad = u.size;
							g.setColor(COLORS[u.color]);
							if (u.pressed || u.pressed2) {
								g.fillOval(u.x - rad, u.y - rad, rad * 2,
										rad * 2);
								g.setColor(Color.BLACK);
								gr.setColor(COLORS[u.color]);
								if (u.pressed2) {
									gr.setColor(Color.WHITE);
									rad = u.size * 5;
								}
								int inix = u.x - rad, iniy = u.y - rad;
								if (u.px != -1 && u.py != -1) {
									inix = u.px - rad;
									iniy = u.py - rad;
								}
								double dist = Math.hypot(u.x - rad - inix, u.y
										- rad - iniy);
								double dx = dist > 0 ? (u.x - rad - inix)
										/ dist : 0;
								double dy = dist > 0 ? (u.y - rad - iniy)
										/ dist : 0;
								for (int j = 0; j < dist; j++) {
									gr.fillOval((int) (inix + dx * j),
											(int) (iniy + dy * j), rad * 2,
											rad * 2);
								}
							}
							g.drawOval(u.x - rad, u.y - rad, rad * 2, rad * 2);
							g.setFont(new Font("Tahoma", Font.PLAIN, 10));
							g.setColor(Color.BLACK);
							g.drawString(u.name, u.x + rad, u.y);
						}
					}
				}
				f.repaint();
				Thread.sleep(30);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class IntegerWrapper {
		int val;

		public IntegerWrapper(int val) {
			this.val = val;
		}

	}
}
