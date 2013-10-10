package se.openmind.kart;

import java.util.List;

import se.openmind.kart.OrderUpdate.Order;

public class GameState {
	private String error;
	private boolean inGame;
	private boolean gameRunning;
	private int secondsLeft;
	private Kart yourKart;
	private List<Kart> enemyKarts;
	private List<Shell> shells;
	private List<ItemBox> itemBoxes;
	
	public static class Entity {
		private int id;
		private double xPos;
		private double yPos;
		
		public double getXPos() {
			return xPos;
		}
		public double getYPos() {
			return yPos;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
	}
	
	public static class MovingEntity extends Entity {
		private double direction;
		private double xSpeed;
		private double ySpeed;
		
		public double getDirection() {
			return direction;
		}
		public double getxSpeed() {
			return xSpeed;
		}
		public double getySpeed() {
			return ySpeed;
		}
	}
	
	public static class Shell extends MovingEntity {
		private double timeLeft;
		private int ownerId;
		private int targetId;
		
		public double getTimeLeft() {
			return timeLeft;
		}
		public int getOwnerId() {
			return ownerId;
		}
		public int getTargetId() {
			return targetId;
		}
	}
	
	public static class ItemBox extends Entity {
		// Nothing but position and id
	}
	
	public static class Kart extends MovingEntity {
		private int baloons;
		private int shells;
		private int hits;
		
		private double shellCooldownTimeLeft;
		private double stunnedTimeLeft;
		private double invulnerableTimeLeft;
		
		private Order order;
		
		public int getBaloons() {
			return baloons;
		}
		public int getShells() {
			return shells;
		}
		public int getHits() {
			return hits;
		}
		public double getShellCooldownTimeLeft() {
			return shellCooldownTimeLeft;
		}
		public double getStunnedTimeLeft() {
			return stunnedTimeLeft;
		}
		public double getInvulnerableTimeLeft() {
			return invulnerableTimeLeft;
		}
		public Order getOrder() {
			return order;
		}
	}

	public String getError() {
		return error;
	}

	public boolean isInGame() {
		return inGame;
	}

	public boolean isGameRunning() {
		return gameRunning;
	}

	public int getSecondsLeft() {
		return secondsLeft;
	}

	public Kart getYourKart() {
		return yourKart;
	}

	public List<Kart> getEnemyKarts() {
		return enemyKarts;
	}

	public List<Shell> getShells() {
		return shells;
	}

	public List<ItemBox> getItemBoxes() {
		return itemBoxes;
	}
	
	// Some generic helper methods
	public Entity getEntity(int id) {
		for(Kart k : enemyKarts) {
			if(k.getId() == id) return k;
		}
		if(yourKart != null && yourKart.getId() == id) {
			return yourKart;
		}
		for(Shell s : shells) {
			if(s.getId() == id) return s;
		}
		for(ItemBox b : itemBoxes) {
			if(b.getId() == id) return b;
		}
		return null;
	}
	
	public Kart getKart(int id) {
		for(Kart k : enemyKarts) {
			if(k.getId() == id) return k;
		}
		if(yourKart != null && yourKart.getId() == id) {
			return yourKart;
		}
		return null;
	}
	
	public Shell getShell(int id) {
		for(Shell s : shells) {
			if(s.getId() == id) return s;
		}
		return null;
	}
	
	public ItemBox getItemBox(int id) {
		for(ItemBox b : itemBoxes) {
			if(b.getId() == id) return b;
		}
		return null;
	}
}
