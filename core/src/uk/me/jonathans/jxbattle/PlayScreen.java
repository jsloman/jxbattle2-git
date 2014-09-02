package uk.me.jonathans.jxbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Logger;

public class PlayScreen implements Screen, GestureListener {
	public static final Logger LOG = new Logger(PlayScreen.class.getName());
	
	JXBattleGame game;

	Music backgroundMusic;
	OrthographicCamera camera;
	OrthographicCamera hudCamera;
	SpriteBatch hudBatch;
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;
	
    BitmapFont font;

	Grid grid;

	float viewCentreX;
	float viewCentreY;
	float viewCentreXV;
	float viewCentreYV;
	
	Player thisPlayer;
	
	long lastTick;
	long startTime;
	long totalFrames = 0;
	long totalUpdates = 0;
	
	boolean gameOver = false;
	Player winner = null;
	
	int screenWidth;
	int screenHeight;
	
	boolean pathDrawing = false;
	Territory pathOrigin = null;
	
	public PlayScreen(JXBattleGame game) {
		this.game = game;
		
		viewCentreX = 0;
		viewCentreY = 0;
		viewCentreXV = 0;
		viewCentreYV = 0;
	
		// Background music
		//backgroundMusic = Gdx.audio.newMusic(Gdx.files
		//		.internal("background_loop.mp3"));
		//backgroundMusic.setLooping(true);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(viewCentreX, viewCentreY, 0);

		
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		hudCamera.update();
		
		hudBatch = new SpriteBatch();
		hudBatch.setProjectionMatrix(hudCamera.combined);
		
		batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);
		
		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);
		
		font = new BitmapFont();
	}
	
	public void startGame(GameConfig config) {
		switch(config.getGridType()) {
		case GameConfig.GRID_HEX:
			grid = new HexGrid(this, config.getGridWidth(), config.getGridHeight(), 
					Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			break;
		case GameConfig.GRID_SQUARE:
			grid = new SquareGrid(this, config.getGridWidth(), config.getGridHeight(), 
					Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			break;
		}
		/* initialise territory types - randomly for now */
		for (int i = 0; i < config.getGridWidth(); i++) {
			for (int j = 0; j < config.getGridHeight(); j++) {
				int rnd = (int)(Math.random() * 4);
				GridType type = GridType.PLAINS;
				switch(rnd) {
				case 0:
					type = GridType.FOREST;
					break;
				case 1:
					type = GridType.MOUNTAINS;
					break;
				case 2:
					type = GridType.PLAINS;
					break;
				case 3:
					type = GridType.WATER;
					break;
				}
				grid.getTerritory(i,  j).setType(type);
			}
		}
		
		Player human = new Player(new Color(1f, 0f, 0f, 0f));
		grid.addPlayer(human);
		thisPlayer = human;
		Player aiPlayer = new AIPlayer(new Color(0f, 0f, 1f, 0f));
		grid.addPlayer(aiPlayer);
		grid.setBase(2, 0, human);
		grid.setBase(3, 0, human);
		grid.setBase(2, 7, aiPlayer);
		grid.setBase(3, 7, aiPlayer);
		
		camera.zoom = grid.getFitZoom();
		camera.update();
		gameOver = false;
		winner = null;
		totalFrames = 0;
		totalUpdates = 0;
	}
	
	public void gameOver(Player winner) {
		gameOver = true;
		this.winner = winner;
	}

	@Override
	public void resize(int width, int height) {
		screenWidth = width;
		screenHeight = height;
		grid.resize(width, height);
		float oldzoom = camera.zoom;
		if (oldzoom > grid.getFitZoom()) {
			oldzoom = grid.getFitZoom();
		}
		camera.setToOrtho(false, width, height);
		camera.position.set(viewCentreX, viewCentreY, 0);
		camera.zoom = oldzoom;
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		shapeRenderer.setProjectionMatrix(camera.combined);
		hudCamera.setToOrtho(false, width, height);
		hudCamera.update();
		hudBatch.setProjectionMatrix(hudCamera.combined);
		normaliseView();
	}

	@Override
	public void render(float delta) {
		totalFrames++;
		if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
			zoom(-.01f);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			zoom(.01f);
		}
		if (viewCentreXV != 0 || viewCentreYV != 0) {
			viewCentreX += viewCentreXV;
			viewCentreY -= viewCentreYV;
			viewCentreXV *= GameConstants.PAN_DAMPER;
			viewCentreYV *= GameConstants.PAN_DAMPER;
			if (Math.abs(viewCentreXV) < 0.1f) {
				viewCentreXV = 0;
			} 
			if (Math.abs(viewCentreYV) < 0.1f) {
				viewCentreYV = 0;
			}
			camera.position.set(viewCentreX, viewCentreY, 0);
			camera.update();
			shapeRenderer.setProjectionMatrix(camera.combined);
			batch.setProjectionMatrix(camera.combined);
			normaliseView();
		}

		Gdx.gl.glClearColor(1f, 0.95f, 0.95f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		grid.renderGrid(shapeRenderer, batch, font);
		renderHud();
		
		if (!gameOver) {
			grid.processAI();
			long thisTick = System.nanoTime();
			if (thisTick - lastTick > GameConstants.TICK_LENGTH) {
				/* don't just set to thisTick as rounding will mean we end up
				 * slower than intended. */
				lastTick = lastTick + GameConstants.TICK_LENGTH;
				totalUpdates++;
				grid.processGameTick();
			}
		}
	}

	private void renderHud() {
		long nowtime = System.nanoTime();
		double fps = totalFrames / (double)((nowtime - startTime) / (1000d * 1000 * 1000));
		double updatesps = totalUpdates / (double)((nowtime - startTime) / (1000d*1000*1000));
		hudBatch.begin();
		font.setScale(1);
		font.setColor(Color.BLACK);
		font.draw(hudBatch, "fps: " + (int)fps + " ups: " + (int)updatesps + " time elapsed: " + ((nowtime - startTime) / (1000*1000*1000)), 10, 20);
		if (gameOver) {
			font.setColor(Color.RED);
			font.setScale(3);
			font.draw(hudBatch, "GAME OVER", 10, screenHeight / 2);
		}
		hudBatch.end();
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
		// Maybe never called ?
		if (backgroundMusic != null) {
			backgroundMusic.dispose();
		}
		hudBatch.dispose();
		batch.dispose();
		font.dispose();
		shapeRenderer.dispose();
	}

	private Territory findTerritory(float x, float y) {
		/* turn our display screen coordinates into game grid coordinates */
		Vector3 point = new Vector3(x, y, 0);
		camera.unproject(point);
		return grid.findTerritory((int) point.x, (int) point.y);
	}
	
	/* Gesture listener */
	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		Territory t = findTerritory(x, y);
		if (t != null && t.owner == thisPlayer) {
			pathDrawing = true;
			pathOrigin = t;
		}
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		/* immediately stop any pan-flinging that's going on */
		viewCentreXV = 0;
		viewCentreYV = 0;
		/* also stop any path-dragging */
		pathDrawing = false;
		if (gameOver) {
			game.switchToMenu();
			return false;
		} else {
			Territory t = findTerritory(x, y);
			if (t != null) {
				t.handleClick(thisPlayer, count);
			}
			return false;
		}
	}

	@Override
	public boolean longPress(float x, float y) {
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		/* stop any path-drawing as this means the end of a pan */
		pathDrawing = false;
		viewCentreXV = -velocityX * camera.zoom / 30;
		viewCentreYV = -velocityY * camera.zoom / 30;
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		/* there are two possibilities here. If we started on an owned territory, we're drawing a path.
		 * otherwise we're panning the map.
		 */
		if (pathDrawing && !gameOver) {
			Territory t = findTerritory(x,  y);
			if (t != null && t != pathOrigin) {
				for (Neighbour n : pathOrigin.getNeighbours()) {
					if (n.getTerritory() == t) {
						n.setPathTo(true);
						if (t.getOwner() == thisPlayer) {
							n.getReciprocal().setPathTo(false);
							pathOrigin = t;
						} 
						break;
					}
				}
			}
		} else {
			viewCentreXV = 0;
			viewCentreYV = 0;
			viewCentreX -= deltaX * camera.zoom;
			viewCentreY += deltaY * camera.zoom;
		
			camera.position.set(viewCentreX, viewCentreY, 0);
			camera.update();
			shapeRenderer.setProjectionMatrix(camera.combined);
			batch.setProjectionMatrix(camera.combined);
			normaliseView();
		}
		return false;
	}

	/* handle zooming in or out */
	private void zoom(float zoomChange) {
		camera.zoom += zoomChange;
		if (camera.zoom > grid.getFitZoom()) {
			camera.zoom = grid.getFitZoom();
		} else if (camera.zoom < GameConstants.MINZOOM) {
			camera.zoom = GameConstants.MINZOOM;
		}
		camera.update();
		shapeRenderer.setProjectionMatrix(camera.combined);
		batch.setProjectionMatrix(camera.combined);
		normaliseView();
	}
	
	/* make sure our view is appropriately centred. Should be called after any pan or zoom.
	 * 
	 * Logic goes like this. Work out camera-coordinate of left edge of game area. If it's further
	 * right than the left edge of the screen (plus a border?) then we move the camera back so
	 * it's bordered, unless the right edge is to the left of the ridge edge of the screen (plus
	 * border), in which case we centre.
	 */
	private void normaliseView() {
		boolean changed = false;
		Vector3 bottomLeft = new Vector3(grid.bottomLeftX, grid.bottomLeftY, 0 );
		Vector3 topRight = new Vector3(grid.topRightX, grid.topRightY, 0 );
		camera.project(bottomLeft);
		camera.project(topRight);
		if (bottomLeft.x > GameConstants.GAME_BORDER) {
			changed = true;
			if (topRight.x < Gdx.graphics.getWidth()) {
				viewCentreX = 0;
			} else {
				viewCentreX += (bottomLeft.x - GameConstants.GAME_BORDER) * camera.zoom;
			}
		} else if (topRight.x < Gdx.graphics.getWidth() - GameConstants.GAME_BORDER) {
			changed = true;
			viewCentreX -= (Gdx.graphics.getWidth() - GameConstants.GAME_BORDER - topRight.x) * camera.zoom;
		}
		
		if (bottomLeft.y > GameConstants.GAME_BORDER) {
			changed = true;
			if (topRight.y < Gdx.graphics.getHeight()) {
				viewCentreY = 0;
			} else {
				viewCentreY += (bottomLeft.y - GameConstants.GAME_BORDER) * camera.zoom;
			}
		} else if (topRight.y < Gdx.graphics.getHeight() - GameConstants.GAME_BORDER) {
			changed = true;
			viewCentreY -= (Gdx.graphics.getHeight() - GameConstants.GAME_BORDER - topRight.y) * camera.zoom;
		}
		
		if (changed) {
			camera.position.set(viewCentreX, viewCentreY, 0);
			camera.update();
			shapeRenderer.setProjectionMatrix(camera.combined);	
			batch.setProjectionMatrix(camera.combined);
		}
	}
	
	@Override
	public boolean zoom(float originalDistance, float currentDistance) {
		if (originalDistance > currentDistance) {
			zoom(0.01f);
		} else if (originalDistance < currentDistance) {
			zoom(-0.01f);
		}
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialFirstPointer,
			Vector2 initialSecondPointer, Vector2 firstPointer,
			Vector2 secondPointer) {
		return false;
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(new GestureDetector(this));
		//	backgroundMusic.play();

		lastTick = System.nanoTime();
		startTime = lastTick;
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}
}
