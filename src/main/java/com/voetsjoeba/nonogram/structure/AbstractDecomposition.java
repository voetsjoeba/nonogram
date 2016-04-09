package com.voetsjoeba.nonogram.structure;

import com.voetsjoeba.nonogram.exception.InconsistentDecompositionException;
import com.voetsjoeba.nonogram.structure.assignment.LocalDecompositionIndex;

/**
 * Provides common functionality for decompositions.
 * 
 * @author Jeroen De Ridder
 */
public abstract class AbstractDecomposition implements Decomposition {
	
	public LocalDecompositionIndex globalToLocal(int globalIndex){
		
		int totalLength = getTotalLength();
		
		if(globalIndex < 0 || globalIndex >= totalLength){
			throw new IllegalArgumentException("Invalid global index '" + globalIndex + "'; must range between 0 and " + (totalLength - 1));
		}
		
		int sequenceOffset = 0;
		int sequenceIndex = 0;
		
		int sequenceCount = getSequenceCount();
		while(sequenceIndex < sequenceCount){
			
			//int candidateLength = sequences.get(sequenceIndex).getLength();
			int candidateLength = getSequenceLength(sequenceIndex);
			
			// check whether the squareIndex lies within this candidate (see also the else clause)
			if(globalIndex < candidateLength){
				
				// sequenceIndex is now correct, now find the offset within the sequence
				// since we're subtracting the sequence lengths from globalIndex at every iteration, 
				// the sequence offset is equal to simply globalIndex
				
				sequenceOffset = globalIndex;
				break;
				
			} else {
				// subtract candidate length from index and move to next sequence
				globalIndex -= candidateLength;
			}
			
			sequenceIndex++;
			
		}
		
		if(globalIndex < 0){
			// no sequence found or invalid global index
			throw new InconsistentDecompositionException("Could not convert global index '" + globalIndex + "' to local index; no corresponding sequence found");
		}
		
		return new LocalDecompositionIndex(sequenceIndex, sequenceOffset);
		
	}
	
}
