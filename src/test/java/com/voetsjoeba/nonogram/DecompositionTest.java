package com.voetsjoeba.nonogram;

import org.junit.Test;

import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.util.TestUtils;

public class DecompositionTest {
	
	private static final int RUN_COMPLETE = -1; // semantic sugar -- use this as a dummy value for validating the left/rightmost ending/starting index of completed runs
	
	/**
	 * Tests the correctness of the decomposition square-index-to-sequence mapping algorithm.
	 */
	@Test
	public void testSquareIndexToSequence(){
		
		Row row = TestUtils.buildRow("3,1,2,1|-...-.....-....-.....-.-");
		RowDecomposition decomposition = row.getDecomposition();
		
		Assert.assertEquals(18, decomposition.getTotalLength());
		Assert.assertEquals(decomposition.getSequence(0), decomposition.getSequenceContaining(0));
		Assert.assertEquals(decomposition.getSequence(0), decomposition.getSequenceContaining(2));
		Assert.assertEquals(decomposition.getSequence(1), decomposition.getSequenceContaining(3));
		Assert.assertEquals(decomposition.getSequence(1), decomposition.getSequenceContaining(7));
		Assert.assertEquals(decomposition.getSequence(2), decomposition.getSequenceContaining(8));
		Assert.assertEquals(decomposition.getSequence(2), decomposition.getSequenceContaining(11));
		Assert.assertEquals(decomposition.getSequence(3), decomposition.getSequenceContaining(12));
		Assert.assertEquals(decomposition.getSequence(3), decomposition.getSequenceContaining(16));
		Assert.assertEquals(decomposition.getSequence(4), decomposition.getSequenceContaining(17));
		
	}
	
	@Test
	public void testFilledSequenceDecomposition(){
		
		Row row;
		RowDecomposition decomposition;
		
		// in the following sequences, the second run is complete and should not be part of the decomposition, because the decomposition
		// represents only non-completed sequences (and because any single whitespaces on either side should be cleared and removed from the sequences
		// upon decomposition generation)
		
		row = TestUtils.buildRow("1,3,1,1|...... -xxx$2- .....");
		decomposition = row.getDecomposition();
		Assert.assertEquals(2, decomposition.getSequences().size());
		Assert.assertEquals(11, decomposition.getTotalLength()); // the center sequence was excluded, so the total length should not include it
		
		row = TestUtils.buildRow("1,3,1,1|...... -xxx$2.- .....");
		decomposition = row.getDecomposition();
		Assert.assertEquals(2, decomposition.getSequences().size());
		Assert.assertEquals(11, decomposition.getTotalLength());
		
		row = TestUtils.buildRow("1,3,1,1|...... -.xxx$2- .....");
		decomposition = row.getDecomposition();
		Assert.assertEquals(2, decomposition.getSequences().size());
		Assert.assertEquals(11, decomposition.getTotalLength());
		
		row = TestUtils.buildRow("1,3,1,1|...... -.xxx$2.- .....");
		decomposition = row.getDecomposition();
		Assert.assertEquals(2, decomposition.getSequences().size());
		Assert.assertEquals(11, decomposition.getTotalLength());
		
		// these all have multiple whitespace squares on either side and should be maintained in the decomposition
		
		row = TestUtils.buildRow("1,3,1,1|...... -xxx$2..- .....");
		decomposition = row.getDecomposition();
		Assert.assertEquals(3, decomposition.getSequences().size());
		Assert.assertEquals(16, decomposition.getTotalLength());
		
		row = TestUtils.buildRow("1,3,1,1|...... -..xxx$2- .....");
		decomposition = row.getDecomposition();
		Assert.assertEquals(3, decomposition.getSequences().size());
		Assert.assertEquals(16, decomposition.getTotalLength());
		
		row = TestUtils.buildRow("1,3,1,1|...... -..xxx$2..- .....");
		decomposition = row.getDecomposition();
		Assert.assertEquals(3, decomposition.getSequences().size());
		Assert.assertEquals(18, decomposition.getTotalLength());
		
	}
	
	@Test
	public void testFilledSequenceRegression(){
		
		Row row;
		RowDecomposition decomposition;
		
		row = TestUtils.buildRow("3,1,2,1|----xxx$1--x-xx$3-x$4");
		decomposition = row.getDecomposition();
		Assert.assertEquals(1, decomposition.getSequences().size());
		Assert.assertEquals(1, decomposition.getTotalLength()); // the center sequence was excluded, so the total length should not include it
		
	}
	
	@Test
	public void testDecompositionRegression(){
		
		Row row;
		RowDecomposition decomposition;
		
		row = TestUtils.buildRow("5|...x...x...----");
		decomposition = row.getDecomposition();
		Assert.assertEquals(1, decomposition.getSequences().size());
		
		row = TestUtils.buildRow("3,12,7,2,1,4|.............................................");
		decomposition = row.getDecomposition();
		Assert.assertEquals(1, decomposition.getSequences().size());
		assertLeftmostStartingIndices(row, 0, 4, 17, 25, 28, 30);
		
	}
	
	/*public void testDecompositionTotalLength(){
		
		Row row;
		RowDecomposition decomposition;
		
		// make sure the decomposition length is correct when excluding sequences with completed runs
		row = TestUtils.buildRow("1,3,2|........-xx$2");
		decomposition = row.getDecomposition();
		
		Assert.assertEquals(8, actual);
		
	}*/
	
