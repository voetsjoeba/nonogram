package com.voetsjoeba.nonogram.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.InconsistentDecompositionException;
import com.voetsjoeba.nonogram.exception.NoSuchSequenceException;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;
import com.voetsjoeba.nonogram.structure.assignment.LocalDecompositionIndex;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * Splits a row into sequence of non-cleared squares. This structure is meant to facilitate the process of solving a row; as such, sequences that are entirely 
 * filled with completed runs are excluded from the decomposition. In other words, this decomposition considers only
 * runs that are incomplete.
 * 
 * <p>Additionally, calculates the rightmost possible starting index within the decomposition for each incomplete run. This information is calculated here once
 * because it frequently accessed by some solvers.</p>
 * 
 * <p>The following conditions are assumed by the decomposer:
 * <ul>
 *     <li>Each run with known squares must be contiguous, i.e. there can be no runs with known squares but gaps inbetween them.</li>
 * </ul>
 * </p>
 * 
 * @author Jeroen De Ridder
 */
public class RowDecomposition extends AbstractDecomposition implements Iterable<Sequence> {
	
	private final Row row;
	private final List<Sequence> sequences;
	
	private final Map<Run, DecompositionRunInfo> runInfo;
	private int totalLength;
	
	public RowDecomposition(Row row) {
		
		this.row = row;
		this.sequences = new ArrayList<Sequence>();
		this.runInfo = new HashMap<Run, DecompositionRunInfo>();
		
		// (!) don't use row.isComplete() here -- it's possible that all squares are filled but not all squares
		// have known runs, and the decomposition needs to be consistent with the row's incomplete runs
		if(row.getIncompleteRuns().size() > 0){
			
			try {
				constructDecomposition();
				calculateRunInformation();
			}
			catch(ConflictingSquareStateException e) {
				throw new RuntimeException(e); // TODO: have this propagate properly
			}
			
		}
		
	}
	
	private void calculateRunInformation(){
		
		// create entries for incomplete runs
		for(Run run : row.getIncompleteRuns()){
			runInfo.put(run, new DecompositionRunInfo(run));
		}
		
		calculateRightmostStartIndices();
		calculateLeftmostStartIndices();
		
	}
	
	/**
	 * Extracts sequences of non-cleared squares that are not completely filled from the row.
	 * These sequences will hold the solution runs.
	 * @throws ConflictingSquareStateException 
	 */
	private void constructDecomposition() throws ConflictingSquareStateException {
		
		// split row into sequences
		
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
				
				//boolean allFilled = true;
				
				// run a non-cleared sequence
				for(; j < row.getLength(); j++){
					
					Square scannedSquare = row.getSquare(j);
					
					if(scannedSquare.isCleared()){
						
						break;
						
					} else {
						
						// scannedSquare can be either FILLED or unknown at this point
						sequenceEndIndex = j; // update end index
						
					}
					
				}
				
				Sequence sequence = new StandardSequence(row, i, sequenceEndIndex);
				sequences.add(sequence);
				
			}
			
