package utils;

public class Box {
	public final double minX;
	public final double minY;
	public final double maxX;
	public final double maxY;
	public final double centreX;
	public final double centreY;

	public Box(double minX, double minY, double maxX, double maxY) {
		this.minX = Math.min(minX, maxX);
		this.minY = Math.min(minY, maxY);
		this.maxX = Math.max(minX, maxX);
		this.maxY = Math.max(minY, maxY);
		this.centreX = (minX + maxX) / 2;
		this.centreY = (minY + maxY) / 2;
	}
	
	public double calcDistance(double x, double y) {
		double distanceX;
		double distanceY;

		if (this.minX <= x && x <= this.maxX) {
			distanceX = 0;
		} else {
			distanceX = Math.min(Math.abs(this.minX - x), Math.abs(this.maxX - x));
		}
		if (this.minY <= y && y <= this.maxY) {
			distanceY = 0;
		} else {
			distanceY = Math.min(Math.abs(this.minY - y), Math.abs(this.maxY - y));
		}

		return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
	}

	public boolean contains(double x, double y) {
		return (x >= this.minX &&
				y >= this.minY &&
				x < this.maxX &&
				y < this.maxY);
	}

	public boolean containsOrEquals(Box box) {
		return (box.minX >= this.minX &&
				box.minY >= this.minY &&
				box.maxX <= this.maxX &&
				box.maxY <= this.maxY);
	}

	public boolean intersects(Box other) {
		if ((this.maxX-this.minX) <= 0 || (this.maxY-this.minY) <= 0) {
			return false;
		}
		return (other.maxX > this.minX &&
				other.maxY > this.minY &&
				other.minX < this.maxX &&
				other.minY < this.maxY);
	}

	public Box intersection(Box r) {
		double tx1 = this.minX;
		double ty1 = this.minY;
		double tx2 = this.maxX;
		double ty2 = this.maxY;
		if (this.minX < r.minX) tx1 = r.minX;
		if (this.minY < r.minY) ty1 = r.minY;
		if (tx2 > r.maxX) tx2 = r.maxX;
		if (ty2 > r.maxY) ty2 = r.maxY;
		// did they intersect at all?
		if(tx2-tx1 <=0.f || ty2-ty1 <= 0.f) return null;

		return new Box(tx1, ty1, tx2, ty2);
	}

	public Box union(Box b) {
		return new Box( Math.min(this.minX, b.minX),
				Math.min(this.minY, b.minY),
				Math.max(this.maxX, b.maxX),
				Math.max(this.maxY, b.maxY));
	}
	
	public Box scale(double scaleX, double scaleY) {
		scaleY *= this.centreY - this.minY;
		scaleX *= this.centreX - this.minX;
		return new Box(this.minX - scaleX, this.minY-scaleY, this.maxX + scaleX, this.maxY + scaleY);
	}
	
	@Override
	public String toString() {
		return "upperLeft: (" + minX + ", " + minY + ") lowerRight: (" + maxX + ", " + maxY + ")";
	}
	
}