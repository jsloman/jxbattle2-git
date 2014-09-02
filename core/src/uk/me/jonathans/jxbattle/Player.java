package uk.me.jonathans.jxbattle;

import com.badlogic.gdx.graphics.Color;

public class Player {
	Color colour;
	int index;
	
	/* used for calculating endgame */
	int total;
	
	public Player(Color colour) {
		this.colour = colour;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isAI() {
		return false;
	}
	
	public Color getColour() {
		return colour;
	}
	
	public int getTotal() {
		return total;
	}
	
	public void clearTotal() {
		total = 0;
	}
	
	public void addTotal(int add) {
		total += add;
	}
	
}
