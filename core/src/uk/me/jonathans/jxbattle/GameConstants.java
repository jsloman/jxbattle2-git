package uk.me.jonathans.jxbattle;

import com.badlogic.gdx.graphics.Color;


public class GameConstants {
	/* size in pixels of a typical territory. */
	public static final int GAME_SIZE = 30;
	/* size (in pixels at full board zoom) of border between territory edge and maximum troop blob */
	public static final int GRID_BORDER = 4;
	/* size in pixels of game border - panning is constrained so the game area edge can't be more than this far from the screen edge. */
	public static final int GAME_BORDER = 5;
	/* the maximum fps of game update ticks, as opposed to graphic renders */
	public static final int GAME_TICK_FPS = 30;
	/* calculated from above, the minimum number of nanoseconds to elapse before the next game update tick */
	public static final long TICK_LENGTH = (1000 * 1000 * 1000) / GAME_TICK_FPS;
	/* minimum number for zooming, based on game_size. maximum number depends on grid size so is calculated */
	public static final float MINZOOM = 0.3f;
	/* amount of damping of panning velocity when flinging */
	public static final float PAN_DAMPER = 0.9f;
	
	/* how many troops does a base produce per tick */
	public static final int BASE_GROW = 1000;
	/* maximum number of troops in a territory */
	public static final int ARMY_MAX = 100000;
	/* minimum number of troops to sustain a territory */
	public static final int ARMY_MIN = 20000;
	/* how many troops can move from one territory to another in a single direction per tick */
	public static final int ARMY_MOVEMENT = 2000;
	/* how many troops can attack from one territory to another in a single direction per tick */
	public static final int ARMY_ATTACK = 4000;
	/* What benefit do you get from just defending - 1 means no benefit, 0 would mean defence was impregnable */
	public static final double DEFENCE_QUOTIENT = 0.8;
	
	public static Color getColourForType(GridType type) {
		switch(type) {
		case PLAINS:
			return Color.YELLOW;
		case FOREST:
			return Color.GREEN;
		case MOUNTAINS:
			return Color.GRAY;
		case WATER:
			return Color.CYAN;
		}
		return Color.MAGENTA;
	}
}
