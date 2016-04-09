package com.voetsjoeba.nonogram.structure.api;

import java.util.List;

/**
 * Partial interface for any object that can contain runs.
 * 
 * @author Jeroen De Ridder
 */
public interface RunContainer {
	
	public List<Run> getRuns();
	public Run getRun(int index);
	public Run getFirstRun();
	public Run getLastRun();
	
	/**
	 * Returns the list of {@link Run}s in this {@link Row} from index "from" (inclusive) to index "to" (inclusive).
	 */
	public int getRunCount();
	public List<Run> getRuns(int from, int to);
	public List<Run> getRunsBefore(int runIndex);
	public List<Run> getRunsBefore(int runIndex, boolean inclusive);
	public List<Run> getRunsAfter(int runIndex);
	public List<Run> getRunsAfter(int runIndex, boolean inclusive);
	
}
