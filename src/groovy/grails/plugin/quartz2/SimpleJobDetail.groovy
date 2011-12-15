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

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.utils.Key;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.quartz.utils.ClassUtils;


/**
 * a Groovy implementation of a JobDetail.
 * allows the contructor to be used for name key and class.
 * has a property for setDisallowConcurrentExecution so the job class does need to be tied to the annotaion
 * makes it easier to put a list of maps in for the JobDataMap
 * 
 * @author Joshua Burnett
 */
class SimpleJobDetail implements Cloneable, java.io.Serializable, JobDetail {

    //String name = Key.createUniqueName(Scheduler.DEFAULT_GROUP)
    //String group = Scheduler.DEFAULT_GROUP
	String name
    String group = Scheduler.DEFAULT_GROUP
    String description
	Class<? extends Job> jobClass
    JobDataMap jobDataMap = new JobDataMap()
    boolean durability = false
    boolean shouldRecover = false
	boolean concurrent = true
	boolean persistJobDataAfterExecution = false
    private transient JobKey key 

   
    SimpleJobDetail() {
    }

    SimpleJobDetail(String name, Class<? extends Job> jobClass) {
		this.name = name
		this.jobClass = jobClass
    }

	SimpleJobDetail(String name, Class<? extends Job> jobClass, Map dataMap) {
		this.name = name
		this.jobClass = jobClass
		if(dataMap instanceof JobDataMap){
			this.jobDataMap = dataMap
		}else{
			this.jobDataMap = new JobDataMap(dataMap)
		}
    }

    String getFullName() {
        return "${key.group}.${key.name}"
    }

    void setName(String name) {
        if (!name || !name.trim() ) throw new IllegalArgumentException("Job name cannot be empty.");

        this.name = name
        this.key = null
    }


    void setGroup(String group) {
        if (!group || !group.trim() ) {
            group = Scheduler.DEFAULT_GROUP
        }
        this.group = group
        this.key = null
    }

	JobKey getKey() {
        if(!this.key) {
            if(!name)
                name = Key.createUniqueName(Scheduler.DEFAULT_GROUP) 
            this.key = new JobKey(name, group);
        }

        return this.key
    }
    
    public void setKey(JobKey jk) {
        if(jk == null) throw new IllegalArgumentException("Key cannot be null!");

        name = jk.name
        group = jk.group
        this.key = jk
    }

	/**
	 * puts a normal map into the jobDataMap
	 */
	void setJobData(Map map){
		@SuppressWarnings("unchecked") // casting to keep API compatible and avoid compiler errors/warnings.
		Map<String, Object> mapTyped = (Map<String, Object>)map
		jobDataMap.putAll(mapTyped)
	}
	

    /**
     * @return whether the associated Job class carries the {@link PersistJobDataAfterExecution} annotation.
     */
	boolean isPersistJobDataAfterExecution() {
		if(persistJobDataAfterExecution){
			return true
		}else{//try the annotation 
			return ClassUtils.isAnnotationPresent(jobClass, PersistJobDataAfterExecution.class);
		}
    }

    /**
     * @return whether the associated Job class carries the {@link DisallowConcurrentExecution} annotation.
     */
    boolean isConcurrentExectionDisallowed() {
        if(!concurrent){
			return true
		} else{
			//try the annotation if concurrent is true just to give it a shot and stick with quartz standard
			return ClassUtils.isAnnotationPresent(jobClass, DisallowConcurrentExecution.class);
		}
        
    }

	/**
     * @return whether the associated Job class carries the {@link DisallowConcurrentExecution} annotation.
     */
    void setConcurrentExectionDisallowed(boolean singleThreaded) {
		concurrent = !singleThreaded
	}

    boolean requestsRecovery() {
        return shouldRecover
    }

	void setRequestsRecovery(boolean shouldRecover) {
        this.shouldRecover = shouldRecover
    }

	void setDurability(boolean durability) {
        this.durability = durability;
    }

    public boolean isDurable() {
        return durability;
    }

    @Override
    String toString() {
        return "JobDetail '${key.group}.${key.name}' :  jobClass: '${jobClass?.name}'"
                + " concurrentExectionDisallowed: " + isConcurrentExectionDisallowed() 
                + " persistJobDataAfterExecution: " + isPersistJobDataAfterExecution() 
                + " isDurable: " + isDurable() + " requestsRecovers: " + requestsRecovery();
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof JobDetail)) {
            return false;
        }
        JobDetail other = (JobDetail) obj;
        if(other.getKey() == null || getKey() == null)
            return false;
        if (!other.getKey().equals(getKey())) {
            return false;
        }
        return true;
    }

    @Override
    int hashCode() {
        return getKey().hashCode();
    }
    
    @Override
    Object clone() {
        SimpleJobDetail copy;
        try {
            copy = (SimpleJobDetail) super.clone();
            if (jobDataMap != null) {
                copy.jobDataMap = (JobDataMap) jobDataMap.clone();
            }
        } catch (CloneNotSupportedException ex) {
            throw new IncompatibleClassChangeError("Not Cloneable.");
        }
        return copy;
    }

	//THIS IS KIND OF FUBAR
    JobBuilder getJobBuilder() {
        JobBuilder b = JobBuilder.newJob()
            .ofType(getJobClass())
            .requestRecovery(requestsRecovery())
            .storeDurably(isDurable())
            .usingJobData(getJobDataMap())
            .withDescription(getDescription())
            .withIdentity(getKey());
        return b;
    }
}
