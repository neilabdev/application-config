package com.neilab.plugins.config

import groovy.transform.CompileStatic
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.core.env.ConfigurableEnvironment
@Deprecated
trait ExternalConfig implements EnvironmentAware, ExternallyConfigurable {
  /*  @Override
    void setEnvironment(Environment environment) {
     //  configureEnvironment(environment)
    } */
}

