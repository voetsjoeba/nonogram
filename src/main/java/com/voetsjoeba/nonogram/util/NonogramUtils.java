package com.voetsjoeba.nonogram.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;

public class NonogramUtils {
	
	private static final Logger log = LoggerFactory.getLogger(NonogramUtils.class);
	
	/**
	 * Returns the minimal amount of squares required to place the provided list of runs consecutively, i.e. this method
	 * returns the smallest amount of squares that can contain the provided list of runs (including obligatory single 
	 * cleared squares inbetween runs).
	 * @see #getMinimumRunAssignmentLength(List, boolean)
	 */
	public static int getMinimumRunAssignmentLength(List<Run> runs){
		return getMinimumRunAssignmentLength(runs, true);		
	}
	
	/**
	 * Returns the minimal amount of squares required to place the provided list of runs consecutively, i.e. this method
	 * returns the smallest amount of squares that can contain the provided list of runs (possibly including obligatory single 
	 * cleared squares inbetween runs).
	 * @param includeWhitespace whether or not to count separating whitespace squares inbetween the runs as part of the minimal assignment length
	 */
	public static int getMinimumRunAssignmentLength(List<Run> runs, boolean includeWhitespace){
		
		if(runs == null || runs.size() == 0) return 0;
		
		int minLength = 0;
		int runCount = runs.size();
		
		for(int i=0; i<runCount; i++){
			minLength += runs.get(i).getLength();
		}
		if(includeWhitespace) minLength += runs.size() - 1;
		
		return minLength;
		
	}
	
	/**
	 * Wraps a {@link Square#setState(SquareState)} and {@link Square#setRun(Run)} call in a try/catch block that converts any error thrown to an {@link UnsolvablePuzzleException}.
	 * 
	 * @param square
	 * @param state
	 * @param run
	 * @throws UnsolvablePuzzleException
	 * 
	 * TODO: this thing shouldn't even be necessary
	 */
	public static void setSquareState(Square square, SquareState state, Run run) throws UnsolvablePuzzleException {
		
		try {
			square.setState(state);
			square.setRun(run);
		}
		catch(ConflictingSquareStateException e) {
			throw new UnsolvablePuzzleException(e);
		}
		catch(ConflictingSquareRunException e) {
			throw new UnsolvablePuzzleException(e);
		}
		catch(RunLengthExceededException ex){
			throw new UnsolvablePuzzleException(ex);
		}
		
	}
	
	/**
	 * Calls {@link #setSquareState(Square, SquareState, Run)} for all squares in <tt>sequence</tt> starting from <tt>startIndex</tt> up to and including <tt>endIndex</tt>.
	 * 
	 * @param sequence
	 * @param startIndex
	 * @param endIndex
	 * @param state
	 * @param run
	 * @throws UnsolvablePuzzleException
	 */
	public static void setSquareState(Sequence sequence, int startIndex, int endIndex, SquareState state, Run run) throws UnsolvablePuzzleException {
		
		for(int i=startIndex; i <= endIndex; i++){
			setSquareState(sequence.getSquare(i), state, run);
		}
		
	}
	
	/**
	 * Calls {@link #setSquareState(Square, SquareState, Run)} for all squares in <tt>row</tt> starting from <tt>startIndex</tt> up to and including <tt>endIndex</tt>.
	 * 
	 * @param row
	 * @param startIndex
	 * @param endIndex
	 * @param state
	 * @param run
	 * @throws UnsolvablePuzzleException
	 */
	public static void setSquareState(Row row, int startIndex, int endIndex, SquareState state, Run run) throws UnsolvablePuzzleException {
		
		for(int i=startIndex; i <= endIndex; i++){
			setSquareState(row.getSquare(i), state, run);
		}
		
	}
	
	/**
	 * Finds the current boundaries of a sequence. Boundaries are constituted by a cleared square or the edge of the field.
	 * The returned values are the first square right after the left boundary and the last square right before the right boundary.
	 */
	public static Square[] getBoundaries(Sequence sequence){
		
		Row row = sequence.getRow();
		int firstSquareIndex = row.getSquareIndex(sequence.getFirstSquare());
		int lastSquareIndex = row.getSquareIndex(sequence.getLastSquare());
		
		int leftBoundaryInclusive = firstSquareIndex;
		int rightBoundaryInclusive = lastSquareIndex;
		
		// find left boundary
		while(leftBoundaryInclusive >= 0){
			
			int newBoundary = leftBoundaryInclusive - 1;
			if(newBoundary < 0) break;
			
			Square scannedSquare = row.getSquare(newBoundary);
			if(scannedSquare.isCleared()) break;
			
			leftBoundaryInclusive = newBoundary;
			
		}
		
		// find right boundary
		while(rightBoundaryInclusive <= row.getLength() - 1){
			
			int newBoundary = rightBoundaryInclusive + 1;
			if(newBoundary > row.getLength() - 1) break;
			
			Square scannedSquare = row.getSquare(newBoundary);
			if(scannedSquare.isCleared()) break;
			
			rightBoundaryInclusive = newBoundary;
			
		}
		
		return new Square[]{row.getSquare(leftBoundaryInclusive), row.getSquare(rightBoundaryInclusive)};
		
	}
	
