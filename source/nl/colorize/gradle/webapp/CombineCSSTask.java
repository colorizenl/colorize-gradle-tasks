//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2017 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import java.io.File;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concatenates a number of CSS files into a single output file.
 */
public class CombineCSSTask extends DefaultTask {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CombineCSSTask.class);

	@TaskAction
	public void run() {
		WebAppExtension config = getProject().getExtensions().getByType(WebAppExtension.class);
		List<File> cssFiles = config.findCombinableCSSFiles(getProject());
		File combinedFile = config.getCombinedCSSFile(getProject());
		
		if (config.getCombineCSSEnabled()) {
			combineCSS(cssFiles, combinedFile, config);
		}
	}
	
	protected void combineCSS(List<File> cssFiles, File combinedFile, WebAppExtension config) {
		LOGGER.debug("Combining CSS files " + cssFiles);
		LOGGER.debug("Creating combined CSS file " + combinedFile.getAbsolutePath());
		
		FileConcatenator fileConcatenator = new FileConcatenator(config);
		fileConcatenator.concatenate(cssFiles, combinedFile, null);
	}
}
