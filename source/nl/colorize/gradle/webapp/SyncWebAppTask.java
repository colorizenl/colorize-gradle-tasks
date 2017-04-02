//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2017 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronized the packages web application from the build directory to a 
 * number of other directories. An example of where this task can be used
 * is when the web application is embedded in another (native, mobile) app. 
 */
public class SyncWebAppTask extends DefaultTask {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SyncWebAppTask.class);
	
	@TaskAction
	public void run() {
		WebAppExtension config = getProject().getExtensions().getByType(WebAppExtension.class);
		File buildDir = config.getBuildDir(getProject());
		
		for (String syncDirPath : config.getSyncDirs()) {
			File syncDir = getProject().file(syncDirPath);
			LOGGER.debug("Synchronizing packaged web application to " + syncDir.getAbsolutePath());
			sync(buildDir, syncDir, config);
		}
	}
	
	protected void sync(File buildDir, File syncDir, WebAppExtension config) {
		if (!syncDir.exists()) {
			syncDir.mkdir();
		}
		
		for (File existingFile : getProject().fileTree(syncDir)) {
			existingFile.delete();
		}
		
		for (File sourceFile : getProject().fileTree(buildDir)) {
			File outputFile = new File(syncDir.getAbsolutePath() + "/" + 
					config.toRelativePath(sourceFile, buildDir));
			try {
				config.prepareOutputFile(outputFile);
				Files.copy(sourceFile.toPath(), outputFile.toPath());
			} catch (IOException e) {
				throw new RuntimeException("Cannot sync file from " + sourceFile.getAbsolutePath() + 
						" to " + outputFile.getAbsolutePath(), e);
			}
		}
	}
}
