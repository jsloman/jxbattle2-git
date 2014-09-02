package uk.me.jonathans.jxbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {

	JXBattleGame game;
	Stage stage;
	Skin skin;
	
	public MainMenuScreen(JXBattleGame game) {
		this.game = game;
		
		skin = new Skin();
		// Generate a 1x1 white texture and store it in the skin named "white".
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		skin.add("white", new Texture(pixmap));

		// Store the default libgdx font under the name "default".
		skin.add("default", new BitmapFont());
		// Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
		TextButtonStyle textButtonStyle = new TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
		textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
		textButtonStyle.font = skin.getFont("default");
		skin.add("default", textButtonStyle);

		
		stage = new Stage();
		
		Table table = new Table();
		table.setFillParent(true);
		
		stage.addActor(table);
		
		final TextButton hexStartButton = new TextButton("Start Hex Game", skin);
		table.add(hexStartButton);
	
		hexStartButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				startGame(GameConfig.GRID_HEX);
			}
		});
		
		table.row();
		table.row();

		
		final TextButton squareStartButton = new TextButton("Start Square Game", skin);
		table.add(squareStartButton);
	
		squareStartButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				startGame(GameConfig.GRID_SQUARE);
			}
		});


		// Add an image actor. Have to set the size, else it would be the size of the drawable (which is the 1x1 texture).
		//table.add(new Image(skin.newDrawable("white", Color.RED))).size(64);
	}
	
	
	private void startGame(int gridType) {
		game.getConfig().setGridType(gridType);
		game.getConfig().setGridHeight(8);
		game.getConfig().setGridWidth(6);
		game.startGame();
	}
		
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1f, 0.95f, 0.95f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		// stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		
		// TODO Table.drawDebug(stage);
	}

	@Override
	public void resize(int width, int height) {
		
		// TODO stage.setViewport(width, height, true);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		stage.dispose();
		skin.dispose();
	}

}
