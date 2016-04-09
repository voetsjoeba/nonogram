package com.voetsjoeba.nonogram.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.StandardRow;
import com.voetsjoeba.nonogram.structure.StandardSquare;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Square;

public class TestUtils {
	
	public static Row buildRow(String formatString){
		
		int runSeparatorPosition = formatString.indexOf('|');
		String runString = formatString.substring(0, runSeparatorPosition);
		String squareString = formatString.substring(runSeparatorPosition + 1);
		
		// build runs
		
		String[] runLengthStrings = runString.split(",");
		int[] runs = new int[runLengthStrings.length];
		
		//for(String runLengthString : runLengthStrings){
		for(int i=0; i<runLengthStrings.length; i++){
			String runLengthString = runLengthStrings[i];
			runs[i] = Integer.parseInt(runLengthString);
		}
		
		// map to remember assignments of runs to squares
		Map<Square, Integer> squareRuns = new HashMap<Square, Integer>();
		
		// build squares
		
		int squareStringLength = squareString.length();
		List<Square> squares = new ArrayList<Square>(squareStringLength);
		
		int squareIndex = 0;
		for(int charIndex=0; charIndex<squareStringLength; charIndex++){
			
			Square square = new StandardSquare(0, squareIndex);
			
			try {
				char squareChar = squareString.charAt(charIndex);
				switch(squareChar){
					case '.':
						break;
					case '-':
						square.setState(SquareState.CLEAR);
						break;
					case 'x':
						square.setState(SquareState.FILLED);
						break;
					case ' ':
						// do nothing, skip to next char please
						continue;
					case '$':
						
						// only valid right after a contiguous sequence of x's, so let's find the sequence of x's
						int xEndIndex = charIndex;
						int xStartIndex = xEndIndex;
						
						// it's the first char, so you can't even look at the previous one
						if(charIndex <= 0) throw new RuntimeException("$ only valid after a sequence of x's");
						
						while(xStartIndex > 0 && squareString.charAt(xStartIndex - 1) == 'x'){
							xStartIndex--;
						}
						
						if(xStartIndex == xEndIndex) throw new RuntimeException("$ only valid after a sequence of x's");
						
						// parse number
						int numberStartIndex = charIndex + 1; 
						
						int numberEndIndex = numberStartIndex;
						while(numberEndIndex < squareString.length() && squareString.charAt(numberEndIndex) >= '0' && squareString.charAt(numberEndIndex) <= '9'){
							numberEndIndex++;
						}
						
						if(numberStartIndex == numberEndIndex) throw new RuntimeException("Expected numeric value after $");
						String numberString = squareString.substring(numberStartIndex, numberEndIndex);
						Integer runIndex = Integer.parseInt(numberString) - 1;
						
						if(runIndex < 0 || runIndex > runs.length - 1) throw new RuntimeException("Invalid run number " + numberString + "; must be between 1 and " + runs.length);
						
						//int runLength = runs[runIndex];
						
						// assign the last (xEndIndex - xStartIndex) squares with the proper run (or rather, remember to set it, because
						// the runs aren't created yet) 
						for(int i=squares.size()-1; i>=squares.size() - (xEndIndex - xStartIndex); i--){
							squareRuns.put(squares.get(i), runIndex);
						}
						
						// fast forward in the string
						charIndex = numberEndIndex - 1;
						continue;
						
					default:
						throw new RuntimeException("Unexpected character '"+squareChar+"'");
				}
			}
			catch(ConflictingSquareStateException e) {
				// shouldn't happen
				throw new RuntimeException(e);
			}
			
			squares.add(square);
			squareIndex = squares.size();
			
		}
		
		Row row = new StandardRow(Orientation.HORIZONTAL, 0, squares, runs);
		
		try {
			
			// assign runs according to the squareRuns map
			for(Map.Entry<Square, Integer> entry : squareRuns.entrySet()){
				Square square = entry.getKey();
				square.setRun(row.getRun(entry.getValue()));
			}
			
		}
		catch(RunLengthExceededException e) {
			throw new RuntimeException(e);
		}
		catch(ConflictingSquareRunException e) {
			throw new RuntimeException(e);
		}
		
		return row;
		
	}
	
}
