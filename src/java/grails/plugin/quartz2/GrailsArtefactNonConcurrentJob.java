package grails.plugin.quartz2;

import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
class GrailsArtefactNonConcurrentJob extends GrailsArtefactJob {
	
	public GrailsArtefactNonConcurrentJob(Object job) {
		super(job);
	}
}
