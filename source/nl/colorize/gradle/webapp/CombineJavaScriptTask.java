//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2017 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concatenates a number of JavaScript files into a single output file. Separate
 * files are typically used during development, while a single file is more
 * suitable for production to minimize the page's load time.
 */
public class CombineJavaScriptTask extends DefaultTask {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CombineJavaScriptTask.class);
	
	@TaskAction
	public void run() {
		WebAppExtension config = getProject().getExtensions().getByType(WebAppExtension.class);
		List<File> jsFiles = getOrderedJavaScriptFiles(config);
		File combinedFile = config.getCombinedJavaScriptFile(getProject());
		
		if (config.getCombineJavaScriptEnabled()) {
			createCombinedFile(jsFiles, combinedFile, config);
		}
	}

	protected void createCombinedFile(List<File> jsFiles, File combinedFile, WebAppExtension config) {
		LOGGER.debug("Combining JavaScript files " + jsFiles);
		LOGGER.debug("Creating combined JavaScript file " + combinedFile.getAbsolutePath());
		
		FileConcatenator fileConcatenator = new FileConcatenator(config);
		fileConcatenator.concatenate(jsFiles, combinedFile, config.getRewriteJavaScriptFilter());
	}

	private List<File> getOrderedJavaScriptFiles(WebAppExtension config) {
		List<File> jsFiles = config.findCombinableJavaScriptFiles(getProject());
		return getOrderedJavaScriptFiles(config, jsFiles);
	}

	protected List<File> getOrderedJavaScriptFiles(WebAppExtension config, List<File> jsFiles) {
		List<File> ordered = new ArrayList<>();
		ordered.addAll(jsFiles);
		Collections.sort(ordered, new LibrariesFirstComparator(getProject(), config));
		return ordered;
	}

	/**
	 * Sorts JavaScript files so that library files appear first. This ensures 
	 * that combining files does not break scripts that have "hard" references
	 * to libraries or frameworks and are dependent on the order in which the
	 * files are included into the HTML page.
	 */
	private static class LibrariesFirstComparator implements Comparator<File> {
		
		private Project project;
		private WebAppExtension config;
		
		public LibrariesFirstComparator(Project project, WebAppExtension config) {
			this.project = project;
			this.config = config;
		}

		public int compare(File a, File b) {
			List<File> libraries = config.findJavaScriptLibraryFiles(project);
			boolean aIsLibrary = libraries.contains(a);
			boolean bIsLibrary = libraries.contains(b);
			
			if (aIsLibrary && !bIsLibrary) {
				return -1;
			} else if (!aIsLibrary && bIsLibrary) {
				return 1;
			} else {
				return a.getAbsolutePath().compareTo(b.getAbsolutePath());
			}
		}
	}
}
