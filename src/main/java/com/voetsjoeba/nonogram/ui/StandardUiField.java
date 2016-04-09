package com.voetsjoeba.nonogram.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JPanel;

import com.voetsjoeba.nonogram.algorithm.Solver;
import com.voetsjoeba.nonogram.exception.ConflictingSquareStateException;
import com.voetsjoeba.nonogram.structure.Orientation;
import com.voetsjoeba.nonogram.structure.SquareState;
import com.voetsjoeba.nonogram.structure.api.Puzzle;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Square;


public class StandardUiField extends JPanel implements MouseMotionListener, MouseListener {
	
	private Puzzle puzzle;
	
	private FontMetrics runIndicatorFontMetrics;
	
	private static final Color outerBorderColor = Color.BLACK;
	private static final Color innerBorderColor = Color.BLACK;
	
	private static final Color runCompletedColor = Color.BLUE;
	private static final Color runUncompletedColor = Color.BLACK;
	private static final Color currentPositionColor = Color.BLACK;
	
	private int outerBorderSize = 3;
	
	/**
	 * Width and height of inner borders when used horizontally and vertically, respectively.
	 */
	private int innerBorderSize = 1;
	
	/**
	 * Width and height of intermediate borders when used horizontally and vertically, respectively.
	 */
	private int intermediateBorderSize = 3;
	private int squareSize = 15;
	//private int runIndicatorHeight = 12; // for the vertical run indicators
	//private int runIndicatorWidth = 12; // for the horizontal run indicators
	private int runIndicatorSpacingX = 6; // horizontal space between run indicators (for rows)
	private int runIndicatorSpacingY = 0; // vertical space between run indicators (for columns)
	private int runIndicatorEdgeOffset = 5; // offset of run areas from puzzle edges
	private int intermediateBorderSpacing = 5;
	private int currentPositionOffset = 10; // offset from the top left corner for the current position indicator
	
	private int rowCount;
	private int columnCount;
	
	private Dimension checkerboardSize;
	private Dimension runAreaSize;
	
	private int currentSquareX = 0; // x index of the square currently hovered over by the mouse
	private int currentSquareY = 0; // y ''
	
	/**
	 * Whether or not to draw run indicators inside squares
	 */
	private boolean drawSquareRunIndicators = true;
	private boolean highlightRunIndicatorsOnMouseOver = true;
	
	private Square highlightedSquare = null;
	private Color highlightedSquareColor = Color.RED;
	private Color highlightedRunIndicatorColor = Color.RED;
	
	private Row highlightedRow = null;
	private Color highlightedRowColor = Color.ORANGE;
	private Color highlightedRowRunColor = Color.RED;
	
	private boolean drawHighlightedRow = true;
	private boolean drawHighlightedRowRuns = true;
	
	private boolean allowUserClicks = true;
	
	public StandardUiField(Puzzle puzzle){
		this.puzzle = puzzle;
		init();
	}
	
	private void init(){
		
		// calculate preferred size
		
		// surrounding run indicators
		runIndicatorFontMetrics = getFontMetrics(getFont());
		
		//int surroundingRunsWidth = maxRowRunCount * runIndicatorWidth;
		//int surroundingRunsHeight = maxColumnRunCount * runIndicatorHeight;
		int surroundingRunsWidth = getRunAreaWidth(puzzle);
		int surroundingRunsHeight = getRunAreaHeight(puzzle);
		
		runAreaSize = new Dimension(surroundingRunsWidth, surroundingRunsHeight);
		
		// puzzle checkerboard
		
		rowCount = puzzle.getRows().size();
		columnCount = puzzle.getColumns().size();
		
		int horizontalIntermediateBorderCount = (columnCount-1)/intermediateBorderSpacing; // integer division, floored result
		int verticalIntermediateBorderCount = (rowCount-1)/intermediateBorderSpacing;
		
		int checkerboardWidth = 2*outerBorderSize + squareSize*columnCount + innerBorderSize*(columnCount-1-horizontalIntermediateBorderCount) + intermediateBorderSize*horizontalIntermediateBorderCount;
		int checkerboardHeight = 2*outerBorderSize + squareSize*rowCount + innerBorderSize*(rowCount-1-verticalIntermediateBorderCount) + intermediateBorderSize*verticalIntermediateBorderCount;
		
		checkerboardSize = new Dimension(checkerboardWidth, checkerboardHeight);
		
		// combine run indicators + checkerboard
		
		int totalWidth = checkerboardSize.width + runAreaSize.width;
		int totalHeight = checkerboardSize.height + runAreaSize.height;
		
		setPreferredSize(new Dimension(totalWidth, totalHeight));
		setBackground(Color.WHITE);
		
		if(highlightRunIndicatorsOnMouseOver){
			addMouseMotionListener(this);
		}
		
		addMouseListener(this);
		
	}
	
