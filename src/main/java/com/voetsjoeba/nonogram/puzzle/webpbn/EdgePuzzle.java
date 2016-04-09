package com.voetsjoeba.nonogram.puzzle.webpbn;

import com.voetsjoeba.nonogram.structure.StandardPuzzle;


public class EdgePuzzle extends StandardPuzzle {
	
	public EdgePuzzle() {
		
		super(
			
			new int[][]{
				{1},
				{3},
				{1},
				{2},
				{1},
				{3},
				{3},
				{1},
				{2},
				{2},
				{4}
			},
			
			new int[][]{
				{1},
				{3},
				{1},
				{2,2},
				{2},
				{4},
				{1},
				{3},
				{3},
				{1}
			}
			
		);
		
	}
	
}