	public static int getMaxSequenceLength(Square[] boundaries, Row row){
		return (row.getSquareIndex(boundaries[1]) - row.getSquareIndex(boundaries[0])) + 1; // right boundary - left boundary + 1
	}
	
	/**
	 * Bounces <tt>runLength</tt> to the left of the provided seed square in <tt>sequence</tt>, if applicable (i.e. if the seed square is 
	 * closes enough to the left sequence edge). If a run to be set for the bounced squares is known, it may be passed it as <tt>run</tt> 
	 * to have it assigned to the bounced squares -- otherwise, leave it null.
	 * 
	 * @param sequence
	 * @param seedSquareIndex
	 * @param runLength
	 * @param run
	 */
	public static void bounceLeft(Sequence sequence, int seedSquareIndex, int runLength, Run run){
		
		// runLength - 1 is the point up to where the run should be filled, but only if the seedSquareIndex is close enough
		// to the left edge (and since the left edge is index 0, this means that seedSquareIndex must be <= runLength - 1)
		// 
		// For example, for a run of length 5:
		// 
		//   0   1   2   3   4   5   6
		// +---+---+---+---+---+---+---+--
		// |   |   | x |   |   |   |   | ...
		// +---+---+---+---+---+---+---+--
		//                   ^
		//                   runLength - 1: the point up to where the bounce needs to be filled
		
		if(seedSquareIndex < runLength){
			NonogramUtils.setSquareState(sequence, seedSquareIndex, runLength - 1, SquareState.FILLED, run);
		}
		
	}
	
	/**
	 * Bounces <tt>runLength</tt> to the right of the provided seed square in <tt>sequence</tt>, if applicable (i.e. if the seed square is 
	 * closes enough to the right sequence edge). If a run to be set for the bounced squares is known, it may be passed it as <tt>run</tt> 
	 * to have it assigned to the bounced squares -- otherwise, leave it null.
	 * 
	 * @param sequence
	 * @param seedSquareIndex
	 * @param runLength
	 * @param run
	 */
	public static void bounceRight(Sequence sequence, int seedSquareIndex, int runLength, Run run) {
		
		int minBounceIndex = sequence.getLength() - runLength; // smallest index seedSquareIndex must reach to be eligible for bouncing
		if(seedSquareIndex >= minBounceIndex){
			NonogramUtils.setSquareState(sequence, minBounceIndex, seedSquareIndex, SquareState.FILLED, run);
		}
		
	}
	
	/**
	 * Given a seed (starting index and length), mercuries <tt>runLength</tt> from the left edge of <tt>sequence</tt>, if applicable (i.e.
	 * if the seed is close enough to the left edge). 
	 * 
	 * @param sequence
	 * @param runLength
	 * @param seedSquareIndex
	 * @param seedLength
	 */
	public static void mercuryLeft(Sequence sequence, int runLength, int seedSquareIndex, int seedLength){
		
		if(seedSquareIndex <= runLength){
			int mercuryLength = seedSquareIndex - runLength + seedLength; // you do the math
			NonogramUtils.setSquareState(sequence, 0, mercuryLength - 1, SquareState.CLEAR, null);
		}
		
	}
	
	/**
	 * Same as {@link #mercuryLeft(Sequence, int, int, int)}, except that this version will first calculate the seed length by expanding to
	 * the right of the seed square to look for more filled squares until it reaches a non-filled square. If you already know the seed length
	 * (for example, if the seed has a known run you can ask it for its amount of known squares), then you should use 
	 * {@link #mercuryLeft(Sequence, int, int, int)} directly to avoid recalculating it.
	 * 
	 * 
	 * @param sequence
	 * @param runLength
	 * @param seedSquareIndex
	 */
	public static void mercuryLeft(Sequence sequence, int runLength, int seedSquareIndex){
		
		int seedLength = getFilledSequenceLengthRight(sequence, seedSquareIndex);
		mercuryLeft(sequence, runLength, seedSquareIndex, seedLength);
		
	}
	
