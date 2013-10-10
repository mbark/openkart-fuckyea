package main;

import javax.swing.text.Position;

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
	
	Kart targetEnemy;
	double closestBoxDistance;
	ItemBox closestBox;
	
	/**
	 * This is the main method that the ApiClient will invoke, put your game logic here.
	 */
	@Override
	public Order playGame(GameState state) {
		Kart me = state.getYourKart();
		Order order = new Order();
		
		double shellPriority = getShellPriority(state);
		double enemyPriority = getEnemyPriority(state);
		Point2d closestBox = moveToClosestBox(state);
		Point2d attackEnemy = asPoint(targetEnemy);
		Point2d fear = avoidEnemy(state);
		
		Point2d goal = asPoint(me);
		
		if(shellPriority == Double.MAX_VALUE) {
			goal = closestBox;
		} else {
			closestBox.normalize(shellPriority);
			attackEnemy.normalize(enemyPriority);
			
			goal.add(closestBox);
			goal.add(attackEnemy);
			goal.add(fear);
			
			goal.normalize(5);
		}
		
		goal = scaleToMap(goal);
		
		order = moveTowards(order, goal);
		if(me.getShells() > 0 && me.getShellCooldownTimeLeft() == 0) {
			order = shootClosestPlayer(order, state);
		}

		return order;
	}
	
	private Point2d avoidEnemy(GameState state) {
		int cutoff = 50;
		Kart me = state.getYourKart();
		Point2d avg = new Point2d();
		double sum = 0;
		
		for(Kart enemy : state.getEnemyKarts()) {
			if(distance(me, enemy) < cutoff) {
				sum += distance(me, enemy);
				avg.add(asPoint(enemy));
			}
		}
		avg.divide(sum);
		Point2d here = asPoint(me);
		
		here.subtract(avg);
		
		return here;
	}
	
	private double getShellPriority(GameState state) {
		int shells = state.getYourKart().getShells();
		if(shells == 0) {
			return Double.MAX_VALUE;
		} else {
			return 10 * (5 - shells);
		}
	}
	
	private double getEnemyPriority(GameState state) {
		Kart me = state.getYourKart();
		if(me.getShellCooldownTimeLeft() > 0) {
			return Double.MIN_VALUE;
		}
		if(me.getShells() == 0) {
			return Double.MIN_VALUE;
		}
		
		double maxScore = Double.MIN_VALUE;
		for(Kart enemy : state.getEnemyKarts()) {
			if(enemy.getInvulnerableTimeLeft() > 0) {
				continue;
			}
			double score = 0;
			if(enemy.getShellCooldownTimeLeft() > 0) {
				score += 5;
			}
			if(enemy.getShells() == 0) {
				score += 10;
			}
			score += (50 - distance(me, enemy));
			if(score > maxScore) {
				targetEnemy = enemy;
			}
		}
		
		return maxScore;
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
		double angle = getAngleBetween(asPoint(me), asPoint(enemy));
		angle %= Math.PI;
		
		if(angle < Math.PI / 2 && distance < 30) {
			return true;
		}
		
		return false;
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
