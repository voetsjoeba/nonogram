package com.voetsjoeba.nonogram.structure.api;

import java.util.List;

import com.voetsjoeba.nonogram.structure.StandardSquare;

/**
 * Represents a nonogram Puzzle.
 * 
 * @author Jeroen De Ridder
 */
public interface Puzzle {
	
	/**
	 * Returns all row {@link Row}s in this puzzle. The returned list is an unmodifiable view of the underlying row list.
	 */
	public List<Row> getRows();
	
	/**
	 * Returns all column {@link Row}s in this puzzle. The returned list is an unmodifiable view of the underlying row list.
	 */
	public List<Row> getColumns();
	
	/**
	 * Returns the {@link Row} corresponding to the provided row index.
	 */
	public Row getRow(int index);
	
	/**
	 * Returns the {@link Row} corresponding to the provided column index.
	 */
	public Row getColumn(int index);
	
	/**
	 * Returns the {@link StandardSquare} at the provided column and row.
	 */
	public Square getSquare(int column, int row);
	
	/**
	 * Returns the largest amount of runs any row in the puzzle has.
	 */
	public int getMaxRowRunCount();
	
	/**
	 * Returns the largest amount of runs any column in the puzzle has.
	 */
	public int getMaxColumnRunCount();
	
	/**
	 * Returns the total amount of squares in this puzzle.
	 */
	public int getSquareCount();
	
	/**
	 * Returns the total amount of squares in this puzzle whose state is known.
	 */
	public int getKnownSquareCount();
	
	/**
	 * Returns true if this puzzle is completed (ie. if all of its squares' states are known), false otherwise.
	 */
	public boolean isComplete();
	
}