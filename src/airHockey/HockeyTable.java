package airHockey;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class HockeyTable {
	public static final int WIDTH = 300, HEIGHT = 600;
	protected int num;
	protected Ball ball;
	protected Player first, second;
	protected int firstScore, secondScore;
	protected long syncMilli = System.currentTimeMillis();
	protected ArrayList<Effect> effects = new ArrayList<Effect>();

	protected HockeyTable(int num) {
		this.num = num;
		ball = new Ball();
		first = new Player(0);
		second = new Player(1);
	}

	synchronized protected void sync(String str) {
		String[] texts = str.split("\\|");
		num = Integer.parseInt(texts[0]);
		ball.sync(texts[1]);
		if (num != 0)
			first.sync(texts[2]);
		firstScore = Integer.parseInt(texts[3]);
		if (num != 1)
			second.sync(texts[4]);
		secondScore = Integer.parseInt(texts[5]);
		if (texts[6].length() > 1) {
			String[] effs = texts[6].split(",");
			effects.add(new Effect(Double.parseDouble(effs[0]), Double
					.parseDouble(effs[1]), Double.parseDouble(effs[2]),
					new Color(175, 225, 255)));
			try {
				Clip c = AudioSystem.getClip();
				c.open(AudioSystem.getAudioInputStream(this.getClass()
						.getResource("bounce.wav")));
				c.start();
			} catch (LineUnavailableException | IOException
					| UnsupportedAudioFileException e) {
				e.printStackTrace();
			}
		}
	}

	synchronized protected void update(int x, int y) {
		if (num == 0)
			first.sync(convertX(x) + "," + convertY(y));
		if (num == 1)
			second.sync(convertX(x) + "," + convertY(y));
	}

	synchronized protected boolean hit() {
		boolean isHit = false;
		for (int n = 0; n < 2; n++) {
			Player p = null;
			if (n == 0)
				p = first;
			else
				p = second;
			if (Math.hypot(p.x - ball.x, p.y - ball.y) < Player.SIZE
					+ Ball.SIZE) {
				double angle = Math.atan2(ball.y - p.y, ball.x - p.x);
				double playerSpeedHit = p.speed * Math.cos(p.angle - angle);
				double ballSpeedHit = ball.speed * Math.cos(ball.angle - angle);
				double ballSpeedPerpen = ball.speed
						* Math.sin(ball.angle - angle);
				ballSpeedHit = playerSpeedHit + Math.abs(ballSpeedHit);
				ball.speed = Math.hypot(ballSpeedHit, ballSpeedPerpen);
				ball.angle = Math.atan2(ballSpeedPerpen, ballSpeedHit) + angle;
				while (Math.hypot(p.x - ball.x, p.y - ball.y) < Player.SIZE
						+ Ball.SIZE) {
					ball.x += 1 * Math.cos(ball.angle);
					ball.y += 1 * Math.sin(ball.angle);
				}
				isHit = true;
				if (num == -1)
					effects.add(new Effect(p.x + Player.SIZE * Math.cos(angle),
							p.y + Player.SIZE * Math.sin(angle), 15, new Color(
									175, 225, 255)));
			}
		}
		double ballSpeedX = ball.speed * Math.cos(ball.angle);
		double ballSpeedY = ball.speed * Math.sin(ball.angle);
		if (ball.x < Ball.SIZE) {
			ballSpeedX = Math.abs(ballSpeedX);
			ball.x = Ball.SIZE;
			isHit = true;
			if (num == -1)
				effects.add(new Effect(0, ball.y, 20, new Color(175, 225, 255)));
		}
		if (ball.x > HockeyTable.WIDTH - Ball.SIZE) {
			ballSpeedX = -Math.abs(ballSpeedX);
			ball.x = HockeyTable.WIDTH - Ball.SIZE;
			isHit = true;
			if (num == -1)
				effects.add(new Effect(HockeyTable.WIDTH, ball.y, 20,
						new Color(175, 225, 255)));
		}
		if (ball.y < Ball.SIZE) {
			ballSpeedY = Math.abs(ballSpeedY);
			ball.y = Ball.SIZE;
			firstScore++;
			isHit = true;
			if (num == -1)
				effects.add(new Effect(ball.x, 0, 50, new Color(175, 225, 255)));
		}
		if (ball.y > HockeyTable.HEIGHT - Ball.SIZE) {
			ballSpeedY = -Math.abs(ballSpeedY);
			ball.y = HockeyTable.HEIGHT - Ball.SIZE;
			secondScore++;
			isHit = true;
			if (num == -1)
				effects.add(new Effect(ball.x, HockeyTable.HEIGHT, 50,
						new Color(175, 225, 255)));
		}
		ball.angle = Math.atan2(ballSpeedY, ballSpeedX);
		if (ball.speed > Ball.MAX_SPEED) {
			ball.speed = Ball.MAX_SPEED;
		} else if (ball.speed < -Ball.MAX_SPEED) {
			ball.speed = -Ball.MAX_SPEED;
		}
		return isHit;
	}

	synchronized private double convertX(double x) {
		if (num <= 0)
			return x;
		return WIDTH - x;
	}

	synchronized private double convertY(double y) {
		if (num <= 0)
			return y;
		return HEIGHT - y;
	}

	synchronized protected void paint(BufferedImage img) {
		Graphics2D g = img.createGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		g.setColor(new Color(0, 0, 0,
				(int) (Math.random() * 5 * Math.random() * 5) + 134));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		for (Effect e : effects) {
			g.setColor(e.c);
			g.fillOval((int) (convertX(e.x) - e.size),
					(int) (convertY(e.y) - e.size), (int) (e.size * 2),
					(int) (e.size * 2));
			e.update();
			if (e.time <= 0) {
				effects.remove(e);
				break;
			}
		}
		g.setPaintMode();
		BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, new float[] {
				1 / (float) ((Math.random() * 3) + 3),
				1 / (float) ((Math.random() * 3) + 3),
				1 / (float) ((Math.random() * 3) + 3),
				1 / (float) ((Math.random() * 3) + 3),
				1 / (float) ((Math.random() * 3) + 3),
				1 / (float) ((Math.random() * 3) + 3),
				1 / (float) ((Math.random() * 3) + 3),
				1 / (float) ((Math.random() * 3) + 3),
				1 / (float) ((Math.random() * 3) + 3) }));
		BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(),
				img.getType());
		op.filter(img, img2);
		g.drawImage(img2, 0, 0, null);
		Player rfirst = num == 0 ? first : second;
		Player rsecond = num == 1 ? first : second;
		if (num == -1) {
			rfirst = first;
			rsecond = second;
		}
		int rfirstScore = num == 0 ? firstScore : secondScore;
		int rsecondScore = num == 1 ? firstScore : secondScore;
		if (num == -1) {
			rfirstScore = firstScore;
			rsecondScore = secondScore;
		}
		g.setFont(new Font("Tahoma", Font.PLAIN, 50));
		FontMetrics fm = g.getFontMetrics();
		for (int i = 0; i < 3; i++) {
			if (i == 0) {
				g.setColor(new Color(150, 200, 255));
				g.setStroke(new BasicStroke(7f));
			} else if (i == 1) {
				g.setColor(new Color(200, 230, 255));
				g.setStroke(new BasicStroke(5f));
			} else {
				g.setColor(new Color(255, 255, 255));
				g.setStroke(new BasicStroke(3f));
			}
			fm = g.getFontMetrics();
			g.drawRect(0, 0, WIDTH, HEIGHT);
			g.drawLine(0, HEIGHT / 2, WIDTH, HEIGHT / 2);
			g.drawOval(WIDTH / 2 - 50, HEIGHT / 2 - 50, 100, 100);
			g.drawOval(WIDTH / 2 - 50, HEIGHT / 2 - 50, 100, 100);
			g.setColor(new Color(g.getColor().getRed(), g.getColor().getBlue(),
					g.getColor().getGreen()));
			g.drawOval((int) convertX(rfirst.x) - Player.SIZE,
					(int) convertY(rfirst.y) - Player.SIZE, Player.SIZE * 2,
					Player.SIZE * 2);
			if (i < 2)
				g.drawString(rfirstScore + "",
						WIDTH - fm.stringWidth(rfirstScore + "") - 20, HEIGHT
								/ 2 + 10 + fm.getAscent());
			g.setColor(new Color(g.getColor().getGreen(), g.getColor()
					.getBlue(), g.getColor().getBlue()));
			g.drawOval((int) convertX(rsecond.x) - Player.SIZE,
					(int) convertY(rsecond.y) - Player.SIZE, Player.SIZE * 2,
					Player.SIZE * 2);
			if (i < 2)
				g.drawString(rsecondScore + "",
						WIDTH - fm.stringWidth(rsecondScore + "") - 20,
						HEIGHT / 2 - 20);
			g.setColor(new Color(g.getColor().getBlue(),
					g.getColor().getBlue(), g.getColor().getGreen()));
			g.drawOval((int) convertX(ball.x) - Ball.SIZE,
					(int) convertY(ball.y) - Ball.SIZE, Ball.SIZE * 2,
					Ball.SIZE * 2);
		}
		g.setColor(new Color(255, 255, 255));
		g.setStroke(new BasicStroke(1f));
		g.setFont(new Font("Tahoma", Font.PLAIN, 12));
		fm = g.getFontMetrics();
		g.drawString(
				rfirst.getName(),
				(int) convertX(rfirst.x) - fm.stringWidth(rfirst.getName()) / 2,
				(int) convertY(rfirst.y) + 5);
		g.drawString(rsecond.getName(),
				(int) convertX(rsecond.x) - fm.stringWidth(rsecond.getName())
						/ 2, (int) convertY(rsecond.y) + 5);
	}

	synchronized public boolean predict() {
		boolean isHit = false;
		long timeDif = System.currentTimeMillis() - syncMilli;
		syncMilli = System.currentTimeMillis();
		double val[] = { first.speed * Math.cos(first.angle) * timeDif,
				first.speed * Math.sin(first.angle) * timeDif,
				second.speed * Math.cos(second.angle) * timeDif,
				second.speed * Math.sin(second.angle) * timeDif,
				ball.speed * Math.cos(ball.angle) * timeDif,
				ball.speed * Math.sin(ball.angle) * timeDif };
		double max = Math.abs(val[0]);
		for (double d : val) {
			max = Math.max(max, Math.abs(d));
		}
		max = Math.ceil(Math.min(20, max));
		for (int i = 0; i < max; i++) {
			val = new double[] { first.speed * Math.cos(first.angle) * timeDif,
					first.speed * Math.sin(first.angle) * timeDif,
					second.speed * Math.cos(second.angle) * timeDif,
					second.speed * Math.sin(second.angle) * timeDif,
					ball.speed * Math.cos(ball.angle) * timeDif,
					ball.speed * Math.sin(ball.angle) * timeDif };
			first.x += val[0] / max;
			first.y += val[1] / max;
			second.x += val[2] / max;
			second.y += val[3] / max;
			ball.x += val[4] / max;
			ball.y += val[5] / max;
			isHit |= hit();
		}
		first.x -= val[0];
		first.y -= val[1];
		second.x -= val[2];
		second.y -= val[3];
		ball.speed *= 0.997;
		return isHit;
	}

	@Override
	synchronized public String toString() {
		return num + "|" + ball + "|" + first + "|" + firstScore + "|" + second
				+ "|" + secondScore;
	}

	synchronized protected void setName(int num, String name) {
		if (num == 0)
			first.setName(name);
		if (num == 1)
			second.setName(name);
	}

	public static String EffectToString(ArrayList<Effect> effects) {
		if (effects.size() <= 0)
			return "X";
		Effect e = effects.get(effects.size() - 1);
		return String.format("%.0f", e.x) + "," + String.format("%.0f", e.y)
				+ "," + String.format("%.0f", e.size);
	}
}
