package com.voetsjoeba.nonogram.structure.assignment;


public interface AssignmentGenerationCallback {
	
	/**
	 * Callback that is called when a complete assignment has been generated. The <tt>assignment</tt> parameter is the current
	 * working version of the assignment, and should not be altered!
	 * @param workingAssignment
	 */
	public void receiveGeneratedAssignment(AssignmentDecomposition workingAssignment);
	
	/**
	 * Callback to update consistency information on a partial assignment. This occurs when, while building an assignment, it is determined at one point that all further building steps 
	 * (i.e. run assignments) would lead to no additional consistency information. When this happens, the assignment generator will call this function passing the 
	 * assignment in its partial form to update the consistency information on the partial construction. 
	 * 
	 * @param partialAssignment
	 * @param startIndex
	 * @param endIndex
	 */
	public void receivePartialAssignment(AssignmentDecomposition partialAssignment, int startIndex, int endIndex);
	
}
