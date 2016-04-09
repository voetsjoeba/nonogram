package com.voetsjoeba.nonogram.puzzle.webpbn;

import com.voetsjoeba.nonogram.structure.StandardPuzzle;

public class CatPuzzle extends StandardPuzzle {
	
	public CatPuzzle() {
		
		super(
			
			new int[][]{
				{2},
				{2},
				{1},
				{1},
				{1,3},
				
				{2,5},
				{1,7,1,1},
				{1,8,2,2},
				{1,9,5},
				{2,16},
				
				{1,17},
				{7,11},
				{5,5,3},
				{5,4},
				{3,3},
				
				{2,2},
				{2,1},
				{1,1},
				{2,2},
				{2,2}
			},
			
			new int[][]{
				{5},
				{5,3},
				{2,3,4},
				{1,7,2},
				{8},
				
				{9},
				{9},
				{8},
				{7},
				{8},
				
				{9},
				{10},
				{13},
				{6,2},
				{4},
				
				{6},
				{6},
				{5},
				{6},
				{6}
			}
				
		);
		
	}
	
}
