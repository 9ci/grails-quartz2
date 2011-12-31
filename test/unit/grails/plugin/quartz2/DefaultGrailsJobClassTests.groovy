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

import org.quartz.SimpleTrigger
import org.quartz.Scheduler;

class DefaultGrailsJobClassTests extends GroovyTestCase {
    protected GroovyClassLoader gcl = new GroovyClassLoader()

    protected void tearDown() {
        super.tearDown()
        gcl.clearCache()
    }

    void testDefaultProperties() {
        def jobClass = gcl.parseClass('class TestJob { def execute(){} }')
        def grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertEquals "Wrong default group", Scheduler.DEFAULT_GROUP, grailsJobClass.group
        assertTrue "Job should require Hibernate session by default", grailsJobClass.sessionRequired
        assertTrue "Job should be concurrent by default", grailsJobClass.concurrent
    }
    
    void testJobClassExecute() {
        boolean wasExecuted = false
        def testClosure = { wasExecuted = true }
        Class jobClass = gcl.parseClass("""
                class TestJob {
                    def testClosure
                    def execute() {
                        testClosure.call()
                    }
                }
                """.stripIndent())
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        grailsJobClass.referenceInstance.testClosure = testClosure
        grailsJobClass.execute()
        assertTrue "Job wasn't executed", wasExecuted
    }

    void testSimpleJob() {
        Class jobClass = gcl.parseClass("""
                class TestJob {
					static triggers = {
					     simple name:'simpleTrigger', startDelay:5000, repeatInterval: 1000
					}
                    def execute() {}
                }
                """.stripIndent())
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertEquals 1, grailsJobClass.triggers.size()
        def trigger = grailsJobClass.triggers['simpleTrigger']
        assertTrue "Trigger with name TestJob should be registered", trigger != null
        assertEquals "Incorrect repeat interval", 1000, trigger.triggerAttributes.repeatInterval
        assertEquals "Incorrect start delay (2)", 5000, trigger.triggerAttributes.startDelay
    }

    void testCronJob() {
        Class jobClass = gcl.parseClass("""
                class TestJob {
					static triggers = {
					     cron name:'cronTrigger', startDelay:10000, cronExpression: '0 1 6 * * ?'
					}
                    def execute() {}
                }
                """.stripIndent())
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertEquals 1, grailsJobClass.triggers.size()
        def trigger = grailsJobClass.triggers['cronTrigger']
        assertTrue "Trigger with name TestJob should be registered", trigger != null
        assertEquals '0 1 6 * * ?', trigger.triggerAttributes.cronExpression
    }

    void testSessionRequiredParameter() {
        Class jobClass = gcl.parseClass("""
                class TestJob {
                    def sessionRequired = false
                    def execute() {}
                }
                """.stripIndent())
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertFalse "Hibernate Session shouldn't be required", grailsJobClass.sessionRequired
    }

    void testConcurrentParameter() {
        Class jobClass = gcl.parseClass("""
                class TestJob {
                    def concurrent = false
                    def execute() {}
                }
                """.stripIndent())
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertFalse "Job class shouldn't be marked as concurrent", grailsJobClass.concurrent
    }

    void testGroupParameter() {
        Class jobClass = gcl.parseClass("""
                class TestJob {
                    def group = 'myGroup'
                    def execute() {}
                }
                """.stripIndent())
        GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
        assertEquals 'myGroup', grailsJobClass.group
    }

    void testWrongCronExpression() {
        def jobClass = gcl.parseClass("""
                class TestJob {
					static triggers = {
					     cron name:'cronTrigger', startDelay:10000, cronExpression: 'Not a cron expression'
					}
                    def execute() {}
                }
                """.stripIndent())
        shouldFail(IllegalArgumentException) {
            GrailsJobClass grailsJobClass = new DefaultGrailsJobClass(jobClass)
			grailsJobClass.triggers
        }
    }
}
