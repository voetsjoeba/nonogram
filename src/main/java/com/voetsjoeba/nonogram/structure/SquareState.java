package com.voetsjoeba.nonogram.structure;

import com.voetsjoeba.nonogram.structure.api.Square;

/**
 * Possible solution states a {@link Square} can be in.
 */
public enum SquareState {
	
	/**
	 * The square is determined to be filled.
	 */
	FILLED,
	
	/**
	 * The square is determined to be empty.
	 */
	CLEAR;
	
}