package com.neilab.plugins.config

import groovy.transform.CompileStatic
import org.grails.config.PropertySourcesConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
@CompileStatic
class ApplicationConfigRunListener implements SpringApplicationRunListener, ExternallyConfigurable {
	ApplicationConfigRunListener(SpringApplication application, String[] args) { }

	@Override
	void environmentPrepared(ConfigurableEnvironment environment) {
		configureEnvironment(environment)
	}
	// Spring Boot 1.4 or higher
	void starting() {}
	// Spring Boot 1.3
	void started() {}
	void contextPrepared(ConfigurableApplicationContext context) {}
	void contextLoaded(ConfigurableApplicationContext context) { }

	// Spring Boot 2.1+
	void started(ConfigurableApplicationContext context) {}

	void running(ConfigurableApplicationContext context) {}

	void failed(ConfigurableApplicationContext context, Throwable exception) {}

	void finished(ConfigurableApplicationContext context, Throwable exception) {}

	static Map getCurrentConfig(ConfigurableEnvironment environment) {
		return new PropertySourcesConfig(environment.propertySources)
	}

}
