package uk.me.jonathans.jxbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Logger;

public class SquareGrid extends Grid {
	public static final Logger LOG = new Logger(SquareGrid.class.getName());
	
	public SquareGrid(PlayScreen screen, int cols, int rows, int gameWidth, int gameHeight) {
		super(screen, cols, rows);

		resize(gameWidth, gameHeight);
		
		bottomLeftX = -(cols * GameConstants.GAME_SIZE);
		bottomLeftY = -(rows * GameConstants.GAME_SIZE);
		topRightX = cols * GameConstants.GAME_SIZE;
		topRightY = rows * GameConstants.GAME_SIZE;
		initCentres();
	}

	@Override
	public void initCentres() {
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				territories[x][y].setCentre(bottomLeftX + (x * 2 + 1) * GameConstants.GAME_SIZE,
						bottomLeftY + (y * 2 + 1) * GameConstants.GAME_SIZE);
			}
		}

	}

	@Override
	public void initNeighbours(int x, int y) {
		if (x >= 1) {
			Neighbour n = new Neighbour(this, x - 1, y, -Math.PI / 2,
					Math.PI / 4);
			territories[x][y].addNeighbour(n);
		}
		if (y >= 1) {
			Neighbour n = new Neighbour(this, x, y - 1, Math.PI, Math.PI / 4);
			territories[x][y].addNeighbour(n);
		}
		if (x < cols - 1) {
			Neighbour n = new Neighbour(this, x + 1, y, Math.PI / 2,
					Math.PI / 4);
			territories[x][y].addNeighbour(n);
		}
		if (y < rows - 1) {
			Neighbour n = new Neighbour(this, x, y + 1, 0, Math.PI / 4);
			territories[x][y].addNeighbour(n);
		}
	}

	@Override
	public void renderGrid(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		/* draw coloured territories */
		shapeRenderer.begin(ShapeType.Filled);
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				shapeRenderer.setColor(GameConstants.getColourForType(territories[x][y].getType()));
				shapeRenderer.rect(territories[x][y].getCentreX()
						- GameConstants.GAME_SIZE, territories[x][y].getCentreY()
						- GameConstants.GAME_SIZE, GameConstants.GAME_SIZE * 2, GameConstants.GAME_SIZE * 2);
			}
		}
		shapeRenderer.end();
		Gdx.gl.glLineWidth(1);

		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);

		for (int x = 0; x <= cols; x++) {
			float thisX = bottomLeftX + (x * GameConstants.GAME_SIZE * 2);
			shapeRenderer.line(thisX, bottomLeftY, thisX, topRightY);
		}
		for (int y = 0; y <= rows; y++) {
			float thisY = bottomLeftY + (y * GameConstants.GAME_SIZE * 2);
			shapeRenderer.line(bottomLeftX, thisY, topRightX, thisY);
		}
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.Filled);
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				if (territories[x][y].getOwner() != null
						&& territories[x][y].getArmySize() > 0) {
					shapeRenderer.setColor(territories[x][y].getOwner().getColour());
					float squareSize = ((GameConstants.GAME_SIZE * 2 - GameConstants.GRID_BORDER * 2) * territories[x][y]
							.getArmySize()) / GameConstants.ARMY_MAX;
					shapeRenderer.rect(territories[x][y].getCentreX()
							- squareSize / 2, territories[x][y].getCentreY()
							- squareSize / 2, squareSize, squareSize);
				}
			}
		}
		shapeRenderer.setColor(0f, 0f, 0f, 1);
		shapeRenderer.end();
		
		Gdx.gl.glLineWidth(4);

		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0f, 0f, 0f, 1);
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				if (territories[x][y].getBaseType() == GridBaseType.FULL) {
					shapeRenderer.circle(territories[x][y].getCentreX(),
							territories[x][y].getCentreY(), GameConstants.GAME_SIZE - GameConstants.GRID_BORDER * 2);
				}
			}
		}
		shapeRenderer.end();
		
		Gdx.gl.glLineWidth(2);

		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0f, 0f, 0f, 1);
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				for (Neighbour n : territories[x][y].getNeighbours()) {
					if (n.isPathTo()) {
						shapeRenderer.line(
								(float) territories[x][y].getCentreX(),
								(float) territories[x][y].getCentreY(),
								(float) (territories[x][y].getCentreX() + GameConstants.GAME_SIZE
										* Math.sin(n.angle)),
								(float) (territories[x][y].getCentreY() + GameConstants.GAME_SIZE
										* Math.cos(n.angle)));
					}
				}
			}
		}
		shapeRenderer.end();
	}
	
	@Override
	public Territory findTerritory(int x, int y) {
		/* first work out which territory click was in */
		int gridX = (int) ((x + cols * GameConstants.GAME_SIZE) / (GameConstants.GAME_SIZE * 2));
		int gridY = (int) ((y + rows * GameConstants.GAME_SIZE) / (GameConstants.GAME_SIZE * 2));
		if (gridX >= 0 && gridX < cols && gridY >= 0 && gridY < rows) {
			Territory t = getTerritory(gridX, gridY);
			t.setInternalX(x - t.getCentreX());
			t.setInternalY(y - t.getCentreY());
			return t;
		} else {
			return null;
		}
	}

	@Override
	public void resize(int width, int height) {
		/*
		 * for squares we computer size as the radius from centre to mid-side
		 * (ie half side-length)
		 */
		int gridWidth = cols * GameConstants.GAME_SIZE * 2 + GameConstants.GAME_BORDER * 2;
		int gridHeight = rows * GameConstants.GAME_SIZE * 2 + GameConstants.GAME_BORDER * 2;
		
		float xFitZoom = gridWidth / (float)width;
		float yFitZoom = gridHeight / (float)height;
		
		if (xFitZoom < yFitZoom) {
			fitZoom = yFitZoom;
		} else {
			fitZoom = xFitZoom;
		}
	}
}
