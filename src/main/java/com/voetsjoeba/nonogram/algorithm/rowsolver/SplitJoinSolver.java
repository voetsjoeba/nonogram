package com.voetsjoeba.nonogram.algorithm.rowsolver;

import java.util.List;

import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * Finds 1-square-wide gaps between two sequences of known runs and evaluates whether it must be cleared or 
 * filled. It happens often that such a square must be cleared, because otherwise it would extend a known run 
 * too far or create a sequence of filled squares that is too long for any run. Similarly, if there are more
 * (sub)sequences of filled squares than there are incomplete runs in the decomposition, then at some point
 * two of the subsequences will need to be joined together.
 * 
 * @author Jeroen De Ridder
 */
public class SplitJoinSolver extends AbstractRowSolver {
	
	public void solve(Row row) throws UnsolvablePuzzleException {
		
		RowDecomposition decomposition = row.getDecomposition();
		
		List<Sequence> sequences = decomposition.getSequences();
		for(Sequence sequence : sequences){
			
			// find unknown squares that are surrounded by known squares
			int sequenceLength = sequence.getLength();
			for(int squareIndex = 1; squareIndex < sequenceLength - 1; squareIndex++){
				
				Square square = sequence.getSquare(squareIndex);
				if(square.isStateKnown()) continue;
				
				// check if it's enclosed by known squares
				Square leftSquare = sequence.getSquare(squareIndex - 1);
				Square rightSquare = sequence.getSquare(squareIndex + 1);
				if(!(leftSquare.isFilled() && rightSquare.isFilled())) continue;
				
				Run leftSquareRun = leftSquare.getRun(row.getOrientation());
				Run rightSquareRun = rightSquare.getRun(row.getOrientation());
				
				
				// find the length of the filled square sequences to the left and right
				int leftIndex = squareIndex - 1;
				while(leftIndex >= 0 && sequence.getSquare(leftIndex).isFilled()) leftIndex--;
				int leftLength = (squareIndex - 1) - leftIndex;
				
				int rightIndex = squareIndex + 1;
				while(rightIndex < sequenceLength && sequence.getSquare(rightIndex).isFilled()) rightIndex++;
				int rightLength = rightIndex - (squareIndex + 1);
				
				int joinedLength = leftLength + rightLength + 1;
				
				// if both the left and the right square's run are known ...
				if(leftSquareRun != null && rightSquareRun != null){
					
					// ... and they're the same, then make this square part of that run
					if(leftSquareRun == rightSquareRun){
						
						NonogramUtils.setSquareState(square, SquareState.FILLED, leftSquareRun);
						
					// ... and they're not the same, then make this square a separator
					} else {
						
						NonogramUtils.setSquareState(square, SquareState.CLEAR, null);
						
					}
				
				// if only the left square's run is known ...
				} else if(leftSquareRun != null){
					
					// check whether the joined length exceeds the run's length
					if(joinedLength > leftSquareRun.getLength()){
						NonogramUtils.setSquareState(square, SquareState.CLEAR, null);
					}
				
				// if only the right square's run is known ...
				} else if(rightSquareRun != null){
					
					if(joinedLength > rightSquareRun.getLength()){
						NonogramUtils.setSquareState(square, SquareState.CLEAR, null);
					}
				
				// if neither square's run is known ...
				} else {
					
					// check whether the joined sequence would be too large for any run
					// TODO: perhaps also, if there turns out to be a large enough run to contain it but it's already
					// assigned elsewhere (and out or range), clear the square
					
					if(joinedLength > row.getMaximumIncompleteRunLength()){
						NonogramUtils.setSquareState(square, SquareState.CLEAR, null);
					}
					
				}
				
			}
			
		}
		
	}
	
}
