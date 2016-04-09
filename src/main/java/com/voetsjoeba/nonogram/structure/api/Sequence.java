package com.voetsjoeba.nonogram.structure.api;

import java.util.List;

import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.NoKnownSquaresException;
import com.voetsjoeba.nonogram.exception.NoSuchSquareException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.structure.SquareState;

/**
 * A contiguous sequence of squares within a particular {@link Row}.
 * 
 * @author Jeroen De Ridder
 */
// TODO: consider making a subclass of Sequence to represent "working" sequences, i.e. sequences that are made up entirely
// out of filled or unknown squares, but that do not consist entirely of filled squares
public interface Sequence {
	
	/**
	 * Returns the {@link Row} that contains this sequence.
	 */
	public Row getRow();
	
	/**
	 * Returns the length of this sequence.
	 */
	public int getLength();
	
	/**
	 * Returns the first square in this sequence.
	 */
	public Square getFirstSquare();
	
	/**
	 * Returns the last square in this sequence.
	 */
	public Square getLastSquare();
	
	/**
	 * Returns the index of the first filled square within this sequence.
	 * @throws NoKnownSquaresException if there are no known squares within this sequence
	 */
	public int getFirstFilledSquareIndex() throws NoSuchSquareException;
	
	/**
	 * Returns the index of the last filled square within this sequence.
	 * @throws NoKnownSquaresException if there are no known squares within this sequence
	 */
	public int getLastFilledSquareIndex() throws NoSuchSquareException;
	
	/**
	 * Returns the absolute index of the first square in this sequence within its row.
	 */
	public int getFirstSquareRowIndex();
	
	/**
	 * Returns the absolute index of the last square in this sequence within its row.
	 */
	public int getLastSquareRowIndex();
	
	/**
	 * Returns the squares in sequence.
	 */
	public List<Square> getSquares();
	
	/**
	 * Returns the square at the given position within this sequence.
	 */
	public Square getSquare(int index);
	
	/**
	 * Clears all squares in this sequence.
	 */
	public void clear() throws ConflictingSquareStateException;
	
	/**
	 * Returns true if all squares in this sequence are cleared, false otherwise.
	 */
	public boolean isCleared();
	
	/**
	 * Returns true if all squares in this sequence are filled, false otherwise.
	 */
	public boolean isFilled();
	
	/**
	 * Returns true if this sequence contains any squares whose state is known, false otherwise.
	 */
	public boolean containsKnownSquares();
	
	/**
	 * Returns true if this sequence contains any squares whose state is {@link SquareState#FILLED}, false otherwise.
	 */
	public boolean containsFilledSquares();
	
	/**
	 * Returns true if this sequence contains <tt>square</tt>, false otherwise.
	 */
	public boolean contains(Square square);
	
	/**
	 * Returns the index of the provided square within this sequence.
	 * @throws NoSuchSquareException if the provided square does not exist within this sequence
	 */
	public int getSquareIndex(Square square) throws NoSuchSquareException;
	
	/**
	 * Adds a square to the sequence.
	 * @throws IllegalArgumentException if the provided square does not contiguously extend the sequence
	 */
	public void addSquare(Square square);
	
	/**
	 * Assigns a {@link Run} to all squares in this sequence.
	 * @throws RunLengthExceededException if assigning all squares in this sequence to the provided run causes the run to exceed its length
	 * @throws ConflictingSquareRunException if assigning all squares in this sequence to the provided run causes a square to receive conflicting run information
	 */
	public void assignRun(Run run) throws RunLengthExceededException, ConflictingSquareRunException;
	
	/**
	 * Returns true if this sequence contains a single complete Run (i.e. if all squares are filled and have the same run assigned),
	 * false otherwise.
	 */
	public boolean containsSingleCompleteRun();
	
	/**
	 * Returns the single complete Run that encompasses the entire sequence, or null if no such run exists.
	 */
	public Run getSingleCompleteRun();
	
	/**
	 * Returns a list of completed runs contained in this sequence.
	 */
	public List<Run> getCompletedRuns();
	
	/**
	 * Clears squares with unknown state from the left and removes them until a square with known state or the end of the sequence is reached.
	 */
	public void trimLeft() throws ConflictingSquareStateException;
	
	/**
	 * Clears squares with unknown state from the right and removes them until a square with known state or the end of the sequence is reached.
	 */
	public void trimRight() throws ConflictingSquareStateException;
	
	/**
	 * Returns the amount of squares in this sequence whose state is known.
	 */
	public int getKnownSquareCount();
	
}