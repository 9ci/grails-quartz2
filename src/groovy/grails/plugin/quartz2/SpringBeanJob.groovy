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
 * Quartz Job implementation that invokes a specified method of an object, or static if class is specified.
 * this has the DisallowConcurrentExecution set to true
 */
public class SpringBeanJob implements Job {
    private static Logger log = LoggerFactory.getLogger(SpringBeanJob.class);

	def  applicationContext
	String  bean 
	String  method
	def arguments 
	def grailsApplication
	
	public SpringBeanJob() {}

    void execute(JobExecutionContext jobCtx) {
		log.debug("SpringBeanJob called for bean:$bean method:$method arguments:$arguments")
		try{
			def beanRef = applicationContext.getBean(bean)
			jobCtx.result = beanRef.invokeMethod( method, makeArgs())
		}catch(JobExecutionException e){
			throw e
		}
		catch(e){
			throw new JobExecutionException(e)
		}
		
	}
	
	Object[] makeArgs(){
		if(!arguments) return new Object[0]
		if(arguments.class.isArray()) return arguments
		if(arguments instanceof Collection) return arguments.toArray()
		//its a single argument then  
		def o = new Object[1]
		o[0]=arguments
		return o
	}
	
	static SimpleJobDetail createJob(Map jobData){
		def name = jobData.remove('name')
		def jobDet = new SimpleJobDetail(name,SpringBeanJob.class, jobData)
		return jobDet
	}
	
	static SimpleJobDetail createJob(String name, String bean, String method, arguments = null){
		def jobDet = new SimpleJobDetail(name,SpringBeanJob.class, [bean:bean, method:method, arguments:arguments])
		return jobDet
	}
	
	static SimpleJobDetail createJob(Map detailParams, Map jobData){
		def det = new SimpleJobDetail(detailParams)
		det.jobClass = SpringBeanJob.class
		det.jobDataMap.putAll(jobData)
		return det
	}
}