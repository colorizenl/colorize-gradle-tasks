HTML/CSS/JavaScript web application plugin for Gradle
=====================================================

Gradle plugin for building HTML/CSS/JavaScript web applications. The plugin adds the 
*packageWebApp* task that automatically runs as part of *assemble*. "Packaging" the web 
application consists of the following steps:

  - Combine all JavaScript sources files into a single combined JavaScript file.
  - Rewrite all references to the original JavaScript source files to reference the combined
    JavaScript file instead.
  - The resulting HTML, CSS, and JavaScript files are copied to the `build/web` directory.
  - If the *war* plugin is also used in the same project to build Java web applications, the 
    packaged web application will also be included in the WAR file.
  
The plugin adds the following properties that can be used to configure these steps:

  - **sourceDir**: Source directory that contains the HTML/CSS/JavaScript files. The default
    location is `web` (relative to the project directory).
  - **buildDir**: Destination directory for the packages web application. The default location is
    `build/web` (relative to the project directory).
  - **combinedJavaScriptFileName**: File name for the JavaScript file that is created during the
    build by combining all JavaScript source files. The default name is `combined.js`. 
  - **charset**: Character encoding that is used to read and write text files. The default value
    is UTF-8.
    
The following example shows how to use the plugin from a Gradle build script:

    buildscript {
        repositories {
            ivy {
                url 'http://dev.colorize.nl/repo'
            }
        }
    
        dependencies {
            classpath 'nl.colorize:colorize-gradle-tasks:2016.4'
        }
    }
    
    apply plugin: 'nl.colorize.gradle.webapp'

    webApp.sourceDir = 'src'
    webApp.buildDir = 'build'
  
Building
--------

Building the plugin itself requires the [Java JDK](http://java.oracle.com) and 
[Gradle](http://gradle.org). The following Gradle build tasks are available:

- `gradle clean` cleans the build directory.
- `gradle assemble` creates the JAR file for distribution.

License
-------

Copyright 2010-2016 Colorize

The source code is licensed under the Apache License 2.0, meaning you can use it free of charge 
in commercial and non-commercial projects as long as you mention the original copyright.
The full license text can be found at 
[http://www.colorize.nl/code_license.txt](http://www.colorize.nl/code_license.txt).

By using the source code you agree to the Colorize terms and conditions, which are available 
from the Colorize website at [http://www.colorize.nl/en/](http://www.colorize.nl/en/).