	/**
	 * Ensures that the rightmost starting index calculations take (incomplete) known runs into account.
	 */
	@Test
	public void testRightmostStartingIndicesKnownRuns(){
		
		assertRightmostStartingIndices("2,4,3|.......-.xxx$2.-........", 5, 8, 17);
		
	}
	
	/**
	 * Ensures that the rightmost starting index calculations take "intermediate" complete runs into account, i.e. that starting indices
	 * for runs that come after a completed run can only be after the (left out) completed run sequence.
	 */
	@Test
	public void testRightmostStartingIndicesIntermediateCompleteRuns(){
		
		// ensure that complete runs are skipped over
		assertRightmostStartingIndices("2,4,3|.......-.xxxx$2.-........", 5, RUN_COMPLETE, 12); // second run is complete; in this case, its sequence will be trimmed and removed from the decomposition
		assertRightmostStartingIndices("2,4,3|.......-.xxxx$2-........", 5, RUN_COMPLETE, 12);
		assertRightmostStartingIndices("2,4,3|.......-xxxx$2.-........", 5, RUN_COMPLETE, 12);
		assertRightmostStartingIndices("2,4,3|.......-xxxx$2-........", 5, RUN_COMPLETE, 12);
		
		// ensure that edge complete runs are ignored (these are outside of the decomposition boundaries and would produce wrong results if included)
		assertRightmostStartingIndices("4,3|.........-xxx$2", 5, RUN_COMPLETE);
		assertRightmostStartingIndices("5,2,4|xxxxx$1-.......", RUN_COMPLETE, 0, 3);
		assertRightmostStartingIndices("5,2,4,3|xxxxx$1-.......-xxx$4", RUN_COMPLETE, 0, 3, RUN_COMPLETE);
		
	}
	
	@Test
	public void testStartingIndicesRegression(){
		
		// ensure that starting offsets bounce against edges established from previous iterations, instead of assume that they will bounce 
		// against the sequence edge
		assertLeftmostStartingIndices("5,1,7,3,2|-..xxx$1..x...xxx$3....-xx........", 0, 6, 8, 18, 22);
		assertRightmostStartingIndices("7,5|.......xxx$1....xxx$2..-", 6, 14);
		
	}
	
	/**
	 * Helper method for easy validation of rightmost run start offsets within a decomposition. 
	 * @param formatString format string to construct the testing row; see {@link TestUtils#buildRow(String)}.
	 * @param rightmostIndices a list of expected rightmost starting offsets of runs 0, 1, 2, ... respectively. Completed runs are ignored, so you may pass any value for these.
	 */
	private void assertRightmostStartingIndices(String formatString, int... rightmostIndices){
		assertRightmostStartingIndices(TestUtils.buildRow(formatString), rightmostIndices);
	}
	
	/**
	 * Helper method for easy validation of rightmost run start offsets within a decomposition. 
	 * @param row format the testing row; see {@link TestUtils#buildRow(String)}.
	 * @param rightmostIndices a list of expected rightmost starting offsets of runs 0, 1, 2, ... respectively. Completed runs are ignored, so you may pass any value for these.
	 */
	private void assertRightmostStartingIndices(Row row, int... rightmostIndices){
		
		RowDecomposition decomposition = row.getDecomposition();
		
		if(rightmostIndices.length != row.getRunCount()){
			throw new IllegalArgumentException("Expected " + row.getRunCount() + " arguments, received " + rightmostIndices.length);
		}
		
		for(int i=0; i<rightmostIndices.length; i++){
			
			Run run = row.getRun(i);
			if(run.isComplete()) continue; // completed run, no rightmost starting position is calculated for these
			
			int expected = rightmostIndices[i];
			int actual = decomposition.getRunInfo(run).rightmostStartOffset;
			Assert.assertEquals("Expected rightmost starting index " + expected + " for run " + run + ", found " + actual, expected, actual);
		}
		
	}
	
	/**
	 * Helper method for easy validation of leftmost run start offsets within a decomposition. 
	 * @param formatString format string to construct the testing row; see {@link TestUtils#buildRow(String)}.
	 * @param leftmostIndices a list of expected leftmost starting offsets of runs 0, 1, 2, ... respectively. Completed runs are ignored, so you may pass any value for these.
	 */
	private void assertLeftmostStartingIndices(String formatString, int... leftmostIndices){
		assertLeftmostStartingIndices(TestUtils.buildRow(formatString), leftmostIndices);
	}
	
	/**
	 * Helper method for easy validation of leftmost run start offsets within a decomposition. 
	 * @param row format the testing row; see {@link TestUtils#buildRow(String)}.
	 * @param leftmostIndices a list of expected leftmost starting offsets of runs 0, 1, 2, ... respectively. Completed runs are ignored, so you may pass any value for these.
	 */
	private void assertLeftmostStartingIndices(Row row, int... leftmostIndices){
		
		RowDecomposition decomposition = row.getDecomposition();
		
		if(leftmostIndices.length != row.getRunCount()){
			throw new IllegalArgumentException("Expected " + row.getRunCount() + " arguments, received " + leftmostIndices.length);
		}
		
		for(int i=0; i<leftmostIndices.length; i++){
			
			Run run = row.getRun(i);
			if(run.isComplete()) continue; // completed run, no rightmost starting position is calculated for these
			
			int expected = leftmostIndices[i];
			int actual = decomposition.getRunInfo(run).leftmostStartOffset;
			Assert.assertEquals("Expected leftmost starting index " + expected + " for run " + run + ", found " + actual, expected, actual);
			
		}
		
	}
	
}
