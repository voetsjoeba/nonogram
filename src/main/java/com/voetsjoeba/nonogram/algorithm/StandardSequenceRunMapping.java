package com.voetsjoeba.nonogram.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.voetsjoeba.nonogram.structure.api.Run;
import com.voetsjoeba.nonogram.structure.api.Sequence;

public class StandardSequenceRunMapping implements SequenceRunMapping {
	
	// maps a sequence to a list of runs it can contain
	private Map<Sequence, List<Run>> sequencePossibleRuns;
	private List<Sequence> sequences;
	private List<Run> runs;
	
	/**
	 * Builds a run mapping where every provided run is a possibility in every provided sequence.
	 * @param availableSequences The sequences available for having runs assigned to them. This list is typically obtained by grabbing a clear-decomposition and filtering out the cleared sequences.
	 * @param remainingRuns
	 */
	public StandardSequenceRunMapping(List<Sequence> availableSequences, List<Run> remainingRuns){
		
		this.sequences = availableSequences;
		this.runs = remainingRuns;
		
		initMapping();
		
	}
	
	protected void initMapping(){
		
		// initially, each sequence can contain any run
		sequencePossibleRuns = new HashMap<Sequence, List<Run>>();
		
		for(Sequence sequence : sequences){
			
			List<Run> initialRunList = new ArrayList<Run>();
			initialRunList.addAll(runs);
			
			sequencePossibleRuns.put(sequence, initialRunList);
			
		}
		
	}
	
	public void eliminatePossibility(Sequence sequence, Run run){
		
		if(run == null || sequence == null) return;
		
		List<Run> sequenceRuns = sequencePossibleRuns.get(sequence);
		if(sequenceRuns != null) sequenceRuns.remove(run);
		
		// if there is only one remaining possible sequence for the given run, assert it
		if(getPossibleSequencesForRun(run).size() == 1) assertMapping(sequence, run);
		
	}
	
	public void assertMapping(Sequence sequence, Run run){
		
		if(run == null) throw new IllegalArgumentException("\"run\" argument must not be null");
		if(sequence == null) throw new IllegalArgumentException("\"sequence\" argument must not be null");
		
		List<Run> possibleRuns = sequencePossibleRuns.get(sequence);
		if(possibleRuns == null) throw new IllegalArgumentException("Provided sequence does not belong to mapping");
		
		if(!possibleRuns.contains(run)) throw new IllegalStateException("Cannot assert run " + run + " as belonging to sequence " + sequence + "; not listed as a possibility");
		
		// remove run from all other mappings
		for(Sequence listSequence : sequences){
			
			if(listSequence == sequence) continue;
			sequencePossibleRuns.get(listSequence).remove(run);
			
		}
		
		int runIndex = runs.indexOf(run);
		int sequenceIndex = sequences.indexOf(sequence);
		List<Run> laterRuns = runs.subList(runIndex + 1, runs.size());
		List<Run> earlierRuns = runs.subList(0, runIndex);
		
		// remove later runs from all earlier sequences , and earlier runs from later sequences
		for(int i = 0; i < sequences.size(); i++){
			
			Sequence listSequence = sequences.get(i);
			List<Run> listSequencePossibleRuns = sequencePossibleRuns.get(listSequence);
			
			if(i < sequenceIndex){
				
				listSequencePossibleRuns.removeAll(laterRuns);
				
			} else if(i > sequenceIndex) {
				
				listSequencePossibleRuns.removeAll(earlierRuns);
				
			}
			
		}
		
	}
	
	public List<Sequence> getEmptySequences(){
		
		List<Sequence> emptySequences = new ArrayList<Sequence>();
		
		for(Sequence sequence : sequences){
			List<Run> runs = sequencePossibleRuns.get(sequence);
			if(runs.size() <= 0) emptySequences.add(sequence);
		}
		
		return emptySequences;
		
	}
	
	public List<Sequence> getSequencesContaining(Run run){
		
		List<Sequence> containingSequences = new ArrayList<Sequence>();
		
		for(Sequence sequence : sequences){
			List<Run> possibleRuns = sequencePossibleRuns.get(sequence);
			if(possibleRuns.contains(run)) containingSequences.add(sequence);
		}
		
		return containingSequences;
		
	}
	
	public List<Run> getPossibleRunsForSequence(Sequence sequence){
		return sequencePossibleRuns.get(sequence);
	}
	
	public List<Sequence> getPossibleSequencesForRun(Run run){
		
		List<Sequence> possibleSequences = new ArrayList<Sequence>();
		
		for(Sequence sequence : sequences){
			if(sequencePossibleRuns.get(sequence).contains(run)) possibleSequences.add(sequence);
		}
		
		return possibleSequences;
		
	}
	
}
