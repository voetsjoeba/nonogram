package com.voetsjoeba.nonogram.algorithm.rowsolver;

import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.api.Row;

/**
 * Removes excess empty sequences from the row. This solver will remove:
 * 
 * <ul>
 *     <li>Empty sequences that are too small to contain any run</li>
 *     <li>Empty sequences enclosed inbetween two sequences containing squares of successive runs (which means 
 *     that the enclosed sequence can never contain any run)</li>
 *     <li>Empty sequences before the sequence containing the first run (if known) and after the sequence containing the last run (if known)</li>
 * </ul>
 * 
 * @author Jeroen De Ridder
 */
public class ClearExcessSequencesSolver extends AbstractRowSolver {
	
	public void solve(Row row) throws UnsolvablePuzzleException {
		
		
		
	}
	
}
