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
  - If the [client dependencies plugin](https://github.com/craigburke/client-dependencies-gradle)
    is also used in the same project, all NPM and/or Bower dependencies will be refreshed before
    packaging the web application.
  
The plugin adds the following properties that can be used to configure these steps:

  - **sourceDir**: Source directory that contains the HTML/CSS/JavaScript files. The default
    location is `web` (relative to the project directory).
  - **buildDir**: Destination directory for the packages web application. The default location is
    `build/web` (relative to the project directory). 
  - **combinedJavaScriptFileName**: File name for the JavaScript file that is created during the
    build by combining all JavaScript source files. The default name is `combined.js`.
  - **excludedJavaScriptFiles**: List of patterns for JavaScript files that should be ignored
    during the build. 
  - **combineJavaScriptLibraries**: Sets if JavaScript libraries should also be included in the
    combined JavaScript file. Detection of library files is based on directory structure (i.e.
    `lib/`, `node_modules/`, `bower_components/`). The default value is `false`.
  - **rewriteJavaScriptFilter**: Takes a closure that can optionally rewrite lines when combining
    the JavaScript files. 
  - **charset**: Character encoding that is used to read and write text files. The default value
    is UTF-8.
  - **syncDirs**: The packages web application can optionally be synchronized to a list of
    other locations. 
    
The following example shows how to use the plugin from a Gradle build script:

    plugins {
    	id 'nl.colorize.gradle.webapp' version '2016.13'
    }
    
    webApp.sourceDir = 'src'
    webApp.buildDir = 'build'
    webApp.combinedJavaScriptFileName = 'my-app-' + version + '.js'
    webApp.rewriteJavaScriptFilter = { line -> line.replace('first', 'first test'); }

License
-------

Copyright 2010-2016 Colorize

The source code is licensed under the Apache License 2.0, meaning you can use it free of charge 
in commercial and non-commercial projects as long as you mention the original copyright.
The full license text can be found at 
[http://www.colorize.nl/code_license.txt](http://www.colorize.nl/code_license.txt).

By using the source code you agree to the Colorize terms and conditions, which are available 
from the Colorize website at [http://www.colorize.nl/en/](http://www.colorize.nl/en/).
