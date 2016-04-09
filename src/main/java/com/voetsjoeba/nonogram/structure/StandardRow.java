package com.voetsjoeba.nonogram.structure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.voetsjoeba.nonogram.event.SquareRunSetEvent;
import com.voetsjoeba.nonogram.event.SquareRunSetListener;
import com.voetsjoeba.nonogram.event.SquareStateSetEvent;
import com.voetsjoeba.nonogram.event.SquareStateSetListener;
import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.InvalidRunConfigurationException;
import com.voetsjoeba.nonogram.exception.NoIncompleteRunsException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Square;
import com.voetsjoeba.nonogram.util.NonogramUtils;

/**
 * Default {@link Row} implementation.
 * 
 * <p>Runs in this row are guaranteed to always be contiguous. That is, whenever a square's run is set, it is evaluated whether other squares
 * for that run are already known. If they are not connected, the intermediate squares are also added to the run.</p>.
 * 
 * @author Jeroen De Ridder
 */
public class StandardRow implements Row, SquareRunSetListener, SquareStateSetListener {
	
	protected int index;
	protected Orientation orientation;
	private int modificationCount = 0;
	
	protected int minRunLength;
	protected int maxRunLength;
	
	protected List<Run> runs;
	protected List<Square> squares;
	protected RowDecomposition decomposition;
	
	protected int knownSquares = 0;
	
	public StandardRow(Orientation orientation, int index, List<Square> squares, int[] runLengths){
		
		this.index = index;
		this.orientation = orientation;
		this.squares = squares;
		
		initListeners();
		initRuns(runLengths);
		
	}
	
	private void initListeners(){
		
		// register listeners
		for(Square square : squares){
			square.addStateSetListener(this);
			square.addRunSetListener(this);
		}
		
	}
	
	/**
	 * @param runLengths
	 * @throws InvalidRunConfigurationException if the minimum required solution length imposed by the runs exceeds the amount of available squares
	 */
	private void initRuns(int[] runLengths){
		
		runs = new ArrayList<Run>();
		
		for(int i=0; i<runLengths.length; i++){
			
			int runLength = runLengths[i];
			if(runLength <= 0) continue;
			
			Run newRun = new StandardRun(this, i, runLength);
			runs.add(newRun);
			
		}
		
		if(runs.size() <= 0){
			
			// no runs, clear the row
			try {
				clearSquares(0, squares.size()-1);
			}
			catch(ConflictingSquareStateException e) {
				// shouldn't happen, no squares should have had their state set yet
				throw new RuntimeException(e);
			}
			
		} else {
			validateRuns();
			calculateMaxMinRunLengths();
		}
		
	}
	
	/**
	 * Recomputes the row decomposition after a (structural) change.
	 */
	private void updateDecomposition(){
		decomposition = new RowDecomposition(this);
	}
	
	// ---- EVENT LISTENERS ---------------------------------------------------------
	
	public void runSet(SquareRunSetEvent e) {
		
		Run run = e.getRun();
		Row row = run.getRow();
		
		// ensure that the run is contiguous
		Square firstSquare = run.getFirstKnownSquare();
		Square lastSquare = run.getLastKnownSquare();
		
		int firstSquareIndex;
		int lastSquareIndex;
		
		if(row.getOrientation() == Orientation.HORIZONTAL){
			firstSquareIndex = firstSquare.getColumn();
			lastSquareIndex = lastSquare.getColumn();
		} else {
			firstSquareIndex = firstSquare.getRow();
			lastSquareIndex = lastSquare.getRow();
		}
		
		if(lastSquareIndex - firstSquareIndex + 1 != run.getKnownSquareCount()){ // i.e. check whether the row is incontiguous
			
			// Solver sets runs on adjacent filled squares, which means that these all need to be removed beforehand (otherwise
			// the solver's neighbor setRun will activate the listener before the loop below will have had the chance to remove it)
			for(int i=firstSquareIndex; i <= lastSquareIndex; i++){
				row.getSquare(i).removeRunSetListener(this);
			}
			
			// assign runs to all squares (while temporarily disabling listeners to prevent an event explosion)
			for(int i=firstSquareIndex; i <= lastSquareIndex; i++){
				
				Square square = row.getSquare(i);
				
				//square.removeRunSetListener(this);
				NonogramUtils.setSquareState(square, SquareState.FILLED, run);
				//square.addRunSetListener(this);
				
			}
			
			for(int i=firstSquareIndex; i <= lastSquareIndex; i++){
				row.getSquare(i).addRunSetListener(this);
			}
			
			assert run.isContiguous() : "Run " + run + " remains uncontiguous after adding a square";
			
		}
		
		decomposition = null; // invalidate decomposition
		modificationCount++;
		
	}
	
