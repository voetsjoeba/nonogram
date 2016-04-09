package com.voetsjoeba.nonogram;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.voetsjoeba.nonogram.algorithm.AssignmentGenerator;
import com.voetsjoeba.nonogram.structure.api.Row;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentDecomposition;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentList;
import com.voetsjoeba.nonogram.structure.assignment.AssignmentListCallback;
import com.voetsjoeba.nonogram.util.TestUtils;

public class AssignmentGenerationTest {
	
	private static final Logger log = LoggerFactory.getLogger(AssignmentGenerationTest.class);
	
	protected static AssignmentList generateAssignmentList(Row row){
		
		AssignmentListCallback listCallback = new AssignmentListCallback();
		(new AssignmentGenerator()).generateAssignments(row, listCallback);
		return listCallback.getAssignments();
		
	}
	
	@Test
	public void test_1x2x4_4x4x4(){
		
		AssignmentList runAssignments = generateAssignmentList(TestUtils.buildRow("1,2,4|....--....-...."));
		Assert.assertEquals(15, runAssignments.size());
		
	}
	
	@Test
	public void testPerfectFit(){
		
		List<AssignmentDecomposition> runAssignments = generateAssignmentList(TestUtils.buildRow("1,2,4|.-..-...."));
		Assert.assertEquals(1, runAssignments.size());
		Assert.assertAssignmentDecomposition("1,2,4|x xx xxxx", runAssignments.get(0));
		
		log.debug(runAssignments.get(0).toString());
		
	}
	
	@Test
	public void testSingularPerfectFit(){
		
		for(int i=1; i<=100; i++){ // i = length of single run
			
			Row row = TestUtils.buildRow(i + "|--"+StringUtils.repeat(".", i)+"--");
			
			List<AssignmentDecomposition> runAssignments = generateAssignmentList(row);
			Assert.assertEquals(1, runAssignments.size());
			Assert.assertAssignmentDecomposition(i+"|"+StringUtils.repeat("x", i), runAssignments.get(0));
			
		}
		
	}
	
	@Test
	public void testPreassignedSquaresRegression(){
		
		AssignmentList assignments;
		
		assignments = generateAssignmentList(TestUtils.buildRow("1,3,1,1|..... .x... ....."));
		Assert.assertEquals(123, assignments.size());
		
		assignments = generateAssignmentList(TestUtils.buildRow("3,4,1|....x xx..x ....."));
		Assert.assertEquals(4, assignments.size());
		Assert.assertContainsDecomposition("3,4,1|xxx.xxxx.x.....", assignments);
		Assert.assertContainsDecomposition("3,4,1|....xxx.xxxx.x.", assignments);
		Assert.assertContainsDecomposition("3,4,1|....xxx.xxxx.x.", assignments);
		Assert.assertContainsDecomposition("3,4,1|....xxx..xxxx.x", assignments);
		
	}
	
	@Test
	public void testPreassignedSquares(){
		
		AssignmentList assignments;
		
		assignments = generateAssignmentList(TestUtils.buildRow("1,2,4|...x$2-....-...."));
		Assert.assertEquals(2, assignments.size());
		Assert.assertContainsDecomposition("1,2,4|x.xx xxxx ....", assignments);
		Assert.assertContainsDecomposition("1,2,4|x.xx .... xxxx", assignments);
		
		assignments = generateAssignmentList(TestUtils.buildRow("1,2,4|..x$2.-....-...."));
		Assert.assertEquals(2, assignments.size());
		Assert.assertContainsDecomposition("1,2,4|x.xx xxxx ....", assignments);
		Assert.assertContainsDecomposition("1,2,4|x.xx .... xxxx", assignments);
		
		assignments = generateAssignmentList(TestUtils.buildRow("1,2,4|.x$2..-....-...."));
		Assert.assertEquals(0, assignments.size());
		
		assignments = generateAssignmentList(TestUtils.buildRow("1,2,4|.x$1..-....-...."));
		Assert.assertEquals(3, assignments.size());
		Assert.assertContainsDecomposition("1,2,4|.x.. xx.. xxxx", assignments);
		Assert.assertContainsDecomposition("1,2,4|.x.. .xx. xxxx", assignments);
		Assert.assertContainsDecomposition("1,2,4|.x.. ..xx xxxx", assignments);
		
		assignments = generateAssignmentList(TestUtils.buildRow("1,2,4|...x$1-....-...."));
		Assert.assertEquals(3, assignments.size());
		Assert.assertContainsDecomposition("1,2,4|...x xx.. xxxx", assignments);
		Assert.assertContainsDecomposition("1,2,4|...x .xx. xxxx", assignments);
		Assert.assertContainsDecomposition("1,2,4|...x ..xx xxxx", assignments);
		
		assignments = generateAssignmentList(TestUtils.buildRow("1,2,4|...x$3-....-...."));
		Assert.assertEquals(0, assignments.size());
		
		assignments = generateAssignmentList(TestUtils.buildRow("1,2,4|....-...x$3-...."));
		Assert.assertEquals(1, assignments.size());
		Assert.assertAssignmentDecomposition("1,2,4|x.xx xxxx ....", assignments.get(0));
		
		assignments = generateAssignmentList(TestUtils.buildRow("1,2,4|....-...x$3-x$3..."));
		Assert.assertEquals(0, assignments.size());
		
		assignments = generateAssignmentList(TestUtils.buildRow("1,2,4|....-...x$3-x$2..."));
		Assert.assertEquals(0, assignments.size());
		
	}
	
	@Test
	public void testCalculateEndOffsetRegression(){
		
		Row row;
		AssignmentDecomposition assignmentDecomposition;
		
		row = TestUtils.buildRow("6|.xxxxx.");
		assignmentDecomposition = new AssignmentDecomposition(row.getDecomposition());
		Assert.assertEquals(1, row.getDecomposition().getRunInfo(row.getFirstRun()).rightmostStartOffset);
		
	}
	
}
