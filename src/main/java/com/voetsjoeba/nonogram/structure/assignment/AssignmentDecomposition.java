package com.voetsjoeba.nonogram.structure.assignment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.InvalidRunConfigurationException;
import com.voetsjoeba.nonogram.exception.SequenceLengthExceededException;
import com.voetsjoeba.nonogram.structure.AbstractDecomposition;
import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * Working decomposition for use with run assignment generation.
 * 
 * @author Jeroen De Ridder
 */
public class AssignmentDecomposition extends AbstractDecomposition {
	
	protected AssignmentSlot[][] slots;
	protected final RowDecomposition rowDecomposition;
	
	// TODO: code duplication of RowDecomposition
	protected int totalLength;
	
	public AssignmentDecomposition(RowDecomposition decomposition){
		this.rowDecomposition = decomposition;
		initSlots(decomposition);
	}
	
	/**
	 * Initializes the slot configuration from a row decomposition.
	 * @param rowDecomposition
	 */
	private void initSlots(RowDecomposition rowDecomposition){
		
		Orientation rowOrientation = rowDecomposition.getRow().getOrientation();
		List<Sequence> sequences = rowDecomposition.getSequences();
		int sequenceCount = sequences.size();
		
		slots = new AssignmentSlot[sequenceCount][];
		
		for(int i=0; i<sequenceCount; i++){
			
			Sequence sequence = sequences.get(i);
			int sequenceLength = sequence.getLength();
			
			slots[i] = new AssignmentSlot[sequenceLength];
			
			for(int j=0; j<sequenceLength; j++){
				
				Square square = sequence.getSquare(j);
				
				assert !square.isCleared(); // square state must be either filled or unknown -- cleared is impossible due to the nature of the row decomposition
				slots[i][j] = new AssignmentSlot();
				slots[i][j].setFixed(square.isFilled());
				
				if(square.hasRun(rowOrientation)){
					assert square.isFilled();
					slots[i][j].setFixedRun(square.getRun(rowOrientation));
				}
				
			}
			
			totalLength += sequenceLength;
			
		}
		
	}
	
	// -- Decomposition interface -----------------------------------------
	
	public int getSequenceCount() {
		return slots.length;
	}
	
	public int getSequenceLength(int i) {
		return slots[i].length;
	}
	
	// --------------------------------------------------------------------
	
