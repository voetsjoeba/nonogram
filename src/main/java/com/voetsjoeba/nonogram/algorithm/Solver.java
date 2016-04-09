package com.voetsjoeba.nonogram.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.voetsjoeba.nonogram.algorithm.rowsolver.ClearExcessSequencesSolver;
import com.voetsjoeba.nonogram.algorithm.rowsolver.ExhaustiveRowSolver;
import com.voetsjoeba.nonogram.algorithm.rowsolver.IdentifyUnknownRunsSolver;
import com.voetsjoeba.nonogram.algorithm.rowsolver.MercuryBounceSolver;
import com.voetsjoeba.nonogram.algorithm.rowsolver.OverlappingRowSolver;
import com.voetsjoeba.nonogram.algorithm.rowsolver.RowSolver;
import com.voetsjoeba.nonogram.algorithm.rowsolver.SplitJoinSolver;
import com.voetsjoeba.nonogram.algorithm.rowsolver.old.ConnectKnownIncompleteRunsSolver;
import com.voetsjoeba.nonogram.algorithm.rowsolver.old.ExpandKnownIncompleteRunsSolver;
import com.voetsjoeba.nonogram.algorithm.rowsolver.old.ExpandWhitespaceSolver;
import com.voetsjoeba.nonogram.algorithm.rowsolver.old.OldExamineSequencesSolver;
import com.voetsjoeba.nonogram.event.SquareRunSetEvent;
import com.voetsjoeba.nonogram.event.SquareRunSetListener;
import com.voetsjoeba.nonogram.event.SquareStateSetEvent;
import com.voetsjoeba.nonogram.event.SquareStateSetListener;
import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.InconsistentDecompositionException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.api.Puzzle;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;
import com.voetsjoeba.nonogram.ui.StandardUiField;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * Solves a nonogram {@link Puzzle}.
 * 
 * @author Jeroen De Ridder
 */
public class Solver implements SquareRunSetListener, SquareStateSetListener {
	
	private static final Logger log = LoggerFactory.getLogger(Solver.class);
	
	private Puzzle puzzle;
	private List<Row> rows;
	private List<Row> columns;
	private StandardUiField uiField;
	
	private PriorityQueue<Row> rowQueue;
	private List<RowSolver> rowSolvers;
	private RowSolver exhaustiveSolver;
	
	private boolean demonstrateProgress = false;
	private int progressDemonstrationTimeout = 25;
	
	public static Puzzle staticPuzzle;
	
	public Solver(Puzzle puzzle){
		this(puzzle, null);
	}
	
	/**
	 * 
	 * @param puzzle The {@link Puzzle} to be solved.
	 * @param uiField Reference to a GUI panel to be updated when a square's status is changed by the solver (can be null).
	 */
	public Solver(Puzzle puzzle, StandardUiField uiField){
		
		this.puzzle = puzzle;
		this.rows = puzzle.getRows();
		this.columns = puzzle.getColumns();
		this.uiField = uiField;
		
		init();
		
	}
	
	private void init(){
		
		rowQueue = new PriorityQueue<Row>(rows.size()*columns.size(), new RowChangecountOverlapComparator());
		rowSolvers = new ArrayList<RowSolver>();
		
		exhaustiveSolver = new ExhaustiveRowSolver();
		
		staticPuzzle = puzzle;
		
	}
	
