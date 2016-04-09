package com.voetsjoeba.nonogram.algorithm.rowsolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.voetsjoeba.nonogram.algorithm.AssignmentGenerator;
import com.voetsjoeba.nonogram.algorithm.ProgressiveExtractionCallback;
import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.api.Row;

/**
 * Exhaustively tries all possible solutions to a row. This solver employs a recursive assignment generator that 
 * feeds its generated assignments to a callback function. This function compares the state of each square across
 * all the generated solutions, and remembers which ones have remained in a consistent state throughout all of them.
 * If a square maintains a consistent state throughout all possible solutions, then that state must be the solution
 * for that square.
 * 
 * <p>Assignments are constructed recursively by choosing a position for each of the remaining runs in the row
 * decomposition. At each level in the recursion tree, a run is positioned across its possible locations.</p>
 * 
 * <p>After all assignments have been generated, the consistency information recorded by the callback function are 
 * applied to the row.</p>
 * 
 * <p>The assignment generator employs several optimizations that allow it to ignore certain assignments.
 * <ul>
 *     <li>For runs of which some squares are known, the range of possible positions to place that run in the
 *     generated assignments is reduced to the range implied by the known squares.</li>
 *     <li>If at any point during the construction of an assignment it is observed that a square that needs to have
 *     a run assigned (as evidenced by the fact that it is filled) is in fact not filled, further construction of
 *     that assignment is aborted.</li>
 *     <li>During construction of the assignments, each slot in the working decomposition maintains a flag whether
 *     the corresponding square is known to be consistent. If at some point all slots to the right of a positioned
 *     run are known to be inconsistent, then further placement of runs is aborted since none of them would
 *     contribute any new information about the consistency status of their squares (because at that point we know 
 *     all of them are already inconsistent).<br/>This allows the generator to quickly skip over large lines with
 *     small runs such as 1,1 in a 45-square row.</li>
 * </ul>
 * 
 * @author Jeroen De Ridder
 */
public class ExhaustiveRowSolver extends AbstractRowSolver {
	
	private static final Logger log = LoggerFactory.getLogger(ExhaustiveRowSolver.class);
	
	protected long totalAssignmentsGenerated = 0;
	
	public ExhaustiveRowSolver() {
		
	}
	
	public void solve(Row row) throws UnsolvablePuzzleException {
		
		if(row.getModificationCount() > 0) return; // use this solver only if previous solvers were unable to deduce any information
		RowDecomposition decomposition = row.getDecomposition();
		//int decompositionLength = decomposition.getTotalLength();
		
		//AssignmentListCallback callback = new AssignmentListCallback();
		ProgressiveExtractionCallback callback = new ProgressiveExtractionCallback(decomposition);
		(new AssignmentGenerator()).generateAssignments(row.getIncompleteRuns(), decomposition, callback); // TODO: yay instantiation
		
		long assignmentsGenerated = callback.getTotalGeneratedAssignments();
		if(assignmentsGenerated == 0){
			throw new UnsolvablePuzzleException("No possible run assignments for row " + row);
		}
		
		totalAssignmentsGenerated += assignmentsGenerated;
		
		try {
			callback.applyConsistencyInformation();
		}
		catch(ConflictingSquareStateException e) {
			throw new UnsolvablePuzzleException(e);
		}
		catch(RunLengthExceededException e) {
			throw new UnsolvablePuzzleException(e);
		}
		catch(ConflictingSquareRunException e) {
			throw new UnsolvablePuzzleException(e);
		}
		
		//AssignmentList assignments = listCallback.getAssignments();
		/*if(assignments.size() == 0){
			throw new UnsolvablePuzzleException("No possible run assignments for row " + row);
		}*/
		
		// for each square in the assignment, check whether all possible solutions agree on the clear/filled/filled-with-run state
		/*for(int squareIndex=0; squareIndex<decompositionLength; squareIndex++){
			
			// check whether this square is cleared in all solutions
			boolean allClear = true;
			boolean allFilled = true;
			boolean allSameRun = true;
			Run sameRun = null; // the run that they all have in common (if any)
			
			for(AssignmentDecomposition assignment : assignments){
				
				Run assignedRun = assignment.getRunAt(squareIndex);
				
				allClear = allClear && (assignedRun == null);
				allFilled = allFilled && (assignedRun != null);
				
				if(sameRun == null) sameRun = assignedRun; // initial value
				allSameRun = allSameRun && (assignedRun != null && assignedRun == sameRun);
				
				
			}
			
			assert !(allClear && allFilled) : "A square was both always cleared and always filled after generation solutions -- this is probably a programming error";
			assert !allSameRun || allFilled : "All squares have the same run assigned, but allgedly not all squares even have runs assigned -- this is probably a programming error"; // a => b <=> not(a) v b
			
			try {
				
				Square square = decomposition.getSquare(squareIndex);
				
				if(allClear){
					square.setState(SquareState.CLEAR);
				}
				
				if(allFilled){
					square.setState(SquareState.FILLED);
					if(allSameRun) square.setRun(sameRun);
				}
				
			}
			catch(ConflictingSquareStateException cssex) {
				throw new UnsolvablePuzzleException(cssex);
			}
			catch(RunLengthExceededException rleex) {
				throw new UnsolvablePuzzleException(rleex);
			}
			catch(ConflictingSquareRunException csrex) {
				throw new UnsolvablePuzzleException(csrex);
			}
			
		}*/
		
	}
	
	public long getTotalAssignmentsGenerated() {
		return totalAssignmentsGenerated;
	}
	
	public void solvingFinished(boolean complete) {
		log.info("Generated {} assignment(s)", totalAssignmentsGenerated);
	}
	
}
