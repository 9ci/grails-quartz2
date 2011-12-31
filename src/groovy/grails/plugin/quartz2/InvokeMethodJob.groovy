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
public class InvokeMethodJob implements Job {
    private static Logger log = LoggerFactory.getLogger(InvokeMethodJob.class);
	
	Class    targetClass //the class if this is a static
	Object   targetObject
	//String   targetBeanName 
	String   targetMethod
	//String   staticMethod
	def arguments 
	def grailsApplication
	
	public InvokeMethodJob() {}

    void execute(JobExecutionContext jobCtx) {
		log.debug("BeanMethodJob called for $targetObject method:$targetMethod targetClass:${targetClass?.name} arguments:$arguments")
		try{
			if(targetClass){
				invokeStatic( jobCtx)
			}
			else if(targetObject){
				invokeNormal( jobCtx)
			}
			else{
				throw new JobExecutionException("Either targetClass or targetObject need to be set for InvokeMethodJob.")
			}
		}catch(JobExecutionException e){
			throw e
		}
		catch(e){
			throw new JobExecutionException(e)
		}
		
	}
	
	void invokeNormal(JobExecutionContext jobCtx){
		try {
			if(!targetMethod) throw new JobExecutionException("targetMethod must be specified")
			jobCtx.result = targetObject.invokeMethod( targetMethod, makeArgs())
		}
		catch(JobExecutionException e){
			throw e
		}
		catch(e){
			throw new JobExecutionException(e)
		}
	}
	
	void invokeStatic(JobExecutionContext jobCtx){
		try{
			jobCtx.result = targetClass.metaClass.invokeStaticMethod(targetClass, targetMethod, makeArgs())
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
}