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

package grails.plugin.quartz2;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * 
 *
 * @author Joshua Burnett
 */
public class GrailsArtefactJobDetailFactoryBean implements FactoryBean, InitializingBean {
    public static final transient String JOB_NAME_PARAMETER = "grailsArtefactJobName";
	
	GrailsJobClass grailsJobClass;
    private SimpleJobDetail jobDetail;


    /**
     * {@inheritDoc}
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {

        if (grailsJobClass == null) {
            throw new IllegalStateException("grailsJobClass is required");
        }

        // Build JobDetail instance.
        jobDetail = new SimpleJobDetail();
		jobDetail.setKey(grailsJobClass.getJobKey());
		jobDetail.setJobClass(GrailsArtefactJob.class);
		jobDetail.setConcurrent(grailsJobClass.isConcurrent());
        jobDetail.getJobDataMap().put(JOB_NAME_PARAMETER, grailsJobClass.getFullName());
		
		//FIXME put the check for sessionRequired
		if(!grailsJobClass.isSessionRequired() || !grailsJobClass.isGormSession()){
			jobDetail.getJobDataMap().put(PersistenceContextJobListener.PERSITENCE_INIT, false);
		}
        jobDetail.setDurability(grailsJobClass.getDurability());
        jobDetail.setRequestsRecovery(grailsJobClass.getRequestsRecovery());

    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() {
        return jobDetail;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return SimpleJobDetail.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }
}
