package com.voetsjoeba.nonogram.algorithm.rowsolver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.InconsistentDecompositionException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.DecompositionRunInfo;
import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.StandardSequence;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;
import com.voetsjoeba.nonogram.structure.assignment.LocalDecompositionIndex;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * For filled (sequences of) squares with unknown run, attempts to uniquely identify the associated run.
 * 
 * <p>It is recommended to execute this solver before running an exhaustive solver, since knowing which run is assigned to filled squares
 * can greatly help the exhaustive solver to eliminate some options.</p>
 */
public class IdentifyUnknownRunsSolver extends AbstractRowSolver {
	
	public void solve(Row row) throws UnsolvablePuzzleException {
		
		List<Run> incompleteRuns = row.getIncompleteRuns();
		if(incompleteRuns.size() <= 0) return;
		
		RowDecomposition decomposition = row.getDecomposition();
		//int firstIncompleteRunAbsoluteIndex = incompleteRuns.get(0).getIndex();
		//int lastIncompleteRunAbsoluteIndex = incompleteRuns.get(incompleteRuns.size()-1).getIndex();
		
		// for each (contiguous) sequence of filled squares with unknown run, see which incomplete runs of sufficient length have a possible
		// position range that overlaps it
		
		// as soon as even 1 of the filled squares is overlapped by only 1 run, then we have found the run to assign to those filled squares
		
		List<UnknownSequence> unknownSequences = getUnknownRunSequences(decomposition);
		for(UnknownSequence unknownSequence : unknownSequences){
			
			// for each square, see how many incomplete runs' possible ranges overlap it
			// if for any square there is only one of them, then that must be this sequence's run
			// as soon as there are multiple, don't bother looking any further
			// if there are none, then something's wrong
			
			Square firstSquare = decomposition.getSquare(unknownSequence.startIndex);
			
			for(int squareIndex = unknownSequence.startIndex; squareIndex <= unknownSequence.endIndex; squareIndex++){
				
				if(firstSquare.hasRun(row.getOrientation())){
					continue; // might have had their run set as a side effect
				}
				
				// find all runs that can be assigned to this sequence
				for(Run incompleteRun : incompleteRuns){
					
					// if the run is smaller than the sequence's length, then this run cannot possibly be assigned to this sequence, so skip it
					if(incompleteRun.getLength() < unknownSequence.getLength()) continue;
					
					DecompositionRunInfo runInfo = decomposition.getRunInfo(incompleteRun);
					
					// the run's leftmost possible position does not equal or preceed the sequence's leftmost square's position, then this run cannot
					// possible be assigned to this sequence (because that would mean there would be cleared demarcation squares on positions occupied
					// by this run). an analogous statement is true for its rightmost possible position.
					// 
					// this is kind of awkward place to perform this check, but since the testing is primarily done on a square-by-square
					// basis, there's really no other place to do it
					if(runInfo.leftmostStartOffset > unknownSequence.startIndex || runInfo.rightmostStartOffset + incompleteRun.getLength() - 1 < unknownSequence.endIndex) continue;
					
					// if the run's leftmost and rightmost starting positions overlap with this square, add it to the possible sequences
					if(NonogramUtils.intervalOverlapsEntirely(runInfo.leftmostStartOffset, runInfo.rightmostStartOffset + incompleteRun.getLength(), squareIndex, squareIndex + 1)){
						
						// the same run might have already been added due to previous squares, so only add one if it's not there yet
						// (or use a Set, but gtfo, they suck)
						if(!unknownSequence.possibleRuns.contains(incompleteRun)) unknownSequence.possibleRuns.add(incompleteRun);
						
					}
					
				}
				
			}
			
		}
		
		// at this point:
		//     - for sequences with only one possible run: apply that run
		//     - for sequences with multiple assignments:
		//         
		//           o a possible run is only _really_ possible if, were it to be assigned, it leaves all sequences before it with at least one 
		//           earlier run possible, and likewise each sequence after it with at least one later run.
		//           
		//           For example, consider the following scenario:
		//           
		//                  --+---+---+---+---+---+---+---+---+---+--
		//           1,17 ... |   | x |   | x |   | x-|-x-|-x-|-x-| ...  
		//                  --+---+---+---+---+---+---+---+---+---+--
		//
		//           .. in such a way that both the unknown runs have 1 and seventeen as their possible runs (for example, one such row is 
		//           1,17|.........x.x.xxxxxx$2...........
		//
		//           It's clear that the second unknown sequence can never be the 1-run, because that would leave the first unknown sequence with
		//           the 17-run as its only remaining possibility. This is impossible, since the 1-run needs to come before the 17-run.
		//           
		//           Note, however, that it is perfectly possible that several separate unknown sequences are close enough together to be in the
		//           same run. In that case, it's not correct to say that the assignment of a run leaving the ones before it without possible runs
		//           makes that run impossible, because there could be sequences in front of it that occupy the same run. We can still use this
		//           rule, however, by applying the conservative rule to only count sequences that are too far to the left of the current one to
		//           possibly be part of the same run.
		//           
		//           So, the rule becomes: a possible run is only really possible if, were it to be assigned, it leaves all sequences that are far
		//           enough in front of it to be unable to share a run with this sequences with at least one earlier remaining run possible, and
		//           likewise each sequence far enough after it to not share a run with at least one later run.
		//           
		//           o if all possible runs share a minimum length, then we know the filled sequence is part of a larger sequence of at least that 
		//           length. therefore, we might be able to infer more filled squares of the same sequence by bouncing that length against either 
		//           sequence edge. there's no reason why a sequence should be close enough to a sequence edge for this to work, but if it is, then 
		//           we can bounce it. as a special case, if all possible runs have the same length and that length also happens to be the length of 
		//           our filled sequence, then we can demarcate the filled sequence with cleared squares.
		
		// (1) for each sequence, eliminate runs that, when applied, would leave any earlier sequence without earlier runs and/or any later sequence
		// without later runs
		
		int sequenceCount = unknownSequences.size();
		for(int i=0; i<sequenceCount; i++){
			
			UnknownSequence sequence = unknownSequences.get(i);
			Iterator<Run> it = sequence.possibleRuns.iterator();
			
			while(it.hasNext()){
				
				Run candidateRun = it.next();
				
				boolean validLeft = true;
				boolean validRight = true;
				
				// check whether applying this run would leave any earlier sequence (if any) without earlier runs
				validLeft = checkValidRunLeft(unknownSequences, i, candidateRun, decomposition);
				
				// check whether applying this run would leave any later sequence (if any) without later runs
				validRight = checkValidRunRight(unknownSequences, i, candidateRun, decomposition);
				
				if(!(validLeft && validRight)){
					// candidateRun is not valid, remove it
					it.remove();
				}
				
			}
			
		}
		
		// (2) for each sequence, take the minimal length shared by all possible runs, and bounce it against both the enclosing sequence's edges.
		// if furthermore all runs have the same length, and that length equals the filled sequence's length, then demarcate the filled sequence 
		// (see notes above).
		for(UnknownSequence unknownSequence : unknownSequences){
			
			Integer sameLength = getSameLength(unknownSequence.possibleRuns);
			Integer minimumLength = getMinimumLength(unknownSequence.possibleRuns);
			//if(sameLength == null) continue;
			
			LocalDecompositionIndex firstSquareLocal = decomposition.globalToLocal(unknownSequence.startIndex);
			LocalDecompositionIndex lastSquareLocal = decomposition.globalToLocal(unknownSequence.endIndex);
			
			NonogramUtils.bounceLeft(unknownSequence.enclosingSequence, firstSquareLocal.sequenceOffset, minimumLength, null);
			NonogramUtils.bounceRight(unknownSequence.enclosingSequence, lastSquareLocal.sequenceOffset, minimumLength, null);
			
			if(sameLength != null && sameLength == unknownSequence.getLength()){
				// demarcate the sequence
				if(firstSquareLocal.sequenceOffset > 0) NonogramUtils.setSquareState(decomposition.getSquare(unknownSequence.startIndex - 1), SquareState.CLEAR, null);//decomposition.getSquare(unknownSequence.startIndex).setState()
				if(lastSquareLocal.sequenceOffset < unknownSequence.enclosingSequence.getLength() - 1) NonogramUtils.setSquareState(decomposition.getSquare(unknownSequence.endIndex + 1), SquareState.CLEAR, null);
			}
			
		}
		
		
		// (3) for each sequence, if there is only one remaining possible run, apply that run and remove it from the others
		// TODO: turn this into a small feed loop so that when a possible run is assigned, the other sequences have it removed 
		// and refed to the loop to see if this time they have only 1 possible run left
		for(UnknownSequence unknownSequence : unknownSequences){
			
			Square firstSquare = decomposition.getSquare(unknownSequence.startIndex);
			Square lastSquare = decomposition.getSquare(unknownSequence.endIndex);
			
			if(unknownSequence.possibleRuns.size() == 0){
				
				// unpossible!
				throw new InconsistentDecompositionException("No possible run assignments for sequence ["+unknownSequence.startIndex+","+unknownSequence.endIndex+"] in " + row);
				
			} else if(unknownSequence.possibleRuns.size() == 1){
				
				// yay assign run :)
				try {
					row.fillSquares(
							row.getSquareIndex(firstSquare),
							row.getSquareIndex(lastSquare),
							unknownSequence.possibleRuns.get(0)
					);
				}
				catch(ConflictingSquareStateException e) {
					throw new UnsolvablePuzzleException(e);
				}
				catch(ConflictingSquareRunException e) {
					throw new UnsolvablePuzzleException(e);
				}
				catch(RunLengthExceededException e) {
					throw new UnsolvablePuzzleException(e);
				}
				
			} else {
				// no decision
			}
			
		}
		
	}
	
