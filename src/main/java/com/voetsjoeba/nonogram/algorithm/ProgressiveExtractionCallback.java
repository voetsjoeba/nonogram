package com.voetsjoeba.nonogram.algorithm;

import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Square;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentDecomposition;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentGenerationCallback;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentSlot;

/**
 * Progressively compares each generated assignment with the previously generated assignment and decides for each square
 * whether it is either always cleared, always filled or always filled with the same run applied.
 * 
 * @author Jeroen De Ridder
 */
public class ProgressiveExtractionCallback implements AssignmentGenerationCallback {
	
	protected ConsistencySlot[] consistencySlots;
	protected RowDecomposition rowDecomposition;
	
	protected long totalGeneratedAssignments = 0;
	protected int consistentSlotCount;
	
	public ProgressiveExtractionCallback(RowDecomposition templateDecomposition) {
		this.rowDecomposition = templateDecomposition;
		initSlots();
	}
	
	private void initSlots(){
		
		int squareCount = rowDecomposition.getTotalLength();
		consistencySlots = new ConsistencySlot[squareCount];
		
		for(int i=0; i<squareCount; i++){
			consistencySlots[i] = new ConsistencySlot();
			consistencySlots[i].square = rowDecomposition.getSquare(i);
			consistencySlots[i].consistent = true;
		}
		
		consistentSlotCount = squareCount;
		
	}
	
	public void receiveGeneratedAssignment(AssignmentDecomposition workingAssignment) {
		
		totalGeneratedAssignments++;
		
		if(totalGeneratedAssignments == 1){
			
			// set initial values
			
			for(int squareIndex=0; squareIndex < workingAssignment.getTotalLength(); squareIndex++){
				
				Run assignedRun = workingAssignment.getSlotAt(squareIndex).getRun();
				ConsistencySlot ex = consistencySlots[squareIndex];
				
				ex.allClear = (assignedRun == null);
				ex.allFilled = (assignedRun != null);
				
				ex.sameRun = assignedRun;
				ex.allSameRun = (ex.sameRun != null);
				
			}
			
		} else {
			
			// update each slot's info
			updateConsistencyInformation(workingAssignment, 0, workingAssignment.getTotalLength());
			
		}
		
		
	}
	
	public void receivePartialAssignment(AssignmentDecomposition partialAssignment, int startIndex, int endIndex) {
		updateConsistencyInformation(partialAssignment, startIndex, endIndex);
	}
	
	/**
	 * Updates the consistency information of squares <tt>startIndex</tt> (inclusive) through <tt>endIndex</tt> (exclusive) in <tt>assignment</tt>.
	 * @param assignment
	 * @param startIndex
	 * @param endIndex
	 */
	protected void updateConsistencyInformation(AssignmentDecomposition assignment, int startIndex, int endIndex){
		
		// update each slot's info
		for(int squareIndex=startIndex; squareIndex<endIndex; squareIndex++){
			
			Run assignedRun = assignment.getSlotAt(squareIndex).getRun();
			ConsistencySlot ex = consistencySlots[squareIndex];
			
			if(!ex.consistent) continue; // skip any disabled slots
			
			//ex.allClear = ex.allClear && (assignedRun == null);
			//ex.allFilled = ex.allFilled && (assignedRun != null);
			
			//if(ex.sameRun == null) ex.sameRun = assignedRun; // initial value
			//ex.allSameRun = ex.allSameRun && (assignedRun != null && assignedRun == ex.sameRun);
			
			boolean newAllClear = ex.allClear && (assignedRun == null);
			boolean newAllFilled = ex.allFilled && (assignedRun != null);
			boolean newAllSameRun = ex.allSameRun && (assignedRun != null && assignedRun == ex.sameRun);
			
			if(newAllClear != ex.allClear){
				// this square will not be cleared in all solutions (even though it was in the first), so don't bother looking at it further because it 
				// won't bring you any useful info
				ex.consistent = false;
			}
			
			if(newAllFilled != ex.allFilled){
				// this square will not be filled in all solutions (even though it was in the first), so don't bother looking at it further because it 
				// won't bring you any useful info
				ex.consistent = false;
			}
			
			// allSameRun can be false but still have the square deliver useful information (if they're all filled but with different runs),
			// so don't check it here
			
			ex.allClear = newAllClear;
			ex.allFilled = newAllFilled;
			ex.allSameRun = newAllSameRun;
			
			// if this slot got disabled, register it
			if(!ex.consistent){
				
				// tell the generation algorithm that this square is inconsistent
				AssignmentSlot assignmentSlot = assignment.getSlotAt(squareIndex);
				
				// obviously, fixed slots can never be inconsistent (because that's the whole reason they're fixed)
				// if we're trying to set a fixed slot as inconsistent, something is wrong with your algorithm
				/*if(assignmentSlot.isFixed()){
					throw new RuntimeException("Cannot mark a fixed slot as inconsistent.");
				}*/
				if(assignmentSlot.isFixed()){
					assert false : "Attempting to mark a fixed slot as inconsistent -- this is most likely an algorithm error";
				}
				
				assignmentSlot.setValid(false);
				
				/*enabledSlotCount--;
				
				// if there are no more candidate slots, terminate assignment generation
				if(enabledSlotCount == 0){
					throw new TerminateAssignmentGenerationException("All slots change across solutions, terminating assignment generation");
				}*/
				
			}
			
			assert !(ex.allClear && ex.allFilled) : "A square was both always cleared and always filled after generation solutions -- this is probably a programming error";
			assert !ex.allSameRun || ex.allFilled : "All squares have the same run assigned, but allgedly not all squares even have runs assigned -- this is probably a programming error"; // a => b <=> not(a) v b
			
		}
		
	}
	