	public void squareStateSet(SquareStateSetEvent e) {
		
		Square square = e.getSource();
		assert square.isStateKnown() : "Received a square set event, but the target square's state appears to be unknown";
		
		knownSquares++;
		
		decomposition = null; // invalidate decomposition
		modificationCount++;
		
	}
	
	public int getModificationCount() {
		return modificationCount;
	}
	
	public void resetModificationCount() {
		modificationCount = 0;
	}
	
	// ---- INTERFACE IMPLEMENTATION ---------------------------------------------------------
	
	public Square getSquare(int index){
		return squares.get(index);
	}
	
	public int getSquareIndex(Square square){
		
		// TODO: replace this with a numeric check
		if(!squares.contains(square)){
			throw new IllegalArgumentException("Row " + this + " does not contain square " + square);
		}
		
		if(orientation == Orientation.HORIZONTAL){
			return square.getColumn();
		} else {
			return square.getRow();
		}
		
	}
	
	public int getRunIndex(Run run){
		
		if(!runs.contains(run)){
			throw new IllegalArgumentException("Row " + this + " does not contain run " + run);
		}
		
		return run.getIndex();
		
	}
	
	public int getLength(){
		return squares.size();
	}
	
	public List<Run> getRuns(){
		return runs;
	}
	
	public Run getRun(int index){
		return runs.get(index);
	}
	
	public Run getFirstRun(){
		return getRun(0);
	}
	
	public Run getLastRun(){
		return getRun(runs.size()-1);
	}
	
	public RowDecomposition getDecomposition() {
		if(decomposition == null) updateDecomposition();
		return decomposition;
	}
	
	/**
	 * Ensures that a fit of this row's runs to its squares can exist.
	 * @throws InvalidRunConfigurationException if the minimum required solution length imposed by the runs exceeds the amount of available squares
	 */
	private void validateRuns() throws InvalidRunConfigurationException {
		
		int minLength = getMinimumSolutionLength();
		
		if(minLength > squares.size()){
			throw new InvalidRunConfigurationException("Minimum required run length "+minLength+" exceeds row length "+squares.size());
		}
		
	}
	
	/**
	 * Calculates and saves the maximum and minimum lengths of the runs present in this row.
	 */
	private void calculateMaxMinRunLengths(){
		
		maxRunLength = 0;
		minRunLength = 0;
		
		for(Run run : runs){
			
			int runLength = run.getLength();
			
			if(runLength > maxRunLength){
				maxRunLength = runLength;
			}
			
			if(minRunLength <= 0){
				// first value
				minRunLength = runLength;
			} else if(runLength < minRunLength){
				minRunLength = runLength;
			}
			
		}
		
	}
	
