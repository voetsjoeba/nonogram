package com.voetsjoeba.nonogram.algorithm.rowsolver.old;

import java.util.LinkedList;
import java.util.List;

import com.voetsjoeba.nonogram.algorithm.rowsolver.AbstractRowSolver;
import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.StandardSequence;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;

/**
 * Interprets the row as an alternating series of cleared sequences and sequences of unknown/filled squares inbetween, and
 * attempts to determine position of runs.
 */
public class OldExamineSequencesSolver extends AbstractRowSolver {
	
	public void solve(Row row) throws UnsolvablePuzzleException {
		
		// 1) for each of the clear-decomposed sequences that contains filled squares, determine whether or not multiple runs 
		// can be placed within that sequence (because if not, then we can leave it out of consideration for 
		// the others and possibly add some extra whitespace)
		
		List<Sequence> decomposedSequences = decomposeByCleared(row);
		
		for(Sequence sequence : decomposedSequences){
			
			//if(!sequence.containsKnownSquares()) continue;
			if(!sequence.containsFilledSquares()) continue;
			
			// find runs that have known squares in this sequence
			List<Run> runs = new LinkedList<Run>();
			for(Square square : sequence.getSquares()){
				
				if(!square.isStateKnown()) continue;
				
				Run run = square.getRun(row.getOrientation());
				if(run != null && !runs.contains(run) && !run.isComplete()) runs.add(run);
				
			}
			
			
			// if there is only one run present:
			// check if the next and/or previous run could be placed alongside this run in the sequence
			// (if, of course, those next and/or previous runs don't already have known squares somewhere else)
			
			if(runs.size() == 1){
				
				Run run = runs.get(0);
				int runIndex = row.getRunIndex(run);
				int squareCountBefore = row.getSquareIndex(run.getFirstKnownSquare()) - sequence.getFirstSquareRowIndex(); // amount of squares in the sequence before the first known square of this run
				int squareCountAfter = sequence.getLastSquareRowIndex() - row.getSquareIndex(run.getLastKnownSquare()); // ...                                  after      last 
				
				Run previousRun = null;
				Run nextRun = null;
				if(runIndex > 0) previousRun = row.getRun(runIndex - 1);
				if(runIndex < row.getRuns().size() - 1) nextRun = row.getRun(runIndex + 1);
				
				// whether or not this run is the only one within the sequence
				boolean singleRunSequence = true;
				
				if(previousRun != null){
					
					if(previousRun.hasKnownSquares()){
						
						// previous run has known squares outside of this sequence, ok
						
					} else {
						
						// check if previous run fits before "run" in "sequence"
						if(squareCountBefore >= previousRun.getLength() + 1){
							// previous run fits, no more single run
							singleRunSequence = false;
						}
						
					}
					
				}
				
				if(nextRun != null){
					
					if(nextRun.hasKnownSquares()){
						// next run has known squares outside of this sequence, ok
					} else {
						
						// check if next run fits after "run" in "sequence"
						if(squareCountAfter >= nextRun.getLength() + 1){
							// next run fits, no more single run
							singleRunSequence = false;
						}
						
					}
					
				}
				
				if(singleRunSequence){
					
					// contract whitespace
					int freeSquares = run.getLength() - run.getKnownSquareCount(); // amount of unknown squares left in this run
					int clearFrontIndex = row.getSquareIndex(run.getFirstKnownSquare()) - freeSquares - 1;
					int clearBackIndex = row.getSquareIndex(run.getLastKnownSquare()) + freeSquares + 1;
					
					try {
						
						// clear [sequence.getFirstSquareRowIndex() - 1, clearFrontIndex]
						int clearStartIndex = sequence.getFirstSquareRowIndex() - 1;
						int clearEndIndex = clearFrontIndex;
						
						if(clearStartIndex > 0 && clearEndIndex > 0){
							row.clearSquares(clearStartIndex, clearEndIndex);
						}
						
						clearStartIndex = clearBackIndex;
						clearEndIndex = sequence.getLastSquareRowIndex() + 1;
						
						if(clearStartIndex < row.getLength() && clearEndIndex < row.getLength()){
							row.clearSquares(clearStartIndex, clearEndIndex);
						}
						
						// TODO: add some checks to clearSquares to avoid this mess
						
					}
					catch(ConflictingSquareStateException asex){
						throw new UnsolvablePuzzleException(asex);
					}
					
				}
				
			}
			
		}
		
		// TODO: do more checks above
		
		// 2) for each run that has no known squares yet, find a spot for the run:
		//    possible candidates for a spot:
		//        - if the previous run is known: either next to the previous run in (what is for now) the same sequence, or, if impossible, in the first sequence of sufficient length to the right of it (any intermediate sequences of insufficient length must be cleared)
		//        - ...    next                 :                    next                                                                                                                      left
		//        - failing the above, try all empty sequences of sufficient length (ie. check if there is still enough room for the previous and next runs if it were assigned to each sequence)
		
		List<Run> runs = row.getRuns();
		for(Run run : runs){
			
			if(run.hasKnownSquares()) continue;
			
			int runIndex = row.getRunIndex(run);
			Run previousRun = (runIndex > 0 ? row.getRun(runIndex - 1) : null);
			Run nextRun = (runIndex < runs.size() - 1 ? row.getRun(runIndex + 1) : null);
			
			boolean previousRunKnown = (previousRun != null && previousRun.hasKnownSquares());
			boolean nextRunKnown = (nextRun != null && nextRun.hasKnownSquares());
			
			/* ------------------------ */
			
			
			
			if(previousRunKnown){
				
				// find sequence that contains this previous run
				
				int previousRunSequenceIndex = -1; // index of the sequence containing the previous run (ie. index within the row decomposition list)
				for(int i=0; i<decomposedSequences.size(); i++){
					
					if(decomposedSequences.get(i).contains(previousRun.getFirstKnownSquare())){
						previousRunSequenceIndex = i;
						break;
					}
					
				}
				
				if(previousRunSequenceIndex < 0){
					throw new UnsolvablePuzzleException("Could not find a cleared-decomposition sequence for run " + previousRun + " with known squares");
				}
				
				Sequence previousRunSequence = decomposedSequences.get(previousRunSequenceIndex);
				
				// check for sufficient space within the same sequence
				// TODO: do this properly (ie. take any filled squares into consideration etc)
				
				int squaresAfterPreviousRun = row.getSquareIndex(previousRunSequence.getLastSquare()) - row.getSquareIndex(previousRun.getLastKnownSquare()); // amount of squares left in the previous run's sequence to the right side of the previous run's last square 
				
				if(squaresAfterPreviousRun < run.getLength() + 1){ // run length + minimum 1 cleared separator
					
					// insufficient space remaining in the previous run's sequence
					// => find further sequences of sufficient length (clearing any intermediate sequences of insufficient length along the way between the previous run's sequence and the first encountered sequence of sufficient length)
					
					List<Sequence> nextSufficientlyLargeSequences = new LinkedList<Sequence>();
					for(int i=previousRunSequenceIndex + 1; i<decomposedSequences.size(); i++){
						
						Sequence sequence = decomposedSequences.get(i);
						if(sequence.isCleared()) continue;
						
						if(sequence.getLength() >= run.getLength()){
							
							nextSufficientlyLargeSequences.add(sequence);
							
						} else if(nextSufficientlyLargeSequences.size() <= 0) { // only clear intermediate sequences of insufficient length between the previous run's sequence and the first sequence of sufficient length
							
							try {
								sequence.clear();
							}
							catch(ConflictingSquareStateException asex){
								throw new UnsolvablePuzzleException(asex);
							}
							
						}
						
					}
					
					if(nextSufficientlyLargeSequences.size() <= 0){
						throw new UnsolvablePuzzleException("No sequence of sufficiently large size found for run " + run + " after sequence " + previousRunSequence + " for previous run " + previousRun);
					}
					
					// if there is only once sufficiently large sequence, place the run in the found sequence
					//
					// 3 possible situations here:
					//     - if there already is a square present in the found sequence, and it's close enough to the left border to necessarily be part of the run:
					//          - if the square's run is already known (and thus necessarily different from our run; see loop condition), throw an exception
					//          - if the square's run is unknown, assign our run to it
					//     - if there is no square present in the found sequence, see if any squares can be assigned through overlap
					
					if(nextSufficientlyLargeSequences.size() == 1){
						
						Sequence firstNextSufficientlyLargeSequence = nextSufficientlyLargeSequences.get(0);
						
						if(firstNextSufficientlyLargeSequence.containsKnownSquares()){
							
							// TODO: finish me
							
						} else {
							
							int lengthDifference = firstNextSufficientlyLargeSequence.getLength() - run.getLength();
							int overlapLength = run.getLength() - lengthDifference;
							
							if(overlapLength > 0){
								
								int fillStartIndex = row.getSquareIndex(firstNextSufficientlyLargeSequence.getFirstSquare()) + lengthDifference;
								int fillEndIndex = fillStartIndex + overlapLength - 1;
								
								try {
									row.fillSquares(fillStartIndex, fillEndIndex, run);
								}
								catch(ConflictingSquareStateException assex) {
									throw new UnsolvablePuzzleException(assex);
								}
								catch(ConflictingSquareRunException asrex) {
									throw new UnsolvablePuzzleException(asrex);
								}
								catch(RunLengthExceededException rleex) {
									throw new UnsolvablePuzzleException(rleex);
								}
								
							}
							
						}
						
					} else {
						
						// TODO: finish me (check for conflicting assignments in possible sequences)
						
					}
					
				} else {
					
					// no decision
					
				}
				
			}
			
			
			
			
			/* -------------------------------- */
			
			
			
			
			if(nextRunKnown){
				
				// find sequence that contains this next run
				
				int nextRunSequenceIndex = -1; // index of the sequence containing the previous run (ie. index within the row decomposition list)
				for(int i=0; i<decomposedSequences.size(); i++){
					
					if(decomposedSequences.get(i).contains(nextRun.getFirstKnownSquare())){
						nextRunSequenceIndex = i;
						break;
					}
					
				}
				
				if(nextRunSequenceIndex < 0){
					throw new UnsolvablePuzzleException("Could not find a cleared-decomposition sequence for run " + nextRun + " with known squares");
				}
				
				Sequence nextRunSequence = decomposedSequences.get(nextRunSequenceIndex);
				
				// check for sufficient space within the same sequence
				// TODO: do this properly (ie. take any filled squares into consideration etc)
				
				int squaresBeforeNextRun = row.getSquareIndex(nextRun.getFirstKnownSquare()) - row.getSquareIndex(nextRunSequence.getFirstSquare()); // amount of squares left in the next run's sequence to the left side of the next run's first square 
				
				if(squaresBeforeNextRun < run.getLength() + 1){ // run length + minimum 1 cleared separator
					
					// insufficient space remaining in the next run's sequence
					// => find earlier sequences of sufficient length (clearing any intermediate sequences of insufficient length along the way between the next run's sequence and the first encountered sequence of sufficient length)
					
					List<Sequence> previousSufficientlyLargeSequences = new LinkedList<Sequence>();
					for(int i=nextRunSequenceIndex-1; i>=0; i--){
						
						Sequence sequence = decomposedSequences.get(i);
						if(sequence.isCleared()) continue;
						
						if(sequence.getLength() >= run.getLength()){
							
							previousSufficientlyLargeSequences.add(sequence);
							
						} else if(previousSufficientlyLargeSequences.size() <= 0) { // only clear intermediate sequences of insufficient length between the next run's sequence and the first sequence of sufficient length
							
							try {
								sequence.clear();
							}
							catch(ConflictingSquareStateException asex){
								throw new UnsolvablePuzzleException(asex);
							}
							
						}
						
					}
					
					if(previousSufficientlyLargeSequences.size() <= 0){
						throw new UnsolvablePuzzleException("No sequence of sufficiently large size found for run " + run + " before sequence " + nextRunSequence + " for next run " + nextRun);
					}
					
					// if there is only once sufficiently large sequence, place the run in the found sequence
					//
					// 3 possible situations here:
					//     - if there already is a square present in the found sequence, and it's close enough to the right border to necessarily be part of the run:
					//          - if the square's run is already known (and thus necessarily different from our run; see loop condition), throw an exception
					//          - if the square's run is unknown, assign our run to it
					//     - if there is no square present in the found sequence, see if any squares can be assigned through overlap
					
					if(previousSufficientlyLargeSequences.size() == 1){
						
						Sequence firstPreviousSufficientlyLargeSequence = previousSufficientlyLargeSequences.get(0);
						assignRunToSequence(firstPreviousSufficientlyLargeSequence, run, row);
						
					} else {
						
						// TODO: finish me (check for conflicting assignments in possible sequences)
						
					}
					
				} else {
					
					// there is enough room in the same sequence; no decision
					
				}
				
			}
			
			
			if(!previousRunKnown && !nextRunKnown){
				
				// no reference points
				// TODO: scan runs for first known run to the left and right, not just previous and next run
				
				// find possible sequences of sufficient length, and search for conflicts
				List<Sequence> possibleSequences = new LinkedList<Sequence>();
				for(Sequence sequence : decomposedSequences){
					
					if(sequence.isCleared()) continue;
					if(sequence.getLength() >= run.getLength()){
						possibleSequences.add(sequence);
					}
					
				}
				
				if(possibleSequences.size() <= 0){
					throw new UnsolvablePuzzleException("No sequences of sufficient length " + run.getLength() + " found for run " + run);
				}
				
				if(possibleSequences.size() == 1){
					
					// assign run to sequence
					assignRunToSequence(possibleSequences.get(0), run, row);
					
				} else {
					
					// TODO: find conflicts
					
				}
				
			}
			
		}
		
	}
	
