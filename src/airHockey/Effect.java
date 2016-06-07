package airHockey;

import java.awt.Color;

public class Effect {
	protected double x, y, size;
	protected int time = 20;
	protected Color c;

	protected Effect(double x, double y, double size, Color c) {
		this.x = x;
		this.y = y;
		this.size = size * 1.5;
		this.c = c;
	}

	protected void update() {
		time--;
		size *= 0.95;
		c = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (20 - time));
	}
}
