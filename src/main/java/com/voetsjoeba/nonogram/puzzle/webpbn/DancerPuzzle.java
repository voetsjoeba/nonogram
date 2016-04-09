package com.voetsjoeba.nonogram.puzzle.webpbn;

import com.voetsjoeba.nonogram.structure.StandardPuzzle;

public class DancerPuzzle extends StandardPuzzle {
	
	public DancerPuzzle() {
		
		super(
			
			new int[][]{
				{2},
				{2,1},
				{1,1},
				{3},
				{1,1},
				
				{1,1},
				{2},
				{1,1},
				{1,2},
				{2}
			},
			
			new int[][]{
				{2,1},
				{2,1,3},
				{7},
				{1,3},
				{2,1}
			}
				
		);
		
	}
	
}
