package uk.me.jonathans.jxbattle;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Logger;

public class Territory {
	public static final Logger LOG = new Logger(Territory.class.getName());

	GridType type; /* terrain type, hills, water etc.. */
	GridBaseType baseType; /* whether we're a base, and if so which size */
	Player owner; /* who owns us currently. null for unowned */
	int armySize; /* if we are owned how big is the army here */
	List<Neighbour> neighbours; /* details of adjoining territories */
	float centreX; /* screen coordinates of centre of territory */
	float centreY;
	int gridX; /* grid coordinates of territory */
	int gridY;
	Grid grid; /* reference to grid */
	float internalX; /* when we've been clicked on, stores internal coordinates of click */
	float internalY;

	public Territory(Grid grid, int x, int y) {
		this.grid = grid;
		gridX = x;
		gridY = y;
		type = GridType.PLAINS;
		baseType = GridBaseType.NONE;
		owner = null;
		armySize = 0;
		neighbours = new ArrayList<Neighbour>();
	}

	public void setCentre(float centreX, float centreY) {
		this.centreX = centreX;
		this.centreY = centreY;
	}

	public void processGameTickBases() {
		if (baseType != GridBaseType.NONE && owner != null) {
			armySize += GameConstants.BASE_GROW;
			/*
			 * we don't check for over-full here as army could be moving out at
			 * the same time
			 */
		}
	}

	public void processGameTickNormalise() {
		if (armySize > GameConstants.ARMY_MAX) {
			armySize = GameConstants.ARMY_MAX;
		}
	}

	/*
	 * We handle movement and combat together. Should we? Tricky to say. Note
	 * that order of processing makes a difference here, as army can't move into
	 * a square until it has space in it. Impossible (well okay very hard) to
	 * solve perfectly, would have to make a tree of movement and handle loops,
	 * so for now at least we just fudge it.
	 */
	public void processGameTickMoveFight() {
		if (owner != null && armySize > GameConstants.ARMY_MIN) {
			/*
			 * combat takes precedence, we work out the maximum amount that
			 * could be involved in fighting Is this right? Should movement and
			 * combat have equal precedence? I'm not sure
			 */
			int maxFighting = 0;
			for (Neighbour n : neighbours) {
				if (n.isPathTo()
						&& grid.getTerritory(n.x, n.y).getOwner() != owner) {
					maxFighting += GameConstants.ARMY_ATTACK;
					;
				}
			}
			/*
			 * if we don't have enough army, work out the proportion which is
			 * involved in fighting each way.
			 */
			double fight_proportion = 1;
			if ((armySize - GameConstants.ARMY_MIN) < maxFighting) {
				fight_proportion = (armySize - GameConstants.ARMY_MIN) / (double) maxFighting;
				maxFighting = armySize - GameConstants.ARMY_MIN;
			}
			/*
			 * now actually resolve combat. If we are attacking and they aren't,
			 * then all our fighting army die, and an amount of their defending
			 * army die depending on the defence quotient. If they are also
			 * attacking us, then equal amounts of our army and their army die.
			 * If this leaves their army totally dead, then work out what
			 * percentage of our army died, and then rest that were attacking
			 * move into the territory and occupy it, resetting any paths out of
			 * that territory.
			 */
			for (Neighbour n : neighbours) {
				Territory otherT = grid.getTerritory(n.x, n.y);
				if (n.isPathTo() && otherT.getOwner() != owner) {
					int attacking = (int) (GameConstants.ARMY_ATTACK * fight_proportion);

					int defending;
					if (n.getReciprocal().isPathTo()) {
						defending = attacking;
					} else {
						defending = (int) (attacking * GameConstants.DEFENCE_QUOTIENT);
					}
					if (defending > otherT.getArmySize()) {
						/* take ownership */
						otherT.setOwner(owner);
						/* clear paths out */
						for (Neighbour n2 : otherT.getNeighbours()) {
							n2.setPathTo(false);
						}
						/*
						 * move in surviving army from attack. Technically if
						 * the army in otherT is defending, we should calculate
						 * how many attackers would have died based on the
						 * defence quotient, but we can consider this final
						 * attack to be a rout where the defence advantage no
						 * longer holds
						 */
						int survivors = attacking - otherT.getArmySize();
						otherT.setArmySize(survivors);
						/*
						 * We remove all attackers from this territory - some
						 * died, some got moved to new territory
						 */
						armySize -= attacking;
					} else {
						/* kill off those that died */
						otherT.setArmySize(otherT.getArmySize() - defending);
						armySize -= attacking;
						/* shouldn't happen but just in case */
						if (armySize < 0) {
							armySize = 0;
						}
					}
				}
			}

			/*
			 * Now movement. First we work out the maximum amount which could be
			 * moving out of this base. Anyone that was attacking can't be
			 * moving. Note that we allow movement into a
			 * just-taken-over-square. Just in case you thought it was
			 * accidental. Not sure it would be of any benefit to disallow it.
			 */
			int maxOut = 0;
			for (Neighbour n : neighbours) {
				Territory otherT = n.getTerritory();
				if (n.isPathTo() && otherT.getOwner() == owner
						&& otherT.getArmySize() < GameConstants.ARMY_MAX) {
					int space = GameConstants.ARMY_MAX - otherT.getArmySize();
					maxOut += space > GameConstants.ARMY_MOVEMENT ? GameConstants.ARMY_MOVEMENT
							: space;
				}
			}
			/*
			 * if we don't have enough army, work out the proportion which
			 * actually goes to each
			 */
			double proportion = 1;
			if ((armySize - GameConstants.ARMY_MIN) < maxOut) {
				proportion = (armySize - GameConstants.ARMY_MIN) / (double) maxOut;
			}
			/* now do the actual movement */
			for (Neighbour n : neighbours) {
				Territory otherT = n.getTerritory();
				if (n.isPathTo() && otherT.getOwner() == owner
						&& otherT.getArmySize() < GameConstants.ARMY_MAX) {
					int space = GameConstants.ARMY_MAX - otherT.getArmySize();
					int move = space > GameConstants.ARMY_MOVEMENT ? GameConstants.ARMY_MOVEMENT
							: space;
					move = (int) (move * proportion);
					otherT.setArmySize(otherT.getArmySize() + move);
					otherT.setOwner(owner);
					armySize -= move;
				}
			}
		}
	}

