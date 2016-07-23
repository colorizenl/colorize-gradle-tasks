//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.tasks.TaskAction;

/**
 * Packages the web application by copying all files to the build directory. Note
 * that this does <em>not</em> copy the JavaScript source files, as they are
 * combined into a single file by the {@link CombineJavaScriptTask}. Also, any
 * HTML referencing the original JavaScript files will be rewritten to reference
 * the new combined JavaScript file instead.
 */
public class PackageWebAppTask extends DefaultTask {

	@TaskAction
	public void run() {
		WebAppExtension config = getProject().getExtensions().getByType(WebAppExtension.class);
		ConfigurableFileTree sourceFiles = getProject().fileTree(config.getSourceDir());
		File buildDir = getProject().file(config.getBuildDir());
		
		for (File sourceFile : sourceFiles.getFiles()) {
			File outputFile = new File(buildDir.getAbsolutePath() + "/" + 
					config.toRelativePath(getProject(), sourceFile));
			
			if (shouldRewriteSourceFile(sourceFile)) {
				rewriteFile(sourceFile, outputFile, config);
			} else if (shouldCopySourceFile(sourceFile)) {
				copyFile(sourceFile, outputFile, config);
			}
		}
	}
	
	private boolean shouldCopySourceFile(File sourceFile) {
		return !sourceFile.getName().endsWith(".js");
	}
	
	private boolean shouldRewriteSourceFile(File sourceFile) {
		return sourceFile.getName().endsWith(".html");
	}
	
	private void copyFile(File sourceFile, File outputFile, WebAppExtension config) {
		config.prepareOutputFile(outputFile);
		
		try {
			Files.copy(sourceFile.toPath(), outputFile.toPath());
		} catch (IOException e) {
			throw new RuntimeException("Cannot create file " + outputFile.getAbsolutePath(), e);
		}
	}
	
	private void rewriteFile(File sourceFile, File outputFile, WebAppExtension config) {
		config.prepareOutputFile(outputFile);
		
		try {
			List<String> lines = Files.readAllLines(sourceFile.toPath(), config.getCharsetObject());
			List<String> rewrittenLines = rewriteLines(lines, config);
			
			PrintWriter writer = new PrintWriter(outputFile, config.getCharset());
			for (String line : rewrittenLines) {
				writer.println(line);
			}
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Cannot create file " + outputFile.getAbsolutePath(), e);
		}
	}
	
	private List<String> rewriteLines(List<String> lines, WebAppExtension config) {
		List<String> rewritten = new ArrayList<>();
		boolean seen = false;
		for (String line : lines) {
			String rewrittenLine = rewriteJavaScriptFileReferences(line, config);
			if (!line.equals(rewrittenLine)) {
				line = seen ? "" : rewrittenLine;
				seen = true;
			}
			rewritten.add(line);
			
		}
		return rewritten;
	}
	
	private String rewriteJavaScriptFileReferences(String line, WebAppExtension config) {
		List<String> references = new ArrayList<>();
		for (File jsFile : config.findJavaScriptFiles(getProject())) {
			references.add(config.toRelativePath(getProject(), jsFile));
		}
		return rewriteJavaScriptFileReferences(line, references, config.getCombinedJavaScriptFileName());
	}
	
	protected String rewriteJavaScriptFileReferences(String line, List<String> references, 
			String replacement) {
		for (String ref : references) {
			if (line.contains(ref)) {
				return line.replace(ref, replacement);
			}
		}
		return line;
	}
}
