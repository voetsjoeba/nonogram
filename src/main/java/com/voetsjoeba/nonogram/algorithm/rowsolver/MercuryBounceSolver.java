package com.voetsjoeba.nonogram.algorithm.rowsolver;

import java.util.List;

import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.InconsistentDecompositionException;
import com.voetsjoeba.nonogram.exception.NoSuchSequenceException;
import com.voetsjoeba.nonogram.exception.NoSuchSquareException;
import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * Removes sequences that cannot contain runs, expands filled-squares sequences against decomposition borders (either
 * based on their known run's length or on the minimum unknown run length) and pulls the border towards a sequence
 * of filled squares where no other runs can exist ("mercury" effect -- see wikipedia).
 * 
 * This solver assumes that runs have already been expanded, i.e. that there are no runs for which any of its known
 * squares borders a filled square that doesn't have the same run set. It will still work if this isn't the case, but
 * the results will be less accurate.
 * 
 * @author Jeroen De Ridder
 */
public class MercuryBounceSolver extends AbstractRowSolver {
	
	public void solve(Row row) throws UnsolvablePuzzleException {
		
		try {
			leftToRightPass(row);
			rightToLeftPass(row);
		}
		catch(InconsistentDecompositionException idex) {
			throw new UnsolvablePuzzleException(idex);
		}
		catch(ConflictingSquareStateException cssex) {
			throw new UnsolvablePuzzleException(cssex);
		}
		catch(NoSuchSquareException nssex){
			throw new UnsolvablePuzzleException(nssex);
		}
		catch(NoSuchSequenceException nssex){
			throw new UnsolvablePuzzleException(nssex);
		}
		
	}
	
	protected void leftToRightPass(Row row) throws InconsistentDecompositionException, ConflictingSquareStateException, NoSuchSquareException, NoSuchSequenceException {
		
		List<Run> incompleteRuns = row.getIncompleteRuns();
		if(incompleteRuns.size() <= 0) return;
		
		RowDecomposition decomposition = row.getDecomposition();
		int sequenceCount = decomposition.getSequences().size();
		
		// first, consider the leftmost incomplete run: remove sequences that are too small; then bounce or mercury
		// using any filled square that is either close enough to have to be part of the first run or is known to be 
		// part of the leftmost incomplete run
		
		Run leftmostRun = incompleteRuns.get(0); // leftmost incomplete run (incomplete is implied)
		int leftmostRunLength = leftmostRun.getLength();
		
		// if the sequence of the first run is unknown:
		// going from left to right among the decomposition sequences, remove any sequence that is too small to 
		// contain the leftmost run.
		
		// if the sequence of the first run is known:
		// remove any sequences that come before it
		
		int firstRunSequenceIndex = -1;
		
		if(leftmostRun.hasKnownSquares()){
			
			Square leftmostRunSquare = leftmostRun.getFirstKnownSquare();
			firstRunSequenceIndex = decomposition.getSequenceIndexContaining(leftmostRunSquare);
			
		} else {
			
			//int minIncompleteRunLength = decomposition.getRow().getMinimumIncompleteRunLength();
			
			// searching left to right, find the first sequence large enough to contain the first run
			
			firstRunSequenceIndex = 0;
			while(decomposition.getSequence(firstRunSequenceIndex).getLength() < leftmostRunLength){
				firstRunSequenceIndex++;
			}
			
			if(firstRunSequenceIndex >= sequenceCount){ // i.e. no such sequence found
				throw new InconsistentDecompositionException("No sequence found capable of containing the leftmost incomplete run: " + leftmostRun);
			}
			
		}
		
		// remove sequences from right to left to avoid index trouble
		for(int i=firstRunSequenceIndex - 1; i >= 0; i--){
			decomposition.clearAndRemoveSequence(i);
		}
		
		// at this point, all the sequences that came before firstRunSequenceIndex have been deleted, so we must 
		// now update its index accordingly (and also update the sequence count)
		firstRunSequenceIndex = 0;
		sequenceCount = decomposition.getSequences().size();
		
		// now mercury/bounce it (if possible).
		// to bounce/mercury, we need a "seed" square that is close enough to the sequence edge to either bounce 
		// off it or mercury it out.
		
		// if the first run has known squares, then mercury is trivial and the requirement that it be close enough 
		// to the edge to "suck out" the edge towards it no longer applies. the condition for bouncing it off the 
		// edge remains the same though.
		
		// if the first run does not have known squares, we can only perform bounce/mercury if there is a square
		// with filled state close enough to the border to serve as the seed square
		
		// sequence containing the leftmost incomplete run -- we just calculated its index in the decomposition
		Sequence firstSequence = decomposition.getSequence(firstRunSequenceIndex);
		
		// determine the seed square and perform mercury
		
		if(leftmostRun.hasKnownSquares()){
			
			Square leftmostRunSquare = leftmostRun.getFirstKnownSquare();
			int leftmostRunSquareOffset = firstSequence.getSquareIndex(leftmostRunSquare);
			
			// if the leftmost square is less than leftmostIncompleteRun.getLength() squares away from the edge,
			// we can bounce it off the edge
			
			/*if(leftmostRunSquareOffset < leftmostRun.getLength()){
				// fill squares [leftmostRunSquareOffset, leftmostIncompletRun.getLength()[
				NonogramUtils.setSquareState(sequence, leftmostRunSquareOffset, leftmostRun.getLength() - 1, SquareState.FILLED, leftmostRun);
			}*/
			NonogramUtils.bounceLeft(firstSequence, leftmostRunSquareOffset, leftmostRunLength, leftmostRun);
			
			// if the leftmost square is more squares away than its run has squares left to be completed, then
			// we can mercury out the edge (mind you, this is only because we know the position of a square of the
			// leftmost run -- see the more general mercury condition below)
			
			/*int remainingRunLength = leftmostRun.getLength() - leftmostRun.getKnownSquareCount();
			int mercuryEndIndex = leftmostRunSquareOffset - remainingRunLength; // exclusive
			
			// clear [0, leftmostRunSquareOffset - (remainingRunLength)[
			NonogramUtils.setSquareState(sequence, 0, mercuryEndIndex - 1, SquareState.CLEAR, null); // -1 because the end index param is inclusive*/
			NonogramUtils.mercuryLeft(firstSequence, leftmostRun.getLength(), leftmostRunSquareOffset, leftmostRun.getKnownSquareCount());
			
		} else {
			
			try {
				
				int seedSquareIndex = firstSequence.getFirstFilledSquareIndex();
				
				// if the seed square is less than leftmostIncompleteRun.getLength() squares away from the edge, 
				// we can bounce it off the edge 
				
				/*if(seedSquareIndex < leftmostRun.getLength()){
					NonogramUtils.setSquareState(sequence, seedSquareIndex, leftmostRun.getLength() - 1, SquareState.FILLED, leftmostRun);
				}*/
				NonogramUtils.bounceLeft(firstSequence, seedSquareIndex, leftmostRunLength, leftmostRun);
				
				
				// for mercury to apply, the seed square cannot be further than runLength away from the edge (because
				// otherwise there would be room for the first run inbetween the edge and the seed square)
				
				// we'll also need to know how many filled squares there are (starting at seedSquareIndex)
				/*int seedEndIndex = seedSquareIndex + 1;
				while(seedEndIndex < sequence.getLength() && sequence.getSquare(seedEndIndex).isFilled()) seedEndIndex++;
				int seedLength = seedEndIndex - seedSquareIndex;
				
				if(seedSquareIndex <= leftmostRun.getLength()){
					int mercuryLength = seedSquareIndex - leftmostRunLength + seedLength; // you do the math
					NonogramUtils.setSquareState(sequence, 0, mercuryLength - 1, SquareState.CLEAR, null);
				}*/
				NonogramUtils.mercuryLeft(firstSequence, leftmostRunLength, seedSquareIndex);
				
			}
			catch(NoSuchSquareException nssex){
				// no go, there is no known square in our sequence
			}
			
		}
		
		if(row.isCompleted()) return;
		
		// now consider further sequences. note that unlike in the case of the first run, this time we do have to make 
		// sure the known square is close enough to the edge, because we can no longer assume that there won't be any 
		// runs assigned to the area we're going to be mercury-ing
		// TODO: unless it is known that the previous run is not allocated where we're gonna be doing the mercury (e.g.
		// in the previous sequence)
		
		// so, scan the sequence from left to right and find the first filled square; this is the seed square. Check to
		// see if it close enough to the border to perform bouncing and/or mercury; if not, skip this sequence
		// 
		// if its run is known: use its run length to determine the extent of the bounce/mercury
		// if not, use the minimum incomplete run length
		
		for(int sequenceIndex = firstRunSequenceIndex + 1; sequenceIndex < sequenceCount; sequenceIndex++){
			
			Sequence sequence = decomposition.getSequence(sequenceIndex);
			
			// find the seed square (if any)
			int seedSquareIndex;
			
			try {
				seedSquareIndex = sequence.getFirstFilledSquareIndex();
			}
			catch(NoSuchSquareException nksex) {
				// no filled squares within this sequence, try next sequence
				continue;
			}
			
			Square seedSquare = sequence.getSquare(seedSquareIndex);
			
			int bounceRunLength;
			int mercuryRunLength;
			int seedLength;
			Run seedSquareRun = seedSquare.getRun(row.getOrientation());
			
			if(seedSquareRun == null){
				bounceRunLength = row.getMinimumIncompleteRunLength(); // default to the minimum incomplete run length
				mercuryRunLength = row.getMaximumIncompleteRunLength(); // default to the maximum (!) incomplete run length (we need a conservative run length, and for mercury longer run lengths result in fewer cleared squares)
				seedLength = NonogramUtils.getFilledSequenceLengthRight(sequence, seedSquareIndex);
			} else {
				bounceRunLength = seedSquareRun.getLength(); // use the actual run's length
				mercuryRunLength = seedSquareRun.getLength();
				seedLength = seedSquareRun.getKnownSquareCount(); // assumes that the run is fully connected (i.e. that there are no neighbour filled squares that don't have the same run set)
			}
			
			// __if the seed square is less than minimumIncompleteRunLength squares away from the edge__, 
			// we can bounce it off the edge or mercury it (ONLY then -- e.g. if you have a known run, you might be looking
			// at the next 
			
			if(seedSquareIndex <= row.getMinimumIncompleteRunLength()){
				NonogramUtils.bounceLeft(sequence, seedSquareIndex, bounceRunLength, seedSquareRun); // seedSquareRun will be run if no run is known
				NonogramUtils.mercuryLeft(sequence, mercuryRunLength, seedSquareIndex, seedLength);
			}
			
		}
		
	}
	
	protected void rightToLeftPass(Row row) throws InconsistentDecompositionException, ConflictingSquareStateException, NoSuchSequenceException, NoSuchSquareException {
		
		
		List<Run> incompleteRuns = row.getIncompleteRuns();
		if(incompleteRuns.size() <= 0) return;
		 
		RowDecomposition decomposition = row.getDecomposition();
		int sequenceCount = decomposition.getSequences().size();
		
		
		// first, consider the rightmost incomplete run: remove sequences that are too small; then bounce or mercury
		// using any filled square that is either close enough to have to be part of the rightmost run or is known to be 
		// part of the rightmost incomplete run
		
		Run rightmostRun = incompleteRuns.get(incompleteRuns.size() - 1);
		int rightmostRunLength = rightmostRun.getLength();
		
		// if the sequence of the last run is unknown:
		// going from right to left among the decomposition sequences, remove any sequence that is too small to 
		// contain the rightmost run.
		
		// if the sequence of the last run is known:
		// remove any sequences that come after it
		
		int lastRunSequenceIndex = -1;
		
		if(rightmostRun.hasKnownSquares()){
			
			Square rightmostRunSquare = rightmostRun.getLastKnownSquare();
			lastRunSequenceIndex = decomposition.getSequenceIndexContaining(rightmostRunSquare);
			
		} else {
			
			//int minIncompleteRunLength = decomposition.getRow().getMinimumIncompleteRunLength();
			
			// searching right to left, find the first sequence large enough to contain the last run
			
			lastRunSequenceIndex = sequenceCount - 1;
			while(lastRunSequenceIndex >= 0 && decomposition.getSequence(lastRunSequenceIndex).getLength() < rightmostRunLength){
				lastRunSequenceIndex--;
			}
			
			if(lastRunSequenceIndex < 0){ // i.e. no such sequence found
				throw new InconsistentDecompositionException("No sequence found capable of containing the rightmost incomplete run: " + rightmostRun);
			}
			
		}
		
		// remove sequences from right to left to avoid index trouble
		for(int i=sequenceCount-1; i > lastRunSequenceIndex; i--){
			decomposition.clearAndRemoveSequence(i); 
		}
		
		// at this point, some sequences may have been deleted, so we need to update our total
		// sequence count
		sequenceCount = decomposition.getSequences().size();
		
		
		
		// now mercury/bounce it (if possible)
		
		// sequence containing the rightmost incomplete run -- we just calculated its index in the decomposition
		Sequence lastSequence = decomposition.getSequence(lastRunSequenceIndex);
		
		// determine the seed square and perform mercury
		
		if(rightmostRun.hasKnownSquares()){
			
			Square rightmostRunSquare = rightmostRun.getLastKnownSquare();
			int rightmostRunSquareOffset = lastSequence.getSquareIndex(rightmostRunSquare);
			
			NonogramUtils.bounceRight(lastSequence, rightmostRunSquareOffset, rightmostRunLength, rightmostRun);
			
			// if the leftmost square is more squares away than its run has squares left to be completed, then
			// we can mercury out the edge (mind you, this is only because we know the position of a square of the
			// leftmost run -- see the more general mercury condition below)
			
			/*int remainingRunLength = leftmostRun.getLength() - leftmostRun.getKnownSquareCount();
			int mercuryEndIndex = leftmostRunSquareOffset - remainingRunLength; // exclusive
			
			// clear [0, leftmostRunSquareOffset - (remainingRunLength)[
			NonogramUtils.setSquareState(sequence, 0, mercuryEndIndex - 1, SquareState.CLEAR, null); // -1 because the end index param is inclusive*/
			NonogramUtils.mercuryRight(lastSequence, rightmostRun.getLength(), rightmostRunSquareOffset, rightmostRun.getKnownSquareCount());
			
		} else {
			
			try {
				
				int seedSquareIndex = lastSequence.getLastFilledSquareIndex();
				
				// if the seed square is less than leftmostIncompleteRun.getLength() squares away from the edge, 
				// we can bounce it off the edge 
				
				/*if(seedSquareIndex < leftmostRun.getLength()){
					NonogramUtils.setSquareState(sequence, seedSquareIndex, leftmostRun.getLength() - 1, SquareState.FILLED, leftmostRun);
				}*/
				NonogramUtils.bounceRight(lastSequence, seedSquareIndex, rightmostRunLength, rightmostRun);
				
				
				// for mercury to apply, the seed square cannot be further than runLength away from the edge (because
				// otherwise there would be room for the first run inbetween the edge and the seed square)
				
				// we'll also need to know how many filled squares there are (starting at seedSquareIndex)
				/*int seedEndIndex = seedSquareIndex + 1;
				while(seedEndIndex < sequence.getLength() && sequence.getSquare(seedEndIndex).isFilled()) seedEndIndex++;
				int seedLength = seedEndIndex - seedSquareIndex;
				
				if(seedSquareIndex <= leftmostRun.getLength()){
					int mercuryLength = seedSquareIndex - leftmostRunLength + seedLength; // you do the math
					NonogramUtils.setSquareState(sequence, 0, mercuryLength - 1, SquareState.CLEAR, null);
				}*/
				NonogramUtils.mercuryRight(lastSequence, rightmostRunLength, seedSquareIndex);
				
			}
			catch(NoSuchSquareException nssex){
				// no go, there is no filled square in our sequence
			}
			
		}
		
		if(row.isCompleted()) return;
		
		// now consider further sequences. note that unlike in the case of the last run, this time we do have to make 
		// sure the known square is close enough to the edge, because we can no longer assume that there won't be any 
		// runs assigned to the area we're going to be mercury-ing
		// TODO: unless it is known that the previous run is not allocated where we're gonna be doing the mercury (e.g.
		// in the previous sequence)
		
		// so, scan the sequence from right to left and find the first filled square; this is the seed square. Check to
		// see if it close enough to the border to perform bouncing and/or mercury; if not, skip this sequence
		// 
		// if its run is known: use its run length to determine the extent of the bounce/mercury
		// if not, use the minimum incomplete run length
		
		for(int sequenceIndex = lastRunSequenceIndex - 1; sequenceIndex >= 0; sequenceIndex--){
			
			Sequence sequence = decomposition.getSequence(sequenceIndex);
			
			// find the seed square (if any)
			int seedSquareIndex;
			
			try {
				seedSquareIndex = sequence.getLastFilledSquareIndex();
			}
			catch(NoSuchSquareException nksex) {
				// no filled squares within this sequence, try next sequence
				continue;
			}
			
			Square seedSquare = sequence.getSquare(seedSquareIndex);
			
			int bounceRunLength;
			int mercuryRunLength;
			int seedLength;
			Run seedSquareRun = seedSquare.getRun(row.getOrientation());
			
			if(seedSquareRun == null){
				bounceRunLength = row.getMinimumIncompleteRunLength(); // default to the minimum incomplete run length
				mercuryRunLength = row.getMaximumIncompleteRunLength(); // default to the maximum (!) incomplete run length (we need a conservative run length, and for mercury longer run lengths result in fewer cleared squares)
				seedLength = NonogramUtils.getFilledSequenceLengthLeft(sequence, seedSquareIndex);
			} else {
				bounceRunLength = seedSquareRun.getLength(); // use the actual run's length
				mercuryRunLength = seedSquareRun.getLength();
				seedLength = seedSquareRun.getKnownSquareCount(); // assumes that the run is fully connected (i.e. that there are no neighbour filled squares that don't have the same run set)
			}
			
			// __if the seed square is less than minimumIncompleteRunLength squares away from the edge__, 
			// we can bounce it off the edge or mercury it (ONLY then -- e.g. if you have a known run, you might be looking
			// at the next 
			
			if(seedSquareIndex >= sequence.getLength() - row.getMinimumIncompleteRunLength() - 1){
				NonogramUtils.bounceRight(sequence, seedSquareIndex, bounceRunLength, seedSquareRun); // seedSquareRun will be run if no run is known
				NonogramUtils.mercuryRight(sequence, mercuryRunLength, seedSquareIndex, seedLength);
			}
			
		}
		
	}
	
}
