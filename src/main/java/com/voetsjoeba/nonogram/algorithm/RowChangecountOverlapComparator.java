package com.voetsjoeba.nonogram.algorithm;

import java.util.Comparator;
import java.util.List;

import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * Compares rows based on the amount of changes made to them first, and (roughly) the amount of "overlap" second.
 * 
 * @author Jeroen De Ridder
 */
public class RowChangecountOverlapComparator implements Comparator<Row> {
	
	// for use with a priorityqueue, so -1 means better, +1 means worse
	public int compare(Row o1, Row o2) {
		
		int modCount1 = o1.getModificationCount();
		int modCount2 = o2.getModificationCount();
		
		if(modCount1 > modCount2){
			
			return -1;
			
		} else if(modCount1 < modCount2){
			
			return 1;
			
		} else {
			
			List<Run> runs1 = o1.getIncompleteRuns();
			List<Run> runs2 = o2.getIncompleteRuns();
			
			RowDecomposition decomp1 = o1.getDecomposition();
			RowDecomposition decomp2 = o2.getDecomposition();
			
			int knownSquareCount1 = decomp1.getKnownSquareCount();
			int knownSquareCount2 = decomp2.getKnownSquareCount();
			
			int minAssignmentLength1 = NonogramUtils.getMinimumRunAssignmentLength(runs1, true) + knownSquareCount1*knownSquareCount1; // heuristic approximation
			int minAssignmentLength2 = NonogramUtils.getMinimumRunAssignmentLength(runs2, true) + knownSquareCount2*knownSquareCount2;
			
			int diff1 = decomp1.getTotalLength() - minAssignmentLength1;
			int diff2 = decomp2.getTotalLength() - minAssignmentLength2;
			
			// the smaller the difference, the better
			
			if(diff1 < diff2){
				return -1;
			} else if(diff1 > diff2) {
				return 1;
			}
			
		}
		
		return 0;
		
	}
	
}
