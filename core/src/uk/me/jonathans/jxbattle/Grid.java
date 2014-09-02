package uk.me.jonathans.jxbattle;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/* abstract grid class - subtypes for specific board types, eg squares or hexagons */
public abstract class Grid {
	/* definition of territory types */
	public static final int BASE_TYPE_NONE = 0;
	public static final int BASE_TYPE_BIG = 1;
	public static final int TYPE_PLAINS = 1;
	public static final int TYPE_FOREST = 2;
	public static final int TYPE_MOUNTAIN = 3;
	public static final int TYPE_WATER = 4;

	/*
	 * size of our game grid. Whether squares or hexagons, we still define this
	 * as a 2d array
	 */
	public int cols;
	public int rows;
	
	/* our containing screen */
	PlayScreen screen;

	/* coordinates of the edges of our grid, used for normalise-view */
	public float bottomLeftX;
	public float bottomLeftY;
	public float topRightX;
	public float topRightY;
	
	/* camera zoom that allows whole game board to be seen - calculated in specific grid-type's initialiser */
	public float fitZoom;

	Territory[][] territories;
	List<Player> players;

	public Grid(PlayScreen screen, int cols, int rows) {
		this.cols = cols;
		this.rows = rows;

		this.screen = screen;
		
		territories = new Territory[cols][rows];

		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				territories[x][y] = new Territory(this, x, y);
				initNeighbours(x, y);
			}
		}
		/*
		 * set up reciprocals for each neighbour - can only be done after all
		 * neighbours have been created.
		 */
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				for (Neighbour n : territories[x][y].getNeighbours()) {
					Territory otherT = getTerritory(n.x, n.y);
					for (Neighbour r : otherT.getNeighbours()) {
						if (r.x == x && r.y == y) {
							n.setReciprocal(r);
							break;
						}
					}
					/*
					 * should be impossible for there to be no reciprocal so we
					 * won't bother trapping the error here
					 */
				}
			}
		}
		players = new ArrayList<Player>();
	}

	/* initialise neighbours for a given territory - depends on grid type */
	public abstract void initNeighbours(int x, int y);

	/*
	 * set the coordinates of the centre of each territory - depends on grid
	 * type
	 */
	public abstract void initCentres();

	public Territory getTerritory(int x, int y) {
		return territories[x][y];
	}

	public void setBase(int x, int y, Player player) {
		territories[x][y].setBaseType(GridBaseType.FULL);
		territories[x][y].setOwner(player);
	}

	public void processGameTick() {
		/* first we do bases generating */
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				territories[x][y].processGameTickBases();
			}
		}
		/* now we do movement and combat */
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				territories[x][y].processGameTickMoveFight();
			}
		}
		/* Make sure any overfull territories are cleared up */
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				territories[x][y].processGameTickNormalise();
			}
		}
		/* Check for endgame */
		for (Player p : players) {
			p.clearTotal();
		}
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				territories[x][y].addPlayerCount();
			}
		}
		int alive = 0;
		int humanalive = 0;
		Player winner = null;
		for (Player p : players) {
			if (p.getTotal() > 0) {
				alive++;
				if (!p.isAI()) {
					humanalive++;
					winner = p;
				}
			}
		}
		if (humanalive == 0 || alive == 1) {
			screen.gameOver(winner);
		}
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public void addPlayer(Player p) {
		players.add(p);
		p.setIndex(players.indexOf(p));
	}

	public Player getPlayer(int index) {
		return players.get(index);
	}

	public void processAI() {
		for (Player p : players) {
			if (p.isAI()) {
				AIPlayer ai = (AIPlayer) p;
				ai.think(this);
			}
		}
	}

	public float getFitZoom() {
		return fitZoom;
	}
	
	/* window has resized, handling is grid specific (for setting fitZoom) */
	public abstract void resize(int width, int height);
	
	/* takes game coordinates, returns the territory that corresponds to (if there is one) */
	public abstract Territory findTerritory(int x, int y);

	/* render the game grid - implementation depends on grid type */
	public abstract void renderGrid(ShapeRenderer shapeRenderer,
			SpriteBatch batch, BitmapFont font);
	
	
}
