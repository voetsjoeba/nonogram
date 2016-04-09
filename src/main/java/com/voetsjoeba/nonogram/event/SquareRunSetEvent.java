package com.voetsjoeba.nonogram.event;

import java.util.EventObject;

import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Square;

/**
 * Represents an event where a {@link Square} has had its run set (in either direction). The run that was set may be retrieved by 
 * calling {@link Square#getRun(Orientation)} on the subject square (obtainable through {@link #getSource()}) and passing the result 
 * from {@link #getOrientation()}.
 * 
 * @author Jeroen De Ridder
 */
public class SquareRunSetEvent extends EventObject {
	
	private Orientation orientation;
	
	public SquareRunSetEvent(Square source, Orientation orientation){
		super(source);
		this.orientation = orientation;
	}
	
	/**
	 * Returns the {@link Orientation} for which the run was assigned.
	 */
	public Orientation getOrientation() {
		return orientation;
	}
	
	/**
	 * Returns the square for which the run was set.
	 */
	@Override
	public Square getSource() {
		return (Square) super.getSource();
	}
	
	/**
	 * Gets the run that was assigned.
	 */
	public Run getRun(){
		return getSource().getRun(orientation);
	}
	
}
