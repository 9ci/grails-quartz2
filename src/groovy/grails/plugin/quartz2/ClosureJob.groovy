/*
 * Copyright (c) 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugin.quartz2

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
//import org.quartz.DisallowConcurrentExecution

/**
 * Quartz Job implementation that invokes a Groovy closure
 */
public class ClosureJob implements Job {
    private static Logger log = LoggerFactory.getLogger(ClosureJob.class);

	Closure   groovyClosure
	def applicationContext
	
	public ClosureJob() {}

    void execute(JobExecutionContext jobCtx) {
		log.debug("ClosureJob")
		try{
			int argNum = groovyClosure.getMaximumNumberOfParameters()
			if( argNum == 2){
				groovyClosure(jobCtx,applicationContext)
			}else if(argNum ==1){
				groovyClosure(jobCtx)
			}else{
				groovyClosure()
			}
			
		}catch(JobExecutionException e){
			throw e
		}
		catch(e){
			throw new JobExecutionException(e)
		}
		
	}
	
	static SimpleJobDetail createJob(Closure c){
		def det = new SimpleJobDetail()
		det.jobClass = ClosureJob.class
		det.jobDataMap.putAll([groovyClosure:c])
		return det
	}
	
	static SimpleJobDetail createJob(String name, Closure c){
		def jobDet = new SimpleJobDetail(name,ClosureJob.class, [groovyClosure:c])
		return jobDet
	}
	
	static SimpleJobDetail createJob(Map params, Closure c){
		def det = new SimpleJobDetail(params)
		det.jobClass = ClosureJob.class
		det.jobDataMap.putAll([groovyClosure:c])
		return det
	}
	
}