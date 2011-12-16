grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
		compile('org.quartz-scheduler:quartz:2.1.1'){ 
			excludes "slf4j-api", "c3p0","jta"
			//excludes([ group: 'org.slf4j', name: 'slf4j-api', version: '1.6.1']) 
		}
        // runtime 'mysql:mysql-connector-java:5.1.13'
    }
	plugins {
		compile ':plugin-config:0.1.5'
	}
}
