//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		createCombinedFile(jsFiles, combinedFile, config.getCharsetObject());
	}

	protected void createCombinedFile(List<File> jsFiles, File combinedFile, Charset charset) {
		LOGGER.debug("Combining JavaScript files " + jsFiles);
		LOGGER.debug("Creating combined JavaScript file " + combinedFile.getAbsolutePath()); 
		
		try {
			PrintWriter writer = new PrintWriter(combinedFile, charset.displayName());
			for (File jsFile : jsFiles) {
				appendFile(jsFile, writer, charset);
			}
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Cannot write to combined JavaScript file", e);
		}
	}

	private List<File> getOrderedJavaScriptFiles(WebAppExtension config) {
		List<File> jsFiles = config.findJavaScriptFiles(getProject());
		return getOrderedJavaScriptFiles(jsFiles);
	}

	protected List<File> getOrderedJavaScriptFiles(List<File> jsFiles) {
		List<File> ordered = new ArrayList<>();
		ordered.addAll(jsFiles);
		Collections.sort(ordered, new LibrariesFirstComparator());
		return ordered;
	}

	private void appendFile(File jsFile, PrintWriter writer, Charset charset) throws IOException {
		List<String> lines = Files.readAllLines(jsFile.toPath(), charset);
		for (String line : lines) {
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
		
		public int compare(File a, File b) {
			boolean aIsLibrary = isLibrary(a);
			boolean bIsLibrary = isLibrary(b);
			
			if (aIsLibrary && !bIsLibrary) {
				return -1;
			} else if (!aIsLibrary && bIsLibrary) {
				return 1;
			} else {
				return a.getAbsolutePath().compareTo(b.getAbsolutePath());
			}
		}
		
		private boolean isLibrary(File jsFile) {
			for (String libDir : WebAppExtension.JAVASCRIPT_LIBRARY_DIRECTORIES) {
				if (jsFile.getAbsolutePath().contains(libDir)) {
					return true;
				}
			}
			return false;
		}
	}
}
