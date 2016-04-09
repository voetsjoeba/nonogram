package com.voetsjoeba.nonogram.algorithm.rowsolver.old;

import java.util.LinkedList;
import java.util.List;

import com.voetsjoeba.nonogram.algorithm.rowsolver.AbstractRowSolver;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.StandardSequence;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;

/**
 * Completes gaps in the border area (ie. packs of cleared squares right next to the field borders)
 * and expands the border area. Also fills in gaps in cleared areas that aren't large enough to contain any run.
 */
public class ExpandWhitespaceSolver extends AbstractRowSolver {
	
	public void solve(Row row) throws UnsolvablePuzzleException {
		
		// 1) if the first and/or last run in this row has known squares, we may be able to expand the border area by 
		// clearing squares that cannot be reached by the first and/or last runs
		// NOTE: if the run is complete, we're basically doing the same thing demarcateCompletedRun does, but meh
		
		try {
			
			Run firstRun = row.getFirstRun();
			Run lastRun = row.getLastRun();
			
			if(firstRun.hasKnownSquares()){
				
				int firstRunLastSquareIndex = row.getSquareIndex(firstRun.getLastKnownSquare());
				int knownLength = firstRun.getKnownSquareCount(); // NOTE: assumed to be contiguous (connectedKnownIncompleteRuns should be called before this method)
				
				int newLeftBorderIndex = firstRunLastSquareIndex - firstRun.getLength();
				row.clearSquares(0, newLeftBorderIndex);
				
			}
			
			if(lastRun.hasKnownSquares()){
				
				int lastRunFirstSquareIndex = row.getSquareIndex(lastRun.getFirstKnownSquare());
				int knownLength = lastRun.getKnownSquareCount(); // NOTE: assumed to be contiguous (connectedKnownIncompleteRuns should be called before this method)
				
				int newRightBorderIndex = lastRunFirstSquareIndex + lastRun.getLength();
				row.clearSquares(newRightBorderIndex, row.getLength()-1);
				
			}
			
			// 2) scan from left to right (right to left) for gaps in the border area (ie. contiguous sequences of unknown squares inbetween cleared squares or a field border)
			// and clear any that aren't large enough to contain the first (last) run
			
			// left to right
			
			int i = 0;
			int lastClearedSquareIndex = -1;
			
			while(i < row.getLength()){
				
				Square square = row.getSquare(i);
				if(square.isCleared()){
					
					// found sequence [lastClearedSquareIndex, i]
					if(lastClearedSquareIndex > -1){
						
						int sequenceLength = i - lastClearedSquareIndex - 1;
						
						if(sequenceLength < firstRun.getLength()){
							row.clearSquares(lastClearedSquareIndex + 1, i - 1);
						} else {
							break; // found a sequence of unknown squares that is large enough to contain the first run, stop searching
						}
						
						// found sequence ]edge, i]
					} else {
						
						int sequenceLength = i;
						
						if(sequenceLength < firstRun.getLength()){
							row.clearSquares(0, i);
						} else {
							break; // found a sequence of unknown squares that is large enough to contain the first run, stop searching
						}
						
					}
					
					lastClearedSquareIndex = i;
					
				} else if(square.isFilled()){
					break;
				}
				
				i++;
				
			}
			
			// right to left
			
			i = row.getLength() - 1;
			lastClearedSquareIndex = -1;
			
			while(i >= 0){
				
				Square square = row.getSquare(i);
				if(square.isCleared()){
					
					// found sequence [i, lastClearedSquareIndex]
					if(lastClearedSquareIndex > -1){
						
						int sequenceLength = lastClearedSquareIndex - i - 1;
						if(sequenceLength < lastRun.getLength()){
							row.clearSquares(i + 1, lastClearedSquareIndex - 1);
						} else {
							break;
						}
						
					} else {
						
						int sequenceLength = row.getLength() - 1 - i;
						if(sequenceLength < lastRun.getLength()){
							row.clearSquares(i + 1, row.getLength() - 1);
						} else {
							break;
						}
						
					}
					
					lastClearedSquareIndex = i;
					
				} else if(square.isFilled()){
					break;
				}
				
				i--;
				
			}
			
			// 3) find whitespace gaps anywhere in the row that aren't large enough to contain any run in the row
			// TODO: we can "save" some work by keeping track of the last sequences scanned from left to right and from right
			// to left above, but multicare at the time of writing
			
			List<Sequence> enclosedUnknownRuns = getEnclosedUnknownSequences(row);
			for(Sequence sequence : enclosedUnknownRuns){
				
				if(sequence.getLength() < row.getMinimumRunLength()){
					sequence.clear();
				}
				
			}
			
		}
		catch(ConflictingSquareStateException asex){
			throw new UnsolvablePuzzleException(asex);
		}
		
		/*int minRowRunLength = row.getMinimumRunLength();
		
		i = 0;
		while(i < row.getLength()){
			
			Square square = row.getSquare(i);
			
			// if the square has an unknown state, scan out to the left and the right 
			// to find if it enclosed within cleared squares
			
			if(!square.isStateKnown()){
				
				// scan out to the right
				
				int sequenceLastSquareIndex = -1;
				
				int j = i + 1;
				for(; j<row.getLength(); j++){
					
					Square scannedSquare = row.getSquare(j);
					if(scannedSquare.isCleared()){
						// found a cleared square to the right, set the sequence's last square index
						sequenceLastSquareIndex = j-1;
					}
					
					if(scannedSquare.isStateKnown()){
						// if the square's state is known, we no longer have a contiguous sequence of unknown squares
						break;
					}
					
				}
				
				if(sequenceLastSquareIndex < 0){
					// don't bother scanning to the left anymore
					i = j + 1; // skip the squares that are known not to belong to a whitespace-enclosed unknown sequence
					continue;
				}
				
				// scan out to the left
				
				// index of the first square in this sequence
				int sequenceFirstSquareIndex = -1;
				
				j = i - 1;
				for(; j>=0; j--){
					
					Square scannedSquare = row.getSquare(j);
					
					if(scannedSquare.isCleared()){
						// found a cleared square to the left, set the sequence's first square index
						sequenceFirstSquareIndex = j+1;
					}
					
					if(scannedSquare.isStateKnown()){
						// if the square's state is known, we no longer have a contiguous sequence of unknown squares
						break;
					}
					
				}
				
				if(sequenceFirstSquareIndex < 0){
					// no left boundary found, try next square
					i++;
					continue;
				}
				
				// found sequence [sequenceFirstSquareIndex, sequenceLastSquareIndex]
				int sequenceLength = (sequenceLastSquareIndex - sequenceFirstSquareIndex) + 1;
				if(sequenceLength < minRowRunLength){
					row.clearSquares(sequenceFirstSquareIndex, sequenceLastSquareIndex);
				}
				
			}
			
			i++;
			
		}*/
		
	}
	
