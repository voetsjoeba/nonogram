package com.voetsjoeba.nonogram.structure.api;

import java.util.Comparator;
import java.util.List;

import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.NoIncompleteRunsException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.StandardSquare;

/**
 * Generic row of squares (not necessarily related to the orientation "row" as opposed to "column").
 * 
 * @author Jeroen De Ridder
 */
public interface Row extends Comparator<Square>, RunContainer {
	
	public int getIndex();
	public Orientation getOrientation();
	
	/**
	 * Returns the {@link StandardSquare} at the provided index within this row.
	 */
	public Square getSquare(int index);
	
	/**
	 * Returns the index of the given {@link StandardSquare} within this row.
	 * @throws IllegalArgumentException if the provided {@link StandardSquare} does not belong to this row.
	 */
	public int getSquareIndex(Square square);
	
	public int getLength();
	
	public RowDecomposition getDecomposition();
	
	/**
	 * Returns the amount of changes that were made (changing a square's state or run) since the beginning of the object's
	 * lifespan, or since the last time resetModificationCount was called.
	 * @return
	 */
	public int getModificationCount();
	
	/**
	 * Resets the modification counter to 0.
	 */
	public void resetModificationCount();
	
	/**
	 * Returns the amount of squares with a known state in this row.
	 */
	public int getKnownSquareCount();
	
	// ------ RUNS ---------------------------------------------------------------------
	
	public int getRunIndex(Run run);
	
	/**
	 * Returns the largest run length of all runs present in this row.
	 */
	public int getMaximumRunLength();
	
	/**
	 * Returns the smallest run length of all runs present in this row.
	 */
	public int getMinimumRunLength();
	
	/**
	 * Returns the largest run length of all incomplete runs in this row.
	 * @throws NoIncompleteRunsException if there are no incomplete runs in this row
	 */
	public int getMaximumIncompleteRunLength();
	
	/**
	 * Returns the smallest run length of all incomplete runs in this row.
	 * @throws NoIncompleteRunsException if there are no incomplete runs in this row
	 */
	public int getMinimumIncompleteRunLength();
	
	/**
	 * Returns a list of runs (of minimum length minLength) whose amount of known squares does not equal their length.
	 */
	public List<Run> getIncompleteRuns(int minLength);
	
	/**
	 * Returns a list of runs whose amount of known squares does not equal their length.
	 */
	public List<Run> getIncompleteRuns();
	
	/**
	 * Returns a list of runs for which no squares are known.
	 */
	public List<Run> getUnknownRuns(int minLength);
	
	/**
	 * Returns a list of runs for which no squares are known (in order of position within this row).
	 */
	public List<Run> getUnknownRuns();
	
	// ------ UTILITIES ---------------------------------------------------------------------
	
	/**
	 * Returns the minimal length for a solution to this row.
	 */
	public int getMinimumSolutionLength();
	
	/**
	 * Returns whether or not this row has been completely determined.
	 */
	public boolean isCompleted();
	
	/**
	 * Clears all squares from <tt>startIndex</tt> up to and including <tt>endIndex</tt>.
	 * 
	 * @throws ConflictingSquareStateException if clearing the squares causes any of them to raise an {@link ConflictingSquareStateException}.
	 */
	public void clearSquares(int startIndex, int endIndex) throws ConflictingSquareStateException;
	
	/**
	 * Fills all squares from <tt>startIndex</tt> up to and including <tt>endIndex</tt> in <tt>row</tt>. Does not assign a run to any of the filled squares.
	 * 
	 * @throws ConflictingSquareStateException if filling the squares causes any of them to raise an {@link ConflictingSquareStateException}.
	 */
	public void fillSquares(int startIndex, int endIndex) throws ConflictingSquareStateException;
	
	/**
	 * Fills all squares from <tt>startIndex</tt> up to and including <tt>endIndex</tt>, and assigns the provided run to every one of them.
	 * 
	 * @throws ConflictingSquareStateException if filling the squares causes any of them to receive conflicting state information
	 * @throws ConflictingSquareRunException if assigning the run to the squares causes any of them to receive conflicting run information
	 * @throws RunLengthExceededException if assigning the run to the squares causes too many squares to have been registered to the run
	 */
	public void fillSquares(int startIndex, int endIndex, Run run) throws ConflictingSquareStateException, ConflictingSquareRunException, RunLengthExceededException;
	
}