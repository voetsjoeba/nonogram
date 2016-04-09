package com.voetsjoeba.nonogram.structure.assignment;

import com.voetsjoeba.nonogram.structure.api.Run;

public class AssignmentSlot {
	
	private Run run;
	private boolean fixed = false; // whether this slot is known to be filled
	private Run fixedRun = null; // whether a run is known for this slot (also implies fixed == true)
	
	private boolean valid = true; // whether or not assignments that use this slot are valid (by whatever definition of valid you choose)
	
	public AssignmentSlot(){
		
	}
	
	// copy constructor
	public AssignmentSlot(AssignmentSlot slot) {
		this.run = slot.run;
		this.fixed = slot.fixed;
		this.fixedRun = slot.fixedRun;
		this.valid = slot.valid;
	}

	public Run getRun() {
		return run;
	}

	public boolean isFixed() {
		return fixed;
	}

	public Run getFixedRun() {
		return fixedRun;
	}

	public void setRun(Run run) {
		this.run = run;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public void setFixedRun(Run fixedRun) {
		this.fixedRun = fixedRun;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public String toString(){
		
		StringBuffer sb = new StringBuffer();
		
		if(fixedRun != null){
			sb.append("$");
		} else if(fixed){
			sb.append("£");
		}
		sb.append(run);
		
		return sb.toString();
		
	}
	
}
