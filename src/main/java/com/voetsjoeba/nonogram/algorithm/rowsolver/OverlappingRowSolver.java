package com.voetsjoeba.nonogram.algorithm.rowsolver;

import java.util.List;

import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.DecompositionRunInfo;
import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * Determines squares that must be filled by virtue of being positioned on the area where the leftmost and rightmost positions of a run overlap.
 * Similarly, any squares that are outside of all run ranges can be cleared.
 * 
 * @author Jeroen De Ridder
 */
public class OverlappingRowSolver extends AbstractRowSolver {
	
	// prevent from having to reinitialize an array every time the solver is called -- uses two arrays to accommodate two different row lengths.
	// note: assumes a rectangular puzzle grid (i.e. there are only two possible row lengths)
	//private int[] rangeVisits1;
	//private int[] rangeVisits2;
	
	private int[] rangeVisits;
	
	public OverlappingRowSolver() {
		
	}
	
	public void solve(Row row) throws UnsolvablePuzzleException {
		
		RowDecomposition decomposition = row.getDecomposition();
		
		// (1) determined filled squares
		
		// the row decomposition has pre-calculated the rightmost and leftmost possible starting offsets for each incomplete run. so, the only thing left for us 
		// to do is to calculate the leftmost possible ending offsets for each incomplete run, and then check whether the first square of its rightmost
		// possible position overlaps with the last square of its leftmost possible position. If so, then there is overlap and we can fill it and assign
		// it that incomplete run.
		
		List<Run> runs = row.getIncompleteRuns();
		int runCount = runs.size();
		
		for(int i=0; i < runCount; i++){
			
			Run run = runs.get(i);
			DecompositionRunInfo runInfo = decomposition.getRunInfo(run);
			int leftmostEndingIndex = runInfo.leftmostStartOffset + run.getLength() - 1;
			int rightmostStartingIndex = runInfo.rightmostStartOffset;
			
			int overlap = leftmostEndingIndex - rightmostStartingIndex + 1;
			if(overlap > 0){
				
				// fill rightmostStartingIndex up to and including leftmostEndingIndex
				for(int j=rightmostStartingIndex; j <= leftmostEndingIndex; j++){
					NonogramUtils.setSquareState(decomposition.getSquare(j), SquareState.FILLED, run);
				}
				
			}
			
		}
		
		// (2) determine cleared squares
		
		int rowLength = row.getLength();
		
		// squares that are not visited by any run's range must be a cleared square (because it can contain no run)
		//int[] visits = new int[row.getLength()];
		
		/*int[] rangeVisitsArray; // visit counter array to use
		
		if(rangeVisits1 == null) rangeVisits1 = new int[rowLength]; // first allocation; use rangeVisits1
		if(rangeVisits2 == null && rowLength != rangeVisits1.length) rangeVisits2 = new int[rowLength]; // second allocation; use rangeVisits2 if the row length is different
		
		rangeVisitsArray = (rangeVisits1.length == rowLength ? rangeVisits1 : null); // use rangeVisits1 first if possible
		rangeVisitsArray = (rangeVisits2.length == rowLength ? rangeVisits2 : null); // otherwise, use rangeVisits2 (which should have the correct length -- see above + assumption of rectangular puzzle grid)
		
		assert rangeVisitsArray != null && rangeVisitsArray.length == rowLength : "Could not find range visits array for row length " + rowLength + "; existing arrays are of length " + rangeVisits1.length + " and " + rangeVisits2.length;
		*/
		
		if(rangeVisits == null) rangeVisits = new int[rowLength]; // initial assignment; allocate a row's length worth of slots -- this ensures that a decomposition always fits
		
		// reset visit counters
		for(int i=0; i<rangeVisits.length; i++){
			rangeVisits[i] = 0;
		}
		
		// enlarge visits array if needed
		if(rangeVisits.length < rowLength){
			// encountered a larger row -- reallocate rangeVisits to match the larger row
			int[] newRangeVisits = new int[rowLength];
			System.arraycopy(rangeVisits, 0, newRangeVisits, 0, rangeVisits.length);
			rangeVisits = newRangeVisits;
		}
		
		// count visits
		for(Run run : runs){
			
			DecompositionRunInfo runInfo = decomposition.getRunInfo(run);
			
			for(int i=runInfo.leftmostStartOffset; i < runInfo.rightmostStartOffset + run.getLength(); i++){
				rangeVisits[i]++;
			}
			
		}
		
		// clear squares with no visits
		for(int i=0; i<decomposition.getTotalLength(); i++){
			if(rangeVisits[i] <= 0){
				NonogramUtils.setSquareState(decomposition.getSquare(i), SquareState.CLEAR, null);
			}
		}
		
	}
	
}
