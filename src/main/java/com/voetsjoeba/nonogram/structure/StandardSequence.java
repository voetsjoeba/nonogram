package com.voetsjoeba.nonogram.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.voetsjoeba.nonogram.exception.ConflictingSquareRunException;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.exception.NoSuchSquareException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;

/**
 * Default {@link Sequence} implementation.
 * 
 * @author Jeroen De Ridder
 */
public class StandardSequence implements Sequence {
	
	private final Row row;
	private LinkedList<Square> squares;
	
	/**
	 * Constructs a new sequence consisting of a single square.
	 * @param row
	 * @param startingSquare
	 */
	public StandardSequence(Row row, Square startingSquare) {
		this.row = row;
		init(startingSquare);
	}
	
	/**
	 * Constructs a new sequence consisting of the squares from <tt>startIndex</tt> up to and including <tt>endIndex</tt>.
	 * @param row
	 * @param startIndex
	 * @param endIndex
	 */
	public StandardSequence(Row row, int startIndex, int endIndex){
		this.row = row;
		init(startIndex, endIndex);
	}
	
	private void init(Square startingSquare){
		squares = new LinkedList<Square>();
		squares.add(startingSquare);
	}
	
	private void init(int startIndex, int endIndex){
		init(row.getSquare(startIndex));
		for(int i=startIndex+1; i<=endIndex; i++) addSquare(row.getSquare(i));
	}
	
	public Row getRow() {
		return row;
	}
	
	public int getLength(){
		return squares.size();
	}
	
	public Square getFirstSquare(){
		return squares.getFirst();
	}
	
	public Square getLastSquare(){
		return squares.getLast();
	}
	
	public Square getSquare(int index) {
		return squares.get(index);
	}
	
	// TODO: in Run, the get...RowIndex() methods are evil because the runs keep their reference to the original StandardPuzzleRow instead of switching to their row's active area;
	// in essence, the same is true here, but since sequences are always newly obtained in each iteration, it doesn't happen (yet)
	// so either figure out a way to use the most specific active area from a row (ie. update the references) or just remove the link to the row entirely
	
	public int getFirstSquareRowIndex(){
		return row.getSquareIndex(getFirstSquare());
	}
	
	public int getLastSquareRowIndex(){
		return row.getSquareIndex(getLastSquare());
	}
	
	public int getFirstFilledSquareIndex() throws NoSuchSquareException {
		
		int squareIndex = 0;
		while(squareIndex < squares.size() && !squares.get(squareIndex).isFilled()) squareIndex++;
		
		if(squareIndex >= squares.size()){
			throw new NoSuchSquareException("No filled squares exist within this sequence");
		}
		
		return squareIndex;
		
	}
	
	public int getLastFilledSquareIndex() throws NoSuchSquareException {
		
		int squareIndex = getLength() - 1;
		while(squareIndex >= 0 && !squares.get(squareIndex).isFilled()) squareIndex--;
		
		if(squareIndex < 0){
			throw new NoSuchSquareException("No filled squares exist within this sequence");
		}
		
		return squareIndex;
		
	}
	
	public void clear() throws ConflictingSquareStateException {
		row.clearSquares(getFirstSquareRowIndex(), getLastSquareRowIndex());
	}
	
	public boolean isCleared(){
		
		for(Square square : squares){
			if(!square.isCleared()) return false;
		}
		
		return true;
		
	}
	
	public boolean isFilled(){
		
		for(Square square : squares){
			if(!square.isFilled()) return false;
		}
		
		return true;
		
	}
	
	public List<Square> getSquares(){
		return Collections.unmodifiableList(squares);
	}
	
	public void addSquare(Square square){
		
		// make sure the provided square contiguously extends the sequence
		
		int newSquareIndex = row.getSquareIndex(square);
		int firstSquareRowIndex = getFirstSquareRowIndex();
		int lastSquareRowIndex = getLastSquareRowIndex();
		
		boolean extendsFront = (newSquareIndex == firstSquareRowIndex - 1);
		boolean extendsBack = (newSquareIndex == lastSquareRowIndex + 1);
		if(!(extendsFront || extendsBack)){
			throw new IllegalArgumentException("Provided square" + square + " does not extend sequence [" + firstSquareRowIndex + "," + lastSquareRowIndex + "]");
		}
		
		if(extendsFront){
			squares.addFirst(square);
		} else if(extendsBack) {
			squares.addLast(square);
		}
		
	}
	