	/**
	 * Allocates <tt>run</tt> at the specified global square index in the decomposition.
	 * 
	 * @param run
	 * @param globalIndex The global decomposition index indicating the first square of the assigned run
	 * @return Whether the assigned run has completely filled the sequence to which it was assigned, i.e. whether the last 
	 * square of the assigned run is also the last square of the enclosing sequence. See also the comments in {@link NonogramUtils#generateAssignmentsRecursive(List, AssignmentDecomposition, int, List)}'s body.
	 * 
	 * @throws SequenceLengthExceededException if the run cannot be assigned because it would exceed its enclosing sequence size
	 * @throws InvalidRunConfigurationException
	 * @throws ConflictingSquareRunException 
	 */
	public boolean assign(Run run, int globalIndex) throws SequenceLengthExceededException, InvalidRunConfigurationException, ConflictingSquareRunException {
		
		// try to assign the run to the sequence of squares starting at globalIndex, throw an error if a run is already
		// assigned to any of the squares
		
		// make sure there is room for the run
		LocalDecompositionIndex startIndexLocal = globalToLocal(globalIndex);
		
		AssignmentSlot[] sequence = slots[startIndexLocal.sequenceIndex];
		int sequenceLengthRemaining = sequence.length - startIndexLocal.sequenceOffset;
		
		if(sequenceLengthRemaining < run.getLength()){
			throw new SequenceLengthExceededException("Cannot assign run; insufficient room for run of length " + run.getLength() + " at offset " + startIndexLocal.sequenceOffset + " in sequence with length " + slots[startIndexLocal.sequenceIndex].length);
		}
		
		// make sure the requested assignment contains all the known squares of this run (if any)
		if(run.hasKnownSquares()){
			
			// build set of squares that will contain the assigned run
			Set<Square> targetSquares = new HashSet<Square>();
			for(int i=globalIndex; i < globalIndex + run.getLength(); i++){
				Square square = rowDecomposition.getSquare(i);
				targetSquares.add(square);
			}
			
			// ensure that all known squares are in the targetSquares set
			if(!targetSquares.containsAll(run.getKnownSquares())){
				throw new InvalidRunConfigurationException("Cannot assign run; target location does not include known squares " + run.getKnownSquares());
			}
			
		}
		
		
		// ensure that the squares on either side of the would-be-assigned run (if they exist) are free
		if(startIndexLocal.sequenceOffset > 0){
			
			AssignmentSlot leftBorderSlot = sequence[startIndexLocal.sequenceOffset - 1];
			if(leftBorderSlot.getRun() != null) throw new InvalidRunConfigurationException("Cannot assign run; left border touches with previously assigned or fixed run " + leftBorderSlot.getRun());
			if(leftBorderSlot.isFixed()) throw new InvalidRunConfigurationException("Cannot assign run; left border touches with fixed slot");
			
			
		}
		
		if(startIndexLocal.sequenceOffset + run.getLength() < sequence.length){
			
			AssignmentSlot rightBorderSlot = sequence[startIndexLocal.sequenceOffset + run.getLength()];
			if(rightBorderSlot.getRun() != null) throw new InvalidRunConfigurationException("Cannot assign run; right border touches with previously assigned run " + rightBorderSlot.getRun());
			if(rightBorderSlot.isFixed()) throw new InvalidRunConfigurationException("Cannot assign run; right border touches with fixed slot");
			
		}
		
		// check whether the squares are free (or already set to the run that is to be assigned; this is needed 
		// so that runs can be positioned over any fixed squares that have already been assigned that run)
		for(int i=globalIndex; i < globalIndex + run.getLength(); i++){
			
			LocalDecompositionIndex localIndex = globalToLocal(i);
			AssignmentSlot slot = sequence[localIndex.sequenceOffset];
			
			if(slot.getFixedRun() != null){
				
				// the run we're going to set must equal the fixed run
				if(run != slot.getFixedRun()){
					throw new ConflictingSquareRunException("Cannot assign run; square is fixed to run " + slot.getFixedRun());
				}
				
			}
			
			// the slot whose run we're going to set must not yet have a run assigned
			if(slot.getRun() != null){
				throw new InvalidRunConfigurationException("Cannot assign run " + run + " at offset " + startIndexLocal.sequenceOffset + " in sequence " + startIndexLocal.sequenceIndex + "; a run is already assigned at index " + localIndex.sequenceOffset + "("+slot.getRun()+")");
			}
			
			//Run assignedRun = sequence[localIndex.sequenceOffset].getRun();
			
			/*if(assignedRun != null && assignedRun != run){
				throw new InvalidRunConfigurationException("Cannot assign run " + run + " at offset " + startIndexLocal.sequenceOffset + " in sequence " + startIndexLocal.sequenceIndex + "; a run is already assigned at index " + localIndex.sequenceOffset + "("+assignedRun+")");
			}*/
			
		}
		
		// assign the run
		for(int i=globalIndex; i < globalIndex + run.getLength(); i++){
			
			LocalDecompositionIndex localIndex = globalToLocal(i);
			sequence[localIndex.sequenceOffset].setRun(run);
			
		}
		
		return (sequenceLengthRemaining == run.getLength());
		
	}
	
	/**
	 * Removes a previous assignment of run <tt>run</tt> at position <tt>globalIndex</tt>.
	 * 
	 * @param run
	 * @param globalIndex
	 * @throws InvalidRunConfigurationException
	 */
	public void unassign(Run run, int globalIndex) throws InvalidRunConfigurationException {
		
		LocalDecompositionIndex startIndexLocal = globalToLocal(globalIndex);
		
		// ensure that all squares that are to be cleared are set to the specified run
		// due to the validation performed by the assign method, we need not explicitly perform any sanity checks here
		// because only valid placements are allowed
		
		for(int i=globalIndex; i < globalIndex + run.getLength(); i++){
			
			LocalDecompositionIndex localIndex = globalToLocal(i);
			if(slots[localIndex.sequenceIndex][localIndex.sequenceOffset].getRun() != run){
				throw new InvalidRunConfigurationException("Cannot undo assignment of run " + run + " at offset " + startIndexLocal.sequenceOffset + " in sequence " + startIndexLocal.sequenceIndex + "; not all squares are set to the to-be-removed run");
			}
			
		}
		
		// undo assignment
		
		for(int i=globalIndex; i < globalIndex + run.getLength(); i++){
			
			LocalDecompositionIndex localIndex = globalToLocal(i);
			AssignmentSlot slot = slots[localIndex.sequenceIndex][localIndex.sequenceOffset];
			
			// remove run registration
			slot.setRun(null);
			
		}
		
	}
	
