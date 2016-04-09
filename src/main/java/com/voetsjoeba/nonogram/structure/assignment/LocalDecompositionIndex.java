package com.voetsjoeba.nonogram.structure.assignment;

import com.voetsjoeba.nonogram.structure.Decomposition;

/**
 * A local index within a {@link Decomposition}. Pairs a sequence index and an offset within that sequence.
 * 
 * @author Jeroen De Ridder
 */
public class LocalDecompositionIndex {
	
	// fuck you, getter and setter bloat
	public final int sequenceIndex;
	public final int sequenceOffset;
	
	public LocalDecompositionIndex(){
		this(0,0);
	}
	
	public LocalDecompositionIndex(int sequenceIndex){
		this(sequenceIndex, 0);
	}
	
	public LocalDecompositionIndex(int sequenceIndex, int sequenceOffset){
		this.sequenceIndex = sequenceIndex;
		this.sequenceOffset = sequenceOffset;
	}
	
	@Override
	public String toString() {
		return "(seq="+sequenceIndex+",idx="+sequenceOffset+")";
	}
	
	/*public int getSequenceIndex() {
		return sequenceIndex;
	}
	public int getSequenceOffset() {
		return sequenceOffset;
	}
	public void setSequenceIndex(int sequence) {
		this.sequenceIndex = sequence;
	}
	public void setSequenceOffset(int offset) {
		this.sequenceOffset = offset;
	}*/
	
}
