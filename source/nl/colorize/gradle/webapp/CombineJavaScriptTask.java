//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2017 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Closure;

/**
 * Combines a number of JavaScript files into a single output file. Separate
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
		config.prepareOutputFile(combinedFile);

		createCombinedFile(jsFiles, combinedFile, config);
	}

	protected void createCombinedFile(List<File> jsFiles, File combinedFile, WebAppExtension config) {
		LOGGER.debug("Combining JavaScript files " + jsFiles);
		LOGGER.debug("Creating combined JavaScript file " + combinedFile.getAbsolutePath()); 
		
		try {
			PrintWriter writer = new PrintWriter(combinedFile, config.getCharset());
			for (File jsFile : jsFiles) {
				appendFile(jsFile, writer, config);
			}
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Cannot write to combined JavaScript file", e);
		}
	}

	private List<File> getOrderedJavaScriptFiles(WebAppExtension config) {
		List<File> jsFiles = config.findJavaScriptFiles(getProject());
		return getOrderedJavaScriptFiles(config, jsFiles);
	}

	protected List<File> getOrderedJavaScriptFiles(WebAppExtension config, List<File> jsFiles) {
		List<File> ordered = new ArrayList<>();
		ordered.addAll(jsFiles);
		Collections.sort(ordered, new LibrariesFirstComparator(config));
		return ordered;
	}

	private void appendFile(File jsFile, PrintWriter writer, WebAppExtension config) throws IOException {
		List<String> lines = Files.readAllLines(jsFile.toPath(), config.getCharsetObject());
		Closure<String> filter = config.getRewriteJavaScriptFilter();
		
		for (String line : lines) {
			if (filter != null) {
				line = filter.call(line);
			}
			writer.println(line);
		}
	}
	
	/**
	 * Sorts JavaScript files so that library files appear first. This ensures 
	 * that combining files does not break scripts that have "hard" references
	 * to libraries or frameworks and are dependent on the order in which the
	 * files are included into the HTML page.
	 */
	private static class LibrariesFirstComparator implements Comparator<File> {
		
		private WebAppExtension config;
		
		public LibrariesFirstComparator(WebAppExtension config) {
			this.config = config;
		}

		public int compare(File a, File b) {
			boolean aIsLibrary = config.isJavaScriptLibrary(a);
			boolean bIsLibrary = config.isJavaScriptLibrary(b);
			
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
