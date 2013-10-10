package se.openmind.kart;

import se.openmind.kart.OrderUpdate.Order;

public interface Bot {
	public Order playGame(GameState currentState);
}