	private int getRunAreaHeight(Puzzle puzzle) {
		
		// the line height is the same for all runs
		
		int maxVerticalRuns = puzzle.getMaxColumnRunCount();
		int columnRunAreaHeight = maxVerticalRuns * runIndicatorFontMetrics.getHeight() + (maxVerticalRuns - 1) * runIndicatorSpacingY;
		
		return columnRunAreaHeight + runIndicatorEdgeOffset;
		
	}
	
	private int getRunAreaWidth(Puzzle puzzle){
		
		int rowRunAreaWidth = runIndicatorEdgeOffset;
		for(Row row : puzzle.getRows()){
			
			List<Run> runs = row.getRuns();
			
			int drawWidth = 0;
			for(Run run : runs){
				int stringWidth = runIndicatorFontMetrics.stringWidth(String.valueOf(run.getLength()));
				drawWidth += stringWidth + runIndicatorSpacingX;
			}
			
			if(drawWidth > rowRunAreaWidth){
				rowRunAreaWidth = drawWidth;
			}
			
		}
		
		return rowRunAreaWidth;
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		Font regularFont = getFont();
		Font boldFont = regularFont.deriveFont(Font.BOLD);
		
		/*
		 * RUN AREA
		 */
		
		// - rows
		
		g.translate(0, runAreaSize.height);
		for(int i=0; i<rowCount; i++){
			
			List<Run> runs = puzzle.getRow(i).getRuns();
			int runCount = runs.size();
			
			int currentRunIndicatorOffset = runIndicatorEdgeOffset; // offset from the checkerboard edge to draw the current run indicator
			
			for(int j=0; j<runCount; j++){
				
				// draw runs right-to-left
				Run run = runs.get(runCount-1-j);
				
				// add the drawn text string (+spacing) to the current run indicator offset
				int runIndicatorWidth = runIndicatorFontMetrics.stringWidth(String.valueOf(run.getLength()));
				currentRunIndicatorOffset += runIndicatorWidth;
				
				// determine the drawing positions
				int xPosition = runAreaSize.width - currentRunIndicatorOffset;
				int yPosition = getSquareYPosition(i);
				
				// add spacing for the next iteration
				currentRunIndicatorOffset += runIndicatorSpacingX;
				
				if(run.isComplete()){
					g.setColor(runCompletedColor);
					g.setFont(boldFont);
				} else {
					g.setColor(runUncompletedColor);
					g.setFont(regularFont);
				}
				
				if(highlightedRow != null && drawHighlightedRowRuns){
					if(highlightedRow.getIndex() == i && highlightedRow.getOrientation() == Orientation.HORIZONTAL){
						g.setColor(highlightedRowRunColor);
					}
				}
				
				if(highlightedSquare != null && highlightedSquare.getRun(Orientation.HORIZONTAL) == run){
					g.setColor(highlightedRunIndicatorColor);
					g.setFont(boldFont);
				}
				
				g.drawString(Integer.valueOf(run.getLength()).toString(), xPosition, yPosition + 4*squareSize/5);
				//g.drawString(Integer.valueOf(run.getLength()).toString(), xPosition, yPosition + 4*squareSize/5);
				
			}
			
		}
		g.translate(0, -runAreaSize.height);
		
		// - columns
		
		g.translate(runAreaSize.width, 0);
		for(int i=0; i<columnCount; i++){
			
			List<Run> runs = puzzle.getColumn(i).getRuns();
			int runCount = runs.size();
			
			int currentRunIndicatorOffset = runIndicatorEdgeOffset; 
			
			for(int j=0; j<runCount; j++){
				
				// draw runs bottom to top
				Run run = runs.get(runCount-1-j);
				
				int xPosition = getSquareXPosition(i);
				int yPosition = runAreaSize.height - currentRunIndicatorOffset;
				
				// add the drawn text string (+spacing) to the current run indicator offset (for the next iteration)
				int runIndicatorHeight = runIndicatorFontMetrics.getHeight();
				int runIndicatorWidth = runIndicatorFontMetrics.stringWidth(String.valueOf(run.getLength()));
				currentRunIndicatorOffset += runIndicatorSpacingY + runIndicatorHeight;
				
				if(run.isComplete()){
					g.setColor(runCompletedColor);
					g.setFont(boldFont);
				} else {
					g.setColor(runUncompletedColor);
					g.setFont(regularFont);
				}
				
				if(highlightedRow != null && drawHighlightedRowRuns){
					if(highlightedRow.getIndex() == i && highlightedRow.getOrientation() == Orientation.VERTICAL){
						g.setColor(highlightedRowRunColor);
					}
				}
				
				if(highlightedSquare != null && highlightedSquare.getRun(Orientation.VERTICAL) == run){
					g.setColor(highlightedRunIndicatorColor);
					g.setFont(boldFont);
				}
				
				g.drawString(Integer.valueOf(run.getLength()).toString(), xPosition + squareSize/2 - (runIndicatorWidth/2) /* center it */, yPosition);
				
			}
			
		}
		g.translate(-runAreaSize.width, 0);
		
		
		/*
		 * CHECKERBOARD
		 */
		
		
		// draw checkerboard
		g.translate(runAreaSize.width, runAreaSize.height);
		
		// draw outer border
		g.setColor(outerBorderColor);
		g.fillRect(0, 0, checkerboardSize.width, outerBorderSize); // top
		g.fillRect(checkerboardSize.width - outerBorderSize, 0, outerBorderSize, checkerboardSize.height); // right
		g.fillRect(0, checkerboardSize.height - outerBorderSize, checkerboardSize.width, outerBorderSize); // bottom
		g.fillRect(0, 0, outerBorderSize, checkerboardSize.height); // left
		
		// draw inner borders
		int xOffset = outerBorderSize + squareSize;
		int yOffset = outerBorderSize + squareSize;
		g.setColor(innerBorderColor);
		
		//  - vertical borders
		for(int i=0; i<columnCount; i++){
			
			// TODO: same formula as getSquareXPosition but with a different offset, maybe make functions for both that use a helper function that takes a variable offset?
			int intermediateBordersPassed = i/intermediateBorderSpacing; // integer division, floored result
			int xPosition = xOffset + (i-intermediateBordersPassed)*(squareSize + innerBorderSize) + intermediateBordersPassed*(squareSize + intermediateBorderSize);
			
			if((i+1) % intermediateBorderSpacing == 0){
				//g.fillRect(xPosition, outerBorderSize, intermediateBorderSize, checkerboardSize.height - 2*outerBorderSize);
				g.fillRect(xPosition, -runAreaSize.height, intermediateBorderSize, checkerboardSize.height - outerBorderSize + runAreaSize.height);
			} else {
				//g.fillRect(xPosition, outerBorderSize, innerBorderSize, checkerboardSize.height - 2*outerBorderSize);
				g.fillRect(xPosition, -runAreaSize.height, innerBorderSize, checkerboardSize.height - outerBorderSize + runAreaSize.height);
			}
			
		}
		
		//  - horizontal borders
		for(int i=0; i<rowCount; i++){
			
			int intermediateBordersPassed = i/intermediateBorderSpacing; // integer division, floored result
			int yPosition = yOffset + (i-intermediateBordersPassed)*(squareSize + innerBorderSize) + intermediateBordersPassed*(squareSize + intermediateBorderSize);
			
			if((i+1) % intermediateBorderSpacing == 0){
				//g.fillRect(outerBorderSize, yPosition, checkerboardSize.width - 2*outerBorderSize, intermediateBorderSize);
				g.fillRect(-runAreaSize.width, yPosition, checkerboardSize.width - outerBorderSize + runAreaSize.width, intermediateBorderSize);
			} else {
				//g.fillRect(outerBorderSize, yPosition, checkerboardSize.width - 2*outerBorderSize, innerBorderSize);
				g.fillRect(-runAreaSize.width, yPosition, checkerboardSize.width - outerBorderSize + runAreaSize.width, innerBorderSize);
			}
			
		}
		
		// draw square fills
		
		for(int i=0; i<rowCount; i++){
			for(int j=0; j<columnCount; j++){
				
				int xPosition = getSquareXPosition(j);
				int yPosition = getSquareYPosition(i);
				
				Square square = puzzle.getRow(i).getSquare(j);
				
				if(square == highlightedSquare){
					g.setColor(highlightedSquareColor);
				} else {
					
					g.setColor(StandardUiSquare.getColor(square));
					
					if(highlightedRow != null && drawHighlightedRow){
						
						if(highlightedRow.getOrientation() == Orientation.HORIZONTAL && highlightedRow.getIndex() == i){
							if(!square.isStateKnown()) g.setColor(highlightedRowColor);
						}
						if(highlightedRow.getOrientation() == Orientation.VERTICAL && highlightedRow.getIndex() == j){
							if(!square.isStateKnown()) g.setColor(highlightedRowColor);
						}
						
					}
					
				}
				
				g.fillRect(xPosition, yPosition, squareSize, squareSize);
				
				if(drawSquareRunIndicators){
					
					Run horizontalRun = square.getRun(Orientation.HORIZONTAL);
					Run verticalRun = square.getRun(Orientation.VERTICAL);
					
					if(horizontalRun != null){
						g.setColor(Color.GREEN);
						g.drawLine(xPosition, yPosition + squareSize/2, xPosition + 4, yPosition + squareSize/2);
					}
					
					if(verticalRun != null){
						g.setColor(Color.ORANGE);
						g.drawLine(xPosition + squareSize/2, yPosition, xPosition + squareSize/2, yPosition + 4);
					}
					
				}
				
			}
		}
		
		g.translate(-runAreaSize.width, -runAreaSize.height);
		
		/*
		 * CURRENT POSITION INDICATOR
		 */
		
		g.setColor(currentPositionColor);
		g.translate(currentPositionOffset, currentPositionOffset);
		g.drawString(String.format("%02d,%02d", currentSquareX, currentSquareY), 0, 0);
		g.translate(-currentPositionOffset, -currentPositionOffset);
		
	}
	
