package com.voetsjoeba.nonogram.algorithm.rowsolver.old;

import com.voetsjoeba.nonogram.algorithm.rowsolver.AbstractRowSolver;
import com.voetsjoeba.nonogram.exception.UncontiguousRunException;
import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * For runs for which some but not all squares are known, determines any squares that must necessarily be part of the same run
 * by "bouncing off" known whitespace.
 */
public class ExpandKnownIncompleteRunsSolver extends AbstractRowSolver {
	
	public void solve(Row row) throws UnsolvablePuzzleException {
		
		for(Run run : row.getRuns()){
			
			if(run.hasKnownSquares() && !run.isComplete() && run.isContiguous()){
				
				try {
					
					Sequence runSequence = run.toSequence();
					Square[] runBoundaries = NonogramUtils.getBoundaries(runSequence);
					//int availableRunSize = getMaxSequenceLength(runBoundaries);
					int availableRunSize = NonogramUtils.getMaxSequenceLength(runBoundaries, row);
					
					// sanity check
					if(run.getLength() > availableRunSize){
						throw new UnsolvablePuzzleException("Insufficient free space available for run " + run + " of length " + run.getLength() + " in row " + row); // TODO: use a more appropriate exception name and let solve() wrap it in an UnsolvablePuzzleException
					}
					
					int firstSquareIndex = row.getSquareIndex(run.getFirstKnownSquare());
					int lastSquareIndex = row.getSquareIndex(run.getLastKnownSquare());
					
					/* 
					 * check to expand against a left border
					 * 
					 *    |LB                                      |LB
					 * ---+---+---+---+---+---+---              ---+---+---+---+---+---+---
					 *  - |   | 4 | 4 |   |   |         ->       - |   | 4 | 4 | 4 |   |   
					 * ---+---+---+---+---+---+---              ---+---+---+---+---+---+---
					 */
					
					int leftBorder = row.getSquareIndex(runBoundaries[0]);
					int squareCountBefore = firstSquareIndex - leftBorder;
					
					if(squareCountBefore < run.getLength() - 1){
						
						// we have offset from the left border, find the index to expand up to and perform the expansion
						
						int expandIndex = leftBorder + run.getLength() - 1; // index to expand up to 
						int fillCount = expandIndex - lastSquareIndex; // amount of squares that will be filled
						
						if(fillCount > 0){
							
							for(int i=lastSquareIndex+1; i<=expandIndex; i++){
								NonogramUtils.setSquareState(row.getSquare(i), SquareState.FILLED, run);
							}
							
						}
						
					}
					
					/* 
					 * check to expand against a right border
					 * 
					 *                      RB|                                      RB|
					 * ---+---+---+---+---+---+---              ---+---+---+---+---+---+---
					 *    |   |   |   | 3 |   | -       ->       - |   |   | 3 | 3 |   | - 
					 * ---+---+---+---+---+---+---              ---+---+---+---+---+---+---
					 */
					
					// NOTE: if the run is sufficiently "encased" by 2 borders,
					// then this will already have been performed by the left border expansion
					
					int rightBorder = row.getSquareIndex(runBoundaries[1]);
					int squareCountAfter = rightBorder - lastSquareIndex;
					
					if(squareCountAfter < run.getLength() - 1){
						
						// we have offset from the right border, find the index to expand up to and perform the expansion
						
						int expandIndex = rightBorder - run.getLength() + 1; // index to expand up to 
						int fillCount = lastSquareIndex - expandIndex; // amount of squares that will be filled
						
						if(fillCount > 0){
							
							for(int i=firstSquareIndex-1; i>=expandIndex; i--){
								NonogramUtils.setSquareState(row.getSquare(i), SquareState.FILLED, run);
							}
							
						}
						
					}
					
				}
				catch(UncontiguousRunException e) {
					// this really shouldn't happen
					throw new RuntimeException("A run previously determined to be contiguous turned out not be so contiguous after all; something is messed up.");
				}
				
			}
			
		}
		
	}
	
}