	/**
	 * Solves the puzzle. The puzzle's squares' states are updated as the solver progresses.
	 * @throws UnsolvablePuzzleException if a conflict occurs during the solving process
	 */
	public void solve() throws UnsolvablePuzzleException {
		
		registerListeners();
		
		rowSolvers.clear();
		
		// infer as much information as possible before resorting to the exhaustive solver
		rowSolvers.add(new ClearExcessSequencesSolver());
		rowSolvers.add(new SplitJoinSolver());
		rowSolvers.add(new OverlappingRowSolver());
		rowSolvers.add(new ConnectKnownIncompleteRunsSolver());
		rowSolvers.add(new MercuryBounceSolver());
		rowSolvers.add(new ExpandWhitespaceSolver());
		rowSolvers.add(new IdentifyUnknownRunsSolver());
		rowSolvers.add(new ExpandKnownIncompleteRunsSolver());
		rowSolvers.add(new OldExamineSequencesSolver());
		
		//rowSolvers.add(new ExhaustiveRowSolver());
		
		// start solving please
		
		int rowsChecked = 0;
		Row nextRow = null;
		
		long timerStart = System.currentTimeMillis();
		
		rowQueue.clear();
		for(Row row : rows) if(row.getRunCount() > 0) rowQueue.add(row);
		for(Row column : columns) if(column.getRunCount() > 0) rowQueue.add(column);
		
		while(true){
			
			// solve by logic as far as possible
			while(true){
				
				nextRow = rowQueue.poll();
				
				if(nextRow == null){
					
					break;
					
				} else {
					
					if(uiField != null) uiField.setHighlightedRow(nextRow);
					
					try {
						check(nextRow);
					}
					catch(UnsolvablePuzzleException upex){
						throw upex;
					}
					
					rowsChecked++;
					
				}
				
			}
			
			int squaresKnown = puzzle.getKnownSquareCount();
			int totalSquareCount = puzzle.getSquareCount();
			
			if(squaresKnown == totalSquareCount) break;
			
			// puzzle not yet completely solved, resort to exhaustive solver
			log.debug("Resorting to exhaustive solver at {}/{} squares solved", squaresKnown, totalSquareCount);
			
			List<Row> allRows = new ArrayList<Row>(rows.size() * columns.size());
			for(Row row : rows) if(!row.isCompleted()) allRows.add(row);
			for(Row column : columns) if(!column.isCompleted()) allRows.add(column);
			Collections.sort(allRows, new RowChangecountOverlapComparator());
			
			int totalSquaresFoundExhaustive = 0; // total amount of squares found by the exhaustive solver
			
			for(Row row : allRows){
				
				if(uiField != null) uiField.setHighlightedRow(row);
				
				int squaresKnownBefore = row.getKnownSquareCount();
				exhaustiveSolver.solve(row);
				int squaresKnownAfter = row.getKnownSquareCount();
				
				totalSquaresFoundExhaustive += squaresKnownAfter - squaresKnownBefore;
				
				if(squaresKnownAfter > squaresKnownBefore){
					// abort exhaustive solver, continue logic loop
					log.debug("Exhaustive solver solved "+totalSquaresFoundExhaustive+" square(s), continuing logic solver");
					break;
				}
				
			}
			
			if(totalSquaresFoundExhaustive <= 0){
				// exhaustive solver couldn't find anything either, unsolvable puzzle
				break;
			}
			
			
		}
		
		long timerEnd = System.currentTimeMillis();
		if(uiField != null) uiField.setHighlightedRow(null);
		
		int squaresKnown = puzzle.getKnownSquareCount();
		int totalSquareCount = puzzle.getSquareCount();
		boolean puzzleCompleted = (squaresKnown == totalSquareCount);
		
		log.info("Done; solved {}/{} squares in {} ms with {} solving operations.", new Object[]{squaresKnown, totalSquareCount, timerEnd - timerStart, rowsChecked});
		
		for(RowSolver rowSolver : rowSolvers) rowSolver.solvingFinished(puzzleCompleted);
		exhaustiveSolver.solvingFinished(puzzleCompleted);
		
	}
	