	/*private void updateActiveArea(){
		
		List<Run> uncompletedRuns = getIncompleteRuns();
		if(uncompletedRuns.size() == 0){
			return;
		}
		
		int firstUncompletedRunIndex = getRunIndex(uncompletedRuns.get(0));
		int lastUncompletedRunIndex = getRunIndex(uncompletedRuns.get(uncompletedRuns.size() - 1));
		
		int leftScanOffset = 0;
		
		if(firstUncompletedRunIndex > 0){
			
			// if there is a completed run prior to this first uncompleted run, update the scan offset
			// to be 2 squares to the right of its last square (+1 for the obligatory cleared square, another +1 to skip it)
			Run previousCompletedRun = getRun(firstUncompletedRunIndex - 1);
			Square lastSquare = previousCompletedRun.getLastKnownSquare();
			leftScanOffset = getSquareIndex(lastSquare) + 2;
			
		}
		
		int rightScanOffset = getLength() - 1;
		
		if(lastUncompletedRunIndex < getRuns().size() - 1){
			
			// if there is a completed run after the last uncompleted run, update the right scan offset
			// to be 1 square to the left of its first square (-1 for the obligatory cleared square, another -1 to skip it)
			Run lastCompletedRun = getRun(lastUncompletedRunIndex + 1);
			Square firstSquare = lastCompletedRun.getFirstKnownSquare();
			rightScanOffset = getSquareIndex(firstSquare) - 2;
			
		}
		
		// start scanning for cleared squares from leftScanOffset to the right and from
		// rightScanOffset to the left
		
		while(leftScanOffset < getLength()){
			
			Square scannedSquare = getSquare(leftScanOffset);
			if(!scannedSquare.isCleared()) break;
			
			leftScanOffset++;
			
		}
		
		while(rightScanOffset >= 0){
			
			Square scannedSquare = getSquare(rightScanOffset);
			if(!scannedSquare.isCleared()) break;
			
			rightScanOffset--;
			
		}
		
		// TODO: update the smallerSquaresArea and smallerRunCount booleans to use the actual square count and run count of the already present view
		
		//boolean smallerSquaresArea = (leftScanOffset > 0 || rightScanOffset < getLength() - 1);
		//boolean smallerRunCount = (firstUncompletedRunIndex > 0 || lastUncompletedRunIndex < runs.size() - 1);
		
		/*if(smallerSquaresArea || smallerRunCount){
			PuzzleRow activeArea = getView(leftScanOffset, rightScanOffset, firstUncompletedRunIndex, lastUncompletedRunIndex);
			this.activeArea = activeArea;
		}*/
	
	// we need to construct a new active area here; an algorithm that performed the change causing this update is likely to still be working on an active area
	// if we don't create a new instance and instead update the existing one, the indices can shift rendering the algorithms further calculations incorrect 
	/*activeArea = new StandardPuzzleRowActiveArea(this);
		
		activeArea.setRunStartIndex(firstUncompletedRunIndex);
		activeArea.setRunEndIndex(lastUncompletedRunIndex);
		activeArea.setSquareStartIndex(leftScanOffset);
		activeArea.setSquareEndIndex(rightScanOffset);
		
	}*/
	
	public int getMinimumSolutionLength(){
		return NonogramUtils.getMinimumRunAssignmentLength(runs);
	}
	
	public boolean isCompleted(){
		
		/*for(Square square : squares){
			if(!square.isStateKnown()) return false;
		}
		
		return true;*/
		
		return (knownSquares == getLength());
		
	}
	
	public int getIndex() {
		return index;
	}
	
	public Orientation getOrientation() {
		return orientation;
	}
	
	/**
	 * Compares two squares in this row by their index.
	 * @throws IllegalArgumentException if either square does not belong to this row.
	 */
	public int compare(Square square1, Square square2) {
		
		int square1Index = getSquareIndex(square1);
		int square2Index = getSquareIndex(square2);
		return Integer.valueOf(square1Index).compareTo(square2Index);
		
	}
	
	public int getMaximumRunLength(){
		return maxRunLength;
	}
	
	public int getMinimumRunLength(){
		return minRunLength;
	}
	
	public int getMaximumIncompleteRunLength() {
		
		List<Run> incompleteRuns = getIncompleteRuns();
		if(incompleteRuns.size() <= 0) throw new NoIncompleteRunsException("Cannot take maximum incomplete run length; no incomplete runs");
		
		int maxIncompleteRunLength = 0;
		for(Run run : getIncompleteRuns()){
			
			int runLength = run.getLength();
			if(runLength > maxIncompleteRunLength) maxIncompleteRunLength = runLength;
			
		}
		
		return maxIncompleteRunLength;
		
	}
	
