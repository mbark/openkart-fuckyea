package se.openmind.kart;

public class OrderUpdate {
	private String apiKey;
	private String teamName;
	private Order order;
	
	public static class Order {
		private Double moveX;
		private Double moveY;
		private Integer fireAt;
		
		public Order() {
		}
		public Order(Double moveX, Double moveY, Integer fireAt) {
			this.moveX = moveX;
			this.moveY = moveY;
			this.fireAt = fireAt;
		}
		public static Order MoveOrder(double moveX, double moveY) {
			return new Order(moveX, moveY, null);
		}
		public static Order FireOrder(int fireAt) {
			return new Order(null, null, fireAt);
		}
		
		public Double getMoveX() {
			return moveX;
		}
		public Double getMoveY() {
			return moveY;
		}
		public Integer getFireAt() {
			return fireAt;
		}
		public void setMoveX(Double moveX) {
			this.moveX = moveX;
		}
		public void setMoveY(Double moveY) {
			this.moveY = moveY;
		}
		public void setFireAt(Integer fireAt) {
			this.fireAt = fireAt;
		}
	}
	
	public OrderUpdate(String apiKey, String teamName) {
		this.apiKey = apiKey;
		this.teamName = teamName;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getTeamName() {
		return teamName;
	}

	public Order getOrder() {
		return order;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
}
