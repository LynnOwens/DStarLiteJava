package net.tofweb.starlite;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class CellSpace {

	private HashMap<Cell, CellInfo> cellHash = new HashMap<Cell, CellInfo>();
	private HashMap<Cell, Float> openHash = new HashMap<Cell, Float>();
	private PriorityQueue<Cell> blockedCells = new PriorityQueue<Cell>();
	private Cell startCell;
	private Cell goalCell;
	private Integer maxSteps = 80000;
	private double kM = 0.0;

	public void blockCell(Cell blockedCell) {
		if ((blockedCell.equals(startCell)) || (blockedCell.equals(goalCell))) {
			return;
		}

		double cost = -1;
		makeNewCell(blockedCell);
		cellHash.get(blockedCell).setCost(cost);
		updateVertex(blockedCell);
	}

	/*
	 * As per [S. Koenig, 2002]
	 */
	private void updateVertex(Cell state) {
		LinkedList<Cell> successors = new LinkedList<Cell>();

		if (!state.equals(goalCell)) {
			successors = getSuccessors(state);
			double tmp = Double.POSITIVE_INFINITY;
			double tmp2;

			for (Cell successor : successors) {
				tmp2 = getG(successor) + Geometry.calcCostToMove(state, successor);
				if (tmp2 < tmp) {
					tmp = tmp2;
				}
			}

			if (!isClose(getRHS(state), tmp)) {
				setRHS(state, tmp);
			}
		}

		if (!isClose(getG(state), getRHS(state))) {
			addBlockedCell(state);
		}
	}

	/*
	 * Returns the rhs value for the provided state
	 */
	public double getRHS(Cell state) {
		if (state == goalCell) {
			return 0;
		}

		if (cellHash.get(state) == null) {
			return Geometry.heuristic(state, goalCell);
		}

		return cellHash.get(state).getRhs();
	}

	/*
	 * Returns the g value for the provided state
	 */
	public double getG(Cell u) {
		if (cellHash.get(u) == null) {
			return Geometry.heuristic(u, goalCell);
		}

		return cellHash.get(u).getG();
	}

	/*
	 * Sets the G value for state u
	 */
	public void setG(Cell state, double g) {
		makeNewCell(state);
		cellHash.get(state).setG(g);
	}

	/*
	 * Sets the rhs value for state u
	 */
	public void setRHS(Cell state, double rhs) {
		makeNewCell(state);
		cellHash.get(state).setRhs(rhs);
	}

	/*
	 * Checks if a cell is in the hash table, if not it adds it in.
	 */
	public void makeNewCell(Cell state) {
		if (cellHash.get(state) != null) {
			return;
		}

		CellInfo tmp = new CellInfo();
		double costToGoal = Geometry.heuristic(state, goalCell);
		tmp.setRhs(costToGoal);
		tmp.setG(costToGoal);
		cellHash.put(state, tmp);
	}

	/*
	 * Inserts state u into openList and openHash
	 */
	public void addBlockedCell(Cell cell) {
		float csum;
		cell = calculateKey(cell);
		csum = cell.getKey().hashCode();

		openHash.put(cell, csum);
		blockedCells.add(cell);
	}

	/*
	 * Returns true if the cell is occupied (non-traversable), false otherwise.
	 * Non-traversable are marked with a cost < 0
	 */
	public boolean isBlocked(Cell state) {
		if (cellHash.get(state) == null) {
			return false;
		}

		return (cellHash.get(state).getCost() < 0);
	}

	/*
	 * Returns a list of successor states for state u, since this is an 8-way
	 * graph this list contains all of a cells neighbours. Unless the cell is
	 * occupied, in which case it has no successors.
	 */
	public LinkedList<Cell> getSuccessors(Cell state) {
		LinkedList<Cell> successors = new LinkedList<Cell>();
		Cell tempState;

		if (isBlocked(state)) {
			// We cannot move into this cell
			// Therefore it has no successor states
			return successors;
		}

		// Generate the successors, starting at the immediate right and moving
		// in a clockwise manner
		tempState = new Cell(state.getX() + 1, state.getY(), state.getZ(), new Costs(-1.0, -1.0));
		successors.addFirst(tempState);

		tempState = new Cell(state.getX(), state.getY() + 1, state.getZ(), new Costs(-1.0, -1.0));
		successors.addFirst(tempState);

		tempState = new Cell(state.getX() - 1, state.getY(), state.getZ(), new Costs(-1.0, -1.0));
		successors.addFirst(tempState);

		tempState = new Cell(state.getX(), state.getY() - 1, state.getZ(), new Costs(-1.0, -1.0));
		successors.addFirst(tempState);

		// Up one z level
		tempState = new Cell(state.getX(), state.getY(), state.getZ() + 1, new Costs(-1.0, -1.0));
		successors.addFirst(tempState);

		// Down one z level
		tempState = new Cell(state.getX(), state.getY(), state.getZ() - 1, new Costs(-1.0, -1.0));
		successors.addFirst(tempState);

		return successors;
	}

	/*
	 * Returns a list of all the predecessor states for state u. Since this is
	 * for an 8-way connected graph, the list contains all the neighbours for
	 * state u. Occupied neighbours are not added to the list
	 */
	public LinkedList<Cell> getPredecessors(Cell state) {
		LinkedList<Cell> predecessors = new LinkedList<Cell>();
		Cell tempState;

		tempState = new Cell(state.getX() + 1, state.getY(), state.getZ(), new Costs(-1.0, -1.0));
		if (!isBlocked(tempState)) {
			predecessors.addFirst(tempState);
		}

		tempState = new Cell(state.getX(), state.getY() + 1, state.getZ(), new Costs(-1.0, -1.0));
		if (!isBlocked(tempState)) {
			predecessors.addFirst(tempState);
		}

		tempState = new Cell(state.getX() - 1, state.getY(), state.getZ(), new Costs(-1.0, -1.0));
		if (!isBlocked(tempState)) {
			predecessors.addFirst(tempState);
		}

		tempState = new Cell(state.getX(), state.getY() - 1, state.getZ(), new Costs(-1.0, -1.0));
		if (!isBlocked(tempState)) {
			predecessors.addFirst(tempState);
		}

		tempState = new Cell(state.getX(), state.getY(), state.getZ() + 1, new Costs(-1.0, -1.0));
		if (!isBlocked(tempState)) {
			predecessors.addFirst(tempState);
		}

		tempState = new Cell(state.getX(), state.getY(), state.getZ() - 1, new Costs(-1.0, -1.0));
		if (!isBlocked(tempState)) {
			predecessors.addFirst(tempState);
		}

		return predecessors;
	}

	/*
	 * Returns true if x and y are within 10E-5, false otherwise
	 */
	public boolean isClose(double var1, double var2) {
		if (var1 == Double.POSITIVE_INFINITY && var2 == Double.POSITIVE_INFINITY) {
			return true;
		}

		return (Math.abs(var1 - var2) < 0.00001);
	}

	public Cell getStartCell() {
		return startCell;
	}

	public void setStartCell(Cell startCell) {
		this.startCell = startCell;

		CellInfo startCellInfo = new CellInfo();
		double totalPathCost = Geometry.heuristic(startCell, goalCell);
		startCellInfo.setRhs(totalPathCost);
		startCellInfo.setG(totalPathCost);
		cellHash.put(startCell, startCellInfo);

		this.startCell = calculateKey(startCell); // used to be last
	}

	public Cell getGoalCell() {
		return goalCell;
	}

	public void setGoalCell(Cell goalCell) {
		this.goalCell = goalCell;
		this.cellHash.put(goalCell, new CellInfo());
	}

	/*
	 * As per [S. Koenig,2002] except for two main modifications:
	 * 
	 * 1. We stop planning after a number of steps, 'maxsteps' we do this
	 * because this algorithm can plan forever if the start is surrounded by
	 * obstacles 2. We lazily remove states from the open list so we never have
	 * to iterate through it.
	 * 
	 * Returns false if there is no path to goal
	 */
	public boolean canPath() {
		LinkedList<Cell> states = new LinkedList<Cell>();

		if (blockedCells.isEmpty()) {
			return false;
		}

		int numSteps = 0;
		setStartCell(calculateKey(getStartCell()));

		while (!blockedCells.isEmpty()) {
			Cell potentiallyBlockedState = blockedCells.poll();

			if (potentiallyBlockedState.isCloser(getStartCell()) || (getRHS(getStartCell()) != getG(getStartCell()))) {

				if (numSteps++ > maxSteps) {
					throw new RuntimeException("Maximum number of iterations hit: " + maxSteps);
				}

				Cell currentState = potentiallyBlockedState;

				openHash.remove(currentState);

				Cell previousState = new Cell(currentState);
				currentState = calculateKey(currentState);

				if (previousState.isCloser(currentState)) {
					addBlockedCell(currentState);
				} else if (getG(currentState) > getRHS(currentState)) {
					setG(currentState, getRHS(currentState));
					states = getPredecessors(currentState);

					// Apparently to indicate paths into the blocked state are
					// inadvisable
					for (Cell state : states) {
						updateVertex(state);
					}
				} else {
					setG(currentState, Double.POSITIVE_INFINITY);
					states = getPredecessors(currentState);

					// Apparently to indicate paths into the blocked state are
					// inadvisable
					for (Cell state : states) {
						updateVertex(state);
					}

					updateVertex(currentState);
				}
			}
		}

		return true;
	}

	/*
	 * CalculateKey As per [S. Koenig, 2002]
	 * 
	 * Key of a node is a value that is going to be used to sort the open list
	 * 
	 * Key is a tuple value = [min(g(x), rhs(x)+h(x)); min(g(x), rhs(s)] These
	 * keys are compared lexiographically so ...
	 * 
	 * u < v if u.first < v.first OR ( u.first == v.first AND u.second <
	 * v.second )
	 */
	private Cell calculateKey(Cell state) {
		double cost = Math.min(getRHS(state), getG(state));

		Costs key = state.getKey();
		key.setCostPlusHeuristic(cost + Geometry.heuristic(state, getStartCell()) + kM);
		key.setCost(cost);

		return state;
	}
}
