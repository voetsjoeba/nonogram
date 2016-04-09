package com.voetsjoeba.nonogram.event;

import java.util.EventListener;

/**
 * Defines the listener interface for {@link SquareRunSetEvent}s.
 * 
 * @author Jeroen De Ridder
 */
public interface SquareRunSetListener extends EventListener {
	
	/**
	 * Called when a run is set for a particular square.
	 */
	public void runSet(SquareRunSetEvent e);
	
}
