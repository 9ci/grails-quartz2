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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import org.codehaus.groovy.grails.support.PersistenceContextInterceptor

public class PersistenceContextJobListener extends JobListenerSupport {
	private static final transient Log log = LogFactory.getLog(PersistenceContextJobListener.class);
    PersistenceContextInterceptor persistenceInterceptor
	public static final transient String PERSITENCE_INIT = "gormSession";
	public static final String NAME = "persistenceContextJobListener"
	
    public String getName() {
        return NAME
    }

    public void jobToBeExecuted(JobExecutionContext context) {
		if( isInitPersistenceContext(context) ){
			persistenceInterceptor.init()
		}
    }

    public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
        if( isInitPersistenceContext(context) ){
			persistenceInterceptor.flush()
			persistenceInterceptor.destroy()
		}
    }
	
	boolean isInitPersistenceContext(context){
		if(context.mergedJobDataMap.containsKey(PERSITENCE_INIT) && context.mergedJobDataMap.get(PERSITENCE_INIT) == false){
			return false
		}else{
			return true
		}
	}
}
