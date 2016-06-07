package airHockey;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.TextArea;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class AirHockeyClient {

	public static void main(String[] args) throws InterruptedException,
			UnknownHostException, IOException {
		JFrame inif = new JFrame("Connecting to the server...");
		inif.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		inif.setAlwaysOnTop(true);
		TextArea inita = new TextArea(5, 50);
		inita.setEditable(false);
		inif.getContentPane().add(inita);
		inif.pack();
		inif.setVisible(true);
		boolean error = true;
		String ip = "127.0.0.1";
		while (error) {
			error = false;
			try {
				inita.setText(inita.getText()
						+ ">> finding server ip address...\n");
				URL url = new URL(
						"URL of an ip address of a server");
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
		JFrame f = new JFrame("AirHockey");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		BufferedImage img = new BufferedImage(300, 600,
				BufferedImage.TYPE_INT_ARGB);
		Container c = f.getContentPane();
		c.add(new JLabel(new ImageIcon(img)));
		f.pack();
		f.pack();
		f.setVisible(true);
		try (Socket s = new Socket(ip, 12345);
				Scanner in = new Scanner(s.getInputStream());
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);) {
			out.println(name);
			String text = in.nextLine();
			final HockeyTable ht = new HockeyTable(Integer.parseInt(text
					.split("\\|")[0]));
			ht.setName(Integer.parseInt(text.split("\\|")[0]),
					text.split("\\|")[1]);
			ht.paint(img);
			Graphics2D g = img.createGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, HockeyTable.WIDTH, HockeyTable.HEIGHT);
			g.setColor(Color.GRAY);
			final Player np = new Player(0);
			c.addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseMoved(MouseEvent e) {
					np.x = e.getX();
					np.y = e.getY();
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					mouseMoved(e);
				}
			});
			g.drawString("Waiting for an opponent..", 50, 310);
			f.repaint();
			Thread.sleep(30);
			text = in.nextLine();
			ht.setName(Integer.parseInt(text.split("\\|")[0]),
					text.split("\\|")[1]);
			ht.paint(img);
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						ht.sync(in.nextLine());
						out.println(ht.toString().split("\\|")[ht.num == 0 ? 2
								: 4]);
					}
				}
			}).start();
			while (true) {
				ht.predict();
				ht.update((int) np.x, (int) np.y);
				ht.paint(img);
				f.repaint();
				Thread.sleep(10);
			}
		}
	}
}