	/**
	 * If all runs in <tt>possibleRuns</tt> are of the same length, return that length. Otherwise, return null.
	 */
	protected Integer getSameLength(List<Run> possibleRuns) {
		
		Integer sameLength = null;
		
		for(Run possibleRun : possibleRuns){
			
			int runLength = possibleRun.getLength();
			if(sameLength == null) sameLength = runLength; // initial value
			
			if(runLength != sameLength){
				return null; // this run's length did not match the previous one, abort
			}
			
		}
		
		return sameLength;
		
	}
	
	/**
	 * Returns the minimum length of all runs in <tt>possibleRuns</tt>.
	 */
	protected Integer getMinimumLength(List<Run> possibleRuns) {
		
		Integer minLength = null;
		
		for(Run possibleRun : possibleRuns){
			
			int runLength = possibleRun.getLength();
			if(minLength == null) minLength = runLength; // initial value
			
			if(runLength < minLength){
				minLength = runLength;
			}
			
		}
		
		return minLength;
		
	}
	
	/**
	 * Finds sequences of filled squares but without known run in <tt>decomposition</tt>.
	 * 
	 * @param decomposition the decomposition to find the sequences of unknown filled squares in
	 * @return a list of {@link UnknownSequence} instances, ordered in left-to-right decomposition traversal order.
	 */
	protected List<UnknownSequence> getUnknownRunSequences(RowDecomposition decomposition){
		
		List<UnknownSequence> boundaries = new ArrayList<UnknownSequence>();
		
		int decompositionLength = decomposition.getTotalLength();
		Orientation orientation = decomposition.getRow().getOrientation();
		
		int sequenceCount = decomposition.getSequenceCount();
		int globalOffset = 0; // global index offset to apply due to the sequence number we're in
		
		// finds sequences of filled, unknown-run squares in a sequence-wise fashion
		// this is necessary because if you didn't do it sequence-per-sequence (for example, using a single advancing global index),
		// then you would simply cross sequence borders every time two adjacent sequences happen to have filled but run-unknown squares
		// on their edges. this is not what you want, because they're 2 separate sequences (this is a decomposition -- every sequence-crossing
		// implies one or more separating whitespace squares)
		
		for(int sequenceIndex = 0; sequenceIndex < sequenceCount; sequenceIndex++){
			
			Sequence sequence = decomposition.getSequence(sequenceIndex);
			int sequenceLength = decomposition.getSequenceLength(sequenceIndex);
			
			// find sequences of filled, unknown-run squares within this sequence (but in global coordinates) (!)
			
			int startIndex = globalOffset;
			while(startIndex < globalOffset + sequenceLength){
				
				Square startSquare = decomposition.getSquare(startIndex);
				if(startSquare.isFilled() && !startSquare.hasRun(orientation)){
					
					// found a starting index, now scan to the right to find the ending index
					int endIndexLocal = startIndex;
					while(endIndexLocal < globalOffset + sequenceLength){
						
						Square endSquare = decomposition.getSquare(endIndexLocal);
						if(!(endSquare.isFilled() && !endSquare.hasRun(orientation))){
							break; // found end of the sequence
						}
						
						endIndexLocal++;
						
					}
					
					boundaries.add(new UnknownSequence(startIndex, endIndexLocal - 1, sequence));
					startIndex = endIndexLocal; // skip separator square (must be there, otherwise the loop wouldn't have exited)
					
				}
				
				startIndex++;
				
			}
			
			globalOffset += sequenceLength; // moving on to next sequence, update the offset
			
		}
		
		return boundaries;
		
	}
	
