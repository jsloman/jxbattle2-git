package uk.me.jonathans.jxbattle;

public class Neighbour {
	/* our parent grid */
	Grid grid;
	/* coordinates of neighbour */
	int x;
	int y;
	/* is the path from this territory to the neighbour active */
	boolean pathTo;
	/* angle for line to neighbour for drawing paths */
	double angle;
	/* range either side of angle to allow for clicks */
	double angleRange;
	/* the neighbour from the territory this neighbour points at that
	 * points back at our territory. This can't be set at initialisation
	 * as some won't yet exist, so has to be set separately after
	 * the initial round of initialisation has been done.
	 */
	Neighbour reciprocal;
	
	public Neighbour(Grid grid, int x, int y, double angle, double angleRange) {
		this.grid = grid;
		this.x = x;
		this.y = y;
		pathTo = false;
		this.angleRange = angleRange;
		this.angle = angle;
	}
	
	public void setReciprocal(Neighbour reciprocal) {
		this.reciprocal = reciprocal;
	}
	
	public Neighbour getReciprocal() {
		return reciprocal;
	}
	
	public Territory getTerritory() {
		return grid.getTerritory(x, y);
	}
	
	public boolean isPathTo() {
		return pathTo;
	}
	
	public void setPathTo(boolean pathTo) {
		this.pathTo = pathTo;
	}
	
	public double getAngle() {
		return angle;
	}
	
	public double getAngleRange() {
		return angleRange;
	}
}
