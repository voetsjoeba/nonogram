package com.voetsjoeba.nonogram.algorithm.rowsolver;

import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.api.Row;

/**
 * A single-row solver; attempts to deduct information about squares' state within a single row.
 * 
 * @author Jeroen De Ridder
 */
public interface RowSolver {
	
	/**
	 * Deduct and apply information about squares' state within <tt>row</tt>.
	 * @param row
	 * @throws UnsolvablePuzzleException in case a conflict occurs setting a square's state
	 */
	public void solve(Row row) throws UnsolvablePuzzleException;
	
	/**
	 * Called by the solver when it has finished solving the puzzle.
	 * @param complete whether the puzzle has been completely solved
	 */
	public void solvingFinished(boolean complete);
	
}