	/**
	 * Main solving method; runs several partial solvers against the provided row.
	 * @throws UnsolvablePuzzleException if a conflict occurred during the solving process.
	 */
	private void check(Row row) throws UnsolvablePuzzleException {
		
		row.resetModificationCount();
		
		if(row.getIndex() == 29 && row.getOrientation() == Orientation.VERTICAL){ // herp die fucking derp
			System.currentTimeMillis();
		}
		
		for(RowSolver rowSolver : rowSolvers){
			if(row.isCompleted()) break;
			rowSolver.solve(row);
		}
		
		//System.currentTimeMillis(); // just to have sth to break on
		
		/*try { 
			
			checkOverlappingRuns(row);
			connectKnownIncompleteRuns(row);
			expandWhitespace(row);
			identifyUnknownRuns(row);
			expandKnownIncompleteRuns(row);
			
			examineSequences_old(row);
			examineSequences(row);*/
		
		// if(!row.isDirty()){
		//exhaustiveEnumeration(row);
		// }
		
		/*}
		catch(InconsistentDecompositionException idex){
			throw new UnsolvablePuzzleException(idex);
		}
		catch(ConflictingSquareStateException cssex){
			throw new UnsolvablePuzzleException(cssex);
		}*/
		
	}
	
	
	/**
	 * For the provided completed run, clears squares up until the previous completed run or border (if any)
	 * and up until the next completed run or border (if any).
	 * 
	 * @throws IllegalArgumentException if the provided run is not complete
	 * @throws UnsolvablePuzzleException if a conflict is encountered while demarcating the run
	 * @throws ConflictingSquareStateException if demarcating this run causes a square to receive conflicting state information
	 */
	private void demarcateCompletedRun(Run run) throws ConflictingSquareStateException {
		
		if(!run.isComplete()){
			throw new IllegalArgumentException("Cannot demarcate uncompleted run " + run);
		}
		
		if(run.getLength() <= 0) return; // won't happen in practice, but technically possible for artificial puzzles (like FullRowPuzzle)
		
		Row row = run.getRow(); // TODO: not necessarily the active area of a row, will be the root row
		List<Run> runs = row.getRuns();
		int runIndex = row.getRunIndex(run);
		
		int firstSquareIndex = row.getSquareIndex(run.getFirstKnownSquare());
		int lastSquareIndex = row.getSquareIndex(run.getLastKnownSquare());
		
		if(firstSquareIndex > 0){
			Square before = row.getSquare(firstSquareIndex-1);
			NonogramUtils.setSquareState(before, SquareState.CLEAR, null);
		}
		
		if(lastSquareIndex < row.getLength()-1){
			Square after = row.getSquare(lastSquareIndex+1);
			NonogramUtils.setSquareState(after, SquareState.CLEAR, null);
		}
		
		// if the previous run is complete (or if there is no previous run),
		// clear all squares up until the previous run (or up to the border)
		
		//try {
		
		Run previousRun = (runIndex > 0 ? runs.get(runIndex-1) : null);
		
		if(previousRun == null){
			
			// clear up to border
			row.clearSquares(0, firstSquareIndex-1);
			
		} else if(previousRun.isComplete()){
			
			// clear up to previousRun
			int previousRunLastSquareIndex = row.getSquareIndex(previousRun.getLastKnownSquare());
			row.clearSquares(previousRunLastSquareIndex+1, firstSquareIndex-1);
			
		}
		
		// if the next run is complete (or if there is no next run),
		// clear all squares up until the next run (or up to the border)
		
		Run nextRun = (runIndex < runs.size()-1 ? runs.get(runIndex+1) : null);
		
		if(nextRun == null){
			
			// clear up to border
			row.clearSquares(lastSquareIndex+1, row.getLength()-1);
			
		} else if(nextRun.isComplete()){
			
			// clear up to next run
			int nextRunFirstSquareIndex = row.getSquareIndex(nextRun.getFirstKnownSquare());
			row.clearSquares(lastSquareIndex+1, nextRunFirstSquareIndex-1);
			
		}
		
		//}
		/*catch(AmbiguousSquareStateException asex){
			throw new UnsolvablePuzzleException(asex);
		}*/
		
	}
	
