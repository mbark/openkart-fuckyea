package main;

import se.openmind.kart.GameState.Entity;

public class Point2d extends Entity {
	public double x;
	public double y;
	
	public Point2d() {
		x = 0;
		y = 0;
	}
	
	public Point2d(double x, double y) {
		this.x = x;
		this.y = y;
	}
	

	@Override
	public double getXPos() {
		return x;
	}
	
	@Override
	public double getYPos() {
		return y;
	}
	
	public double distance(Point2d point) {
		return Math.sqrt((point.x - x)*(point.x - x) + (point.y - y)*(point.y - y));
	}
	
	public double distance(double x, double y) {
		return distance(new Point2d(x, y));
	}
	
	public Point2d getLocation() {
		return new Point2d(x,y);
	}

	public void divide(double... d) {
		double divisor = 1;
		for(int i = 0; i < d.length; i++) {
			divisor *= d[i];
		}
		x /= divisor;
		y /= divisor;
	}
	
	public void multiply(double... d) {
		double multiplier = 1;
		for(int i = 0; i < d.length; i++) {
			multiplier *= d[i];
		}
		x *= multiplier;
		y *= multiplier;
	}
	
	public void subtract(Point2d... p) {
		for(int i = 0; i<p.length; i++) {
			x -= p[i].x;
			y -= p[i].y;
		}
	}
		
	public void add(Point2d... p) {
		for(int i = 0; i<p.length; i++) {
			x += p[i].x;
			y += p[i].y;
		}
	}
	
	public double norm() {
		return Math.sqrt(x*x + y*y);
	}
	
	public void normalize(double... multipliers) {
		if (norm() == 0) {
			return;
		}
		double multiplier = 1;
		for(int i = 0; i<multipliers.length; i++) {
			multiplier *= multipliers[i];
		}
		x = (x / norm() * multiplier);
		y = (y / norm() * multiplier);
	}
	
	public static Point2d generateRandom(double upperLimit) {
		return generateRandom(0, upperLimit);
	}
	
	public static Point2d generateRandom(double lowerLimit, double upperLimit) {
		Point2d result = new Point2d(1, 1);
		result.multiply(Math.random()*upperLimit);
		result.add(lowerLimit);
		result.normalize(1);
		return result;
	}
	
	public static Point2d pointFromAngle(double angle) {
		return new Point2d(Math.cos(angle), Math.sin(angle));
	}
	
	private void add(double d) {
		x += d;
		y += d;
	}

	public double angle() {
		return Math.atan2(y, x);
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