	public void assignRun(Run run) throws RunLengthExceededException, ConflictingSquareRunException {
		for(Square square : squares){
			square.setRun(run);
		}
	}
	
	public boolean containsKnownSquares() {
		
		for(Square square : squares){
			if(square.isStateKnown()) return true;
		}
		
		return false;
		
	}
	
	public boolean containsSingleCompleteRun() {
		return (getSingleCompleteRun() != null);
	}
	
	public Run getSingleCompleteRun() {
		
		// there can be only one
		List<Run> completedRuns = getCompletedRuns();
		if(completedRuns.size() != 1) return null;
		
		// to completely fill the sequence, the run's length must equal this sequence's length
		Run completedRun = completedRuns.get(0);
		if(completedRun.getLength() != getLength()) return null;
		
		return completedRun;
		
	}
	
	public List<Run> getCompletedRuns(){
		
		List<Run> completedRuns = new ArrayList<Run>();
		
		int length = getLength();
		Orientation orientation = row.getOrientation();
		
		for(int squareIndex = 0; squareIndex < length; squareIndex++){
			
			Square square = getSquare(squareIndex);
			if(!square.isFilled()) continue;
			
			Run run = square.getRun(orientation);
			if(run == null) continue;
			
			// found first square with an assigned run -- now scan out to the right for more squares of the same run
			// and then check if the amount of squares found equals the run length
			
			// ACHTUNG -- for loop index modifications ahead
			
			int lastSquareIndex = squareIndex;
			while(lastSquareIndex < length){
				if(getSquare(lastSquareIndex).getRun(orientation) != run) break;
				lastSquareIndex++;
			}
			
			int presentRunLength = (lastSquareIndex - squareIndex);
			if(presentRunLength == run.getLength()){
				completedRuns.add(run);
			}
			
			squareIndex = lastSquareIndex - 1; // continue at the last square (-1 to balance out the +1 of the for loop)
			
		}
		
		return completedRuns;
		
	}
	
	public boolean containsFilledSquares() {
		
		for(Square square : squares){
			if(square.isFilled()) return true;
		}
		
		return false;
		
	}
	
	public int getKnownSquareCount() {
		
		int count = 0;
		for(Square square : squares){
			if(square.isStateKnown()) count++;
		}
		return count;
		
	}
	
	public boolean contains(Square square){
		return squares.contains(square);
	}
	
	public int getSquareIndex(Square square) throws NoSuchSquareException {
		
		int result = 0;
		
		while(squares.get(result) != square){
			result++;
		}
		
		if(result >= squares.size()){
			throw new NoSuchSquareException("Cannot return square index; square " + square + " does not exist within this sequence");
		}
		
		return result;
		
	}
	
	public String toString(){
		return squares.toString();
	}
	
	public void trimLeft() throws ConflictingSquareStateException {
		
		int squareIndex = 0;
		while(squareIndex < squares.size() && !squares.get(squareIndex).isStateKnown()){
			squares.get(squareIndex).setState(SquareState.CLEAR);
			squareIndex++;
		}
		
		squares = new LinkedList<Square>(squares.subList(squareIndex, squares.size())); // TODO: the new LinkedList is retarded, find a way to do this more elegantly
		
	}
	
	public void trimRight() throws ConflictingSquareStateException {
		
		int squareIndex = squares.size() - 1;
		while(squareIndex >= 0 && !squares.get(squareIndex).isStateKnown()){
			squares.get(squareIndex).setState(SquareState.CLEAR);
			squareIndex--;
		}
		
		squares = new LinkedList<Square>(squares.subList(0, squareIndex + 1));
		
	}
	
}
