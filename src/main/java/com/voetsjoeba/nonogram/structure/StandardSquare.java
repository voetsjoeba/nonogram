package com.voetsjoeba.nonogram.structure;

import java.util.HashMap;
import java.util.Map;

import javax.swing.event.EventListenerList;

import com.voetsjoeba.nonogram.event.SquareRunSetEvent;
import com.voetsjoeba.nonogram.event.SquareRunSetListener;
import com.voetsjoeba.nonogram.event.SquareStateSetEvent;
import com.voetsjoeba.nonogram.event.SquareStateSetListener;
import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.structure.api.Puzzle;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Square;

/**
 * A single square in a nonogram {@link Puzzle}.
 * 
 * @author Jeroen De Ridder
 */
public class StandardSquare implements Square {
	
	private SquareState state;
	private final int row;
	private final int column;
	
	private Map<Orientation, Run> runs;
	private EventListenerList eventListeners;
	
	public StandardSquare(int row, int column){
		this.row = row;
		this.column = column;
		init();
	}
	
	private void init(){
		state = null;
		runs = new HashMap<Orientation, Run>();
		eventListeners = new EventListenerList();
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}
	
	/**
	 * Returns whether the state of this square is known. 
	 */
	public boolean isStateKnown(){
		return (state != null);
	}
	
	public boolean isFilled(){
		return (state == SquareState.FILLED);
	}
	
	public boolean isCleared(){
		return (state == SquareState.CLEAR);
	}
	
	public String toString() {
		return "("+column+","+row+";"+state+")";
	}
	
	/**
	 * Changes the state of this square.
	 * If this square's state was already known but is now being switched to a different known state, an {@link ConflictingSquareStateException} is thrown.
	 * @param newState
	 * @throws ConflictingSquareStateException
	 */
	public void setState(SquareState newState) throws ConflictingSquareStateException {
		
		if(state != null){
			
			if(state == newState){
				return; // ok, no change
			} else {
				throw new ConflictingSquareStateException("Could not change square("+column+","+row+")'s status to " + newState + ", was already set to " + this.state);
			}
			
		}
		
		state = newState;
		fireStateSet();
		
	}
	
	/**
	 * Returns the {@link SquareState} of this square, or null if the state is unknown.
	 */
	public SquareState getState(){
		return state;
	}
	
	private void fireStateSet(){
		for(SquareStateSetListener listener : eventListeners.getListeners(SquareStateSetListener.class)){
			listener.squareStateSet(new SquareStateSetEvent(this));
		}
	}
	
	private void fireRunSet(Orientation orientation){
		for(SquareRunSetListener listener : eventListeners.getListeners(SquareRunSetListener.class)){
			listener.runSet(new SquareRunSetEvent(this, orientation));
		}
	}
	
	public void addStateSetListener(SquareStateSetListener listener){
		eventListeners.add(SquareStateSetListener.class, listener);
	}
	
	public void addRunSetListener(SquareRunSetListener listener){
		eventListeners.add(SquareRunSetListener.class, listener);
	}
	
	public void removeRunSetListener(SquareRunSetListener listener) {
		eventListeners.remove(SquareRunSetListener.class, listener);
	}
	
	public void removeStateSetListener(SquareStateSetListener listener) {
		eventListeners.remove(SquareStateSetListener.class, listener);
	}
	
	/**
	 * Registers a {@link Run} to this {@link StandardSquare}.
	 * @throws RunLengthExceededException if the run to be registered is already complete (see {@link Run#addSquare(StandardSquare)}) 
	 * @throws ConflictingSquareRunException if this Square already has a {@link Run} previously assigned for the provided {@link Run}'s {@link Orientation}.
	 * @throws IllegalStateException if this {@link StandardSquare}'s status is not currently {@link SquareState#FILLED}.
	 */
	public void setRun(Run run) throws RunLengthExceededException, ConflictingSquareRunException {
		
		if(run == null) return;
		
		if(state != SquareState.FILLED){
			throw new IllegalStateException("Cannot register " + run.getOrientation() + " run " + run + " for square(" + row + "," + column + ") with state " + state + "; state must be " + SquareState.FILLED);
		}
		
		Run existingRun = runs.get(run.getOrientation());
		if(existingRun != null){
			
			if(run == existingRun){
				return; // ok, no change
			} else {
				throw new ConflictingSquareRunException("Cannot register " + run.getOrientation() + " run " + run + " for square(" + row + "," + column + ") with state " + state + "; a different run is already assigned");
			}
			
		}
		
		runs.put(run.getOrientation(), run);
		run.addSquare(this);
		
		fireRunSet(run.getOrientation());
		
	}
	
	public Run getRun(Orientation orientation){
		return runs.get(orientation);
	}
	
	public boolean hasRun(Orientation orientation){
		return runs.containsKey(orientation);
	}
	
}
