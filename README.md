Application-Config
===============
<<<<<<< HEAD
[![Build Status](https://travis-ci.org/neilabdev/application-config.svg?branch=master)](https://travis-ci.org/neilabdev/application-config)
=======
[![Build Status](https://travis-ci.org/sbglasius/external-config.svg?branch=master)](https://travis-ci.org/sbglasius/external-config)

This plugin will mimic the Grails 2 way of handling external configurations defined in `grails.config.locations`.

VERSIONS!
---------
The `grails-3.x` branch is for Grails 3.x, `master` will, moving forward be for Grails 4.x

IMPORTANT!
----------
The External Config Plugin (1.1.0 and above) no longer needs to implement `ExternalConfig` on `Application.groovy`. It now uses a `SpringApplicationRunListener`and hooks into the startup automagically. So if you used the plugin in prior versions, please remove `implements ExternalConfig` from `Application.groovy`
>>>>>>> external-config/master

This plugin based on the amazing work started in plugin  [external-config](http://plugins.grails.org/plugin/grails/external-config) by [Sudhir Nimavat](https://github.com/snimavat) and  [Dennie de Lange](https://github.com/tkvw),
 which mimiced the Grails 2 way of handling external configurations defined in `grails.config.locations`; with the necessary additions to allow configurations via command line, system properties, and JNDI.

Contributors
------------

<<<<<<< HEAD
* [neilabdev](https://github.com/neilabdev)
=======
Major contributors

* [Jesper Steen Møller](https://github.com/jespersm)
* [Tucker J. Pelletier](https://github.com/virtualdogbert)
* [Dennie de Lange](https://github.com/tkvw)
* [Sudhir Nimavat](https://github.com/snimavat) 
* [Anders Aaberg](https://github.com/andersaaberg)
>>>>>>> external-config/master


Installation
------------

Add dependency to your `build.gradle`:

```
dependencies {
<<<<<<< HEAD
    compile 'com.neilab.plugins:application-config:1.1.3'
=======
    compile 'org.grails.plugins:external-config:1.2.2'
}
```

To use a snapshot-version

add JFrog OSS Repository to the `repositories`:
```
repositories {
    maven { url "https://oss.jfrog.org/repo/" }
}
```

and specify the snapshot version as a dependency:
```
dependencies {
    compile 'org.grails.plugins:external-config:1.3.0.BUILD-SNAPSHOT'
>>>>>>> external-config/master
}
```

Usage
-----

When you add this plugin to your Grails build, it will automatically look for the property `grails.config.locations`. Define this in in either `application.yml` like this:

```
grails:
    config:
        locations:
            - classpath:myconfig.groovy
            - classpath:myconfig.yml
            - classpath:myconfig.properties
            - file:///etc/app/myconfig.groovy
            - file:///etc/app/myconfig.yml
            - file:///etc/app/myconfig.properties
            - ~/.grails/myconfig.groovy
            - ~/.grails/myconfig.yml
            - ~/.grails/myconfig.properties
            - file:${catalina.base}/myconfig.groovy
            - file:${catalina.base}/myconfig.yml
            - file:${catalina.base}/myconfig.properties
```

or in `application.groovy` like this:

```
grails.config.locations = [
        "classpath:myconfig.groovy",
        "classpath:myconfig.yml",
        "classpath:myconfig.properties",
        "file:///etc/app/myconfig.groovy",
        "file:///etc/app/myconfig.yml",
        "file:///etc/app/myconfig.properties",
        "~/.grails/myconfig.groovy",
        "~/.grails/myconfig.yml",
        "~/.grails/myconfig.properties",
        'file:${catalina.base}/myconfig.groovy',
        'file:${catalina.base}/myconfig.yml',
        'file:${catalina.base}/myconfig.properties',
]
```

<<<<<<< HEAD
You may also include external configs using '-D' arguments which match the system properties
the application is seeking, preceded by an application prefix determined by *info.app.name* in your default *application.yml*, *application.groovy*  or "app" if none exists. By default the following should work:

```
-DappName.config="/path/to/config"
-DappName.external.config="/path/to/config"
-DappName.database.config="/path/to/config"
-DappName.logging.config="/path/to/config"

```

or using JNDI variables *CONFIG*, *EXTERNAL_CONFIG*, *LOGGING_CONFIG*, *DATABASE_CONFIG* in tomcat for example:

```xml
<Context path="" docBase="/path/to/app.war"  reloadable="false">
        <Environment name="APP_CONFIG"
                value="file:/path/to/external_config.groovy"
                type="java.lang.String"/>
        <Environment name="DATABASE_CONFIG"
                value="file:/path/to/external_config.database.groovy"
                type="java.lang.String"/>
</Context>
```

While  no-longer necessary as of version 1.1.0 of [external-config](http://plugins.grails.org/plugin/grails/external-config), which this fork is based, for comparability you  may edit your Grails projects `Application.groovy` and implement the trait `com.neilab.plugins.config.ApplicationConfig` (formally ExternalConfig): 

```
import com.neilab.plugins.config.ApplicationConfig

class Application extends GrailsAutoConfiguration implements ApplicationConfig {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
```

This above is necessary only when *ApplicationConfigRunListener* is not executed, and   personally  have used it only when developing the plugin as an inline plugin where *SpringApplicationRunListener* was not loaded.


Notes
-----
Notice, that `~/` references the users `$HOME` directory.
=======
It is also possible to define it in an environment specific block (groovy):
```$xslt
environments {
    test {
        grails {
            config {
                locations = [...]
            }
        }
    }
}   
```

or (yml)

```
environments:
    test:
        grails:
            config:
                locations:
                - ... 
```

`~/` references the users `$HOME` directory.
>>>>>>> external-config/master
Notice, that using a system property you should use single quotes because otherwise it's interpreted as a Gstring.

The plugin will skip configuration files that are not found. 

For `.groovy` and `.yml` files the `environments` blocks in the config file are interpreted the same way, as in `application.yml` or `application.groovy`.

<<<<<<< HEAD
Alternatively, you can make a gradle script to move the external configuration file to your classpath (e.g. /build/classes)
=======
**Wildcard support**

It is possible to use `*` as wildcards in the filename part of the configuration:

```
grails:
    config:
        locations:
            - file:/etc/app/myconfig*.groovy
            - ~/.grails/myconfig*.groovy
```
or
```
grails.config.locations = [
        "file:/etc/app/myconfig*.groovy",
        "~/.grails/myconfig*.groovy",
]
```
__Note__: that it only works for the `file:` and `~/` prefix. 

__Note__: the wildcards are in the order they are found in the `locations` list, but the order of the expanded `locations` for each wildcard is not guaranteed, and is dependent on the OS used.

**Getting configuration from another folder than /conf on classpath without moving it with Gradle script**
>>>>>>> external-config/master

**Using IntelliJ or gradle to specify configurations via system properties**

When passing system properties via *VM Options* in IntelliJ or *-D* properties in gradle, it may be necessary to assign the parameters to the app via *bootRun* in your *build.gradle* configuration.

```groovy
//build.gradle
bootRun {
    systemProperties = System.properties
}
```

Scripts
-----
This plugin also includes two scripts, one for converting yml config, to groovy config,
and one for converting groovy config to yml config. These scripts are not guaranteed to be 
perfect, but you should report any edge cases for the yml to groovy config here:
https://github.com/virtualdogbert/GroovyConfigWriter/issues

grails yml-to-groovy-config has the following parameters:
* ymlFile - The yml input file.
* asClosure - An optional flag to set the output to be closure based or map based. The Default is closure based 
* outputFile - The optional output file. If none is provided, then the output will go to System.out.
* indent - Optional indent. The default is 4 spaces
* escapeList - An optional CSV list of values to escape, with no spaces. The default is 'default'


Sample usage:
```
grails yml-to-groovy-config [ymlFile] [optional asClosure] [optional outputFile] [optional indent] [optional escape list]
```

grails groovy-to-yml-config has the following parameters:
* groovy - The groovy input file.'
* outputFile - The optional output file. If none is provided, then the output will go to System.out.
* indent' - Sets the optional indent level for a file. The default is 4
* flow - Sets the optional style of BLOCK or FLOWS. The default is BLOCK.

sample usage:
```
grails groovy-to-yml-config [ymlFile] [optional outputFile] [optional indent] [optinal flow]
```
