package com.voetsjoeba.nonogram.ui;

import java.awt.Color;

import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.api.Square;

/**
 * Wraps a Square for GUI display.
 * 
 * @author Jeroen De Ridder
 */
public class StandardUiSquare {
	
	private static final Color filledColor = Color.DARK_GRAY;
	private static final Color clearColor = Color.WHITE;
	private static final Color unknownColor = Color.decode("0xD2D2D2");
	
	/*private Square square;
	
	public UiSquare(Square square){
		this.square = square;
	}*/
	
	public static Color getColor(Square square){
		
		SquareState squareState = square.getState();
		
		if(squareState == null){
			return unknownColor;
		} else if(squareState == SquareState.FILLED){
			return filledColor;
		} else if(squareState == SquareState.CLEAR){
			return clearColor;
		}
		
		return null;
		
	}
	
}
