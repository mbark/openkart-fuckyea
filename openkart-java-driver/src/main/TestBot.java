package main;

import se.openmind.kart.*;
import se.openmind.kart.GameState.*;
import se.openmind.kart.OrderUpdate.Order;

public class TestBot implements Bot {
	/*
	 * The first thing you should do is enter your access key and team name 
	 * Note: Team name cannot be changed once set (without magic from contest administrators)
	 */
	private static String accessKey = "testkey";
	private static String teamName =  "Testbot";
	
	public static void main(String[] args) {
		String url = "http://localhost:8080/api/GameState";
		if(args.length > 1) {
			url = args[0];
		}
		
		ApiClient client = new ApiClient(url, accessKey, teamName);
		client.Run(new TestBot());
	}
	
	Integer targetEnemy;
	
	/**
	 * This is the main method that the ApiClient will invoke, put your game logic here.
	 */
	@Override
	public Order playGame(GameState state) {
		try {
			Kart me = state.getYourKart();
			Order order = new Order();
			
			// This default implementation will move towards the closest item box
			ItemBox closestItemBox = null;
			for(ItemBox i : state.getItemBoxes()) {
				if(closestItemBox == null || distance(me, i) < distance(me, closestItemBox)) {
					closestItemBox = i;
				}
			}
			if(closestItemBox != null) {
				order = Order.MoveOrder(closestItemBox.getXPos(), closestItemBox.getYPos());
			}
			if(me.getShells() > 0 && me.getShellCooldownTimeLeft() == 0) {
				order = shootClosestPlayer(order, state);
			}
	
			return order;
		} catch (Exception e) {
			return null;
		}
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
		
		if(distance(me, interpolate(closest)) < 30) {
			order.setFireAt(closest.getId());
		}
		
		return order;
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
