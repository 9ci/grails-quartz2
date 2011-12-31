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

class MockDoWithSpring {

    def quartzProperties
    def application = [jobClasses: null]
    def manager

    def ref( def whatever ) {
        null
    }

    def quartzJobFactory( def whatever ) {
        null
    }

    def exceptionPrinterListener( def whatever ) {
        null
    }

    def sessionBinderListener( def something, def whatever ) {
        null
    }

    void quartzScheduler( def whatever, Closure props ) {
        def data = [:]
        props.delegate = data
        props.resolveStrategy = Closure.DELEGATE_FIRST
        props.call()
        println "xxxxxxxx=$data"
        this.quartzProperties = data.quartzProperties
    }
}