	/**
	 * For filled squares with known runs, find any adjacent contiguous sequence of 
	 * filled squares but with unknown runs, and add them to the run.
	 */
	/*private void appendAdjacentSquaresToRuns(PuzzleRow row) throws UnsolvablePuzzleException {
		
		Orientation rowOrientation = row.getOrientation();
		
		for(int i=0; i<row.getLength(); i++){
			
			Square square = row.getSquare(i);
			boolean squareFilled = square.isFilled();
			boolean runKnown = (square.getRun(rowOrientation) != null);
			
			if(squareFilled && !runKnown){
				
				// scan to the right for connected filled squares but with unknown run
				
				int rightExpandIndex = -1;
				
				for(int s=i+1; s<row.getLength(); s++){
					
					Square scannedSquare = row.getSquare(s);
					
					boolean scannedSquareFilled = scannedSquare.isFilled();
					boolean scannedSquareRunKnown = scannedSquare.hasRun(rowOrientation);
					
					if(scannedSquareFilled){
						
						if(scannedSquareRunKnown){
							rightExpandIndex = s;
							break;
						}
						
					} else {
						break;
					}
					
				}
				
				if(rightExpandIndex > -1){
					
					// expand to the right
					for(int j=i; j<rightExpandIndex; j++){
						Run expandRun = row.getSquare(rightExpandIndex).getRun(rowOrientation);
						//row.getSquare(j).registerRun(expandRun);
						setSquareState(row.getSquare(j), Square.State.FILLED, expandRun);
					}
					
				}
				
				
				// scan to the left for connected filled squares but with unknown run
				
				int leftExpandIndex = -1;
				
				for(int s=i-1; s>=0; s--){
					
					Square scannedSquare = row.getSquare(s);
					
					boolean scannedSquareFilled = scannedSquare.isFilled();
					boolean scannedSquareRunKnown = scannedSquare.hasRun(rowOrientation);
					
					if(scannedSquareFilled){
						
						if(scannedSquareRunKnown){
							leftExpandIndex = s;
							break;
						}
						
					} else {
						break;
					}
					
				}
				
				if(leftExpandIndex > -1){
					
					// expand to the left
					for(int j=i; j>leftExpandIndex; j--){
						Run expandRun = row.getSquare(leftExpandIndex).getRun(rowOrientation);
						//row.getSquare(j).registerRun(expandRun);
						setSquareState(row.getSquare(j), Square.State.FILLED, expandRun);
					}
					
				}
				
			}
			
		}
		
	}*/
	
	/**
	 * Adds an updated square's row and column back into the checklist.
	 */
	private void squareUpdated(Square square){
		
		// IMPORTANT: get root level rows and columns! (not views)
		Row row = rows.get(square.getRow());
		Row column = columns.get(square.getColumn());
		
		/*synchronized(checkList){
			if(!row.isCompleted()) checkList.add(row);
			if(!column.isCompleted()) checkList.add(column);
		}*/
		
		//boolean queueUpdated = false;
		
		if(!row.isCompleted() && !rowQueue.contains(row)){
			//if(solverRunning){
			rowQueue.add(row);
			/*} else {
				check(row);
			}*/
		}
		
		if(!column.isCompleted() && !rowQueue.contains(column)){
			//if(solverRunning){
			rowQueue.add(column);
			/*} else {
				check(column);
			}*/
		}
		
		if(demonstrateProgress){
			
			try {
				Thread.sleep(progressDemonstrationTimeout);
			}
			catch(Exception ex){
				
			}
			
		}
		
		if(uiField != null){
			uiField.repaint();
		}
		
	}
	
	public void runSet(SquareRunSetEvent e) {
		
		Square square = e.getSource();
		Run run = square.getRun(e.getOrientation());
		Row row = run.getRow();
		
		// check if the run was completed
		
		if(run.isComplete()){
			
			try {
				demarcateCompletedRun(run);
			}
			catch(ConflictingSquareStateException assex){
				throw new UnsolvablePuzzleException(assex);
			}
			
		} else {
			
			// append adjacent filled squares with unknown runs to run
			try {
				
				//appendAdjacentSquares(square, run);
				int squareIndex = row.getSquareIndex(square);
				Square previousSquare = (squareIndex > 0 ? row.getSquare(squareIndex - 1) : null);
				Square nextSquare = (squareIndex < row.getLength() - 1 ? row.getSquare(squareIndex + 1) : null);
				
				if(previousSquare != null && previousSquare.isFilled() && !previousSquare.hasRun(row.getOrientation())){
					previousSquare.setRun(run);
				}
				
				if(nextSquare != null && nextSquare.isFilled() && !nextSquare.hasRun(row.getOrientation())){
					nextSquare.setRun(run);
				}
				
			}
			catch(ConflictingSquareRunException asrex) {
				throw new UnsolvablePuzzleException(asrex);
			}
			catch(RunLengthExceededException rleex) {
				throw new UnsolvablePuzzleException(rleex);
			}
			
		}
		
		squareUpdated(square);
		
	}
	
	public void squareStateSet(SquareStateSetEvent e) {
		Square square = e.getSource();
		squareUpdated(square);
	}
	
	private void registerListeners(){
		
		for(Row row : rows){
			
			for(int i=0; i<row.getLength(); i++){
				Square square = row.getSquare(i);
				square.addRunSetListener(this);
				square.addStateSetListener(this);
			}
			
		}
		
	}
	