	/**
	 * Returns the x position of a square at the provided column index relative to the top left of the checkerboard.
	 */
	private int getSquareXPosition(int j){
		int xIntermediateBordersPassed = j/intermediateBorderSpacing; // integer division, floored result
		int xPosition = outerBorderSize + (j-xIntermediateBordersPassed)*(squareSize + innerBorderSize) + xIntermediateBordersPassed*(squareSize + intermediateBorderSize);
		return xPosition;
	}
	
	/**
	 * Returns the y position of a square at the provided row index relative to the top left of the checkerboard.
	 */
	private int getSquareYPosition(int i){
		int yIntermediateBordersPassed = i/intermediateBorderSpacing; // integer division, floored result
		int yPosition = outerBorderSize + (i-yIntermediateBordersPassed)*(squareSize + innerBorderSize) + yIntermediateBordersPassed*(squareSize + intermediateBorderSize);
		return yPosition;
	}
	
	/**
	 * Returns the x index of a square at the provided horizontal mouse position relative to the top left of the checkerboard.
	 */
	private int getSquareXIndex(int x){
		
		x -= outerBorderSize; // outer border is useless in these calculations, get rid of it
		
		// horizontal size of one block of <intermediateBorderSpacing> squares, including 4 inner borders and 1 intermediate borders
		int intermediatelySpacedSquareBlockWidth = (intermediateBorderSpacing - 1)*(squareSize + innerBorderSize) + (squareSize + intermediateBorderSize);
		int intermediateBordersPassed = x/intermediatelySpacedSquareBlockWidth; // integer division, floored result
		
		int intermediateBlocksSquaresPassed = intermediateBordersPassed * intermediateBorderSpacing; // amount of squares passed from having passed <intermediateBordersPassed> intermediate borders
		int remainingBlocksPassed = (x % intermediatelySpacedSquareBlockWidth)/(squareSize + innerBorderSize); // integer division, floored result
		
		int xIndex = intermediateBlocksSquaresPassed + remainingBlocksPassed;
		return xIndex;
		
	}
	
