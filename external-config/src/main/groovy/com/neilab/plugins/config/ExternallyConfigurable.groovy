package com.neilab.plugins.config


import javax.naming.Context
import javax.naming.InitialContext
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import grails.util.Environment
import org.grails.config.NavigableMapPropertySource
import org.grails.config.PropertySourcesConfig
import org.grails.config.yaml.YamlPropertySourceLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

trait ExternallyConfigurable {
    private ResourceLoader defaultResourceLoader = new DefaultResourceLoader()
    private YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader()
    private Logger log = LoggerFactory.getLogger('com.neilab.plugins.config.ApplicationConfig')
    private String userHome = System.properties.getProperty('user.home')
    private String separator = System.properties.getProperty('file.separator')

    /**
     * Returns the name of the prefix to be used for config file property names. If null is returned, the name returned
     * from getExternalConfigKey will be used. e.g. A prefix of appPrefix would return config property names:
     *
     *  appPrefix.config
     *  appPrefix.database.config
     *  appPrefix.logging.config
     *  appPrefix.external.config
     *
     * @return  the name to be used as the prefix for computed property names.
     */
    String getExternalConfigPrefix() {
        return null
    }

    /**
     *  Override to return the key to be used to obtain the prefix in the grails application config.
     *  The default value is "info.app.name" which by default is configured as the application name.
     *
     * @return  The name of the key to be used to obtain the prefix
     */

    String getExternalConfigKey() {
        return "info.app.name"
    }

    /**
     *  Override to return the names of application environment entities to be used in retrieving a path
     *  of application config files.
     *
     * @return  A list of Application Environment Entry Names
     */
    List<String> getExternalConfigEnvironmentNames() {
        return ["CONFIG","EXTERNAL_CONFIG","LOGGING_CONFIG","DATABASE_CONFIG"]
    }

    void configureEnvironment(ConfigurableEnvironment environment) {

        List locations = getLocations(environment)
        String encoding = environment.getProperty('grails.config.encoding', String, 'UTF-8')

        locations += variableNameList(environment)
        locations += systemPropertyNameList(environment)

        for (location in locations) {
            MapPropertySource propertySource = null
            Map currentProperties = getCurrentConfig(environment)
            if (location instanceof Class) {
                propertySource = loadClassConfig(location as Class,currentProperties)
            } else {
                // Replace placeholders from known locations
                String finalLocation = environment.resolvePlaceholders(location as String)

                Resource resource = defaultResourceLoader.getResource(finalLocation)
                if (resource.exists()) {
                    if (finalLocation.endsWith('.groovy')) {
                        propertySource = loadGroovyConfig(resource, encoding, currentProperties)
                    } else if (finalLocation.endsWith('.yml')) {
                        environment.activeProfiles
                        propertySource = loadYamlConfig(resource)
                    } else {
                        // Attempt to load the config as plain old properties file (POPF)
                        propertySource = loadPropertiesConfig(resource)
                    }
                } else {
                    log.debug("Config file {} not found", [finalLocation] as Object[])
                }
            }
            if (propertySource?.getSource() && !propertySource.getSource().isEmpty()) {
                environment.propertySources.addFirst(propertySource)
            }
        }
    }

    String getDefaultExternalConfigPrefix(ConfigurableEnvironment environment) {
        String defaultConfigPrefix = this.externalConfigPrefix ?:
                environment.getProperty(this.externalConfigKey,String,"app")
        return defaultConfigPrefix
    }

    private List variableNameList(ConfigurableEnvironment environment) {
        List locations = []

        this.externalConfigEnvironmentNames.each {
            try {
                def external_config = ((Context)(new InitialContext().lookup("java:comp/env"))).lookup("${it}")

                if(external_config) {
                    log.info("Loading configuration: $external_config")
                    locations <<  "${external_config}"
                }
            } catch (Exception e) {
                log.debug("External configuration lookup failed: " + e)
            }
        }
        return locations
    }

