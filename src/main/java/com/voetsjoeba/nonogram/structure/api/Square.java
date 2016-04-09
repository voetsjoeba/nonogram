package com.voetsjoeba.nonogram.structure.api;

import com.voetsjoeba.nonogram.event.SquareRunSetListener;
import com.voetsjoeba.nonogram.event.SquareStateSetListener;
import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.StandardSquare;

public interface Square {
	
	public int getRow();
	
	public int getColumn();
	
	/**
	 * Returns whether the state of this square is known. 
	 */
	public boolean isStateKnown();
	
	public boolean isFilled();
	
	public boolean isCleared();
	
	/**
	 * Changes the state of this square.
	 * If this square's state was already known but is now being switched to a different known state, an {@link ConflictingSquareStateException} is thrown.
	 * @param newState
	 * @throws ConflictingSquareStateException
	 */
	public void setState(SquareState newState) throws ConflictingSquareStateException;
	
	/**
	 * Returns the {@link SquareState} of this square, or null if the state is unknown.
	 */
	public SquareState getState();
	
	public void addStateSetListener(SquareStateSetListener listener);
	
	public void addRunSetListener(SquareRunSetListener listener);
	
	public void removeStateSetListener(SquareStateSetListener listener);
	
	public void removeRunSetListener(SquareRunSetListener listener);
	
	/**
	 * Registers a {@link Run} to this {@link StandardSquare}.
	 * @throws RunLengthExceededException if the run to be registered is already complete (see {@link Run#addSquare(StandardSquare)}) 
	 * @throws ConflictingSquareRunException if this Square already has a {@link Run} previously assigned for the provided {@link Run}'s {@link Orientation}.
	 * @throws IllegalStateException if this {@link StandardSquare}'s status is not currently {@link SquareState#FILLED}.
	 */
	public void setRun(Run run) throws RunLengthExceededException, ConflictingSquareRunException;
	
	public Run getRun(Orientation orientation);
	
	public boolean hasRun(Orientation orientation);
	
}