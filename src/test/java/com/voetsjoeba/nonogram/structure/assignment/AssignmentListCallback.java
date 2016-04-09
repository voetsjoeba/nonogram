package com.voetsjoeba.nonogram.structure.assignment;

import java.util.List;

import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.api.Sequence;


public class AssignmentListCallback implements AssignmentGenerationCallback {
	
	private AssignmentList assignments;
	
	public AssignmentListCallback() {
		assignments = new AssignmentList();
	}
	
	public void receiveGeneratedAssignment(AssignmentDecomposition workingAssignment) {
		
		// make a copy of the assignment
		
		// inefficient since we're gonna overwrite the assignment this will generate from the decomposition, 
		// but who cares, this is test
		AssignmentDecomposition copy = new AssignmentDecomposition(workingAssignment.rowDecomposition);
		
		Orientation rowOrientation = workingAssignment.rowDecomposition.getRow().getOrientation();
		List<Sequence> sequences = workingAssignment.rowDecomposition.getSequences();
		int sequenceCount = sequences.size();
		
		for(int i=0; i<sequenceCount; i++){
			
			Sequence sequence = sequences.get(i);
			int sequenceLength = sequence.getLength();
			
			for(int j=0; j<sequenceLength; j++){
				
				copy.slots[i][j].setFixed(workingAssignment.slots[i][j].isFixed());
				copy.slots[i][j].setFixedRun(workingAssignment.slots[i][j].getFixedRun());
				copy.slots[i][j].setRun(workingAssignment.slots[i][j].getRun());
				copy.slots[i][j].setValid(workingAssignment.slots[i][j].isValid());
				
			}
			
		}
		
		assignments.add(copy);
		
	}
	
	public void receivePartialAssignment(AssignmentDecomposition partialAssignment, int startIndex, int endIndex) {
		// does not apply
	}
	
	public AssignmentList getAssignments() {
		return assignments;
	}
	
}
