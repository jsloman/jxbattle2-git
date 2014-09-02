package uk.me.jonathans.jxbattle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;

public class AIPlayer extends Player {
	long lastThink;

	/* how many times we think per minute */
	static final int THINK_FPM = 30;
	static final long THINK_LENGTH = (1000l * 1000l * 1000l * 60l) / THINK_FPM;

	Random random;

	public AIPlayer(Color colour) {
		super(colour);
		lastThink = System.nanoTime();
		random = new Random(lastThink);
	}

	public boolean isAI() {
		return true;
	}

	public void think(Grid grid) {
		long thisTick = System.nanoTime();
		if (thisTick - lastThink > THINK_LENGTH) {
			/*
			 * don't just set to thisThink as rounding will mean we end up
			 * slower than intended.
			 */
			lastThink = lastThink + THINK_LENGTH;
			doThink(grid);
		}
	}

	private void doThink(Grid grid) {
		/* get all possible moves */
		List<Neighbour> moves = new ArrayList<Neighbour>();
		for (int x = 0; x < grid.getCols(); x++) {
			for (int y = 0; y < grid.getRows(); y++) {
				Territory t = grid.getTerritory(x, y);
				if (t.getOwner() == this) {
					for (Neighbour n : t.getNeighbours()) {
						if (!n.isPathTo()) {
							moves.add(n);
						}
					}
				}
			}
		}
		if (moves.size() > 0) {
			/* choose one at random */
			Neighbour move = moves.get(random.nextInt(moves.size()));
			move.setPathTo(true);
		}
	}
}