	private int getSquareYIndex(int y){
		
		y -= outerBorderSize;
		
		int intermediatelySpacedSquareBlockWidth = (intermediateBorderSpacing - 1)*(squareSize + innerBorderSize) + (squareSize + intermediateBorderSize);
		int intermediateBordersPassed = y/intermediatelySpacedSquareBlockWidth; // integer division, floored result
		
		int intermediateBlocksSquaresPassed = intermediateBordersPassed * intermediateBorderSpacing; // amount of squares passed from having passed <intermediateBordersPassed> intermediate borders
		int remainingBlocksPassed = (y % intermediatelySpacedSquareBlockWidth)/(squareSize + innerBorderSize); // integer division, floored result
		
		int yIndex = intermediateBlocksSquaresPassed + remainingBlocksPassed;
		return yIndex;
		
	}
	
	public void setHighlightedRow(Row row){
		highlightedRow = row;
		repaint();
	}
	
	public void mouseMoved(MouseEvent e) {
		
		int xPosition = e.getX();
		int yPosition = e.getY();
		
		// offset mouse cursor relative to the checkerboard area
		xPosition -= runAreaSize.width;
		yPosition -= runAreaSize.height;
		
		// get square at the position
		highlightedSquare = null;
		
		if(xPosition >= 0 && xPosition < checkerboardSize.width && yPosition >= 0 && yPosition < checkerboardSize.height){
			
			currentSquareX = getSquareXIndex(xPosition);
			currentSquareY = getSquareYIndex(yPosition);
			//int xIndex = getSquareXIndex(xPosition);
			//int yIndex = getSquareYIndex(yPosition);
			
			if(currentSquareX >= 0 && currentSquareX < puzzle.getColumns().size() && currentSquareY >= 0 && currentSquareY < puzzle.getRows().size()){
				
				Square square = puzzle.getSquare(currentSquareX, currentSquareY);
				if(square.isFilled()) highlightedSquare = square;
				
			}
			
		}
		
		repaint();
		
	}
	
