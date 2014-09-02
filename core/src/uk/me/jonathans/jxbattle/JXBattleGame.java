package uk.me.jonathans.jxbattle;

import com.badlogic.gdx.Game;

public class JXBattleGame extends Game {

	 MainMenuScreen mainMenuScreen;
     PlayScreen playScreen;
     
     GameConfig config;
	
	@Override
	public void create() {
		config = new GameConfig();
		mainMenuScreen = new MainMenuScreen(this);
        playScreen = new PlayScreen(this);
        setScreen(mainMenuScreen);           
	}

	public void switchToMenu() {
		setScreen(mainMenuScreen);
	}
	
	public void switchToGame() {
		setScreen(playScreen);
	}
	
	public void startGame() {
		playScreen.startGame(config);
		switchToGame();
	}
	
	public GameConfig getConfig() {
		return config;
	}
}