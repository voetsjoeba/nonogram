package com.voetsjoeba.nonogram.algorithm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.InvalidRunConfigurationException;
import com.voetsjoeba.nonogram.exception.SequenceLengthExceededException;
import com.voetsjoeba.nonogram.structure.DecompositionRunInfo;
import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentDecomposition;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentGenerationCallback;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentSlot;

public class AssignmentGenerator {
	
	private static final Logger log = LoggerFactory.getLogger(AssignmentGenerator.class);
	
	public AssignmentGenerator() {
		
	}
	
	/**
	 * Generates all possible assignments for <tt>row</tt>. Utility function; delegates to {@link #generateAssignments(List, RowDecomposition)}.
	 * @param row
	 */
	public void generateAssignments(Row row, AssignmentGenerationCallback generationCallback){
		generateAssignments(row.getRuns(), row.getDecomposition(), generationCallback);
	}
	
	/**
	 * Generate all possible assignments of <tt>runs</tt> to <tt>sequences</tt>.
	 */
	public void generateAssignments(List<Run> runs, RowDecomposition rowDecomposition, AssignmentGenerationCallback generationCallback){
		
		// assign the first run at positions 0 up to and including (availableDecompositionLength - minimalRunAssignmentLength)
		// and recursively assign further runs, and see if the assignment fits
		// for each run, scan left to right for a sequence that contain it (and all following runs recursively)
		// if no such sequence exists, no assignment is possible
		// as soon as a sequence is found that can contain the run (not yet recursively checked), check recursively whether
		// an assignment exists for all further runs.
		// if a run is already fixed in place, then no sequence needs to be looked for
		
		AssignmentDecomposition workingDecomposition = new AssignmentDecomposition(rowDecomposition); // decomposition to work with during algorithm
		generateAssignmentsRecursive(runs, workingDecomposition, 0, generationCallback);
		
		return;
		
	}
	