	public void mouseDragged(MouseEvent e) {
		
	}
	
	public void mouseClicked(MouseEvent e) {
		
		if(!allowUserClicks) return;
		
		int xPosition = e.getX();
		int yPosition = e.getY();
		
		// offset mouse cursor relative to the checkerboard area
		xPosition -= runAreaSize.width;
		yPosition -= runAreaSize.height;
		
		// get square at the position
		highlightedSquare = null;
		
		if(xPosition >= 0 && xPosition < checkerboardSize.width && yPosition >= 0 && yPosition < checkerboardSize.height){
			
			int xIndex = getSquareXIndex(xPosition);
			int yIndex = getSquareYIndex(yPosition);
			
			if(xIndex >= 0 && xIndex < puzzle.getColumns().size() && yIndex >= 0 && yIndex < puzzle.getRows().size()){
				
				Square square = puzzle.getSquare(xIndex, yIndex);
				if(square.isStateKnown()) return;
				
				if(e.getButton() == MouseEvent.BUTTON3){
					
					// flag square as clear
					try {
						square.setState(SquareState.CLEAR);
					}
					catch(ConflictingSquareStateException e1) {
						
					}
					
				} else {
					
					// flag square as clear
					try {
						square.setState(SquareState.FILLED);
					}
					catch(ConflictingSquareStateException e1) {
						
					}
					
				}
				
				final StandardUiField uiField = this;
				new Thread(new Runnable(){
					public void run() {
						Solver solver = new Solver(puzzle, uiField);
						//solver.setDemonstrateProgress(true);
						solver.solve();
					}
				}).start();
				
			}
			
		}
		
		
		
	}
	
	public void mouseEntered(MouseEvent e) {
		
	}
	
	public void mouseExited(MouseEvent e) {
		
	}
	
	public void mousePressed(MouseEvent e) {
		
	}
	
	public void mouseReleased(MouseEvent e) {
		
	}
	
}