	/**
	 * Clears all squares in the provided row, starting at startIndex (inclusive) up to endIndex (inclusive).
	 * @throws UnsolvablePuzzleException 
	 */
	/*private void clearSquares(IPuzzleRow row, int startIndex, int endIndex) throws UnsolvablePuzzleException{
		
		for(int c=startIndex; c<=endIndex; c++){
			setSquareState(row.getSquare(c), Square.State.CLEAR, null);
		}
		
	}*/
	
	
	/* --------- ALGORITHM HELPER FUNCTIONS --------- */
	
	
	
	
	/**
	 * Filters out all cleared sequences in the given list of sequences.
	 * @return A new list containing only the non-cleared sequences present in <i>sequences</i>, in their same order.
	 */
	public static List<Sequence> filterCleared(List<Sequence> sequences){
		
		ArrayList<Sequence> filteredSequences = new ArrayList<Sequence>();
		
		for(Sequence sequence : sequences){
			if(sequence.isCleared()) continue;
			filteredSequences.add(sequence);
		}
		
		return filteredSequences;
		
	}
	
	/**
	 * Finds the particular sequence with a clear-decomposition that can contain the provided first run.
	 * @param firstRun The first run to be assigned to the decomposition.
	 * @param decompositionNonClear if the first run has known squares, but no sequence could be found in the provided decomposition that contains them.
	 * @return
	 * @throws InconsistentDecompositionException
	 */
	private static int findFirstRunSequence(Run firstRun, List<Sequence> decompositionNonClear) throws InconsistentDecompositionException{
		
		int firstRunSequenceIndex = -1;
		
		if(firstRun.hasKnownSquares()){
			
			// first run has a known square, ie. there must be a sequence in the decomposition that contains it
			firstRunSequenceIndex = findSequenceIndexContainingSquare(decompositionNonClear, firstRun.getFirstKnownSquare());
			if(firstRunSequenceIndex < 0) throw new InconsistentDecompositionException("Could not find non-cleared sequence containing square " + firstRun.getFirstKnownSquare());
			
		} else {
			
			// determine the first sequence that can possibly contain the first run
			// logically, this has to be the first sequence that is large enough to contain the first run
			//for(Sequence candidateSequence : decompositionNonClear){
			for(int i = 0; i < decompositionNonClear.size(); i++){
				
				Sequence candidateSequence = decompositionNonClear.get(i);
				if(candidateSequence.getLength() >= firstRun.getLength()){
					firstRunSequenceIndex = i;
					break;
				}
				
			}
			
		}
		
		return firstRunSequenceIndex;
		
	}
	
	private static int findLastRunSequence(Run lastRun, List<Sequence> decompositionNonClear) throws InconsistentDecompositionException{
		
		int lastRunSequenceIndex = -1;
		
		if(lastRun.hasKnownSquares()){
			
			lastRunSequenceIndex = findSequenceIndexContainingSquare(decompositionNonClear, lastRun.getFirstKnownSquare());
			if(lastRunSequenceIndex < 0) throw new InconsistentDecompositionException("Could not find non-cleared sequence containing square " + lastRun.getFirstKnownSquare());
			
		} else {
			
			// determine the last sequence that can possibly contain the last run
			// logically, this has to be the first sequence that is large enough to contain the last run, iterated back-to-front
			
			for(int i=decompositionNonClear.size()-1; i >= 0; i--){
				
				Sequence candidateSequence = decompositionNonClear.get(i);
				if(candidateSequence.getLength() >= lastRun.getLength()){
					lastRunSequenceIndex = i;
					break;
				}
				
			}
			
		}
		
		return lastRunSequenceIndex;
		
	}
	
