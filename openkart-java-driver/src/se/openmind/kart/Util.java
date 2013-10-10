package se.openmind.kart;

import main.MyBot;

public class Util {
	/**
	 * Utility for running multiple bots in the same process
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://localhost:8080/api/GameState";
		if(args.length > 1) {
			url = args[0];
		}
		
		// runBot(url, new MyBot(), "banankontakt1", "MyBot");
		// runBot(url, new TestBot(), "banankontakt2", "TestBot1");
		// runBot(url, new TestBot(), "banankontakt3", "TestBot2");
		// runBot(url, new TestBot(), "banankontakt4", "TestBot3");
		// runBot(url, new TestBot(), "banankontakt5", "TestBot4");
	}
	
	public static void runBot(final String url, final Bot bot, final String accessKey, final String teamName) {
		Runnable r = new Runnable() {
			public void run() {
				ApiClient client = new ApiClient(url, accessKey, teamName);
				client.Run(bot);
			}
		};
		new Thread(r).start();
	}
}
