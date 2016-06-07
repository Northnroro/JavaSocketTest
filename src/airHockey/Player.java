package airHockey;

public class Player {
	public static final int SIZE = 30;
	private int num;
	private String name = "XXX";
	protected double x, y;
	protected double speed, angle;
	protected long syncMilli = System.currentTimeMillis();

	protected Player(int num) {
		if (num == 0) {
			x = HockeyTable.WIDTH / 2;
			y = HockeyTable.HEIGHT - 100;
		} else {
			x = HockeyTable.WIDTH / 2;
			y = 100;
		}
		this.num = num;
	}

	protected void sync(String str) {
		long timeDif = System.currentTimeMillis() - syncMilli;
		String[] texts = str.split(",");
		double x = Double.parseDouble(texts[0]);
		double y = Double.parseDouble(texts[1]);
		if (num == 0) {
			x = Math.max(Player.SIZE,
					Math.min(HockeyTable.WIDTH - Player.SIZE, x));
			y = Math.max(HockeyTable.HEIGHT / 2 + Player.SIZE,
					Math.min(HockeyTable.HEIGHT - Player.SIZE, y));
		} else if (num == 1) {
			x = Math.max(Player.SIZE,
					Math.min(HockeyTable.WIDTH - Player.SIZE, x));
			y = Math.max(0, Math.min(HockeyTable.HEIGHT / 2 - Player.SIZE, y));
		}
		if (timeDif > 0) {
			speed = Math.hypot(x - this.x, y - this.y) / timeDif;
			angle = Math.atan2(y - this.y, x - this.x);
		}
		this.x = x;
		this.y = y;
		syncMilli = System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return String.format("%.0f", x) + "," + String.format("%.0f", y);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