	/**
	 * Recursively generates run assignments.
	 * 
	 * @param runs
	 * @param workingAssignment The assignment decomposition to work on. As results are generated, snapshots will be taken of this working assignment.
	 * @param baseOffset Base offset at which to consider the decomposition. The decomposition must be treated as though everything before baseOffset is set in stone and cannot be changed.
	 * @return the amount of run assignments generated
	 */
	public void generateAssignmentsRecursive(List<Run> runs, AssignmentDecomposition workingAssignment, int baseOffset, AssignmentGenerationCallback generationCallback){
		
		if(runs.size() == 0){
			
			// there are no more runs to assign. Since this function is only called recursively or with a non-zero starting list, 
			// this means the "recursion parent"'s configuration is valid so far and the generation callback might have to be called.
			
			// first though, we need to verify whether the fixed assignments are ok up until the end of the sequence, because it's possible
			// that we've ended our assignment before having used up all of the decomposition space (there may be more fixed assignments
			// further down the road, which would mean that this assignment is invalid after all (since we ran out of runs to assign but there
			// are still squares set further on in the decomposition))
			
			// so, validate the fixed slots up until the end of the sequence, and if it comes back OK, feed it to the generator callback
			// (if there are no more slots left over, the for loop in validateFixedSlots will instantly return anyway, so no biggie)
			
			try {
				workingAssignment.validateFixedSlots(baseOffset, workingAssignment.getTotalLength());
				generationCallback.receiveGeneratedAssignment(workingAssignment);
			}
			catch(InvalidRunConfigurationException ircex){
				// callback is not executed
			}
			
			return;
			
		}
		
		// validate any fixed assignments slots before startOffset (because if any of them before startOffset are invalid, 
		// then it's no use to keep generating assignments because any further assignment that is generated will also
		// be invalid)
		/*try {
			workingAssignment.validateFixedSlots(0, startOffset); // TODO: use previousStartOffset so that you don't need to validate all the way from the start all the time
		}
		catch(InvalidRunConfigurationException ircex){
			// 
		}*/
		
		// if each run is assigned to its own sequence that happens to fit perfectly, then the minimal run assignment length
		// is the sum of the run lengths without any additional 'whitespace'
		//int minimalRunAssignmentLength = 0;
		//for(Run run : runs) minimalRunAssignmentLength += run.getLength();
		
		//int availableDecompositionLength = workingAssignment.getTotalLength() - startOffset;
		long runAssignmentsGenerated = 0;
		
		int runStartOffset; // index to start trying to position the run at
		int runEndOffset; // last index to try positioning the run at
		
		// if the next run in the list is completed, then you can update the startOffset to skip past it, because any 
		// runs after it will necessarily have to come after those squares
		
		// TODO: even for runs that are incomplete that have known squares, you can update startOffset to be the leftmost square for which the run
		// will overlap with the known squares (or, scanning from the leftmost known square to the target starting point,
		// if there are any filled squares with other runs along the way, you can offset back against them) 
		
		Run subjectRun = runs.get(0); // run to be assigned
		int subjectRunKnownSquareCount = subjectRun.getKnownSquareCount();
		
		//if(subjectRun.isComplete()){
		if(subjectRunKnownSquareCount > 0){ // i.e. subjectRun.hasKnownSquares()
			
			// this run has known squares, so there are only a few possible positions to try for this run
			// in particular, we only have to consider the positions inbetween the situations where the run is assigned to its leftmost positions
			// (so that its starting index "reaches out" to the left of the known squares) and where it's assigned to its rightmost position
			// (so that its starting index is the first known square).
			
			// so, let's find the first known square of the run so that we can also determine the leftmost and rightmost starting points
			// mind you, along the way there can't be any other slots with other assigned runs or other fixed runs (because that would imply a different run order)
			// fixed but non-fixed-run squares are allowed, since they could be part of the run
			
			int firstKnownSquareIndex = baseOffset; // starting index for finding the first known square of the run
			
			while(true){
				
				AssignmentSlot assignmentSlot = workingAssignment.getSlotAt(firstKnownSquareIndex);
				
				Run fixedRun = assignmentSlot.getFixedRun();
				Run assignedRun = assignmentSlot.getRun();
				boolean fixed = assignmentSlot.isFixed();
				
				if(fixedRun == subjectRun){
					
					break; // found our square
					
				} else {
					
					// there's a square with a different fixed run inbetween the end of the previous run and the start of the successor run; not a valid option
					// (no need to check whether fixedRun != subjectRun -- this is always true at this point, because otherwise the loop would've ended (see above))
					if(fixedRun != null){
						assert false : "Encountered a square with a different fixed run inbetween the end of the previous run and its successor run";
						return;
					}
					
					if(assignedRun != null){
						// there's square with a different assigned run inbetween the end of the previous run and its successor run; not a valid option 
						// (not sure how this would happen -- this probably indicates a programming error)
						assert false : "Encountered a square with a different assigned run inbetween the end of the previous run and its successor run";
						return;
					}
					
				}
				
				firstKnownSquareIndex++;
				
			}
			
			if(firstKnownSquareIndex == workingAssignment.getTotalLength()){
				throw new RuntimeException("Run " + subjectRun + " is completed in the decomposition, but its squares could not be found");
			}
			
			int remainingRunLength = subjectRun.getLength() - subjectRunKnownSquareCount;
			
			int leftmostStartOffset = firstKnownSquareIndex - remainingRunLength;
			int rightmostStartOffset = firstKnownSquareIndex; // +1 since endOffset is exclusive
			
			runStartOffset = Math.max(baseOffset, leftmostStartOffset); // make sure startOffset is at least leftmostStartOffset (i.e. make sure you don't try any positions that are impossible)
			runEndOffset = rightmostStartOffset + 1; // the last (rightmost) position to try is always the one where the run starts at its first known square
			
			// TODO: isn't all the above also performed by the leftmost and rightmost start offset calculations?
			
		} else {
			
			//runStartOffset = baseOffset; // we don't know anything about where the run is located, so let's start looking from the beginning
			//runEndOffset = calculateEndOffset(workingAssignment, runs) + 1; // +1 since endOffset is exclusive
			
			DecompositionRunInfo runInfo = workingAssignment.getRowDecomposition().getRunInfo(subjectRun);
			runStartOffset = Math.max(baseOffset, runInfo.leftmostStartOffset);
			runEndOffset = runInfo.rightmostStartOffset + 1; // +1 since endOffset is exclusive
			
		}
		
		// assign the run to each candidate position in turn
		
		for(int runOffset=runStartOffset; runOffset < runEndOffset; runOffset++){
			
			boolean sequenceFilled = false;
			
			try {
				sequenceFilled = workingAssignment.assign(subjectRun, runOffset);
			}
			catch(SequenceLengthExceededException sleex){
				// invalid sequence position, try next one
				continue;
			}
			catch(InvalidRunConfigurationException ircex){
				// invalid sequence position, try next one
				continue;
			}
			catch(ConflictingSquareRunException e) {
				// invalid sequence position, try next one
				continue;
			}
			
			// if the assigned run did not completely fill up the sequence to which it was assigned, then it must leave a whitespace square
			// and so add additional +1 needs to be added to newStartOffset. Otherwise, the next run assignment can start at the leftmost
			// border of the next sequence and it would be wrong to add an extra whitespace square.
			int newBaseOffset = runOffset + subjectRun.getLength();
			if(!sequenceFilled) newBaseOffset++;
			
			// validate the assignment against any fixed assignment slots in the affected area (because if any of them
			// are invalid, then it's no use to keep generating assignments because any further assignment that is
			// generated will also be invalid)
			
			// we're responsible for the assignment of the slots from startOffset up to the newStartOffset.
			// anything before startOffset will have been the responsibility of the recursion parent and may be assumed to
			// be always valid, and anything after newStartOffset will be the responsibility of the next recursion child.
			try {
				workingAssignment.validateFixedSlots(baseOffset, newBaseOffset);
			}
			catch(InvalidRunConfigurationException ircex){
				workingAssignment.unassign(subjectRun, runOffset);
				continue;
			}
			
			
			// --------------------------------------------------------------------
			// ***            AT THIS POINT, THE ASSIGNMENT IS VALID            ***
			// --------------------------------------------------------------------
			
			
			// if all squares to the right of the would-be-assigned run are known to be inconsistent, and the run itself also occupies only inconsistent squares,
			// then stop building this assignment (because any runs assigned further to the right would be pointless since we already know that all of them will 
			// be placed upon inconsistent squares)
			//
			// however, we still need to inform the callback of this partial assignment so that it can update the consistency information of the part of the
			// assignment that isn't necessarily inconsistent (i.e. the part that we've already built up to this point). If we had continued building the assignment, 
			// this would have happened when the assignment was complete (see the recursion end case code at the start of this function). Since we're aborting the 
			// recursive build process early, though, we still need to inform the callback of the part of the assignment that we _don't_ know to be all-inconsistent. 
			// That is, the part of the assignment that we built up to this point.
			
			// NOTE: THIS CHECK (AND ITS CORRESPONDING EARLY BAILOUT) MAY ONLY BE PERFORMED ONCE IT IS KNOWN THAT THE ASSIGNMENT IS VALID!
			// The thing is, the early bailout involves updating the consistency information of a partial assignment. If you do this before knowing that the 
			// assignment is (or rather will be) valid, you'll be updating with the wrong consistency information. When I first tried this I put this bailout early, 
			// before other checks that asserted the validity of the assignment, and I've seen this happen. You would get a situation like this:
			//
			// 2,6,5|xx...................xxxxxx.........
			//          IIIIIIIIIIIIIIIIII IIIIIIIIIIIIII
			//
			// .. where that single square that is valid amidst the invalid ones is a fixed square with unknown run. This assignment of the 6-run is perfectly valid,
			// and the consequent 5-run assignments will not be performed because they're on all-inconsistent grounds. So far so good. 
			//
			// Consider now the next position of the 6-run:
			// 
			// 2,6,5|xx....................xxxxxx........
			//          IIIIIIIIIIIIIIIIII IIIIIIIIIIIIII
			//
			// This assignment (and for that matter all further assignments of the 6-run) is invalid because the fixed square remains unassigned. However, if now you
			// perform the early bailout before discarding this invalid run, you would cause the callback to update its consistency information about the partial 
			// assignment that spans from the beginning of the sequence right up to the 6-run. Surprise! Since the fixed square is now unassigned, it has now been 
			// marked as inconsistent, which is a contradiction. I added asserts to the code that sets a square as inconsistent to ensure this does not inadvertly
			// happen again.
			//
			// Furthermore, it's important that you never attempt to assign a run beyond its last possible position, because it won't be detected that the position
			// is invalid if all further squares are inconsistent. This is of course because those further assignments will not even be looked at due to them being
			// on all-inconsistent grounds. Had the recursion continued as normal, it would have generated no assignments because none of the further runs would fit,
			// but we bailed out early and told the callback "look at this (partial) assignment", implying that it is valid while in fact it was not.
			//
			// So, the function calculateEndOffset was introduced, which calculates the last possible position of a run, taking into account the separations between
			// different sequences in the decomposition but not taking into account fixed squares (!). Now, you may wonder whether this could cause it to return an 
			// invalid end offset due to a fixed square being mis-assigned -- and so did I initially. The answer is no, because we only skip completing the 
			// assignment when all squares to the right of the run (and including the run) are inconsistent, and it is impossible for a fixed square to be 
			// inconsistent.
			//
			// Therefore, calculateEndOffset will always return the correct value in those cases where we need it to (i.e. in those cases where we bail out early). 
			// As illustrated above, this is important to make sure that we do not present partial assignments to the receiver that might later turn out to be 
			// impossible had we continued building the assignment.
			
			//if(globalIndex + run.getLength() < totalLength){ // otherwise, for edge assignments the for loop wouldn't execute and allRightInconsistent would default to true (which is obviously wrong since there are no squares to be inconsistent)
			
			boolean allRightInconsistent = true;
			//for(int i=globalIndex + run.getLength(); i<totalLength; i++){
			for(int j=runOffset; j<workingAssignment.getTotalLength(); j++){
				
				/*LocalDecompositionIndex localIndex = workingAssignment.globalToLocal(j);
					AssignmentSlot slot = slots[localIndex.sequenceIndex][localIndex.sequenceOffset];*/
				AssignmentSlot slot = workingAssignment.getSlotAt(j);
				
				if(slot.isValid()){
					allRightInconsistent = false;
					break;
				}
				
			}
			
			if(allRightInconsistent){
				
				// unassign the run so we can continue with the parent
				workingAssignment.unassign(subjectRun, runOffset);
				
				//throw new NoFurtherConsistencyInformationException("Not assigning run; all further squares are known to be inconsistent and cannot create useful information");
				
				// no further consistency information -- manually inform the callback of the partial assignment
				
				// endIndex argument is exclusive, i.e. update the consistency of everything right up until the run that would get assigned on all-inconsistent ground
				generationCallback.receivePartialAssignment(workingAssignment, 0, runOffset);
				
				// (!) not continue; continue would mean trying the next position for this run, but we already know that no position for this run will ever get assigned because it's on all-inconsistent grounds
				// so instead of trying the next position, we should just return control to the parent recursive call
				return;
				
			}
			
			//}
			
			
			// recursion, maestro
			/*try {*/
			generateAssignmentsRecursive(runs.subList(1, runs.size()), workingAssignment, newBaseOffset, generationCallback);
			//runAssignmentsGenerated += validChildAssignments;
			/*}
			catch(TerminateAssignmentGenerationException tagex){
				// callback requested termination
				throw tagex;
			}*/
			
			try {
				workingAssignment.unassign(subjectRun, runOffset);
			}
			catch(InvalidRunConfigurationException ircex){
				// shouldn't happen, the run assignment worked and the recursive call should not update anything before
				// its startOffset so any run that was succesfully assigned should also be un-assignable
				throw ircex;
			}
			
		}
		
		if(runAssignmentsGenerated == 0){
			// evidently, there are no valid placements for subjectRun
			// TODO: does anything need to be done here? don't think so, I think it's only an error if the entire recursion
			// tree returns 0 -- it's perfectly valid for a subtree returns no valid placements. It's only a problem if 
			// the entire tree returns no valid placements.
		}
		
		//return;
		
	}
	
}
