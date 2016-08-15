//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.gradle.api.DefaultTask;
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
		File buildDir = config.getBuildDir(getProject());
		cleanBuildDir(buildDir, config);
		
		for (File sourceFile : config.getSourceTree(getProject()).getFiles()) {
			File outputFile = new File(buildDir.getAbsolutePath() + "/" + 
					config.toRelativePath(getProject(), sourceFile));
			
			if (shouldRewriteSourceFile(sourceFile)) {
				rewriteSourceFile(sourceFile, outputFile, config);
			} else if (shouldCopySourceFile(sourceFile, config)) {
				copyFile(sourceFile, outputFile, config);
			}
		}
	}
	
	protected void cleanBuildDir(File buildDir, WebAppExtension config) {
		final List<File> generatedFiles = new ArrayList<>();
		generatedFiles.add(config.getCombinedJavaScriptFile(getProject()));
		
		getProject().fileTree(buildDir).forEach(new Consumer<File>() {
			public void accept(File file) {
				if (!generatedFiles.contains(file)) {
					file.delete();
				}
			}
		});
	}

	private boolean shouldCopySourceFile(File sourceFile, WebAppExtension config) {
		List<File> jsSourceFiles = config.findJavaScriptFiles(getProject());
		return !jsSourceFiles.contains(sourceFile);
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
	
	private void rewriteSourceFile(File sourceFile, File outputFile, WebAppExtension config) {
		config.prepareOutputFile(outputFile);
		
		try {
			List<String> lines = Files.readAllLines(sourceFile.toPath(), config.getCharsetObject());
			List<String> rewrittenLines = rewriteSourceFile(lines, config);
			
			PrintWriter writer = new PrintWriter(outputFile, config.getCharset());
			for (String line : rewrittenLines) {
				writer.println(line);
			}
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Cannot create file " + outputFile.getAbsolutePath(), e);
		}
	}
	
	private List<String> rewriteSourceFile(List<String> lines, WebAppExtension config) {
		List<String> rewritten = new ArrayList<>();
		boolean seen = false;
		for (String line : lines) {
			String rewrittenLine = rewriteJavaScriptSourceFileReferences(line, config);
			if (!line.equals(rewrittenLine)) {
				line = seen ? "" : rewrittenLine;
				seen = true;
			}
			rewritten.add(line);
			
		}
		return rewritten;
	}
	
	private String rewriteJavaScriptSourceFileReferences(String line, WebAppExtension config) {
		List<String> references = new ArrayList<>();
		for (File jsFile : config.findJavaScriptFiles(getProject())) {
			references.add(jsFile.getName());
		}
		//TODO support replacement file in relative path (currently assumes the
		//     combined JavaScript file is always created in the web build
		//     directory root.
		File replacement = config.getCombinedJavaScriptFile(getProject());
		return rewriteJavaScriptSourceFileReferences(line, references, replacement.getName());
	}
	
	protected String rewriteJavaScriptSourceFileReferences(String line, List<String> references, 
			String replacement) {
		for (String ref : references) {
			if (isJavaScriptSourceFileReference(line, ref)) {
				return "<script src=\"" + replacement + "\"></script>";
			}
		}
		return line;
	}

	private boolean isJavaScriptSourceFileReference(String line, String sourceFile) {
		return line.trim().startsWith("<script ") && line.contains(" src=\"") && 
				line.toLowerCase().contains(sourceFile.toLowerCase());
	}
}