	/**
	 * To be called when all assignments have been generated; applies the extracted consistency information for each square as applicable.
	 * (e.g. squares that were always clear will be cleared, squares that were always filled will be filled, etc.)
	 * 
	 * @throws ConflictingSquareStateException 
	 * @throws ConflictingSquareRunException 
	 * @throws RunLengthExceededException 
	 */
	public void applyConsistencyInformation() throws ConflictingSquareStateException, RunLengthExceededException, ConflictingSquareRunException{
		
		for(int squareIndex=0; squareIndex<rowDecomposition.getTotalLength(); squareIndex++){
			
			ConsistencySlot ex = consistencySlots[squareIndex];
			Square square = ex.square;
			
			// square has been determined to not be consistent, don't apply its settings (cause they're still stuck at the settings of that assignment that caused
			// the square to be non-consistent, i.e. the first assignment that had a different setting for that square than the time before)
			if(!ex.consistent) continue;
			
			if(ex.allClear){
				square.setState(SquareState.CLEAR);
			}
			
			if(ex.allFilled){
				square.setState(SquareState.FILLED);
				if(ex.allSameRun) square.setRun(ex.sameRun);
			}
			
		}
		
	}
	
	public long getTotalGeneratedAssignments() {
		return totalGeneratedAssignments;
	}
	
	public class ConsistencySlot {
		
		// all booleans are initially set to false -- initial values will be set upon receiving the first generated assignment
		
		/**
		 * Whether all instances of this square across the generated assignments so far have been cleared.
		 */
		public boolean allClear = false;
		
		/**
		 * Whether all instances of this square across the generated assignments so far have been filled.
		 */
		public boolean allFilled = false;
		
		/**
		 * Whether all instances of this square across the generated assignments so far have been filled, and also have all had the same {@link Run} 
		 * assigned to them (this run is kept as the {@link #sameRun} field). If this value is true, then {@link #allFilled} must also be true.
		 */
		public boolean allSameRun = false;
		
		/**
		 * The {@link Run} that all instances of this square across the generated assignments so far have in common (if any).
		 */
		public Run sameRun = null;
		
		/**
		 * Square associated with this slot.
		 */
		public Square square;
		
		/**
		 * Whether this square's assignment (or lack thereof) has been consistent throughout the generated assignments so far.
		 */
		public boolean consistent = true;
		
		@Override
		public String toString() {
			return "(C=" + (allClear ? "1" : "0") + ",F=" + (allFilled ? "1" : "0") + ",SR=" + (allSameRun ? "1" : "0")+")";
		}
		
	}
	
}
