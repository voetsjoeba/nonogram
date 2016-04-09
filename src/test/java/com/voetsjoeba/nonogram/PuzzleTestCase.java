package com.voetsjoeba.nonogram;

import junit.framework.TestCase;

import com.voetsjoeba.nonogram.algorithm.Solver;
import com.voetsjoeba.nonogram.structure.api.Puzzle;

public abstract class PuzzleTestCase extends TestCase {
	
	protected void solvePuzzle(Puzzle puzzle) throws Exception {
		Solver solver = new Solver(puzzle);
		solver.solve();
		assertTrue(puzzle.isComplete());
	}
	
}