	/**
	 * Decomposes the row into sequences whose squares are all cleared, and sequences that contain no cleared squares. Returns a list
	 * of sequences that alternates between cleared/not-cleared sequences.
	 */
	public static List<Sequence> decomposeByCleared(Row row){
		
		List<Sequence> decomposition = new LinkedList<Sequence>();
		
		int i = 0;
		while(i < row.getLength()){
			
			Square square = row.getSquare(i);
			
			int j = i + 1;
			int sequenceEndIndex = i;
			
			if(square.isCleared()){
				
				// run a cleared sequence
				for(; j < row.getLength(); j++){
					
					Square scannedSquare = row.getSquare(j);
					
					if(scannedSquare.isCleared()){
						sequenceEndIndex = j; // update end index
					} else {
						break;
					}
					
				}
				
			} else {
				
				// run a non-cleared sequence
				for(; j < row.getLength(); j++){
					
					Square scannedSquare = row.getSquare(j);
					
					if(!scannedSquare.isCleared()){
						sequenceEndIndex = j; // update end index
					} else {
						break;
					}
					
				}
				
			}
			
			Sequence sequence = new StandardSequence(row, i, sequenceEndIndex);
			decomposition.add(sequence);
			
			i = sequenceEndIndex + 1; // skip forward
			
		}
		
		return decomposition;
		
	}
	
	/**
	 * Assigns a {@link Run} to a {@link Sequence}. Filled squares will be added where possible.
	 * 
	 */
	private void assignRunToSequence(Sequence sequence, Run run, Row row){
		
		if(sequence.containsKnownSquares()){
			
			// TODO: finish me
			//throw new UnsupportedOperationException("Nooez");
			
		} else {
			
			int lengthDifference = sequence.getLength() - run.getLength();
			int overlapLength = run.getLength() - lengthDifference;
			
			if(overlapLength > 0){
				
				int fillStartIndex = row.getSquareIndex(sequence.getFirstSquare()) + lengthDifference;
				int fillEndIndex = fillStartIndex + overlapLength - 1;
				
				try {
					row.fillSquares(fillStartIndex, fillEndIndex, run);
				}
				catch(ConflictingSquareStateException assex) {
					throw new UnsolvablePuzzleException(assex);
				}
				catch(ConflictingSquareRunException asrex) {
					throw new UnsolvablePuzzleException(asrex);
				}
				catch(RunLengthExceededException rleex) {
					throw new UnsolvablePuzzleException(rleex);
				}
				
			}
			
		}
		
	}
	
}