	/*private void examineSequences(Row row) throws UnsolvablePuzzleException, ConflictingSquareStateException, InconsistentDecompositionException {
		
		// for each non-cleared sequence in the clear/non-clear-decomposition, determine which runs can possibly be assigned to it
		
		// 1) find the leftmost decomposition sequence that can contain the first run and the rightmost decomposition sequence that can contain the last run
		// any sequences outside of those can never contain any run and can be cleared
		
		List<Sequence> decomposition = decomposeByCleared(row);
		List<Sequence> decompositionNonClear = filterCleared(decomposition);
		
		// don't bother if there aren't any cleared squares or if the row is already complete
		if(decompositionNonClear.size() == decomposition.size() || row.isCompleted()) return;
		
		List<Run> runs = row.getRuns();
		int runCount = runs.size();
		Run firstRun = row.getFirstRun();
		Run lastRun = row.getLastRun();
		
		// find leftmost decomposition sequence that can contain the first run
		int firstRunSequenceIndex = findFirstRunSequence(firstRun, decompositionNonClear); 
		if(firstRunSequenceIndex < 0) throw new UnsolvablePuzzleException("Could not identify any sequence capable of containing first run " + firstRun);
		
		// find rightmost decomposition sequence that can contain the last run
		int lastRunSequenceIndex = findLastRunSequence(lastRun, decompositionNonClear);
		if(lastRunSequenceIndex < 0) throw new UnsolvablePuzzleException("Could not identify any sequence capable of containing last run " + lastRun);
		
		// clear any sequences to the left of the first run sequence and to the right of the last run sequence
		
		for(int i = 0; i < firstRunSequenceIndex - 1; i++){
			decompositionNonClear.get(i).clear();
		}
		
		for(int i = decompositionNonClear.size() - 1; i >= lastRunSequenceIndex + 1; i--){
			decompositionNonClear.get(i).clear();
		}
		
		// -------------------------------------------------------------------
		
		// build a run mapping and refine it along the way
		
		StandardSequenceRunMapping sequenceRunMapping = new StandardSequenceRunMapping(decompositionNonClear, row.getRuns());
		
		// assert the sequences of runs with known squares
		
		for(Run run : runs){
			
			if(!run.hasKnownSquares()) continue;
			
			Sequence containingSequence = decompositionNonClear.get(findSequenceIndexContainingSquare(decompositionNonClear, run.getFirstKnownSquare()));
			sequenceRunMapping.assertMapping(containingSequence, run);
			
		}
		
		// maps each sequence of the decomposition to the relative index of the square within that sequence where the next run can start at its earliest
		// Since runs must necessarily be assigned left-to-right and a single sequence can host multiple runs, this starting square index within
		// each sequence will shift to the right as (possibly multiple) runs are assigned to the sequence.
		
		/*Map<Sequence, Integer> sequenceRunAssignmentStartSquares = new HashMap<Sequence, Integer>();
		
		for(Sequence sequence : decompositionNonClear){
			sequenceRunAssignmentStartSquares.put(sequence, 0); // initialize at 0, all assignments start out at the front
		}*/
	
	// go go gadget solver
	
	/*for(Run run : runs){
			
			List<Sequence> possibleSequences = sequenceRunMapping.getPossibleSequencesForRun(run);
			for(Sequence possibleSequence : possibleSequences){
				
				//int startAssignmentSquareIndex = sequenceRunAssignmentStartSquares.get(possibleSequence); 
				
				// try to assign this run to the given sequence
				// two cases can occur: either the run doesn't have any known squares in this sequence,
				// or it does, which must mean that this is the only possible sequence for this run (due to the contract of assertMapping).
				
				boolean hasKnownSquares = run.hasKnownSquares();
				if(hasKnownSquares){
					
				} else {
					
					// see if <runLength> squares can be filled starting at the startSquare for this sequence
					
				}
				
			}
			
		}
		
	}*/
	
	/**
	 * From the provided list of sequences, returns the index of the one containing the provided square or -1 if no such sequence exists.
	 */
	public static int findSequenceIndexContainingSquare(List<Sequence> sequences, Square square){
		
		for(int i = 0; i < sequences.size(); i++){
			if(sequences.get(i).contains(square)) return i;
		}
		
		return -1;
		
	}
	
	/**
	 * Determines whether or not the provided {@link Run} can be assigned to the {@link Sequence} with index sequenceIndex within
	 * the provided decomposition.
	 */
	public static boolean isRunAssignmentPossible(Row row, List<Sequence> decomposition, int sequenceIndex, Run run){
		
		// first check if the sequence's length is sufficient to contain the run
		//     - if not, then this run cannot be assigned to this sequence
		//     - if it is, check if there is any other run 
		
		return true;
		
	}
	
	public void setDemonstrateProgress(boolean demonstrateProgress) {
		this.demonstrateProgress = demonstrateProgress;
	}
	
}
