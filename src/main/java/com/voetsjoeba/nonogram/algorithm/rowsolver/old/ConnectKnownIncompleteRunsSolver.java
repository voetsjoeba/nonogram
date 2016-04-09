package com.voetsjoeba.nonogram.algorithm.rowsolver.old;

import com.voetsjoeba.nonogram.algorithm.rowsolver.AbstractRowSolver;
import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Square;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * For known runs with gaps in its squares, fills the gaps. Also appends any adjacent filled squares with unknown run to the run.
 */
public class ConnectKnownIncompleteRunsSolver extends AbstractRowSolver {
	
	
	public void solve(Row row) throws UnsolvablePuzzleException {
		
		/*
		 * ---+---+---+---+---+---+---            ---+---+---+---+---+---+---
		 *  - | 5 |   |   | 5 | 5 | -       ->     - | 5 | 5 | 5 | 5 | 5 | - 
		 * ---+---+---+---+---+---+---            ---+---+---+---+---+---+---
		 */
		
		for(Run run : row.getRuns()){
			
			int runKnownSquareCount = run.getKnownSquareCount();
			
			if(runKnownSquareCount > 0){
				
				int firstSquareIndex = row.getSquareIndex(run.getFirstKnownSquare());
				int lastSquareIndex = row.getSquareIndex(run.getLastKnownSquare());
				
				if((lastSquareIndex - firstSquareIndex) + 1 != run.getKnownSquareCount()){ // ie. !run.isContiguous()
					
					// make the run contiguous
					for(int i=firstSquareIndex+1; i<=lastSquareIndex; i++){
						Square scannedSquare = row.getSquare(i);
						NonogramUtils.setSquareState(scannedSquare, SquareState.FILLED, run); // won't affect any squares already filled and with its run assigned assigned
					}
					
				}
				
				// scan out for any adjacent squares
				int leftScanOut = firstSquareIndex - 1;
				while(leftScanOut >= 0){
					
					Square candidateSquare = row.getSquare(leftScanOut);
					if(!(candidateSquare.isFilled() && !candidateSquare.hasRun(row.getOrientation()))) break;
					
					// append square to run
					NonogramUtils.setSquareState(candidateSquare, SquareState.FILLED, run);
					leftScanOut--;
					
				}
				
				int rightScanOut = lastSquareIndex + 1;
				while(rightScanOut < row.getLength()){
					
					Square candidateSquare = row.getSquare(rightScanOut);
					if(!(candidateSquare.isFilled() && !candidateSquare.hasRun(row.getOrientation()))) break;
					
					// append square to run
					NonogramUtils.setSquareState(candidateSquare, SquareState.FILLED, run);
					rightScanOut++;
					
				}
				
			}
			
		}
		
	}
	
}
