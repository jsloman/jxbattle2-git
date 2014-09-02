package uk.me.jonathans.jxbattle;

public class GameConfig {
	public static final int GRID_HEX = 1;
	public static final int GRID_SQUARE = 2;
	
	int gridType;
	int gridWidth;
	int gridHeight;
	
	public int getGridType() {
		return gridType;
	}
	public void setGridType(int gridType) {
		this.gridType = gridType;
	}
	public int getGridWidth() {
		return gridWidth;
	}
	public void setGridWidth(int gridWidth) {
		this.gridWidth = gridWidth;
	}
	public int getGridHeight() {
		return gridHeight;
	}
	public void setGridHeight(int gridHeight) {
		this.gridHeight = gridHeight;
	}

}
