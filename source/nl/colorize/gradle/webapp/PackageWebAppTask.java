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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Closure;

/**
 * Packages the web application. This will process the HTML/CSS/JavaScript files
 * in the web app's source directory, and copies the results to the build
 * directory.
 */
public class PackageWebAppTask extends DefaultTask {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PackageWebAppTask.class);

	@TaskAction
	public void run() {
		WebAppExtension config = getProject().getExtensions().getByType(WebAppExtension.class);
		File webAppSourceDir = config.getSourceDir(getProject());
		File buildDir = config.getBuildDir(getProject());
		
		if (webAppSourceDir.exists()) {
			cleanBuildDir(buildDir, config);
			packageWebApp(buildDir, config);
			
			// Configure the WAR plugin to use the packaged web app, by
			// pointing the "webAppDirName" property to the build dir.
			File warWebAppDir = (File) getProject().getProperties().get("webAppDir");
			if (warWebAppDir != null) {
				getProject().setProperty("webAppDirName", config.getBuildDir());
			}
		}
	}
	
	protected void cleanBuildDir(File buildDir, WebAppExtension config) {
		final List<File> generatedFiles = new ArrayList<>();
		generatedFiles.add(config.getCombinedJavaScriptFile(getProject()));
		generatedFiles.add(config.getCombinedCSSFile(getProject()));
		
		getProject().fileTree(buildDir).forEach(new Consumer<File>() {
			public void accept(File file) {
				if (!generatedFiles.contains(file)) {
					file.delete();
				}
			}
		});
	}
	
	private void packageWebApp(File buildDir, WebAppExtension config) {
		if (config.getCombineJavaScriptEnabled()) {
			combineJavaScript(buildDir, config);
		}
		
		if (config.getCombineCSSEnabled()) {
			combineCSS(buildDir, config);
		}
		
		if (isTypeScriptWebApp(config)) {
			compileTypeScript();
		}
		
		copyFiles(buildDir, config);
	}

	protected void combineJavaScript(File buildDir, WebAppExtension config) {
		List<File> jsFiles = config.findCombinableJavaScriptFiles(getProject());
		File combinedFile = config.getCombinedJavaScriptFile(getProject());
		
		LOGGER.debug("Combining JavaScript files " + jsFiles);
		LOGGER.debug("Creating combined JavaScript file " + combinedFile.getAbsolutePath());
		
		combineFiles(jsFiles, combinedFile, config, config.getRewriteJavaScriptFilter());
	}
	
	protected void combineCSS(File buildDir, WebAppExtension config) {
		List<File> cssFiles = config.findCombinableCSSFiles(getProject());
		File combinedFile = config.getCombinedCSSFile(getProject());
		
		LOGGER.debug("Combining CSS files " + cssFiles);
		LOGGER.debug("Creating combined CSS file " + combinedFile.getAbsolutePath());
		
		combineFiles(cssFiles, combinedFile, config, null);
	}
	
	protected void combineFiles(List<File> sourceFiles, File outputFile, 
			WebAppExtension config, Closure<String> filter) {
		FileConcatenator fileConcatenator = new FileConcatenator(config);
		fileConcatenator.concatenate(sourceFiles, outputFile, config.getRewriteJavaScriptFilter());
	}
	
	private boolean isTypeScriptWebApp(WebAppExtension config) {
		List<File> tsFiles = config.findWebAppFiles(getProject()).stream()
			.filter(file -> isTypeScriptFile(file))
			.filter(file -> !file.getName().endsWith(".d.ts"))
			.collect(Collectors.toList());
		
		return !tsFiles.isEmpty();
	}
	
	private void compileTypeScript() {
		getProject().exec(spec -> spec.commandLine("tsc"));
	}
	
	private void copyFiles(File buildDir, WebAppExtension config) {
		for (File sourceFile : config.findWebAppFiles(getProject())) {
			File outputFile = new File(buildDir.getAbsolutePath() + "/" + 
					config.toRelativePath(getProject(), sourceFile));
			
			if (shouldRewriteSourceFile(sourceFile)) {
				rewriteSourceFile(sourceFile, outputFile, config);
			} else if (shouldCopySourceFile(sourceFile, config)) {
				copyFile(sourceFile, outputFile, config);
			}
		}
	}

	protected boolean shouldCopySourceFile(File sourceFile, WebAppExtension config) {
		if (sourceFile.getName().endsWith(".js")) {
			List<File> combinableJavaScriptFiles = config.findCombinableJavaScriptFiles(getProject());
			boolean isGenerated = config.getCombinedJavaScriptFile(getProject()).equals(sourceFile);
			
			return !combinableJavaScriptFiles.contains(sourceFile) && !isGenerated;
		} else if (isTypeScriptFile(sourceFile)) {
			return false;
		} else if (sourceFile.getName().endsWith(".css")) {
			return !config.findCombinableCSSFiles(getProject()).contains(sourceFile);
		} else {
			return true;
		}
	}
	
	private boolean isTypeScriptFile(File sourceFile) {
		return sourceFile.getName().endsWith(".ts") || sourceFile.getName().endsWith(".tsx");
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

		LOGGER.debug("Rewriting web app source file " + sourceFile.getAbsolutePath());
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
		List<String> result = new ArrayList<>();
		Set<String> inserted = new HashSet<>();
		
		for (String line : lines) {
			String rewrittenLine = rewriteSourceFileReferences(line, config);
			
			if (line.equals(rewrittenLine)) {
				result.add(line);
			} else if (!inserted.contains(rewrittenLine)) {
				result.add(rewrittenLine);
				inserted.add(rewrittenLine);
			}
		}
		
		return result;
	}
	
	private String rewriteSourceFileReferences(String line, WebAppExtension config) {
		Project project = getProject();
		//TODO support replacement file in relative path (currently assumes the
		//     combined file is always created in the web build directory root.
		line = rewriteSourceFileReferences(line, config.findCombinableJavaScriptFiles(project), 
				config.getCombinedJavaScriptFile(project));
		line = rewriteSourceFileReferences(line, config.findCombinableCSSFiles(project), 
				config.getCombinedCSSFile(project));
		return line;
	}
	
	protected String rewriteSourceFileReferences(String line, List<File> sourceFiles, File replacement) {
		//TODO this check is based on the file name, meaning it will not work
		//     if there are multiple source files in different directories
		//     with the same name.
		List<String> sourceFileNames = sourceFiles.stream()
			.map(f -> f.getName())
			.collect(Collectors.toList());
		
		return rewriteSourceFileReferences(line, sourceFileNames, replacement.getName());
	}
	
	protected String rewriteSourceFileReferences(String line, List<String> sourceFileNames, 
			String replacementFileName) {
		for (String sourceFileName : sourceFileNames) {
			if (isJavaScriptFileReference(line, sourceFileName)) {
				return "<script src=\"" + replacementFileName + "\"></script>";
			}
			
			if (isCSSFileReference(line, sourceFileName)) {
				return "<link rel=\"stylesheet\" href=\"" + replacementFileName + "\" />";
			}
		}
		
		return line;
	}

	private boolean isJavaScriptFileReference(String line, String sourceFileName) {
		return line.trim().startsWith("<script ") && line.contains(" src=\"") && 
				line.toLowerCase().contains(sourceFileName.toLowerCase());
	}
	
	private boolean isCSSFileReference(String line, String sourceFileName) {
		return line.trim().startsWith("<link ") && line.contains("rel=\"stylesheet\"") &&
				line.contains(" href=\"") && line.toLowerCase().contains(sourceFileName.toLowerCase());
	}
}