	/**
	 * Ensures that fixed slots 
	 */
	public void validateFixedSlots() throws InvalidRunConfigurationException {
		validateFixedSlots(0, totalLength);
	}
	
	public void validateFixedSlots(int startIndex, int endIndex) throws InvalidRunConfigurationException {
		
		// make sure that, within the range [startIndex, endIndex[:
		// 1) each fixed slot has a run assigned
		// 2) each fixed run slot has the right run assigned
		
		for(int globalIndex = startIndex; globalIndex < endIndex; globalIndex++){
			
			LocalDecompositionIndex localIndex = globalToLocal(globalIndex);
			AssignmentSlot slot = slots[localIndex.sequenceIndex][localIndex.sequenceOffset];
			if(!slot.isFixed()) continue;
			
			Run assignedRun = slot.getRun();
			Run fixedRun = slot.getFixedRun();
			
			if(assignedRun == null){
				throw new InvalidRunConfigurationException("No run configured for fixed slot " + slot);
			}
			
			if(fixedRun != null && assignedRun != fixedRun){
				throw new InvalidRunConfigurationException("Invalid run configured for fixed run slot " + slot + "; expected " + slot.getFixedRun() + ", found " + assignedRun);
			}
			
		}
		
		
	}
	
	public int getTotalLength(){
		return totalLength;
	}
	
	/**
	 * Returns the assigned run at <tt>globalIndex</tt>, or null if no such run exists.
	 * @param globalIndex
	 * @return
	 */
	/*public Run getRunAt(int globalIndex){
		LocalDecompositionIndex localIndex = globalToLocal(globalIndex);
		return slots[localIndex.sequenceIndex][localIndex.sequenceOffset].getRun();
	}*/
	
	/**
	 * Returns the fixed run (if any) at <tt>globalIndex</tt>, or null if no such fixed run exists.
	 * @param globalIndex
	 * @return
	 */
	/*public Run getFixedRunAt(int globalIndex){
		LocalDecompositionIndex localIndex = globalToLocal(globalIndex);
		return slots[localIndex.sequenceIndex][localIndex.sequenceOffset].getFixedRun();
	}*/
	
	/**
	 * Returns the assignment slot at <tt>globalIndex</tt>.
	 */
	public AssignmentSlot getSlotAt(int globalIndex){
		LocalDecompositionIndex localIndex = globalToLocal(globalIndex);
		return slots[localIndex.sequenceIndex][localIndex.sequenceOffset];
	}
	
	public String toString(){
		
		StringBuffer sb = new StringBuffer();
		
		// runs
		List<Run> runs = rowDecomposition.getRow().getRuns();
		int runCount = runs.size();
		
		for(int i=0; i<runCount; i++){
			sb.append(runs.get(i).getLength());
			if(i < runCount - 1) sb.append(",");
		}
		
		int runStringLength = sb.length();
		sb.append("|");
		/*for(Run run : rowDecomposition.getRow().getRuns()){
			sb.append("")
		}*/
		
		// squares
		
		for(int i=0; i<slots.length; i++){
			
			for(int j=0; j<slots[i].length; j++){
				
				Run run = slots[i][j].getRun();
				sb.append(run == null ? "." : "x");
				
			}
			
			if(i < slots.length - 1) sb.append(" ");
			
		}
		
		sb.append("\n");
		sb.append(StringUtils.repeat(" ", runStringLength + 1));
		
		// add second row of consistency markers
		for(int i=0; i<slots.length; i++){
			
			for(int j=0; j<slots[i].length; j++){
				
				boolean valid = slots[i][j].isValid();
				sb.append(valid ? " " : "I");
				
			}
			
			if(i < slots.length - 1) sb.append(" ");
			
		}
		
		
		return sb.toString();
		
	}
	
	/**
	 * Returns the row decomposition corresponding to this assignment.
	 */
	public RowDecomposition getRowDecomposition(){
		return rowDecomposition;
	}
	
}
