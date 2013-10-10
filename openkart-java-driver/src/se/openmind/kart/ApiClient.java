package se.openmind.kart;

import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;

import se.openmind.kart.OrderUpdate.Order;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApiClient {
	private String apiUrl;
	private String accessKey;
	private String teamName; 
	
	public ApiClient(String apiUrl, String accessKey, String teamName) {
		super();
		this.apiUrl = apiUrl;
		this.accessKey = accessKey;
		this.teamName = teamName;
		
		if(accessKey == null || accessKey.equals("")) {
			throw new IllegalArgumentException("accessKey must be defined");
		}
		
		if(teamName == null || teamName.equals("")) {
			throw new IllegalArgumentException("teamName must be defined");
		}
	}

	public void Run(Bot bot) {
		OrderUpdate emptyUpdate = new OrderUpdate(accessKey, teamName);
		OrderUpdate update = emptyUpdate;
		boolean inGame = false;
		
		while(true) {
			GameState newState = updateState(update);
			
			if(newState.getError() != null) {
				System.err.println("API response indicates error: " + newState.getError());
				sleep(1000);
				update = emptyUpdate;
				continue;
			}
			
			if(newState.isInGame()) {
				if(!inGame) {
					System.out.println("ApiClient message: Your bot is now in a game");
					inGame = true;
				}
				
				Order newOrder = bot.playGame(newState);
				update = new OrderUpdate(accessKey, teamName);
				update.setOrder(newOrder);
			} else {
				if(inGame) {
					System.out.println("ApiClient message: Your bot is no longer in the game");
					inGame = false;
				}
				
				// A game is currently in progress, and we are not in it
				// Safe to sleep for almost 10s to save api from requests
				if(!newState.isInGame() && newState.isGameRunning()) {
					try {
						Thread.sleep(9900);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				update = emptyUpdate;
			}
		}
	}
	
	HttpClient client = HttpClientBuilder.create().build();
	
	public GameState updateState(OrderUpdate update) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(update);
		
		try {
			HttpPost post = new HttpPost(new URL(new URL(apiUrl), "/api/GameState").toURI());
			
			StringEntity entity = new StringEntity(json, CharsetUtils.get("UTF-8"));
			entity.setContentType("application/json");
			post.setEntity(entity);
			
			HttpResponse response = client.execute(post);
			
			String responseString = EntityUtils.toString(response.getEntity());
			GameState ret = gson.fromJson(responseString, GameState.class);
			
			return ret;
		} catch (Exception e) {
			System.err.println(e);
			System.err.println("Connection error - Will retry after 1s");
			sleep(1000);
		}
		
		return new GameState();
	}
	
	private void sleep(int millis) {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			
		}
	}
}
