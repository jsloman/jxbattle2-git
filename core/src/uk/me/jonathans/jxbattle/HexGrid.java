package uk.me.jonathans.jxbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Logger;

public class HexGrid extends Grid {
	public static final Logger LOG = new Logger(HexGrid.class.getName());
	
	/* useful things to have precomputer for drawing hexagons */
	float rCos60;
	float rSin60;

	public HexGrid(PlayScreen screen, int cols, int rows, int gameWidth, int gameHeight) {
		super(screen, cols, rows);
		
		/* some useful values that'll we'll use lots, so precompute */
		rCos60 = (float)(GameConstants.GAME_SIZE * Math.cos(Math.PI / 3));
		rSin60 = (float)(GameConstants.GAME_SIZE * Math.sin(Math.PI / 3));
		
		resize(gameWidth, gameHeight);
		
		bottomLeftX = - (cols * rSin60 + rSin60 / 2);
		bottomLeftY = - ((rows * (GameConstants.GAME_SIZE + rCos60)) / 2 + (rCos60 / 2));
		topRightX = (cols * rSin60 + rSin60 / 2);
		topRightY = (rows * (GameConstants.GAME_SIZE + rCos60)) / 2 + (rCos60 / 2);
		initCentres();
	}
	
	@Override
	public void initCentres() {
		for (int y = 0; y < rows; y++) {
			 float thisY = bottomLeftY + (y * (GameConstants.GAME_SIZE + rCos60)) + GameConstants.GAME_SIZE;

			for (int x = 0; x < cols; x++) {
				float thisX = bottomLeftX + (x * 2 + 1) * rSin60;
				if ((y % 2) == 1) {
					thisX += rSin60;
				}
				territories[x][y].setCentre(thisX, thisY);
			}
		}

	}


	@Override
	public void initNeighbours(int x, int y) {
		if (x >= 1) {
			Neighbour n = new Neighbour(this, x - 1, y, -Math.PI / 2,
					Math.PI / 6);
			territories[x][y].addNeighbour(n);
		}
		if (x < cols - 1) {
			Neighbour n = new Neighbour(this, x + 1, y, Math.PI / 2,
					Math.PI / 6);
			territories[x][y].addNeighbour(n);
		}
		/* handle alternating y rows differently */
		if ((y % 2) == 0) {
			/* below neighbours */
			if (y >= 1) {
				if (x >= 1) {
					Neighbour n = new Neighbour(this, x - 1, y - 1, - 5 * Math.PI / 6,
						Math.PI / 6);
					territories[x][y].addNeighbour(n);
				}
				Neighbour n = new Neighbour(this, x, y - 1, 5 * Math.PI / 6,
						Math.PI / 6);
				territories[x][y].addNeighbour(n);
			}
			/* above neighbours */
			if (y < rows - 1) {
				if (x >= 1) {
					Neighbour n = new Neighbour(this, x - 1, y + 1, - Math.PI / 6,
						Math.PI / 6);
					territories[x][y].addNeighbour(n);
				}
				Neighbour n = new Neighbour(this, x, y + 1, Math.PI / 6,
						Math.PI / 6);
				territories[x][y].addNeighbour(n);
			}
		} else {
			/* below neighbours */
			if (y >= 1) {
				if (x < cols - 1) {
					Neighbour n = new Neighbour(this, x + 1, y - 1, 5 * Math.PI / 6,
						Math.PI / 6);
					territories[x][y].addNeighbour(n);
				}
				Neighbour n = new Neighbour(this, x, y - 1, - 5 * Math.PI / 6,
						Math.PI / 6);
				territories[x][y].addNeighbour(n);
			}
			/* above neighbours */
			if (y < rows - 1) {
				if (x < cols - 1) {
					Neighbour n = new Neighbour(this, x + 1, y + 1, Math.PI / 6,
						Math.PI / 6);
					territories[x][y].addNeighbour(n);
				}
				Neighbour n = new Neighbour(this, x, y + 1, - Math.PI / 6,
						Math.PI / 6);
				territories[x][y].addNeighbour(n);
			}
		}
	}