	/**
	 * Returns all sequences of unknown squares in this row that are enclosed either by a border or cleared squares, ordered from left to right.
	 */
	protected static List<Sequence> getEnclosedUnknownSequences(Row row) {
		
		List<Sequence> sequences = new LinkedList<Sequence>();
		int minRowRunLength = row.getMinimumRunLength();
		
		int i = 0;
		while(i < row.getLength()){
			
			Square square = row.getSquare(i);
			
			// if the square has an unknown state, scan out to the left and the right 
			// to find if it enclosed within cleared squares
			
			if(!square.isStateKnown()){
				
				// scan out to the right
				
				int sequenceLastSquareIndex = -1;
				
				int j = i + 1;
				for(; j < row.getLength(); j++){
					
					Square scannedSquare = row.getSquare(j);
					if(scannedSquare.isCleared()){
						// found a cleared square to the right, set the sequence's last square index
						sequenceLastSquareIndex = j-1;
					}
					
					if(scannedSquare.isStateKnown()){
						// if the square's state is known, we no longer have a contiguous sequence of unknown squares
						break;
					}
					
				}
				
				if(sequenceLastSquareIndex < 0){
					// don't bother scanning to the left anymore
					i = j + 1; // skip the squares that are known not to belong to a whitespace-enclosed unknown sequence
					continue;
				}
				
				// scan out to the left
				
				// index of the first square in this sequence
				int sequenceFirstSquareIndex = -1;
				
				j = i - 1;
				for(; j>=0; j--){
					
					Square scannedSquare = row.getSquare(j);
					
					if(scannedSquare.isCleared()){
						// found a cleared square to the left, set the sequence's first square index
						sequenceFirstSquareIndex = j+1;
					}
					
					if(scannedSquare.isStateKnown()){
						// if the square's state is known, we no longer have a contiguous sequence of unknown squares
						break;
					}
					
				}
				
				if(sequenceFirstSquareIndex < 0){
					// no left boundary found, try next square
					i++;
					continue;
				}
				
				// found sequence [sequenceFirstSquareIndex, sequenceLastSquareIndex]
				Sequence sequence = new StandardSequence(row, sequenceFirstSquareIndex, sequenceLastSquareIndex);
				sequences.add(sequence);
				
				
			}
			
			i++;
			
		}
		
		return sequences;
		
	}
	
}
