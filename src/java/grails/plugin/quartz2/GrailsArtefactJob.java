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
import org.springframework.beans.BeansException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Quartz Job implementation that invokes execute() on the application's job class.
 */
public class GrailsArtefactJob implements InterruptableJob {
    Object job;
    Method executeMethod;
    Method interruptMethod;
    boolean passExecutionContext;

    public GrailsArtefactJob(Object job) {
        this.job = job;
        this.executeMethod = ReflectionUtils.findMethod(job.getClass(), GrailsJobClassConstants.EXECUTE, (Class<?>[]) null);
        if (executeMethod == null) {
            throw new IllegalArgumentException(job.getClass().getName() + " should declare #execute() method");
        }
        switch (executeMethod.getParameterTypes().length) {
            case 0:
                passExecutionContext = false;
                break;
            case 1:
                passExecutionContext = true;
                break;
            default:
                throw new IllegalArgumentException(job.getClass().getName() + "#execute() method should take either no arguments or one argument of type JobExecutionContext");
        }
        this.interruptMethod = ReflectionUtils.findMethod(job.getClass(), GrailsJobClassConstants.INTERRUPT);
    }

    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            if (passExecutionContext) {
                executeMethod.invoke(job, context);
            } else {
                executeMethod.invoke(job);
            }
        } catch (InvocationTargetException ite) {
            Throwable targetException = ite.getTargetException();
            if (targetException instanceof JobExecutionException) {
                throw (JobExecutionException) targetException;
            } else {
                throw new JobExecutionException(targetException);
            }
        } catch (IllegalAccessException iae) {
            JobExecutionException criticalError = new JobExecutionException("Cannot invoke " + job.getClass().getName() + "#execute() method", iae);
            criticalError.setUnscheduleAllTriggers(true);
            throw criticalError;
        }
    }

    public void interrupt() throws UnableToInterruptJobException {
        if (interruptMethod != null) {
            try {
                interruptMethod.invoke(job);
            } catch (Throwable e) {
                throw new UnableToInterruptJobException(e);
            }
        } else {
            throw new UnableToInterruptJobException(job.getClass().getName() + " doesn't support interruption");
        }
    }
}