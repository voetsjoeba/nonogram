package com.voetsjoeba.nonogram.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.voetsjoeba.nonogram.exception.NoKnownSquaresException;
import com.voetsjoeba.nonogram.exception.RunLengthExceededException;
import com.voetsjoeba.nonogram.exception.UncontiguousRunException;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.structure.api.Square;

/**
 * Default {@link Run} implementation.
 * 
 * @author Jeroen De Ridder
 */
public final class StandardRun implements Run {
	
	private final SortedSet<Square> squares;
	
	private final int length;
	private final int index;
	private final Row row;
	
	public StandardRun(Row row, int index, int length) {
		this.row = row;
		this.index = index;
		this.length = length;
		this.squares = new TreeSet<Square>(row);
	}
	
	public final int getLength() {
		return length;
	}
	
	public final Row getRow() {
		return row;
	}
	
	public final int getIndex(){
		return index;
	}
	
	public final Orientation getOrientation(){
		return row.getOrientation();
	}
	
	public void addSquare(StandardSquare square) throws RunLengthExceededException {
		
		if(isComplete()){
			throw new RunLengthExceededException("Run length of " + length + " exceeded.");
		}
		
		squares.add(square);
		
	}
	
	public boolean containsSquare(Square square){
		return squares.contains(square);
	}
	
	public Square getFirstKnownSquare() throws NoKnownSquaresException {
		if(getKnownSquareCount() <= 0) throw new NoKnownSquaresException("No known squares in " + this);
		return squares.first();
	}
	
	public List<Square> getKnownSquares(){
		return Collections.unmodifiableList(new ArrayList<Square>(squares));
	}
	
	public Square getLastKnownSquare() throws NoKnownSquaresException {
		if(getKnownSquareCount() <= 0) throw new NoKnownSquaresException("No known squares in " + this);
		return squares.last();
	}
	
	// TODO: link to "row" object becomes obsolete as soon as a StandardPuzzleRow creates a PuzzleRowView as activeArea
	
	public int getKnownSquareCount(){
		return squares.size();
	}
	
	public boolean hasKnownSquares() {
		return (getKnownSquareCount() > 0);
	}

	public boolean isComplete(){
		return (getKnownSquareCount() == length);
	}
	
	public boolean isContiguous(){
		
		if(!hasKnownSquares()) return true;
		
		try {
			int firstSquareIndex = getRow().getSquareIndex(getFirstKnownSquare());
			int lastSquareIndex = getRow().getSquareIndex(getLastKnownSquare());
			int knownSquaresIntervalLength = (lastSquareIndex - firstSquareIndex) + 1;
			
			return (knownSquaresIntervalLength == getKnownSquareCount());
		}
		catch(NoKnownSquaresException e) {
			throw new RuntimeException(e); // this shouldn't happen
		}
		
	}
	
	public Sequence toSequence() throws UncontiguousRunException {
		
		if(!isContiguous()){
			throw new UncontiguousRunException();
		}
		
		Sequence runSequence = new StandardSequence(row, squares.first());
		
		int index = 0;
		Iterator<Square> iterator = squares.iterator();
		
		while(iterator.hasNext()){
			
			Square square = iterator.next();
			if(index++ == 0) continue; // skip the first square
			
			runSequence.addSquare(square);
			
		}
		
		return runSequence;
		
	}
	
	public String toString(){
		return "("+index+",length="+length+")";
	}
	
}
