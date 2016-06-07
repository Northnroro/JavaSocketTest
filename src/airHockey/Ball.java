package airHockey;

public class Ball {
	public static final int SIZE = 10;
	public static final double MAX_SPEED = 0.7;
	protected double x, y;
	protected double px, py;
	protected double speed, angle;

	protected Ball() {
		x = HockeyTable.WIDTH / 2;
		y = HockeyTable.HEIGHT / 2;
	}

	protected void sync(String str) {
		String[] texts = str.split(",");
		x = Double.parseDouble(texts[0]);
		y = Double.parseDouble(texts[1]);
		speed = Double.parseDouble(texts[2]);
		angle = Double.parseDouble(texts[3]);
	}

	@Override
	public String toString() {
		return String.format("%.0f", x) + "," + String.format("%.0f", y) + ","
				+ String.format("%.2f", speed) + ","
				+ String.format("%.2f", angle);
	}
}
