package com.voetsjoeba.nonogram.puzzle;

import com.voetsjoeba.nonogram.structure.StandardPuzzle;


public class FullRowPuzzle extends StandardPuzzle {
	
	public FullRowPuzzle(){
		
		super(
			
			new int[][]{
				{3,1,2,3,1,3,1}
			},
			
			new int[][]{
				{1},
				{1},
				{1},
				{0},
				{1},
				{0},
				{1},
				{1},
				{0},
				{1},
				{1},
				{1},
				{0},
				{1},
				{0},
				{1},
				{1},
				{1},
				{0},
				{1}	
			}
			
		);
		
	}
	
}
