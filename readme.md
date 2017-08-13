HTML/CSS/JavaScript web application plugin for Gradle
=====================================================

Gradle plugin for building HTML/CSS/JavaScript web applications. The plugin adds the 
*packageWebApp* task that automatically runs as part of *assemble*. "Packaging" the web 
application consists of the following steps:

  - Concatenates all JavaScript files into a single combined JavaScript file.
  - If the project uses TypeScript instead of JavaScript, compiles the TypeScript files
    into a JavaScript file.
  - Concatenates all CSS files into a single combined CSS file.
  - Rewrite all references to the original JavaScript and CSS source files to reference the
    combined files instead.
  - The resulting HTML, CSS, and JavaScript files are copied to the build directory.
  - If the *war* plugin is also used in the same project to build Java web applications, the 
    packaged web application will also be included in the WAR file.
  - If the [client dependencies plugin](https://github.com/craigburke/client-dependencies-gradle)
    is also used in the same project, all NPM and/or Bower dependencies will be refreshed before
    packaging the web application.
    
Usage
-----
    
The following example shows how to use the plugin from a Gradle build script:

    plugins {
        id 'nl.colorize.gradle.webapp' version '2017.9'
    }
    
    webApp.sourceDir = 'src'
    webApp.buildDir = 'build'
    webApp.combinedJavaScriptFileName = 'my-app-' + version + '.js'
    webApp.excludedJavaScriptFiles = ['gulpfile.js', '**/*.map']
    webApp.rewriteJavaScriptFilter = { line -> line.replace('first', 'first test'); }
    
As this example shows, the plugin is configured using a number of properties specified in the
build script. These properties are described in more detail in the next section. 
    
Configuration
-------------
  
The plugin can be configured using the following properties:

| Property | Description | Default |
|----------|-------------|---------|
| sourceDir | Source directory that contains the HTML/CSS/JavaScript files. The directory path is relative to the project directory. | web |
| buildDir | Destination directory for the packaged web application. The directory path is relative to the project directory. | build/web |
| excludes | List of exclude patterns (e.g. `['gulpfile.js', '*.map']`) of files that should not be included in the packaged web application. | (none) |
| charset | Character encoding that is used to read and write text files. | UTF-8 |
| combineJavaScriptEnabled | Configures if JavaScript files should be combined during the build. | true |
| combinedJavaScriptFileName | File name for the JavaScript file that is created during the build by combining all JavaScript source files. | (projectName).js |
| combineJavaScriptExcludes | List of exclude patterns for JavaScript files that should not be combined, and that will be packaged in their original form. | (none) |
| combineJavaScriptLibraries | Configures if JavaScript libraries should also be included in the combined JavaScript file. Detection of library files is based on directory structure (i.e. `lib/`, `node_modules/`, `bower_components/`). | false |
| combineCSSEnabled | Configures if CSS files should be combined during the build. | false |
| combinedCSSFileName | File name used for the CSS file that is created during the build by combining all CSS source files. | (projectName).css |
| combineCSSExcludes | List of exclude patterns for CSS files that should not be combined, and that will be packaged in their original form. | (none) |
| syncDirs | The packages web application can optionally be synchronized to a list of other locations. | (none) |

Note that JavaScript files are combined in alphabetical order. This is only relevant if your
JavaScript files depend on the order in which they are loaded. In that case, exclude those files
from being combined and set the load order manually.
    
Build
-----
    
The build is cross-platform and supports Windows, macOS, and Linux, but requires the following 
software to be available:

  - [Java JDK](http://java.oracle.com)
  - [Gradle](http://gradle.org)
  
The following Gradle build tasks are available:

  - `gradle clean` cleans the build directory
  - `gradle assemble` creates the JAR file for distribution
  - `gradle test` runs all unit tests, then reports on test results and test coverage

License
-------

Copyright 2010-2017 Colorize

The source code is licensed under the Apache License 2.0, meaning you can use it free of charge 
in commercial and non-commercial projects as long as you mention the original copyright.
The full license text can be found at 
[http://www.colorize.nl/code_license.txt](http://www.colorize.nl/code_license.txt).

By using the source code you agree to the Colorize terms and conditions, which are available 
from the Colorize website at [http://www.colorize.nl/en/](http://www.colorize.nl/en/).
