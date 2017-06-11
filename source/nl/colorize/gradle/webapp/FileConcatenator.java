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
import java.util.List;
import java.util.stream.Collectors;

import groovy.lang.Closure;

/**
 * Concatenates a number of source files into a single output file.
 */
public class FileConcatenator {
	
	private WebAppExtension config;
	
	public FileConcatenator(WebAppExtension config) {
		this.config = config;
	}

	public void concatenate(List<File> sourceFiles, File outputFile, Closure<String> filter) {
		if (!sourceFiles.isEmpty()) {
			List<String> contents = readSourceFileContents(sourceFiles);
			contents = rewriteContents(contents, filter);
			
			config.prepareOutputFile(outputFile);
			
			try (PrintWriter writer = new PrintWriter(outputFile, config.getCharset())) {
				for (String line : contents) {
					writer.println(line);
				}
			} catch (IOException e) {
				throw new RuntimeException("Cannot write to combined file", e);
			}
		}
	}
	
	private List<String> readSourceFileContents(List<File> sourceFiles) {
		List<String> combinedContents = new ArrayList<>();
		for (File sourceFile : sourceFiles) {
			try {
				List<String> fileContents = Files.readAllLines(sourceFile.toPath(), 
						config.getCharsetObject());
				combinedContents.addAll(fileContents);
				// Add an empty line between files in the combined file.
				combinedContents.add("");
			} catch (IOException e) {
				throw new RuntimeException("Cannot read file " + sourceFile.getAbsolutePath(), e);
			}
		}
		return combinedContents;
	}
	
	private List<String> rewriteContents(List<String> contents, Closure<String> filter) {
		if (filter == null) {
			return contents;
		}
		
		return contents.stream()
			.map(line -> filter.call(line))
			.collect(Collectors.toList());
	}
}