			i = sequenceEndIndex + 1; // skip forward
			
		}
		
		// remove any sequences that are completely filled with a known run (or that have 1 extra space on either side and remove the space)
		
		Iterator<Sequence> it = sequences.iterator();
		while(it.hasNext()){
			
			Sequence sequence = it.next();
			List<Run> completedRuns = sequence.getCompletedRuns();
			
			if(completedRuns.size() != 1) continue;
			Run completedRun = completedRuns.get(0);
			
			int sequenceLength = sequence.getLength();
			int completedRunLength = completedRun.getLength();
			
			// run fills sequence completely
			if(sequenceLength == completedRunLength){
				it.remove();
				continue;
			}
			
			// 1 extra square on either side
			if(sequenceLength == completedRunLength + 1){
				
				sequence.trimLeft();
				sequence.trimRight();
				
				it.remove();
				continue;
				
			}
			
			// 2 extra squares, whitespace can be removed only if it's 1 on both sides
			if(sequence.getLength() == completedRunLength + 2){
				
				Square runFirstSquare = completedRun.getFirstKnownSquare();
				
				// find the index of the completed run's first square
				// TODO: find a better way of doing this, this sucks
				int firstSquareIndex = 0;
				while(sequence.getSquare(firstSquareIndex) != runFirstSquare) firstSquareIndex++;
				
				if(firstSquareIndex == sequence.getLength()){
					throw new RuntimeException("Could not find first square of completed run " + completedRun+ " in sequence " + sequence); // TODO: use better exception
				}
				
				if(firstSquareIndex == 1){ // run starts right in the middle of the sequence with 2 whitespace squares on either side
					
					sequence.trimLeft();
					sequence.trimRight();
					
					it.remove();
					continue;
					
				}
				
			}
			
		}
		
		// calculate final total length
		totalLength = 0;
		for(Sequence sequence : sequences) totalLength += sequence.getLength();
		
	}
	
	/**
	 * Returns the rightmost possible index of the provided run's first square within this decomposition. That is, the returned value is the last (i.e. rightmost)
	 * possible starting position of the provided run, returned as an index within this decomposition.
	 * @param run
	 * @return
	 */
	/*public int getRightmostStartOffset(Run run){
		
		DecompositionRunInfo runInfoz = runInfo.get(run);
		
		if(runInfoz == null){
			throw new IllegalArgumentException("No run decomposition information found for run " + run + "; make sure it's an incomplete run before calling this function");
		}
		
		return runInfoz.rightmostStartOffset;
		
	}*/
	
	/*public int getLeftmostStartOffset(Run run){
		
		DecompositionRunInfo runInfoz = runInfo.get(run);
		
		if(runInfoz == null
				
	}*/
	
	public DecompositionRunInfo getRunInfo(Run run){
		
		DecompositionRunInfo runInfoz = runInfo.get(run);
		
		if(runInfoz == null){
			throw new IllegalArgumentException("No run decomposition information found for run " + run + "; make sure it's an incomplete run before calling this function");
		}
		
		return runInfoz;
		
	}
	
	/**
	 * For each incomplete run, calculates its rightmost start index.
	 */
	private void calculateRightmostStartIndices() {
		
		//List<Run> runs = row.getIncompleteRuns();
		
		// get all runs inbetween the first and last incomplete run. note that this means that "edge complete runs" are excluded, since they are not
		// "contained" by the decomposition; for these edge runs, there is nothing to skip over in the decomposition, so including them would produce wrong
		// results (besides, they're on the edge of the row, so they can ignored regardless)
		List<Run> runs = NonogramUtils.getWorkingRuns(row);
		
		// working from right to left, fill up the sequences with our incomplete runs. After each run has been placed, write its corresponding start index, apply
		// a separator whitespace square and continue with the next run (unless of course there are no more runs).
		// if the edge of a sequence is reached, no extra whitespace square is needed because there will already be one in the final row (due to the nature
		// of the decomposition).
		
		int runCount = runs.size();
		
		int globalInsertionIndex = totalLength - 1; // global decomposition index of the point to insert the next run
		int currentRunIndex = runCount - 1; // index of the currently to-be-assigned run
		
		while(currentRunIndex >= 0){ // while not all runs have been placed
			
			Run currentRun = runs.get(currentRunIndex);
			
			if(currentRun.hasKnownSquares()){
				
				// grab the local index of the current global insertion pointer
				LocalDecompositionIndex currentLocal = globalToLocal(globalInsertionIndex);
				
				if(currentRun.isComplete()){
					
					// special case -- if the run is complete, then it occupies its own separate filled sequence and will not be part of the decomposition.
					// also, since the next run (i.e. the one before this, since we're working right-to-left) needs to be allocated in front of this one,
					// it must necessarily be allocated to the next sequence, so let's skip to the next sequence after this one
					
					// one problem though: the sequence containing this run is not part of the decomposition, so we can't know what position to skip to because
					// it could be placed inbetween any two decomposition runs for all we know
					
					// to solve this, we move to absolute square index space. the idea is this: we take the absolute square indices of the last squares from each 
					// of our decomposition sequences, going right to left starting from the current sequence, and we compare each one with the absolute index
					// of the first square of the completed run. As we skip sequences that aren't the one we need, we also update the insertion index to move 
					// along. As soon as we've found one whose last square's absolute index is lower, then we've found our sequence, and the insertion index
					// will automatically be in the right position.
					
					// NOTE: there must always be such a sequence. If not, this would imply that this run is an edge completed run, which should have been
					// excluded from consideration
					
					// NOTE: observe that there are two possible outcomes of the previous iteration: either our global insertion index has remained in the 
					// same sequence as before, or it has already moved on to a new sequence (which might already be the one we need since -- we can't know 
					// inbetween which two sequences the completed run is situated). Also observe that the algorithm outlined above will produce correct
					// results for either case, provided that in the first case we first move globalInsertionIndex to also point at the next sequence.
					// This is always valid, because since this run is completed, the next one must necessarily occupy another sequence.
					
					Square firstSquare = currentRun.getFirstKnownSquare();
					int firstSquareAbsoluteIndex = row.getSquareIndex(firstSquare);
					
					// if needed, move the current insertion pointer to the next sequence. it may already be in the required position -- see note above
					if(currentLocal.sequenceOffset < getSequenceLength(currentLocal.sequenceIndex) - 1){
						
						globalInsertionIndex -= currentLocal.sequenceOffset + 1;
						
						// update the local index of the insertion pointer because we need this later (technically we could 
						// update this by just subtracting 1 from the sequence index and maxing the offset, but local indices
						// were read-only structs at the time of writing)
						currentLocal = globalToLocal(globalInsertionIndex);
						
					}
					
					// now find the first decomposition sequence whose last square's absolute index is lower than that of the completed run's first square's
					// absolute index
					
					int sequenceIndex = currentLocal.sequenceIndex;
					while(true){
						
						Sequence sequence = getSequence(sequenceIndex);
						
						Square lastSequenceSquare = sequence.getLastSquare();
						int lastSequenceSquareAbsoluteIndex = row.getSquareIndex(lastSequenceSquare);
						
						if(lastSequenceSquareAbsoluteIndex < firstSquareAbsoluteIndex){
							break;
						}
						
						sequenceIndex--; // move to the next sequence ...
						globalInsertionIndex -= sequence.getLength(); // ... and take the insertion index with you please
						
					}
					
					if(sequenceIndex < 0){
						// impossible -- this can happen only if currentRun is a completed edge run
						throw new InconsistentDecompositionException("Could not find decomposition sequence to the left of completed run " + currentRun + "; only non-edge completed runs are allowed in the decomposition calculation");
					}
					
					// at this point, globalInsertionIndex is at the correct position to starting inserting the next run(s)
					
					currentRunIndex--;
					continue;
					
				}
				
				// the run has known squares but it's not complete; in this case its rightmost starting index is found by taking its last known 
				// square and bounce it against the right sequence edge so as to (possibly) expand the run length
				// 
				// the rest of this algorithm works by first positioning globalInsertionIndex at the run's last square and subtracting the run 
				// length, so let's do the same so we can just feed the code below our new values. that means we need to find the global index
				// of the last square of this run in its rightmost position (taking bounce into account)
				
				// NOTE: we don't need to check whether there is enough space available in this sequence because there simply has to be since the 
				// run has known squares in this sequence
				
				Square lastSquare = currentRun.getLastKnownSquare();
				
				// skip to the run's last known square
				int lastKnownSquareGlobalIndex = globalInsertionIndex;
				while(!lastSquare.equals(getSquare(lastKnownSquareGlobalIndex))){
					lastKnownSquareGlobalIndex--;
				}
				
				// at this point, lastKnownSquareGlobalIndex points at the last known square (in global decomposition space)
				// let's find the sequence and offset where it's at
				LocalDecompositionIndex lastKnownSquareLocal = globalToLocal(lastKnownSquareGlobalIndex);
				
				// find the remaining amount of squares left to be assigned to the run
				int remainingRunLength = currentRun.getLength() - currentRun.getKnownSquareCount();
				
				// if there is sufficient room left to the right of the last known square, we can just add the remaining run length to it to find the 
				// index of the run's last square in its rightmost position. But, if the run needs to bounce against the existing insertion index (i.e. there 
				// is less than remainingRunLength space available to the right of lastSquare), then we need to cap it at the existing local insertion offset 
				// within the sequence (so that when we subtract the length from it, we'll get the bounced index of the first square)
				//
				// (see also the leftmost calculation for a more detailed explanation)
				
				// first bounce against the right sequence edge to be sure (this always needs to happen -- we can still bounce against the established insertion
				// index later)
				int lastSquareOffset = Math.min(lastKnownSquareLocal.sequenceOffset + remainingRunLength, getSequenceLength(lastKnownSquareLocal.sequenceIndex) - 1);
				
				// now bounce against the established insertion index, but only if we're still in the same sequence (otherwise currentLocal.sequenceOffset would 
				// be meaningless since it would be from another sequence)
				if(currentLocal.sequenceIndex == lastKnownSquareLocal.sequenceIndex){
					lastSquareOffset = Math.min(lastSquareOffset, currentLocal.sequenceOffset);
				}
				
				// move globalInsertionIndex to match lastSquareOffset. we know the global index of the last known square, we know the local offset of the
				// last known (!) square, and we know the local offset of the last square. So, first move global index to the last known square's global position
				// and then apply the difference between the last square and the last known square.
				globalInsertionIndex = lastKnownSquareGlobalIndex + (lastSquareOffset - lastKnownSquareLocal.sequenceOffset);
				
				// at this point, globalInsertionIndex is correctly positioned for the rest of the code below
				
			}
			
			// get current insertion local index (i.e. sequence index and offset within that sequence)
			LocalDecompositionIndex localIndex = globalToLocal(globalInsertionIndex);
			int sequenceOffset = localIndex.sequenceOffset;
			int sequenceSpaceLeft = sequenceOffset + 1; // amount of space left in the target sequence before assigning the run
			
			// try to place the current run in the current sequence -- see how much room is left after placing the run
			int newSequenceSpaceLeft = sequenceOffset - currentRun.getLength() + 1;
			
			// if there's a negative amount of room left, then the run overshoots the sequence -- try again in the next sequence
			if(newSequenceSpaceLeft < 0){
				
				// move to the next sequence by subtracting sequenceOffset + 1 (i.e. the remaini from the global insertion index)
				globalInsertionIndex -= sequenceSpaceLeft;
				continue;
				
			}
			
			// if there's 0 room left, then the run fits the sequence perfectly and no whitespace sentinel needs to be applied -- move on to the next 
			// run and sequence
			
			// if there's 1 room left, then the run fits the sequence and leaves a sentinel whitespace square. No further run can possibly be assigned there, so
			// move on to the next run and sequence
			if(newSequenceSpaceLeft == 0 || newSequenceSpaceLeft == 1){
				
				// subtract the current run's length from the global index -- this will make globalInsertionIndex point to the position to start filling the next run
				globalInsertionIndex -= currentRun.getLength();
				
				// at this point, globalInsertionIndex + 1 is the rightmost starting offset for currentRun
				runInfo.get(currentRun).rightmostStartOffset = globalInsertionIndex + 1;
				
				// if there's 1 space left, then we should discard it because no further runs can be assigned there and we need to start filling one more square
				// to the left (which will be in the next sequence). But, not if this is the last run, because then there are no further runs and the returned 
				// value would point to the whitespace square instead of the first run square.
				//if(newSequenceSpaceLeft == 1 && !isLastRun) globalInsertionIndex--;
				
				// if there's 1 space left, then we should discard it because no further runs can be assigned there and so we need to start filling the next runs
				// one more square to the left (which will be in the next sequence).
				if(newSequenceSpaceLeft == 1) globalInsertionIndex--;
				
				currentRunIndex--;
				continue;
				
			}
			
			// if there's > 1 room left, then the run fits the sequence and leaves further room for another run. apply a sentinel whitespace square 
			// (unless we're at the last run) and move on to the next run
			if(newSequenceSpaceLeft > 1){
				
				// subtract the current run's length from the global index -- this will make globalInsertionIndex point to the square right before the first
				// square of the run we just allocated
				globalInsertionIndex -= currentRun.getLength();
				
				// at this point, globalInsertionIndex + 1 is the rightmost starting offset for currentRun
				runInfo.get(currentRun).rightmostStartOffset = globalInsertionIndex + 1;
				
				globalInsertionIndex--; // add one whitespace to separate the next run
				
				// move on to the next run
				currentRunIndex--;
				continue;
				
			}
			
		}
		
	}
	
	/**
	 * For each incomplete run, calculates its leftmost start index.
	 */
	private void calculateLeftmostStartIndices() {
		
		//List<Run> runs = row.getIncompleteRuns();
		
		// get all runs inbetween the first and last incomplete run. note that this means that "edge complete runs" are excluded, since they are not
		// "contained" by the decomposition; for these edge runs, there is nothing to skip over in the decomposition, so including them would produce wrong
		// results (besides, they're on the edge of the row, so they can ignored regardless)
		List<Run> runs = NonogramUtils.getWorkingRuns(row);
		
		// working from left to right, fill up the sequences with our incomplete runs. After each run has been placed, write its corresponding start index, 
		// apply a separator whitespace square (if needed) and continue with the next run (unless of course there are no more runs).
		// if the edge of a sequence is reached, no extra whitespace square is needed because there will already be one in the final row (due to the nature
		// of the decomposition).
		
		int runCount = runs.size();
		
		int globalInsertionIndex = 0; // global decomposition index of the point to insert the next run
		int currentRunIndex = 0; // index of the currently to-be-assigned run
		
		while(currentRunIndex < runCount){ // while not all runs have been placed
			
			Run currentRun = runs.get(currentRunIndex);
			
			if(currentRun.hasKnownSquares()){
				
				// grab the local index of the current global insertion pointer
				LocalDecompositionIndex currentLocal = globalToLocal(globalInsertionIndex);
				
				if(currentRun.isComplete()){
					
					// special case -- if the run is complete, then it occupies its own separate filled sequence and will not be part of the decomposition.
					// also, since the next run (i.e. the one after this, since we're working left-to-right) needs to be allocated in front of this one,
					// it must necessarily be allocated to the next sequence, so let's skip to the next sequence after this one
					
					// one problem though: the sequence containing this run is not part of the decomposition, so we can't know what position to skip to because
					// it could be placed inbetween any two decomposition runs for all we know
					
					// to solve this, we move to absolute square index space. the idea is this: we take the absolute square indices of the first squares from each 
					// of our decomposition sequences, going right to left starting from the current sequence, and we compare each one with the absolute index
					// of the last square of the completed run. As we skip sequences that aren't the one we need, we also update the insertion index to move 
					// along. As soon as we've found one whose first square's absolute index is higher, then we've found our sequence, and the insertion index
					// will automatically be in the right position.
					
					// NOTE: there must always be such a sequence. If not, this would imply that this run is an edge completed run, which should have been
					// excluded from consideration
					
					// NOTE: observe that there are two possible outcomes of the previous iteration: either our global insertion index has remained in the 
					// same sequence as before, or it has already moved on to a new sequence (which might already be the one we need since -- we can't know 
					// inbetween which two sequences the completed run is situated). Also observe that the algorithm outlined above will produce correct
					// results for either case, provided that in the first case we first move globalInsertionIndex to also point at the next sequence.
					// This is always valid, because since this run is completed, the next one must necessarily occupy another sequence.
					
					Square lastSquare = currentRun.getLastKnownSquare();
					int lastSquareAbsoluteIndex = row.getSquareIndex(lastSquare);
					
					// if needed, move the current insertion pointer to the next sequence. it may already be in the required position -- see note above
					//if(currentLocal.sequenceOffset < getSequenceLength(currentLocal.sequenceIndex) - 1){
					if(currentLocal.sequenceOffset > 0){
						
						globalInsertionIndex += getSequenceLength(currentLocal.sequenceIndex) - currentLocal.sequenceOffset;
						
						// update the local index of the insertion pointer because we need this later (technically we could 
						// update this by just adding 1 to the sequence index and 0'ing the offset, but local indices
						// were read-only structs at the time of writing)
						currentLocal = globalToLocal(globalInsertionIndex);
						assert currentLocal.sequenceOffset == 0 : "Misaligned global insertion pointer; expected 0, found " + currentLocal.sequenceOffset + ". This indicates a wrong change to globalInsertionIndex to skip a sequence, please review code.";
						
					}
					
					// now find the first decomposition sequence whose first square's absolute index is higher than that of the completed run's last square's
					// absolute index
					
					int sequenceIndex = currentLocal.sequenceIndex;
					while(true){
						
						Sequence sequence = getSequence(sequenceIndex);
						
						Square firstSequenceSquare = sequence.getFirstSquare();
						int firstSequenceSquareAbsoluteIndex = row.getSquareIndex(firstSequenceSquare);
						
						if(firstSequenceSquareAbsoluteIndex > lastSquareAbsoluteIndex){
							break;
						}
						
						sequenceIndex++; // move to the next sequence ...
						globalInsertionIndex += sequence.getLength(); // ... and take the insertion index with you please
						
					}
					
					if(sequenceIndex >= getSequenceCount()){
						// impossible -- this can happen only if currentRun is a completed edge run
						throw new InconsistentDecompositionException("Could not find decomposition sequence to the right of completed run " + currentRun + "; only non-edge completed runs are allowed in the decomposition calculation");
					}
					
					// at this point, globalInsertionIndex is at the correct position to starting inserting the next run(s)
					
					currentRunIndex++;
					continue;
					
				}
				
				// the run has known squares but it's not complete; in this case its leftmost starting index is found by taking its first known 
				// square and subtracting the amount of remaining squares to complete the run (or to reach the left sequence edge, whichever
				// comes first).
				// 
				// the rest of this algorithm works by first positioning globalInsertionIndex at the run's first square and adding the run 
				// length, so let's do the same so we can just feed the code below our new values. that means we need to find the global index
				// of the first square of this run in its leftmost position (taking the left sequence into account)
				
				// NOTE: we don't need to check whether there is enough space available in this sequence because there simply has to be since the 
				// run has known squares in this sequence
				
				Square firstSquare = currentRun.getFirstKnownSquare();
				
				// skip to the run's first known square
				int firstKnownSquareGlobalIndex = globalInsertionIndex;
				while(!firstSquare.equals(getSquare(firstKnownSquareGlobalIndex))){
					firstKnownSquareGlobalIndex++;
				}
				
				// at this point, firstKnownSquareGlobalIndex points at the first known square (in global decomposition space)
				// let's find the sequence and offset where it's at
				LocalDecompositionIndex firstKnownSquareLocal = globalToLocal(firstKnownSquareGlobalIndex);
				
				// find the remaining amount of squares left to be assigned to the run
				int remainingRunLength = currentRun.getLength() - currentRun.getKnownSquareCount();
				
				// if there is sufficient room to the right of the last known square, we can just subtract the remaining run length from it to find the 
				// index of the run's first square in its leftmost position. But, if the run needs to bounce against the existing insertion index determined 
				// from the previous iteration(s) (or 0) (i.e. there is less than remainingRunLength space available to the left of firstSquare until the 
				// previous iteration's insertion index is reached), then we need to cap it at the existing insertion index.
				//
				// For example: consider the following scenario:
				//
				//       0   1   2   3   4   5   6   7   8   9   10  11  12
				//     +---+---+---+---+---+---+---+---+---+---+---+---+---+--
				// 5,7 |   |   | x | x | x |   |   |   |   | x | x | x |   | ...
				//     +---+---+---+---+---+---+---+---+---+---+---+---+---+--
				// 
				// The iteration that looks at the 5-run will update globalInsertionIndex to position 6, because clearly run 7 can only start at or after
				// that position. The situation is now as follows:
				//
				//       0   1   2   3   4   5   6   7   8   9   10  11  12
				//     +---+---+---+---+---+---+---+---+---+---+---+---+---+--
				// 5,7 |   |   | x | x | x |   |   |   |   | x | x | x |   | ...
				//     +---+---+---+---+---+---+---+---+---+---+---+---+---+--
				//                               ^
				//                               globalInsertionIndex
				//
				// Now, to find the leftmost starting index of the 7-run, we must respect the limit that was set in the previous iteration (i.e. we must
				// respect that the starting position for the 7-run cannot go past the globalInsertionIndex position. Note that globalInsertionIndex is a 
				// global index, and so we'll need to take the sequence-local offset to limit against.
				//
				// HOWEVER: this only applies if the known squares are in the same sequence as where the current insertion pointer is at! Otherwise, it is
				// meaningless to bounce against the (local offset of the) insertion pointer, since that value does not apply in another sequence. So, in
				// conclusion, we can first bounce against the left edge (this is always valid to do) and, if needed (i.e. in the same sequence), also bounce
				// against the established insertion pointer.
				
				int firstSquareOffset = Math.max(firstKnownSquareLocal.sequenceOffset - remainingRunLength, 0);
				
				if(currentLocal.sequenceIndex == firstKnownSquareLocal.sequenceIndex){
					firstSquareOffset = Math.max(firstSquareOffset, currentLocal.sequenceOffset);
				}
				
				// move globalInsertionIndex to match firstSquareOffset. we know the global index of the first known square, we know the local offset of the
				// first known (!) square, and we know the local offset of the first square. So, first move global index to the first known square's global 
				// position and then apply the difference between the first square and the first known square.
				globalInsertionIndex = firstKnownSquareGlobalIndex - (firstKnownSquareLocal.sequenceOffset - firstSquareOffset);
				
				// at this point, globalInsertionIndex is correctly positioned for the rest of the code below
				
			}
			
			// get current insertion local index (i.e. sequence index and offset within that sequence)
			LocalDecompositionIndex localIndex = globalToLocal(globalInsertionIndex);
			int sequenceOffset = localIndex.sequenceOffset;
			int sequenceLength = getSequenceLength(localIndex.sequenceIndex);
			int sequenceSpaceLeft = sequenceLength - sequenceOffset; // amount of space left in the target sequence before assigning the run
			
			// try to place the current run in the current sequence -- see how much room is left after placing the run
			int newSequenceSpaceLeft = sequenceLength - (sequenceOffset + currentRun.getLength());
			
			// if there's a negative amount of room left, then the run overshoots the sequence -- try again in the next sequence
			if(newSequenceSpaceLeft < 0){
				
				// move to the next sequence by subtracting sequenceOffset + 1 (i.e. the remaini from the global insertion index)
				globalInsertionIndex += sequenceSpaceLeft;
				continue;
				
			}
			
			// if there's 0 room left, then the run fits the sequence perfectly and no whitespace sentinel needs to be applied -- move on to the next 
			// run and sequence
			
			// if there's 1 room left, then the run fits the sequence and leaves a sentinel whitespace square. No further run can possibly be assigned there, so
			// move on to the next run and sequence
			if(newSequenceSpaceLeft == 0 || newSequenceSpaceLeft == 1){
				
				// at this point, globalInsertionIndex - 1 is the leftmost starting offset for currentRun
				runInfo.get(currentRun).leftmostStartOffset = globalInsertionIndex;
				
				// add the current run's length to the global index -- this will make globalInsertionIndex point to the position to start filling the next run
				globalInsertionIndex += currentRun.getLength();
				
				// if there's 1 space left, then we should discard it because no further runs can be assigned there and so we need to start filling the next runs
				// one more square to the right (which will be in the next sequence).
				if(newSequenceSpaceLeft == 1) globalInsertionIndex++;
				
				currentRunIndex++;
				continue;
				
			}
			
			// if there's > 1 room left, then the run fits the sequence and leaves further room for another run. apply a sentinel whitespace square 
			// and move on to the next run
			if(newSequenceSpaceLeft > 1){
				
				// at this point, globalInsertionIndex - 1 is the leftmost starting offset for currentRun
				getRunInfo(currentRun).leftmostStartOffset = globalInsertionIndex;
				
				// add the current run's length to the global index -- this will make globalInsertionIndex point to the square right after the last
				// square of the run we just allocated
				globalInsertionIndex += currentRun.getLength();
				
				globalInsertionIndex++; // add one whitespace to separate the next run (if any)
				
				// move on to the next run
				currentRunIndex++;
				continue;
				
			}
			
		}
		
	}
	
	/**
	 * Returns the sequence containing <tt>square</tt>, or null if no such sequence exists.
	 * @param square
	 */
	public Sequence getSequenceContaining(Square square){
		
		try {
			int sequenceIndex = getSequenceIndexContaining(square);
			return sequences.get(sequenceIndex);
		}
		catch(NoSuchSequenceException nsqex) {
			return null;
		}
		
	}
	
	/**
	 * Returns the index within this decomposition of the sequence containing <tt>square</tt>.
	 * @throws NoSuchSequenceException if no such sequence exists
	 */
	public int getSequenceIndexContaining(Square square) throws NoSuchSequenceException {
		
		int sequenceCount = sequences.size();
		for(int i=0; i<sequenceCount; i++){
			if(sequences.get(i).contains(square)) return i;
		}
		
		throw new NoSuchSequenceException("Square " + square + " not found in this decomposition");
		
	}
	
	/**
	 * Returns the sequence containing the square with index <tt>squareIndex</tt>.
	 */
	public Sequence getSequenceContaining(int squareIndex){
		
		// TODO: use globalToLocal like in AssignmentDecomposition
		
		if(squareIndex < 0 || squareIndex >= getTotalLength()){
			throw new IndexOutOfBoundsException("Index " + squareIndex + " out of bounds in decomposition of total length " + totalLength);
		}
		
		Sequence result = null;
		int candidateIndex = 0;
		
		while(candidateIndex < sequences.size()){
			
			Sequence candidate = sequences.get(candidateIndex);
			int candidateLength = candidate.getLength();
			
			// check whether the squareIndex lies within this candidate (see also the else clause)
			if(squareIndex < candidateLength){
				
				result = candidate;
				break; // found our sequence
				
			} else {
				// subtract candidate length from index and move to next sequence
				squareIndex -= candidateLength;
			}
			
			candidateIndex++;
			
		}
		
		return result;
		
	}
	
	/**
	 * Returns the square with index <tt>squareIndex</tt>.
	 * @param squareIndex
	 * @return
	 */
	public Square getSquare(int squareIndex){
		
		// TODO: use globalToLocal like in AssignmentDecomposition
		
		if(squareIndex < 0 || squareIndex >= getTotalLength()){
			throw new IndexOutOfBoundsException("Index " + squareIndex + " out of bounds in decomposition of total length " + totalLength);
		}
		
		Square result = null;
		int candidateIndex = 0; // candidate index of enclosing sequence
		
		while(candidateIndex < sequences.size()){
			
			Sequence candidate = sequences.get(candidateIndex);
			int candidateLength = candidate.getLength();
			
			// check whether the squareIndex lies within this candidate (see also the else clause)
			if(squareIndex < candidateLength){
				
				result = candidate.getSquare(squareIndex);
				break; // found our sequence
				
			} else {
				// subtract candidate length from index and move to next sequence
				squareIndex -= candidateLength;
			}
			
			candidateIndex++;
			
		}
		
		return result;
		
	}
	
	public List<Sequence> getSequences(){
		return Collections.unmodifiableList(sequences);
	}
	
	public Sequence getSequence(int sequenceIndex){
		return sequences.get(sequenceIndex);
	}
	
	// -- Decomposition interface -----------------------------------------
	
	public int getSequenceCount() {
		return sequences.size();
	}
	
	public int getSequenceLength(int i) {
		return sequences.get(i).getLength();
	}
	
	// --------------------------------------------------------------------
	
	/**
	 * Returns the row of which this is is a decomposition.
	 * @return
	 */
	public Row getRow(){
		return row;
	}
	
	/**
	 * Returns the total amount of squares in this decomposition.
	 */
	public int getTotalLength(){
		return totalLength;
	}
	
	/**
	 * Returns the amount of squares in this decompositions whose state is known.
	 * @return
	 */
	public int getKnownSquareCount() {
		
		int count = 0;
		for(Sequence sequence : sequences){
			count += sequence.getKnownSquareCount();
		}
		return count;
		
	}
	
	public Iterator<Sequence> iterator() {
		return sequences.iterator();
	}
	
	public void clearAndRemoveSequence(int i) throws ConflictingSquareStateException {
		
		if(i < 0 || i >= sequences.size()) throw new ArrayIndexOutOfBoundsException("Illegal sequence index " + i + "; must be between 0 and " + (sequences.size() - 1));
		
		Sequence removedSequence = sequences.remove(i);
		totalLength -= removedSequence.getLength();
		
		removedSequence.clear();
		
		// recalculate rightmost starting indices
		calculateRightmostStartIndices();
		
	}
	
	/**
	 * Returns a sub-decomposition consisting of this decomposition 
	 * @param sequence
	 * @param sequenceOffset
	 * @return
	 */
	/*public RowDecomposition getSubDecomposition(Sequence sequence, int sequenceOffset){
		return null;
	}*/
	
	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		for(Sequence sequence : sequences){
			sb.append(sequence);
			sb.append("\n");
		}
		return sb.toString();
		
	}
	
}
