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
		
		// This default implementation will move towards the closest item box
		ItemBox closestItemBox = null;
		for(ItemBox i : state.getItemBoxes()) {
			if(closestItemBox == null || distance(me, i) < distance(me, closestItemBox)) {
				closestItemBox = i;
			}
		}
		if(closestItemBox != null) {
			return Order.MoveOrder(closestItemBox.getXPos(), closestItemBox.getYPos());
		}
		
		// Returning null is ok, your bot will continue doing what it is doing
		return null;
	}
	
	private double distance(Entity a, Entity b) {
		double xDist = a.getXPos() - b.getXPos();
		double yDist = a.getYPos() - b.getYPos();
		return Math.sqrt(xDist*xDist + yDist*yDist);
	}
}
