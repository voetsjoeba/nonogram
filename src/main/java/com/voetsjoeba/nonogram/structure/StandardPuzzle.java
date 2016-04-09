package com.voetsjoeba.nonogram.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.voetsjoeba.nonogram.structure.api.Puzzle;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Square;

/**
 * Default {@link Puzzle} implementation.
 * 
 * @author Jeroen De Ridder
 */
public class StandardPuzzle implements Puzzle {
	
	private final int[][] rowRuns;
	private final int[][] columnRuns;
	
	private List<Row> rows;
	private List<Row> columns;
	
	public StandardPuzzle(int[][] rowRuns, int[][] columnRuns){
		
		this.rowRuns = rowRuns;
		this.columnRuns = columnRuns;
		
		initPuzzle();
		
	}
	
	private void initPuzzle(){
		
		int rowCount = rowRuns.length;
		int columnCount = columnRuns.length;
		
		rows = new ArrayList<Row>(rowCount);
		for(int i=0; i<rowCount; i++){
			
			// build squares
			List<Square> squares = new ArrayList<Square>(columnCount);
			for(int j=0; j<columnCount; j++){
				StandardSquare s = new StandardSquare(i, j);
				squares.add(s);
			}
			
			Row row = new StandardRow(Orientation.HORIZONTAL, i, squares, rowRuns[i]);
			rows.add(row);
			
		}
		
		
		columns = new ArrayList<Row>(columnCount);
		for(int i=0; i<columnCount; i++){
			
			// build (column) row
			List<Square> squares = new ArrayList<Square>(rowCount);
			for(int j=0; j<rowCount; j++){
				Row row = rows.get(j);
				squares.add(row.getSquare(i));
			}
			
			Row column = new StandardRow(Orientation.VERTICAL, i, squares, columnRuns[i]);
			columns.add(column);
			
		}
		
	}
	
	public List<Row> getRows() {
		return Collections.unmodifiableList(rows);
	}
	
	public List<Row> getColumns() {
		return Collections.unmodifiableList(columns);
	}
	
	public Row getRow(int index){
		return rows.get(index);
	}
	
	public Row getColumn(int index){
		return columns.get(index);
	}
	
	public Square getSquare(int column, int row){
		return rows.get(row).getSquare(column);
	}
	
	public int getMaxRowRunCount(){
		
		int maxRowRunCount = 0;
		
		for(Row row : rows){
			int runCount = row.getRuns().size();
			if(runCount > maxRowRunCount) maxRowRunCount = runCount;
		}
		
		return maxRowRunCount;
		
	}
	
	public int getMaxColumnRunCount(){
		
		int maxColumnRunCount = 0;
		
		for(Row column : columns){
			int runCount = column.getRuns().size();
			if(runCount > maxColumnRunCount) maxColumnRunCount = runCount;
		}
		
		return maxColumnRunCount;
		
	}
	
	public int getKnownSquareCount() {
		
		int knownSquareCount = 0;
		
		for(Row row : rows){
			for(int i=0; i<row.getLength(); i++){
				if(row.getSquare(i).isStateKnown()) knownSquareCount++;
			}
		}
		
		return knownSquareCount;
		
	}
	
	public String toString(){
		
		StringBuffer stringBuffer = new StringBuffer();
		
		for(Row row : rows){
			
			for(int i=0; i<row.getLength(); i++){
				Square square = row.getSquare(i);
				stringBuffer.append(square.toString() + " ");
			}
			
			stringBuffer.append("\n");
			
		}
		
		return stringBuffer.toString();
		
	}
	
	public boolean isComplete(){
		return (getKnownSquareCount() == getSquareCount());
	}
	
	public int getSquareCount() {
		return (getRows().size() * getColumns().size());
	}
	
}