	@Override
	public void renderGrid(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		/* draw coloured territories */
		shapeRenderer.begin(ShapeType.Filled); // was filledtriangle
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				shapeRenderer.setColor(GameConstants.getColourForType(territories[x][y].getType()));
				float thisX = territories[x][y].getCentreX();
				float thisY = territories[x][y].getCentreY(); 
				shapeRenderer.triangle(thisX, thisY - GameConstants.GAME_SIZE, 
							thisX - rSin60, thisY - GameConstants.GAME_SIZE / 2 , 
							thisX + rSin60, thisY - GameConstants.GAME_SIZE / 2);
				shapeRenderer.triangle(thisX, thisY + GameConstants.GAME_SIZE, 
						thisX - rSin60, thisY - GameConstants.GAME_SIZE / 2 , 
						thisX - rSin60, thisY + GameConstants.GAME_SIZE / 2);
				shapeRenderer.triangle(thisX, thisY + GameConstants.GAME_SIZE, 
						thisX + rSin60, thisY - GameConstants.GAME_SIZE / 2 , 
						thisX + rSin60, thisY + GameConstants.GAME_SIZE / 2);
				shapeRenderer.triangle(thisX, thisY + GameConstants.GAME_SIZE, 
						thisX - rSin60, thisY - GameConstants.GAME_SIZE / 2 , 
						thisX + rSin60, thisY - GameConstants.GAME_SIZE / 2);
			}
		}
		shapeRenderer.end();
		Gdx.gl.glLineWidth(1);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
		/* draw grid */
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				/* we avoid drawing any line more than once. So for every hex
				 * we only draw the left, bottom left and bottom sides.
				 */
				float thisX = territories[x][y].getCentreX();
				float thisY = territories[x][y].getCentreY();
				shapeRenderer.line(thisX - rSin60, thisY + GameConstants.GAME_SIZE / 2, thisX - rSin60, thisY - GameConstants.GAME_SIZE / 2);
				shapeRenderer.line(thisX - rSin60, thisY - GameConstants.GAME_SIZE / 2, thisX, thisY - GameConstants.GAME_SIZE);
				shapeRenderer.line(thisX, thisY - GameConstants.GAME_SIZE, thisX + rSin60, thisY - GameConstants.GAME_SIZE / 2);
				/* fill in missing lines for hexes at the border */
				if (x == 0 && (y % 2) == 0 && y != rows - 1) {
					// draw top left (not done here for top row as done lower down )
					shapeRenderer.line(thisX - rSin60, thisY + GameConstants.GAME_SIZE / 2, thisX, thisY + GameConstants.GAME_SIZE);
				}
				if (x == cols - 1) {
					// draw right
					shapeRenderer.line(thisX + rSin60, thisY + GameConstants.GAME_SIZE / 2, thisX + rSin60, thisY - GameConstants.GAME_SIZE / 2);
					if ((y % 2) == 1) {
						// draw top right
						shapeRenderer.line(thisX + rSin60, thisY + GameConstants.GAME_SIZE / 2, thisX, thisY + GameConstants.GAME_SIZE);
					}
				}
				if (y == rows - 1) {
					// draw top 2
					shapeRenderer.line(thisX + rSin60, thisY + GameConstants.GAME_SIZE / 2, thisX, thisY + GameConstants.GAME_SIZE);
					shapeRenderer.line(thisX - rSin60, thisY + GameConstants.GAME_SIZE / 2, thisX, thisY + GameConstants.GAME_SIZE);
				}
			}
		}
		shapeRenderer.end();
		/* draw armies */
		
		shapeRenderer.begin(ShapeType.Filled);
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				if (territories[x][y].getOwner() != null
						&& territories[x][y].getArmySize() > 0) {
					shapeRenderer.setColor(territories[x][y].getOwner().getColour());

					float radius = ((rSin60 - GameConstants.GRID_BORDER * 2) * territories[x][y]
							.getArmySize()) / GameConstants.ARMY_MAX;
					if (radius < GameConstants.GAME_SIZE / 100f) {
						radius = GameConstants.GAME_SIZE / 100f;
					}
					shapeRenderer.circle(territories[x][y].getCentreX(), territories[x][y].getCentreY(), radius);
				}
			}
		}
		shapeRenderer.setColor(0f, 0f, 0f, 1);
		shapeRenderer.end();

			/* draw bases */	
		Gdx.gl.glLineWidth(4);
		shapeRenderer.begin(ShapeType.Line); 
		shapeRenderer.setColor(0f, 0f, 0f, 1);
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				if (territories[x][y].getBaseType() == GridBaseType.FULL) {
					shapeRenderer.circle(territories[x][y].getCentreX(), territories[x][y].getCentreY(), rSin60 - GameConstants.GRID_BORDER * 2);
				}
			}
		}
		shapeRenderer.end();

		/* draw path-lines */
		Gdx.gl.glLineWidth(2);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0f, 0f, 0f, 1);
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				for (Neighbour n : territories[x][y].getNeighbours()) {
					if (n.isPathTo()) {
						shapeRenderer.line(territories[x][y].getCentreX(), territories[x][y].getCentreY(),
								 territories[x][y].getCentreX() + rSin60 * (float)Math.sin(n.angle),
								 territories[x][y].getCentreY() + rSin60 * (float)Math.cos(n.angle));
					}
				}
			}
		}
		shapeRenderer.end();
		/* DEBUG stuff */
		/*
		batch.begin();
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				font.draw(batch, Integer.toString(territories[x][y].getArmySize()), territories[x][y].getCentreX(), territories[x][y].getCentreY());
			}
		}
		batch.end();
		*/
	}

	@Override
	public Territory findTerritory(int x, int y) {
		/* first work out which territory click was in 
		 * At this point, internalX/internalY are relative to bottom left of space containing hexagon */
		int gridY = (int)((y - bottomLeftY) / (GameConstants.GAME_SIZE  + rCos60));
		float internalY = (y - bottomLeftY) - gridY * (GameConstants.GAME_SIZE + rCos60);
		int gridX;
		float internalX;
		if (gridY % 2 == 0) {
			gridX = (int)((x - bottomLeftX) / (2 * rSin60));
			internalX = (x - bottomLeftX) - gridX * 2 * rSin60;
		} else {
			gridX = (int)((x - bottomLeftX - rSin60) / (2 * rSin60));
			internalX = (x - bottomLeftX - rSin60) - gridX * 2 * rSin60;
		}
		/* check if we might actually be in the hex below */
		if (internalY < rCos60) {
			if (internalX < rSin60) {
				/* check bottom left triangle */
				if (internalY < rCos60 - internalX * rCos60 / rSin60) {
					gridY = gridY - 1;
					/* adjust to internal coordinates of new hexagon */
					internalY = GameConstants.GAME_SIZE + rCos60 + internalY;
					internalX = rSin60 + internalX;
					/* check if we need to adjust our X coordinate */
					if ((gridY % 2) == 1) {
						gridX = gridX - 1;
					}
				}
			} else {
				/* check bottom right triangle */
				if (internalY < internalX * rCos60 / rSin60 - rCos60) {
					gridY = gridY - 1;
					/* adjust to internal coordinates of new hexagon */
					internalY = GameConstants.GAME_SIZE + rCos60 + internalY;
					internalX = internalX - rSin60;
					/* check if we need to adjust our X coordinate */
					if ((gridY % 2) == 0){
						gridX = gridX + 1;
					}
				}
				
			}
		}
		if (gridX >= 0 && gridX < cols && gridY >= 0 && gridY < rows) {
			Territory t = getTerritory(gridX, gridY);
			/*
			 * now work out where in territory click was - pass in coordinates
			 * with 0,0 as centre of territory, so adjust our internalX/Y from above.
			 */
			internalX -= rSin60;
			internalY -= GameConstants.GAME_SIZE;
			t.setInternalX(internalX);
			t.setInternalY(internalY);
			// LOG.error("Got click in territory: " + gridX + "," + gridY + " with internal coords: " + internalX + "," + internalY);
			return t;
		} else {
			return null;
		}
	}

	
	@Override
	public void resize(int width, int height) {
		/*
		 * this is more complex for hexagons - we work size out to be the radius
		 * from centre to vertex
		 */
		/*
		 * first work out the total width of hexagons fitting horizontally. This
		 * is easy as we'll stack them horizontally, though note that alternate
		 * rows are staggered so stick out.
		 * width = x * 2 * r * sin(60) + r * sin(60)
		 * so w = (x * 2 + 1) * r * sin(60)
		 * r = w / (x * 2 + 1) * sin(60);
		 */
		float gridWidth = (cols * 2 + 1) * rSin60 + GameConstants.GAME_BORDER * 2;
		/*
		 * height is harder due to tessellation. y hexagons of radius-size r
		 * take up this much space: height = y * (r + r *cos(60)) + r * cos(60)
		 * Solving this for r...
		 * h = y * r + (y + 1) * cos(60) * r
		 * r = h / (y + (y + 1) * cos(60))
		 */
		float gridHeight = (rows * GameConstants.GAME_SIZE) + (rows + 1) * rCos60 + GameConstants.GAME_BORDER * 2;
	
		float xFitZoom = gridWidth / (float)width;
		float yFitZoom = gridHeight / (float)height;
		
		if (xFitZoom < yFitZoom) {
			fitZoom = yFitZoom;
		} else {
			fitZoom = xFitZoom;
		}
	}

}
