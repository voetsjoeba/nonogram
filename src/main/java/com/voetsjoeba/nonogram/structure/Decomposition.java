package com.voetsjoeba.nonogram.structure;

import com.voetsjoeba.nonogram.structure.assignment.LocalDecompositionIndex;

public interface Decomposition {
	
	/**
	 * Returns the total length of this decomposition, i.e. the total amount of squares/slots it contains.
	 */
	public int getTotalLength();
	
	/**
	 * Returns the amount of sequences that this decomposition is split up into.
	 */
	public int getSequenceCount();
	
	/**
	 * Returns the length of the sequence with index <tt>i</tt>.
	 */
	public int getSequenceLength(int i);
	
	/**
	 * Converts a global index (i.e. within the contiguous space of squares/slots) to a local index (i.e. a sequence index + an 
	 * offset within that sequence)
	 * 
	 * @param globalIndex the global index to convert to a local index
	 */
	public LocalDecompositionIndex globalToLocal(int globalIndex);
	
}
