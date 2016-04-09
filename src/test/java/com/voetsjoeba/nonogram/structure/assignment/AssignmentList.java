package com.voetsjoeba.nonogram.structure.assignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.voetsjoeba.nonogram.structure.assignment.AssignmentDecomposition;

/**
 * This is only here to have a prettier toString.
 * 
 * @author Jeroen De Ridder
 */
public class AssignmentList extends ArrayList<AssignmentDecomposition> {
	
	public AssignmentList() {
		super();
	}
	
	public AssignmentList(Collection<? extends AssignmentDecomposition> c) {
		super(c);
	}
	
	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		Iterator<AssignmentDecomposition> it = iterator();
		while(it.hasNext()){
			AssignmentDecomposition assignmentDecomposition = it.next();
			sb.append(assignmentDecomposition.toString());
			sb.append("\n");
		}
		
		return sb.toString();
		
	}
	
}