	/**
	 * Represents a sequence of filled squares (but without a known run) within a row decomposition.
	 * 
	 * @author Jeroen De Ridder
	 */
	private class UnknownSequence {
		
		/**
		 * Global start index of this sequence within the decomposition
		 */
		public final int startIndex;
		
		/**
		 * Global end index (inclusive) of this sequence within the decomposition
		 */
		public final int endIndex;
		
		/**
		 * Possible runs that can be assigned to this sequence.
		 */
		public final List<Run> possibleRuns;
		
		/**
		 * Enclosing decomposition sequence
		 */
		public final Sequence enclosingSequence;
		
		public UnknownSequence(int startIndex, int endIndex, Sequence enclosingSequence) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.enclosingSequence = enclosingSequence;
			this.possibleRuns = new ArrayList<Run>();
		}
		
		public int getLength(){
			return (endIndex - startIndex + 1);
		}
		
		@Override
		public String toString() {
			return "["+startIndex+","+endIndex+"]";
		}
		
	}
	
	/**
	 * Checks whether assigning <tt>candidateRun</tt> to <tt>currentSequence</tt> would leave any other sequences before <tt>currentSequence</tt>
	 * without possible runs that come before <tt>candidateRun</tt>. Returns true if not, false otherwise. 
	 * 
	 * @param sequences
	 * @param currentSequenceIndex
	 * @param candidateRun
	 * @return
	 */
	protected boolean checkValidRunLeft(List<UnknownSequence> sequences, int currentSequenceIndex, Run candidateRun, RowDecomposition decomposition){
		
		boolean valid = true;
		UnknownSequence currentSequence = sequences.get(currentSequenceIndex);
		
		// if any earlier sequence that is too far out is left without possible earlier runs, return false
		for(int i=0; i<currentSequenceIndex; i++){
			
			UnknownSequence earlierSequence = sequences.get(i);
			
			int remainingEarlierRuns = 0;
			
			// check whether this sequence can possible share candidateRun with the current sequence; i.e., whether this sequence is close
			// enough to the current one to share candidateRun with it
			// if so, ignore it, because it might have the same run and therefore it would be wrong to flag candidateRun as "cannot be assigned
			// to currentSequence" because this sequence is left with no runs before it
			//
			// to express this, we artificially increment remainingEarlierRuns -- even though it's technically not an earlier run, it serves
			// to have it counted as one
			
			if(canShareRun(currentSequence, earlierSequence, candidateRun, decomposition)){
				remainingEarlierRuns++;
			}
			
			for(Run possibleRun : earlierSequence.possibleRuns){
				if(possibleRun.getIndex() < candidateRun.getIndex()) remainingEarlierRuns++;
			}
			
			if(remainingEarlierRuns <= 0) return false; // no more earlier runs would remain, not a valid possibility
			
		}
		
		return valid;
		
	}
	
	/**
	 * Checks whether assigning <tt>candidateRun</tt> to <tt>currentSequence</tt> would leave any other sequences after <tt>currentSequence</tt>
	 * without possible runs that come after <tt>candidateRun</tt>. Returns true if not, false otherwise. 
	 * 
	 * @param sequences
	 * @param currentSequenceIndex
	 * @param candidateRun
	 * @return
	 */
	protected boolean checkValidRunRight(List<UnknownSequence> sequences, int currentSequenceIndex, Run candidateRun, RowDecomposition decomposition){
		
		boolean valid = true;
		UnknownSequence currentSequence = sequences.get(currentSequenceIndex);
		
		// if any later sequence is left without possible later runs, return false
		for(int i=currentSequenceIndex + 1; i<sequences.size(); i++){
			
			UnknownSequence laterSequence = sequences.get(i);
			
			int remainingLaterRuns = 0;
			
			if(canShareRun(currentSequence, laterSequence, candidateRun, decomposition)){
				remainingLaterRuns++;
			}
			
			for(Run possibleRun : laterSequence.possibleRuns){
				if(possibleRun.getIndex() > candidateRun.getIndex()) remainingLaterRuns++;
			}
			
			if(remainingLaterRuns <= 0) return false; // no more later runs would remain, not a valid possibility
			
		}
		
		return valid;
		
	}
	
	/**
	 * Checks whether <tt>candidateSequence</tt> is close enough to <tt>fixedSequence</tt> to share <tt>run</tt> with it, were <tt>run</tt> to be
	 * assigned to <tt>fixedSequence</tt>.
	 * 
	 * <p>This can only be the case if:
	 * <ul>
	 *     <li><tt>candidateSequence</tt> and <tt>fixedSequence</tt> are both in the same enclosing decomposition sequence
	 * (otherwise they can never share a run)</li>
	 *     <li><tt>candidateSequence</tt> is close enough to <tt>fixedSequence</tt>. More specifically, it needs to be close enough so
	 *     that the distance from <tt>candidateSequence</tt>'s left edge to <tt>fixedSequence</tt>'s right edge does not exceed the run's
	 *     length (assuming that <tt>candidateSequence</tt> is located to the left of <tt>fixedSequence</tt> -- the other case is analogous).
	 *     </li>
	 * </ul></p>
	 * 
	 * <p>Note that this cannot be determined by examining the runs' leftmost and outermost positions (and thus the possibleRuns of each
	 * sequence which are derived from this information), because they do not take positions of filled squares into account.</p>
	 * 
	 * @param candidateSequence
	 * @param fixedSequence
	 * @param run
	 */
	protected boolean canShareRun(UnknownSequence fixedSequence, UnknownSequence candidateSequence, Run run, RowDecomposition decomposition){
		
		if(!candidateSequence.enclosingSequence.equals(fixedSequence.enclosingSequence)) return false;
		
		// length achieved by joining candidateSequence's and fixedSequence's filled squares together as part of the same run
		// this length must not exceed the run's length -- otherwise, the sequences cannot share it
		int joinedLength;
		
		// local decomposition indices of the left and right edges of the joined sequence (used to calculate joinedKnownRunLength with)
		LocalDecompositionIndex localLeft;
		LocalDecompositionIndex localRight;
		
		if(candidateSequence.endIndex < fixedSequence.startIndex){
			
			// candidateSequence is to the left of fixedSequence
			localLeft = decomposition.globalToLocal(candidateSequence.startIndex);
			localRight = decomposition.globalToLocal(fixedSequence.endIndex);
			
		} else if(fixedSequence.endIndex < candidateSequence.startIndex){
			
			// candidateSequence is to the right of fixedSequence
			localLeft = decomposition.globalToLocal(fixedSequence.startIndex);
			localRight = decomposition.globalToLocal(candidateSequence.endIndex);
			
		} else {
			
			// sequences overlap -- should not happen
			throw new InconsistentDecompositionException("Sequence " + candidateSequence + " is neither to the left nor right of " + fixedSequence);
			
		}
		
		// calculate the joined run length
		// since both sequences are at this point known to be part of the same sequence, it is valid to use sequence-local offsets for this
		joinedLength = (localRight.sequenceOffset - localLeft.sequenceOffset + 1);
		
		return (joinedLength <= run.getLength());
		
	}
	
	/**
	 * Bugged; can't deal with an autofilling sequence of filled squares that may occur courtesy of the solver to automatically
	 * extend a run to neighbouring filled squares.
	 * @param row
	 * @throws UnsolvablePuzzleException
	 */
	public void solve_old(Row row) throws UnsolvablePuzzleException {
		
		// find connected sequences of filled squares with unknown run
		// for each "sequence" of the found sequences:
		//     for each "run" of the remaining (ie. uncompleted) runs of length at least the length of "sequence":
		//         check if marking "sequence" as belonging to "run" results in a conflict
		//         if only one non-conflicting run "ncRun" remains, mark "sequence as belonging to "run"
		
		// find connected sequences of filled squares with unknown run
		List<Sequence> sequences = findUnknownRunSequences_old(row);
		
		// sequences found, now check for conflicting assignments to incomplete runs
		
		for(Sequence sequence : sequences){
			
			// get all incomplete runs of sufficient length. Note that we're explicitly including
			// runs for which some squares are already known, even though the sequence at hand has no known run.
			// this is because the sequence might be a satellite of a long run for which a bunch of other squares
			// are already known.
			
			List<Run> incompleteRuns = row.getIncompleteRuns(sequence.getLength());
			
			// hypothetically assign "sequence" to each of the unknown runs and 
			// see which runs are possible assigments
			
			List<Run> possibleRuns = new LinkedList<Run>();
			
			int firstSquareIndex = sequence.getFirstSquareRowIndex();
			int lastSquareIndex = sequence.getLastSquareRowIndex();
			
			// TODO: these numbers are wrong if for example the first square comes right after a cleared square
			int squaresBefore = firstSquareIndex; // amount of squares before this sequence
			int squaresAfter = row.getLength() - lastSquareIndex - 1; // amount of squares after this sequence
			
			for(Run unknownRun : incompleteRuns){
				
				//boolean runPossible = true;
				
				// 1) make sure there is enough room in the row before and after the sequence 
				// for the runs that come before and after "uncompletedRun", respectively
				
				List<Run> runsBefore = row.getRunsBefore(row.getRunIndex(unknownRun));
				List<Run> runsAfter = row.getRunsAfter(row.getRunIndex(unknownRun));
				
				// TODO: use actual information about the cleared decomposition instead of only the bounds check
				int minBeforeRunLength = NonogramUtils.getMinimumRunAssignmentLength(runsBefore); //StandardRow.getMinimumRunAssignmentLength(runsBefore);
				int minAfterRunLength = NonogramUtils.getMinimumRunAssignmentLength(runsAfter); //StandardRow.getMinimumRunAssignmentLength(runsAfter);
				if(runsBefore.size() > 0) minBeforeRunLength += 1; // one extra cleared square between this sequence and the compact solution to runBefore
				if(runsAfter.size() > 0) minAfterRunLength += 1; // one extra cleared square between this sequence and the compact solution to runsAfter
				
				if(minBeforeRunLength > squaresBefore || minAfterRunLength > squaresAfter){
					continue; // no go, try next run
				}
				
				// 2) make sure there is enough actual room for this sequence (ie. within its current
				// boundaries (cleared squares or borders))
				
				Square[] boundaries = NonogramUtils.getBoundaries(sequence);
				
				/*Square leftBoundary = boundaries[0];
				Square rightBoundary = boundaries[1];*/
				//int maxSequenceLength = (rightBoundary - leftBoundary) + 1;
				int maxSequenceLength = NonogramUtils.getMaxSequenceLength(boundaries, row);
				
				if(sequence.getLength() > maxSequenceLength){
					continue;
				}
				
				possibleRuns.add(unknownRun);
				
				
			}
			
			if(possibleRuns.size() == 0){
				throw new UnsolvablePuzzleException("No possible run assignments for sequence " + sequence + " of length " + sequence.getLength());
			}
			
			if(possibleRuns.size() == 1){
				
				// assign sequence to run
				try {
					sequence.assignRun(possibleRuns.get(0));
				}
				catch(RunLengthExceededException e) {
					throw new UnsolvablePuzzleException(e);
				}
				catch(ConflictingSquareRunException e) {
					throw new UnsolvablePuzzleException(e);
				}
				
			} else {
				
				// 1) if the maximum run length of the row equals this sequence's length, then we
				// can be sure that this sequence is some completed run (we just don't know which one yet)
				// therefore, we can demarcate it.
				
				int rowMaxRunLength = row.getMaximumRunLength();
				if(sequence.getLength() == rowMaxRunLength){
					
					int squareIndexBefore = firstSquareIndex - 1;
					int squareIndexAfter = lastSquareIndex + 1;
					
					// demarcate sequence
					try {
						if(squareIndexBefore >= 0) row.clearSquares(squareIndexBefore, squareIndexBefore);
						if(squareIndexAfter < row.getLength()) row.clearSquares(squareIndexAfter, squareIndexAfter);
					}
					catch(ConflictingSquareStateException asex){
						throw new UnsolvablePuzzleException(asex);
					}
					
				} else if(sequence.getLength() > rowMaxRunLength){
					throw new UnsolvablePuzzleException("Found contiguous filled sequence of length " + sequence.getLength() + " in row with maximum run length " + rowMaxRunLength);
				}
				
			}
			
		}
		
	}
	
	/**
	 * Finds contiguous sequences of filled squares with unknown run within the provided row.
	 */
	protected static List<Sequence> findUnknownRunSequences_old(Row row){
		
		int rowLength = row.getLength();
		Orientation rowOrientation = row.getOrientation();
		List<Sequence> sequences = new ArrayList<Sequence>();
		
		int i=0;
		while(i < rowLength){
			
			Square square = row.getSquare(i);
			
			boolean squareFilled = square.isFilled();
			boolean runKnown = (square.getRun(rowOrientation) != null);
			
			if(squareFilled && !runKnown){
				
				// found starting square of a sequence, scan the row and construct the sequence
				Sequence sequence = new StandardSequence(row, square);
				
				int s = i+1;
				for(; s<row.getLength(); s++){
					
					Square scannedSquare = row.getSquare(s);
					
					boolean scannedSquareFilled = scannedSquare.isFilled();
					boolean scannedSquareRunKnown = (scannedSquare.getRun(rowOrientation) != null);
					
					if(scannedSquareFilled && !scannedSquareRunKnown){
						sequence.addSquare(scannedSquare);
					} else {
						break; // end of sequence
					}
					
				}
				
				sequences.add(sequence);
				i = s + 1; // skip the last scanned square (the one that ended the sequence)
				
			} else {
				
				i++;
				
			}
			
		}
		
		return sequences;
		
	}
	
}
