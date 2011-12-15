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

public class JobErrorLoggerListener extends JobListenerSupport {
    private static final transient Log log = LogFactory.getLog(JobErrorLoggerListener.class);

	public static final String NAME = "jobErrorLoggerListener"
	
    public String getName() {
        return NAME
    }

    public void jobWasExecuted(JobExecutionContext ctx, JobExecutionException ex) {
		if (log.debugEnabled) log.debug("job:$ctx.jobDetail.key.name was executed")
        if (ex) log.error("Exception thrown in job:$ctx.jobDetail.key.name", ex)
    }
}