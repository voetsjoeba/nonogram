package com.voetsjoeba.nonogram.event;

import java.util.EventObject;

import com.voetsjoeba.nonogram.structure.api.Square;

/**
 * Indicates that a {@link Square}'s state was changed (or initially set).
 * 
 * @author Jeroen De Ridder
 */
public class SquareStateSetEvent extends EventObject {

	public SquareStateSetEvent(Square source) {
		super(source);
	}
	
	@Override
	public Square getSource() {
		return (Square) super.getSource();
	}
	
}