	public int getMinimumIncompleteRunLength() {
		
		List<Run> incompleteRuns = getIncompleteRuns();
		if(incompleteRuns.size() <= 0) throw new NoIncompleteRunsException("Cannot take minimum incomplete run length; no incomplete runs");
		
		int minIncompleteRunLength = -1; // marker value indicating "no value set yet" (assuming that no runs exist with negative lengths)
		for(Run run : getIncompleteRuns()){
			
			int runLength = run.getLength();
			if(minIncompleteRunLength == -1 || runLength < minIncompleteRunLength) minIncompleteRunLength = runLength;
			
		}
		
		return minIncompleteRunLength;
		
	}
	
	public int getKnownSquareCount() {
		
		int knownSquareCount = 0;
		
		for(Square square : squares){
			if(square.isStateKnown()) knownSquareCount++;
		}
		
		return knownSquareCount;
		
	}
	
	public List<Run> getRuns(int from, int to){
		return runs.subList(from, to + 1); // subList's "to" argument is exclusive
	}
	
	public int getRunCount(){
		return runs.size();
	}
	
	public List<Run> getRunsBefore(int runIndex){
		return getRunsBefore(runIndex, false);
	}
	
	public List<Run> getRunsBefore(int runIndex, boolean inclusive){
		return getRuns(0, inclusive ? runIndex : runIndex - 1);
	}
	
	public List<Run> getRunsAfter(int runIndex){
		return getRunsAfter(runIndex, false);
	}
	
	public List<Run> getRunsAfter(int runIndex, boolean inclusive){
		return getRuns(inclusive ? runIndex : runIndex + 1, runs.size() - 1);
	}
	
	public List<Run> getIncompleteRuns(int minLength){
		
		List<Run> incompleteRuns = new LinkedList<Run>();
		
		for(Run run : runs){
			if(!run.isComplete() && run.getLength() >= minLength) incompleteRuns.add(run);
		}
		
		return incompleteRuns;
		
	}
	
	public List<Run> getIncompleteRuns(){
		return getIncompleteRuns(0);
	}
	
	public List<Run> getUnknownRuns(int minLength){
		
		List<Run> unknownRuns = new LinkedList<Run>();
		
		for(Run run : runs){
			if(!run.hasKnownSquares() && run.getLength() >= minLength) unknownRuns.add(run);
		}
		
		return unknownRuns;
		
	}
	
	public List<Run> getUnknownRuns(){
		return getUnknownRuns(0);
	}
	
	public String toString() {
		return "("+index+","+orientation+")";
	}
	
	public void clearSquares(int startIndex, int endIndex) throws ConflictingSquareStateException {
		setSquareStates(startIndex, endIndex, SquareState.CLEAR);
	}
	
	public void fillSquares(int startIndex, int endIndex) throws ConflictingSquareStateException {
		setSquareStates(startIndex, endIndex, SquareState.FILLED);
	}
	
	/**
	 * Helper method; sets all squares from <tt>startIndex</tt> up to and including <tt>endIndex</tt> to state <tt>squareState</tt>. Does not assign a run to any of the filled squares.
	 * 
	 * @throws ConflictingSquareStateException if filling the squares causes any of them to raise an {@link ConflictingSquareStateException}.
	 */
	protected void setSquareStates(int startIndex, int endIndex, SquareState squareState) throws ConflictingSquareStateException {
		for(int c = startIndex; c <= endIndex; c++){
			getSquare(c).setState(squareState);
		}
	}
	
	public void fillSquares(int startIndex, int endIndex, Run run) throws ConflictingSquareStateException, ConflictingSquareRunException, RunLengthExceededException {
		
		for(int c=startIndex; c<=endIndex; c++){
			getSquare(c).setState(SquareState.FILLED);
			getSquare(c).setRun(run);
		}
		
	}
	
}