    private List systemPropertyNameList(ConfigurableEnvironment environment) {
        List locations = []
        String defaultConfigPrefix = this.getDefaultExternalConfigPrefix(environment)
        [
                defaultConfigPrefix,"database","logging","external"
        ].each {
            String configKey = it == defaultConfigPrefix ?
                    "${defaultConfigPrefix}.config" : // appName.config
                    "${defaultConfigPrefix}.${it}.config" // appName.[database,logging,exteral].config
            String configPath = System.properties[configKey]
            if (configPath) {
                log.info("Loading configuration: $configPath")
                locations << "file:" + configPath
            }
        }
        return locations
    }

    // Resolve final locations, taking into account user home prefix and file wildcards
    private List<Object> getLocations(ConfigurableEnvironment environment) {
        List<Object> locations = environment.getProperty('grails.config.locations', List, []) as List<Object>
        // See if grails.config.locations is defined in an environments block like 'development' or 'test'
        String environmentString = "environments.${Environment.current.name}.grails.config.locations"
        locations = environment.getProperty(environmentString, List, locations)
        locations.collectMany { Object location ->
            if(location instanceof CharSequence) {
                location = replaceUserHomePrefix(location as String)
                List<Object> expandedLocations = handleWildcardLocation(location as String)
                if(expandedLocations) {
                    return expandedLocations
                }
            }
            return [location]
        }
    }

    // Expands wildcards if any
    List<Object> handleWildcardLocation(String location) {
        if(location.startsWith('file:')) {
            String locationFileName = location.tokenize(separator)[-1]
            if(locationFileName.contains('*')) {
                String parentLocation = location - locationFileName
                try {
                    Resource resource = defaultResourceLoader.getResource(parentLocation)
                    if(resource.file.exists() && resource.file.isDirectory()) {
                        Path dir = resource.file.toPath()
                        DirectoryStream<Path> stream = Files.newDirectoryStream(dir, locationFileName)

                        return stream.collect { Path p ->
                            "file:${p.toAbsolutePath()}"
                        } as List<Object>
                    }
                } catch (FileNotFoundException ignore) {
                    return null
                }

            }
        }
        return null
    }

    // Replace ~ with value from system property 'user.home' if set
    private String replaceUserHomePrefix(String location) {
        if (userHome && location.startsWith('~/')) {
            location = "file:${userHome}${location[1..-1]}"
        }
        return location
    }
    // Load groovy config from classpath
    private MapPropertySource loadClassConfig(Class location, Map currentConfig) {
        log.info("Loading config class {}", location.name)
        ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
        WriteFilteringMap filterMap = new WriteFilteringMap(currentConfig)
        slurper.binding = filterMap
        Map properties = slurper.parse((Class) location)?.flatten()
        Map writtenValues = filterMap.getWrittenValues()
        properties.putAll(writtenValues)
        new MapPropertySource(location.toString(), properties)
    }

    // Load groovy config from resource
    private MapPropertySource loadGroovyConfig(Resource resource, String encoding, Map currentConfig) {
        log.info("Loading groovy config file {}", resource.URI)
        String configText = resource.inputStream.getText(encoding)
        ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
        WriteFilteringMap filterMap = new WriteFilteringMap(currentConfig)
        slurper.binding = filterMap
        ConfigObject configObject = slurper.parse(configText)
        Map<String, Object> properties = configText ? configObject?.flatten() as Map<String, Object> : [:]
        Map writtenValues = filterMap.getWrittenValues()
        properties.putAll(writtenValues)
        new MapPropertySource(resource.filename, properties)
    }

    private NavigableMapPropertySource loadYamlConfig(Resource resource) {
        log.info("Loading YAML config file {}", resource.URI)
        NavigableMapPropertySource propertySource = yamlPropertySourceLoader.load(resource.filename, resource, null)?.first() as NavigableMapPropertySource
        return propertySource
    }

    private MapPropertySource loadPropertiesConfig(Resource resource) {
        log.info("Loading properties config file {}", resource.URI)
        Properties properties = new Properties()
        properties.load(resource.inputStream)
        new MapPropertySource(resource.filename, properties as Map)
    }

    static Map getCurrentConfig(ConfigurableEnvironment environment) {
        return new PropertySourcesConfig(environment.propertySources)
    }

}
