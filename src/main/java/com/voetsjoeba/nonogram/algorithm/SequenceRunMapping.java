package com.voetsjoeba.nonogram.algorithm;

import java.util.List;

import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;

public interface SequenceRunMapping {
	
	/**
	 * Asserts that the provided sequence cannot contain the provided run.
	 * @param sequence
	 * @param run
	 */
	public void eliminatePossibility(Sequence sequence, Run run);
	
	/**
	 * Asserts that the provided run belongs to the specified sequence. Removes the run from all other sequences. Removes later run
	 * from earlier sequences and earlier runs from later sequences (since runs that come after a particular run can only be assigned
	 * the same or later sequences and vice-versa).
	 * @param sequence
	 * @param run
	 * @throws IllegalArgumentException if either argument is null, or if the provided sequence does not belong to the mapping.
	 * @throws IllegalStateException if the asserted run is not listed as a possibility in the target sequence. 
	 */
	public void assertMapping(Sequence sequence, Run run);
	
	/**
	 * Returns a list of sequences that have no runs mapped to them.
	 */
	public List<Sequence> getEmptySequences();
	
	/**
	 * Returns a list of all Sequences containing the provided run as a possibility.
	 */
	public List<Sequence> getSequencesContaining(Run run);
	
	public List<Run> getPossibleRunsForSequence(Sequence sequence);
	public List<Sequence> getPossibleSequencesForRun(Run run);
	
}
