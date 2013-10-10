package main;

import se.openmind.kart.*;
import se.openmind.kart.GameState.*;
import se.openmind.kart.OrderUpdate.Order;

public class MyBot implements Bot {
	/*
	 * The first thing you should do is enter your access key and team name 
	 * Note: Team name cannot be changed once set (without magic from contest administrators)
	 */
	private static String accessKey = "7d06282d";
	private static String teamName =  "Bartin Shlied";
	
	public static void main(String[] args) {
		String url = "http://localhost:8080/api/GameState";
		if(args.length > 1) {
			url = args[0];
		}
		
		ApiClient client = new ApiClient(url, accessKey, teamName);
		client.Run(new MyBot());
	}
	
	Integer targetEnemy;
	
	/**
	 * This is the main method that the ApiClient will invoke, put your game logic here.
	 */
	@Override
	public Order playGame(GameState state) {
		Kart me = state.getYourKart();
		Order order = new Order();
		Point2d position = new Point2d(me.getXPos(), me.getYPos());
		
		if(playDefensive(state) != null) {
			position = playDefensive(state);
		} else if(me.getShells() == 5) {
			position = moveToClosestEnemy(state);
		} else {
			position = moveToClosestBox(state);
		}
		
		moveTowards(order, position);
		if(me.getShells() > 0 && me.getShellCooldownTimeLeft() == 0) {
			order = shootClosestPlayer(order, state);
		}

		return order;
	}
	
	private Point2d playDefensive(GameState state) {
		Kart me = state.getYourKart();
		if(me.getShellCooldownTimeLeft() > 0) {
			return getAveragePosition(state);
		} else {
			return null;
		}
	}
	
	private Point2d getAveragePosition(GameState state) {
		Kart me = state.getYourKart();
		int cutoff = 40;
		Point2d result = new Point2d();
		for(Kart kart : state.getEnemyKarts()) {
			if(distance(me, kart) < cutoff) {
				Point2d position = new Point2d(kart.getXPos(), kart.getYPos());
				result.subtract(position);
			}
		}
		result.divide(state.getEnemyKarts().size());
		return result;
	}
	
	private Point2d moveToClosestBox(GameState state) {
		Kart me = state.getYourKart();
		ItemBox closestItemBox = null;
		for(ItemBox i : state.getItemBoxes()) {
			if(closestItemBox == null || distance(me, i) < distance(me, closestItemBox)) {
				closestItemBox = i;
			}
		}
		if(closestItemBox != null) {
			return new Point2d(closestItemBox.getXPos(), closestItemBox.getYPos());
		}
		return null;
	}
	
	private Point2d moveToClosestEnemy(GameState state) {
		Kart me = state.getYourKart();
		double distance = Double.MAX_VALUE;
		Kart closest = null;
		
		for(Kart enemy : state.getEnemyKarts()) {
			if(distance(me, enemy) < distance) {
				distance = distance(me, enemy);
				closest = enemy;
			}
		}
		if(closest == null) {
			return null;
		}
		
		Point2d c = interpolate(closest);
		c = scaleToMap(c);
		
		return c;
	}
	
	public double getAngleBetween(Point2d p1, Point2d p2) {
		double a1 = Math.atan2(p1.y, p1.x);
		double a2 = Math.atan2(p2.y, p2.x);

		if (a1 < -Math.PI/2 && a2 > Math.PI/2) a1 += Math.PI * 2;
		if (a2 < -Math.PI/2 && a1 > Math.PI/2) a2 += Math.PI * 2;

		return a2-a1;
	}
	
	private Order moveTowards(Order order, Point2d c) {
		order.setMoveX(c.getXPos());
		order.setMoveY(c.getYPos());
		
		return order;
	}
	
	private Order shootClosestPlayer(Order order, GameState state) {
		Kart me = state.getYourKart();
		double shortest = Double.MAX_VALUE;
		Kart closest = null;
		for(Kart kart : state.getEnemyKarts()) {
			if(distance(kart, me) < shortest) {
				shortest = distance(kart, me);
				closest = kart;
			}
		}
		if(closest == null) {
			return order;
		}
		
		if(willMostDefinitelyHit(me, closest)) {
			if(canBeShot(closest)) {
				order.setFireAt(closest.getId());
			}
		}
		
		return order;
	}
	
