package com.voetsjoeba.nonogram.algorithm.rowsolver;

import org.junit.Before;
import org.junit.Test;

import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.util.TestUtils;

public class IdentifyUnknownRunsTest {
	
	protected IdentifyUnknownRunsSolver solver;
	
	@Before
	public void beforeEach(){
		solver = new IdentifyUnknownRunsSolver();
	}
	
	/**
	 * Ensures that no sequence receives possible runs that are smaller than the sequence's length
	 */
	@Test
	public void testRunLengthPossibleRuns(){
		
		Row row;
		
		row = TestUtils.buildRow("1,7,5,7|.....xx.x.............-xxxxxxx$4");
		solver.solve(row);
		
		// TODO: save unknownSequences in identifyunknownruns solver so that they can be retrieved and checked here
		
	}
	
}
