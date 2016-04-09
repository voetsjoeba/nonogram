package com.voetsjoeba.nonogram.algorithm;

import java.util.Comparator;

import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.api.Row;

/**
 * Compares rows based on:
 *  - their maximum run length
 *  - the amount of runs in the row
 *  - orientation enum ordinal
 *  - row index
 *  
 * <i>Note: must be consistent with equals</i>! (see {@link Comparable} for details).
 * 
 * @author Jeroen De Ridder
 */
public class RowMaxRunLengthComparator implements Comparator<Row> {

	public int compare(Row row1, Row row2) {
		
		int row1RunCount = row1.getRuns().size();
		int row2RunCount = row2.getRuns().size();
		
		int row1MaxRunLength = row1.getMaximumRunLength();
		int row2MaxRunLength = row2.getMaximumRunLength();
		
		Orientation row1Orientation = row1.getOrientation();
		Orientation row2Orientation = row2.getOrientation();
		
		if(row1MaxRunLength > row2MaxRunLength){
			return 1;
		} else if(row1MaxRunLength < row2MaxRunLength){
			return -1;
		}
		
		if(row1RunCount > row2RunCount){
			return 1;
		} else if(row1RunCount < row2RunCount) {
			return -1;
		}
		
		if(row1Orientation != row2Orientation){
			return Integer.valueOf(row1Orientation.ordinal()).compareTo(Integer.valueOf(row2Orientation.ordinal()));
		}
		
		return Integer.valueOf(row1.getIndex()).compareTo(Integer.valueOf(row2.getIndex()));
		
	}
	
}