	/* before calling this, internalX and internalY will have been set */
	public void handleClick(Player player, int count) {
		if (count == 1) {
			/*
			 * single click - if we own this territory, create or clear path in
			 * clicked-on segment. 
			 */
			if (owner != player) {
				return;
			}
			/* we discard clicks in the middle of the territory */
			if (Math.abs(internalX) > GameConstants.GAME_SIZE / 5
					|| Math.abs(internalY) > GameConstants.GAME_SIZE / 5) {
				double angle = Math.atan2(internalX, internalY);
				/*
				 * we check each neighbour to see which one this click-angle
				 * relates to
				 */
				for (Neighbour n : neighbours) {
					double difference = Math.abs(n.getAngle() - angle);
					if (difference > Math.PI) {
						difference = 2 * Math.PI - difference;
					}
					if (difference < n.getAngleRange()) {
						// LOG.error("Got difference with: " + (n.getAngle() *
						// 180 /
						// Math.PI)+ " is " + (difference * 180 / Math.PI));
						n.setPathTo(!n.isPathTo());
						/*
						 * clear the reciprocal path if there is one - find
						 * reciprocal neighbour
						 */
						n.getReciprocal().setPathTo(false);
					}
				}
			}
		} else if (count == 2) {
			/* double click - we make all territories we own that surround this one have paths going
			 * to this territory only
			 */
			for (Neighbour n : neighbours) {
				if (owner == player) {
					/* if we own it, cancel all paths out of it */
					n.setPathTo(false);
				}
				if (n.getTerritory().getOwner() == player) {
					for (Neighbour ntn : n.getTerritory().getNeighbours()) {
						ntn.setPathTo(false);
					}
					n.getReciprocal().setPathTo(true);
				}
			}
		}
	}

	public void setType(GridType type) {
		this.type = type;
	}

	public GridType getType() {
		return type;
	}
	
	public void setBaseType(GridBaseType gbt) {
		this.baseType = gbt;
	}
	
	public GridBaseType getBaseType() {
		return baseType;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public Player getOwner() {
		return owner;
	}

	public int getArmySize() {
		return armySize;
	}

	public void setArmySize(int armySize) {
		this.armySize = armySize;
	}

	public void addNeighbour(Neighbour neighbour) {
		neighbours.add(neighbour);
	}

	public List<Neighbour> getNeighbours() {
		return neighbours;
	}

	public float getCentreX() {
		return centreX;
	}

	public float getCentreY() {
		return centreY;
	}
	
	public void addPlayerCount() {
		if (owner != null) {
			owner.addTotal(armySize);
		}
	}
	
	public float getInternalX() {
		return internalX;
	}	
	
	public void setInternalX(float intX) {
		internalX = intX;
	}
	
	public float getInternalY() {
		return internalY;
	}
	
	public void setInternalY(float intY) {
		internalY = intY;
	}
}
