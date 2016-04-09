package com.voetsjoeba.nonogram;

import com.voetsjoeba.nonogram.puzzle.webpbn.SkidPuzzle;

public class WebPbnPuzzleRegressionTest extends PuzzleTestCase {
	
	public void testSkid() throws Exception {
		solvePuzzle(new SkidPuzzle());
	}
	
}
