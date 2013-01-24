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

import org.quartz.*;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.simpl.PropertySettingJobFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A Quartz 2 job factory which will attempt to first get job instances from the ApplicationContext.
 *
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 * @author Joshua Burnett (modified for Quartz2)
 * @since 0.3.2
 */
public class GrailsJobFactory extends PropertySettingJobFactory implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
		//JobDetail jobDetail = bundle.getJobDetail();
        //Class<? extends Job> jobClass = jobDetail.getJobClass();
        
    	JobDetail jobDetail = bundle.getJobDetail();
		String grailsJobName = (String) jobDetail.getJobDataMap().get(GrailsArtefactJobDetailFactoryBean.JOB_NAME_PARAMETER);
        if (grailsJobName != null) {
            Object job = applicationContext.getBean(grailsJobName);
            if (!jobDetail.isConcurrentExectionDisallowed()) {
            	return new GrailsArtefactNonConcurrentJob(job);
            }
        	return new GrailsArtefactJob(job);
        } else {
            return super.newJob(bundle,scheduler);
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
