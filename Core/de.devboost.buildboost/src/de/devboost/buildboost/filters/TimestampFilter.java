package de.devboost.buildboost.filters;


import de.devboost.buildboost.model.IArtifact;

/**
 * Only accepts artifacts that have a time stamp after the given
 * one or artifacts that have no time stamp (-1);
 * 
 * TODO currently unused / for incremental build
 */
public class TimestampFilter extends AbstractFilter {
	
	private long timestamp;
	
	public TimestampFilter(long timestamp) {
		this.timestamp = timestamp;
	}
	
	@Override
	public boolean accept(IArtifact artifact) {
		if (artifact.getTimestamp() < 0) {
			return true;
		}
		else {
			return timestamp < artifact.getTimestamp();
		}
	}

}
