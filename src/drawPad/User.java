package drawPad;

public class User {
	public String name;
	public int x, y;
	public boolean pressed;
	public int px, py;
	public boolean pressed2;
	public int color;
	public int size;

	public User(String name, int x, int y, boolean pressed, int px, int py,
			boolean pressed2, int color, int size) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.pressed = pressed;
		this.px = px;
		this.py = py;
		this.pressed2 = pressed2;
		this.color = color;
		this.size = size;
	}

	public User(String str) {
		this.name = str.split(",")[0];
		this.x = Integer.parseInt(str.split(",")[1]);
		this.y = Integer.parseInt(str.split(",")[2]);
		this.pressed = str.split(",")[3].equals("T");
		this.px = Integer.parseInt(str.split(",")[4]);
		this.py = Integer.parseInt(str.split(",")[5]);
		this.pressed2 = str.split(",")[6].equals("T");
		this.color = str.split(",")[7].charAt(0) - 'A';
		this.size = Integer.parseInt(str.split(",")[8]);
	}

	@Override
	public String toString() {
		return name + "," + x + "," + y + "," + (pressed ? "T" : "F") + ","
				+ px + "," + py + "," + (pressed2 ? "T" : "F") + ","
				+ (char) ('A' + color) + "," + size;
	}
}