	/**
	 * Given a seed (starting index and length), mercuries <tt>runLength</tt> from the right edge of <tt>sequence</tt>, if applicable (i.e.
	 * if the seed is close enough to the right edge). 
	 * 
	 * @param sequence
	 * @param runLength
	 * @param seedSquareIndex
	 * @param seedLength
	 */
	public static void mercuryRight(Sequence sequence, int runLength, int seedSquareIndex, int seedLength){
		
		int sequenceLength = sequence.getLength();
		
		if(seedSquareIndex >= sequenceLength - (runLength + 1)){
			int mercuryLength = ((sequenceLength - runLength) - seedSquareIndex) + (seedLength - 1); // = baseMercury + (seed_length - 1) -- you do the math
			NonogramUtils.setSquareState(sequence, sequenceLength - mercuryLength, sequenceLength - 1, SquareState.CLEAR, null);
		}
		
	}
	
	/**
	 * Same as {@link #mercuryRight(Sequence, int, int, int)}, except that this version will first calculate the seed length by expanding to
	 * the left of the seed square to look for more filled squares until it reaches a non-filled square. If you already know the seed length
	 * (for example, if the seed has a known run you can ask it for its amount of known squares), then you should use 
	 * {@link #mercuryRight(Sequence, int, int, int)} directly to avoid recalculating it.
	 * 
	 * @param sequence
	 * @param runLength
	 * @param seedSquareIndex
	 */
	public static void mercuryRight(Sequence sequence, int runLength, int seedSquareIndex){
		
		int seedLength = getFilledSequenceLengthLeft(sequence, seedSquareIndex);
		mercuryRight(sequence, runLength, seedSquareIndex, seedLength);
		
	}
	
	/**
	 * Finds the length of the contiguous sequence of filled squares within <tt>sequence</tt> starting with the square at startingIndex
	 * and searching to the right.
	 * 
	 * @param sequence
	 * @param startingIndex
	 */
	public static int getFilledSequenceLengthRight(Sequence sequence, int startingIndex){
		
		int endIndex = startingIndex;
		while(endIndex < sequence.getLength() && sequence.getSquare(endIndex).isFilled()){
			endIndex++;
		}
		
		return (endIndex - startingIndex);
		
	}
	
	/**
	 * Finds the length of the contiguous sequence of filled squares within <tt>sequence</tt> starting with the square at startingIndex
	 * and searching to the left.
	 * 
	 * @param sequence
	 * @param startingIndex
	 */
	public static int getFilledSequenceLengthLeft(Sequence sequence, int startingIndex){
		
		int endIndex = startingIndex;
		while(endIndex >= 0 && sequence.getSquare(endIndex).isFilled()){
			endIndex--;
		}
		
		//return (endIndex - startingIndex);
		return (startingIndex - endIndex);
		
	}
	
	/**
	 * Returns the working runs of <tt>row</tt>. That is, returns all runs inbetween and including the first and last
	 * incomplete runs. In other words, returns all runs except edge completed runs.
	 * @param row
	 */
	public static List<Run> getWorkingRuns(Row row){
		
		List<Run> allRuns = row.getRuns();
		
		// find first incomplete run index
		int firstIncompleteRunIndex = 0;
		int lastIncompleteRunIndex = allRuns.size() - 1;
		while(allRuns.get(firstIncompleteRunIndex).isComplete()) firstIncompleteRunIndex++;
		while(allRuns.get(lastIncompleteRunIndex).isComplete()) lastIncompleteRunIndex--;
		
		List<Run> workingRuns = allRuns.subList(firstIncompleteRunIndex, lastIncompleteRunIndex+1); // +1 because second param is exclusive
		return workingRuns;
		
	}
	
	/**
	 * Determines whether the interval <tt>[left1, right1[</tt> overlaps <tt>[left2, right2[</tt> entirely (i.e. all of interval 2 is covered by interval 1).
	 * Returns true if so, false otherwise.
	 * 
	 * <p>Note that this does not imply the reverse; just because interval 1 overlaps interval 2 entirely, does not mean that interval 2 also
	 * overlaps interval 1 entirely (this is only true if both intervals are equal).</p>
	 * 
	 * @param intervalLeft1
	 * @param intervalRight1
	 * @param intervalLeft2
	 * @param intervalRight2
	 */
	public static boolean intervalOverlapsEntirely(int intervalLeft1, int intervalRight1, int intervalLeft2, int intervalRight2){
		
		// there are four ways an interval X can (entirely) overlap another interval A:
		// 
		// - X's left edge (strictly) preceeds A's left edge and:
		//       o X's right edge coincides with A's right edge
		//       o X's right edge surpasses A's right edge
		// - X's left edge coicides with A's left edge and:
		//       o X's right edge coincides with A's right edge
		//       o X's right edge surpasses A's right edge
		
		return (intervalLeft1 <= intervalLeft2 && intervalRight1 >= intervalRight2);
		
	}
	
	
}
