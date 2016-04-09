package com.voetsjoeba.nonogram.structure;

import com.voetsjoeba.nonogram.structure.api.Run;

/**
 * Holds information about the position of (incomplete) {@link Run}s within a {@link RowDecomposition}.
 * 
 * @author Jeroen De Ridder
 */
public class DecompositionRunInfo {
	
	/**
	 * The subject run.
	 */
	public final Run run;
	
	/**
	 * Index of run's first square when the run is at its rightmost possible position (not taking into account squares that are already filled in).
	 * The returned value is a global index within the decomposition at hand.
	 */
	public int rightmostStartOffset;
	
	/**
	 * Index of run's first square when the run is at its leftmost possible position (not taking into account squares that are already filled in).
	 * The returned value is a global index within the decomposition at hand.
	 */
	public int leftmostStartOffset;
	
	/**
	 * Index of first sequence that can contain this run
	 */
	//public int minSequenceIndex;
	
	/**
	 * Index of last sequence that can contain this run
	 */
	//public int maxSequenceIndex;
	
	public DecompositionRunInfo(Run run) {
		this.run = run;
	}
	
	@Override
	public String toString() {
		return "<"+leftmostStartOffset+","+rightmostStartOffset+">";
	}
	
}
