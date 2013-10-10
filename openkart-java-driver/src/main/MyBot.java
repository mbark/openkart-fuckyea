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
		
		if(me.getShells() == 5) {
			order = moveToClosestEnemy(order, state);
		} else {
			order = moveToClosestBox(order, state);
		}
		
		if(me.getShells() > 0 && me.getShellCooldownTimeLeft() == 0) {
			order = shootClosestPlayer(order, state);
		}

		return order;
	}
	
	private Order moveToClosestBox(Order order, GameState state) {
		Kart me = state.getYourKart();
		ItemBox closestItemBox = null;
		for(ItemBox i : state.getItemBoxes()) {
			if(closestItemBox == null || distance(me, i) < distance(me, closestItemBox)) {
				closestItemBox = i;
			}
		}
		if(closestItemBox != null) {
			order.setMoveX(closestItemBox.getXPos());
			order.setMoveY(closestItemBox.getYPos());
		}
		return order;
	}
	
	private Order moveToClosestEnemy(Order order, GameState state) {
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
			return order;
		}
		
		Coordinate c = interpolate(closest);
		c = scaleToMap(c);
		
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
		double angleDiff = Math.abs(me.getDirection() - enemy.getDirection()) % Math.PI;
		double distance = distance(me, interpolate(enemy));
		
		if(distance > 30) {
			return false;
		}
		
		if(angleDiff < Math.PI / 4) {
			return true;
		}
		
		return false;
	}
	
	private Coordinate scaleToMap(Coordinate c) {
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
	
	private Coordinate interpolate(MovingEntity e) {
		double xSpeed = 10 * Math.cos(e.getDirection());
		double ySpeed = 10 * Math.sin(e.getDirection());
		
		double x = (e.getXPos() + xSpeed);
		double y = (e.getYPos() + ySpeed);
		
		return new Coordinate(x, y);
	}
	
	private final class Coordinate extends Entity {
		double x;
		double y;
		private Coordinate(double x, double y) {
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
	}
	
	private double distance(Entity a, Entity b) {
		double xDist = a.getXPos() - b.getXPos();
		double yDist = a.getYPos() - b.getYPos();
		return Math.sqrt(xDist*xDist + yDist*yDist);
	}
}
