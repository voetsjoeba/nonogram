package com.voetsjoeba.nonogram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentDecomposition;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentList;
import com.voetsjoeba.nonogram.structure.assignment.LocalDecompositionIndex;

/**
 * Extends the JUnit asserts with some custom assertion methods.
 * 
 * @author Jeroen De Ridder
 */
public class Assert extends org.junit.Assert {
	
	private static final Logger log = LoggerFactory.getLogger(Assert.class);
	
	/**
	 * Horribly inefficient, but who cares, this is testing.
	 * @param formatString
	 * @param assignments
	 */
	public static void assertContainsDecomposition(String formatString, AssignmentList assignments){
		
		boolean contains = false;
		
		for(AssignmentDecomposition assignment : assignments){
			
			try {
				assertAssignmentDecomposition(formatString, assignment);
				contains = true;
			}
			catch(AssertionError ae){
				// nope, try next one
			}
			
		}
		
		assertTrue(contains);
		
	}
	
	/**
	 * Asserts that <tt>runAssignment</tt> adheres to <tt>formatString</tt>.
	 * @param formatExpected
	 * @param runAssignment
	 */
	public static void assertAssignmentDecomposition(String formatString, AssignmentDecomposition runAssignment){
		
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
		
		// traverse format string and validate against runAssignment
		int currentSequenceIndex = 0; // current matched sequence index
		int currentRunIndex = -1; // current matched run index
		
		// current global index within the decomposition (different from the iteration variable, because the spaces in 
		// the format string are not part of the decomposition and therefore need to be excluded from the global index).
		// Therefore, we maintain a separate variable.
		int currentAssignmentIndex = 0;
		
		int squareStringLength = squareString.length();
		for(int i=0; i<squareStringLength; i++){
			
			Character previousChar = (i > 0 ? squareString.charAt(i-1) : null);
			char currentChar = squareString.charAt(i);
			
			// needs to be taken care of before the other characters because it can't update currentAssignmentIndex
			// (spaces delimit sequences and are not part of the decomposition, and must therefore not be counted).
			if(currentChar == ' '){
				currentSequenceIndex++; // update our sequence counter; next local index will be validated against it
				continue;
			}
			
			// ensure that we're in the correct sequence (our sequence index gets updated with every space encountered)
			LocalDecompositionIndex localIndex = runAssignment.globalToLocal(currentAssignmentIndex);
			assertEquals(currentSequenceIndex, localIndex.sequenceIndex);
			
			Run run = runAssignment.getSlotAt(currentAssignmentIndex).getRun();
			
			switch(currentChar){
				
				case '.':
					Assert.assertNull(run);
					break;
					
				case 'x':
					//if(previousChar != null && previousChar != 'x') currentRunIndex++;
					if(previousChar == null || (previousChar != null && previousChar != 'x')) currentRunIndex++;
					assertNotNull(run);
					assertEquals(runs[currentRunIndex], run.getLength());
					break;
					
				default:
					fail("Unknown character '"+currentChar+"' encountered in format string");
					break;
					
			}
			
			currentAssignmentIndex++;
			
		}
		
	}
	
}
