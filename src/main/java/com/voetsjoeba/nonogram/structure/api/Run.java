package com.voetsjoeba.nonogram.structure.api;

import java.util.List;

import com.voetsjoeba.nonogram.exception.NoKnownSquaresException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.exception.UncontiguousRunException;
import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.StandardSquare;

/**
 * Represents a single run of squares within a {@link Row}, as defined by the {@link Puzzle} problem to be solved.
 * 
 * @author Jeroen De Ridder
 */
public interface Run {
	
	/**
	 * Adds a square to this run.
	 * @throws RunLengthExceededException if the run is already complete.
	 */
	public void addSquare(StandardSquare square) throws RunLengthExceededException;
	
	/**
	 * Returns the total length of this run.
	 */
	public int getLength();
	
	/**
	 * Returns the {@link Row} to which this run belongs.
	 */
	public Row getRow();
	
	/**
	 * Returns this run's absolute index amongst the runs of its enclosing row.
	 */
	public int getIndex();
	
	/**
	 * Returns the orientation of this run. This orientation is always the same as the orientation of the row to which
	 * this run belongs.
	 */
	public Orientation getOrientation();
	
	/**
	 * Returns the first known square in this run.
	 */
	public Square getFirstKnownSquare() throws NoKnownSquaresException;
	
	/**
	 * Returns the last known square in this run. Since this square is known to be part of this run, it
	 * will necessarily be filled and have this run assigned.
	 */
	public Square getLastKnownSquare() throws NoKnownSquaresException;
	
	/**
	 * Returns true if any square is known to belong to this run, false otherwise.
	 */
	public boolean hasKnownSquares();
	
	/**
	 * Returns the set of known squares in this run. The returned list is ordered first square to last.
	 */
	public List<Square> getKnownSquares();
	
	/**
	 * Returns the amount of squares that are known to belong to this run.
	 */
	public int getKnownSquareCount();
	
	/**
	 * Returns true if this run is complete (ie. if exactly <i>length</i> squares are known to belong to this run), false otherwise.
	 */
	public boolean isComplete();
	
	/**
	 * Returns true if the known squares in this run form a contiguous sequence, false otherwise. If no squares are currently known to belong to this run, true is returned.
	 */
	public boolean isContiguous();
	
	/**
	 * Returns this run as a {@link Sequence}.
	 * @throws UncontiguousRunException if the run is not contiguous and can therefore not be converted to a {@link Sequence}
	 */
	public Sequence toSequence() throws UncontiguousRunException ;
	
}