	private boolean canBeShot(Kart enemy) {
		return enemy.getInvulnerableTimeLeft() == 0;
	}
	
	private boolean willMostDefinitelyHit(Kart me, Kart enemy) {
		double distance = distance(me, interpolate(enemy));
		if(distance > 30) {
			return false;
		}
		return isInsideShootingTriangle(me, enemy);
		/*double angle = getAngleBetween(asPoint(me), asPoint(enemy));
		angle %= Math.PI;
		
		if(angle < Math.PI / 2) {
			return true;
		}
		
		return false;*/
	}
	
	private boolean isInsideShootingTriangle(Kart me, Kart enemy) {
		double range = 40;
		Point2d myPos = asPoint(me);
		Point2d enemyPos = asPoint(enemy);
		double diffusion = Math.PI / 3;
		double shootingAngle = me.getDirection();
		double rightAngle = shootingAngle - diffusion;
		double leftAngle = shootingAngle + diffusion;
		Point2d rightPoint = new Point2d(
				myPos.x + Math.cos(rightAngle) * range, 
				myPos.y + Math.sin(rightAngle) * range);
		Point2d leftPoint = new Point2d(
				myPos.x + Math.cos(leftAngle) * range, 
				myPos.y + Math.sin(leftAngle) * range);
		double reverse = shootingAngle + Math.PI;
		double rightAngleReverse = reverse - diffusion;
		double leftAngleReverse = reverse + diffusion;
		Point2d rightPointReverse = new Point2d(
				myPos.x + Math.cos(rightAngleReverse) * range, 
				myPos.y + Math.sin(rightAngleReverse) * range);
		Point2d leftPointReverse = new Point2d(
				myPos.x + Math.cos(leftAngleReverse) * range, 
				myPos.y + Math.sin(leftAngleReverse) * range);
		
		boolean goodForward = isPointInsideTriangle(enemyPos, myPos, leftPoint, rightPoint);
		boolean goodBackwards = isPointInsideTriangle(enemyPos, myPos, leftPointReverse, rightPointReverse);
		return goodForward || goodBackwards;
	}
	
	private boolean isPointInsideTriangle(Point2d s, Point2d a, Point2d b, Point2d c)
	{
	    int as_x = (int) (s.x - a.x);
	    int as_y = (int) (s.y - a.y);
	    boolean s_ab = (b.x-a.x)*as_y-(b.y-a.y)*as_x > 0;
	    if((c.x-a.x)*as_y-(c.y-a.y)*as_x > 0 == s_ab) return false;
	    if((c.x-b.x)*(s.y-b.y)-(c.y-b.y)*(s.x-b.x) > 0 != s_ab) return false;
	    return true;
	}
	
	private Point2d asPoint(Entity e) {
		return new Point2d(e.getXPos(), e.getYPos());
	}
	
	private Point2d scaleToMap(Point2d c) {
		if(c.x > GameConstants.MaxBoundX) {
			c.x = GameConstants.MaxBoundX;
		} else if(c.x < GameConstants.MinBoundX) {
			c.x = GameConstants.MinBoundX;
		}
		
		if(c.y > GameConstants.MaxBoundY) {
			c.y = GameConstants.MaxBoundY;
		} else if(c.y < GameConstants.MinBoundY) {
			c.y = GameConstants.MinBoundY;
		}
		
		return c;
	}
	
	private Point2d interpolate(MovingEntity e) {
		double xSpeed = 10 * Math.cos(e.getDirection());
		double ySpeed = 10 * Math.sin(e.getDirection());
		
		double x = (e.getXPos() + xSpeed);
		double y = (e.getYPos() + ySpeed);
		
		return new Point2d(x, y);
	}
	
	private double distance(Entity a, Entity b) {
		double xDist = a.getXPos() - b.getXPos();
		double yDist = a.getYPos() - b.getYPos();
		return Math.sqrt(xDist*xDist + yDist*yDist);
	}
}
