package airHockey;

import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicBorders;

public class AirHockeyServer {

	private static boolean hit, hit0, hit1;

	public static void main(String[] args) throws IOException {
		JFrame f = new JFrame("AirHockeyServer");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setAlwaysOnTop(true);
		final JTextArea ta = new JTextArea(30, 30);
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setBorder(BasicBorders.getInternalFrameBorder());
		JScrollPane sp = new JScrollPane(ta);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		f.getContentPane().add(sp);
		f.pack();
		f.setVisible(true);
		ServerSocket ss = new ServerSocket(12345, 10);
		ta.setText(ta.getText() + "Server Start.\n");
		final HockeyTable ht = new HockeyTable(-1);
		Socket s0 = ss.accept();
		String remoteIP0 = s0.getRemoteSocketAddress() + "";
		ta.setText(ta.getText() + "Accepted First Client(" + remoteIP0 + ").\n");
		try (PrintWriter out0 = new PrintWriter(s0.getOutputStream(), true);
				Scanner in0 = new Scanner(s0.getInputStream());) {
			ht.first.setName(in0.nextLine());
			out0.println("0|" + ht.first.getName());
			Socket s1 = ss.accept();
			String remoteIP1 = s0.getRemoteSocketAddress() + "";
			ta.setText(ta.getText() + "Accepted Second Client(" + remoteIP1
					+ ").\n");
			try (PrintWriter out1 = new PrintWriter(s1.getOutputStream(), true);
					Scanner in1 = new Scanner(s1.getInputStream());) {
				ht.second.setName(in1.nextLine());
				out1.println("1|" + ht.second.getName());
				out0.println("1|" + ht.second.getName());
				out1.println("0|" + ht.first.getName());
				new Thread(new Runnable() {
					@Override
					public void run() {
						JFrame f = new JFrame("AirHockeyServer");
						f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						f.setResizable(false);
						BufferedImage img = new BufferedImage(300, 600,
								BufferedImage.TYPE_INT_ARGB);
						Container c = f.getContentPane();
						c.add(new JLabel(new ImageIcon(img)));
						f.pack();
						f.pack();
						f.setVisible(true);
						while (true) {
							hit |= ht.predict();
							ht.paint(img);
							f.repaint();
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							out0.println("0"
									+ ht.toString().substring(2)
									+ "|"
									+ (hit0 ? HockeyTable
											.EffectToString(ht.effects) : "X"));
							hit0 = false;
							// try {
							// Thread.sleep(20 + (int) (Math.random() * 60));
							ht.first.sync(in0.nextLine());
							// Thread.sleep(20 + (int) (Math.random() * 60));
							// } catch (InterruptedException e) {
							// e.printStackTrace();
							// }
						}
					}
				}).start();
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							out1.println("1"
									+ ht.toString().substring(2)
									+ "|"
									+ (hit1 ? HockeyTable
											.EffectToString(ht.effects) : "X"));
							hit1 = false;
							// try {
							// Thread.sleep(20 + (int) (Math.random() * 60));
							ht.second.sync(in1.nextLine());
							// Thread.sleep(20 + (int) (Math.random() * 60));
							// } catch (InterruptedException e) {
							// e.printStackTrace();
							// }
						}
					}
				}).start();
				while (true) {
					if (hit) {
						try {
							Clip c = AudioSystem.getClip();
							c.open(AudioSystem
									.getAudioInputStream(AirHockeyServer.class
											.getResource("bounce.wav")));
							c.start();
						} catch (LineUnavailableException | IOException
								| UnsupportedAudioFileException e) {
							e.printStackTrace();
						}
						hit = false;
						hit0 = true;
						hit1 = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		ss.close();
		ta.setText(ta.getText() + "Server Stop.\n");
	}
}
