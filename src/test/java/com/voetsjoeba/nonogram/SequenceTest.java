package com.voetsjoeba.nonogram;

import org.junit.Test;

import com.voetsjoeba.nonogram.structure.RowDecomposition;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;
import com.voetsjoeba.nonogram.util.TestUtils;

public class SequenceTest {
	
	@Test
	public void testSingleCompleteRun(){
		
		Row row;
		RowDecomposition decomposition;
		Run singleCompleteRun;
		Sequence sequence;
		
		// -----------------------
		
		row = TestUtils.buildRow("3|----xxx$1----");
		decomposition = row.getDecomposition();
		
		Assert.assertEquals(0, decomposition.getSequences().size()); // sequence is complete, no need to add it to decomposition
		
		// -----------------------
		
		row = TestUtils.buildRow("3|----xxx$1.----");
		decomposition = row.getDecomposition();
		
		Assert.assertEquals(0, decomposition.getSequences().size());
		/*sequence = decomposition.getSequence(0);
		
		singleCompleteRun = sequence.getSingleCompleteRun();
		Assert.assertNull(singleCompleteRun);*/
		
		// -----------------------
		
		row = TestUtils.buildRow("3|----.xxx$1----");
		decomposition = row.getDecomposition();
		
		Assert.assertEquals(0, decomposition.getSequences().size());
		/*sequence = decomposition.getSequence(0);
		
		singleCompleteRun = sequence.getSingleCompleteRun();
		Assert.assertNull(singleCompleteRun);*/
		
		// -----------------------
		
		row = TestUtils.buildRow("3|----..xxx$1..----");
		decomposition = row.getDecomposition();
		
		Assert.assertEquals(1, decomposition.getSequences().size());
		sequence = decomposition.getSequence(0);
		
		singleCompleteRun = sequence.getSingleCompleteRun();
		Assert.assertNull(singleCompleteRun);
		
	}
	
}
