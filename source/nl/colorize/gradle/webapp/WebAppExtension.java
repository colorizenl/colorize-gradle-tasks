//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileTree;

/**
 * Configuration for the web application, including the locations of the source
 * files and information for how it should be packaged.
 */
public class WebAppExtension {

	private String sourceDir;
	private String buildDir;
	private String combinedJavaScriptFileName;
	private List<String> excludedJavaScriptFiles;
	private String charset;
	
	private static final List<String> DEFAULT_JAVASCRIPT_EXCLUDES = Arrays.asList(
			"index.js", "*.bundle.js");
	
	public WebAppExtension() {
		sourceDir = "web";
		buildDir = "build/web";
		combinedJavaScriptFileName = "combined.js";
		excludedJavaScriptFiles = new ArrayList<>();
		charset = "UTF-8";
	}
	
	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}
	
	public String getSourceDir() {
		return sourceDir;
	}
	
	public ConfigurableFileTree getSourceTree(Project project) {
		return project.fileTree(sourceDir);
	}
	
	public void setBuildDir(String buildDir) {
		this.buildDir = buildDir;
	}
	
	public String getBuildDir() {
		return buildDir;
	}
	
	public File getBuildDir(Project project) {
		return project.file(buildDir);
	}
	
	public void setCombinedJavaScriptFileName(String combinedJavaScriptFileName) {
		this.combinedJavaScriptFileName = combinedJavaScriptFileName;
	}
	
	public String getCombinedJavaScriptFileName() {
		return combinedJavaScriptFileName;
	}

	public File getCombinedJavaScriptFile(Project project) {
		return new File(getBuildDir(project), combinedJavaScriptFileName);
	}
	
	public void setExcludedJavaScriptFiles(List<String> excludedJavaScriptFiles) {
		this.excludedJavaScriptFiles = excludedJavaScriptFiles;
	}
	
	public List<String> getExcludedJavaScriptFiles() {
		List<String> excluded = new ArrayList<>();
		excluded.addAll(excludedJavaScriptFiles);
		excluded.addAll(DEFAULT_JAVASCRIPT_EXCLUDES);
		return excluded;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	public String getCharset() {
		return charset;
	}
	
	public Charset getCharsetObject() {
		return Charset.forName(charset);
	}
	
	/**
	 * Returns the path of the specified file relative to the project directory.
	 * @throws IllegalArgumentException if the file is located in a directory
	 *         outside of the source directory.
	 */
	public String toRelativePath(Project project, File sourceFile) {
		return toRelativePath(sourceFile, project.file(sourceDir));
	}
	
	/**
	 * Returns the path of the specified file relative to the build directory.
	 * @throws IllegalArgumentException if the file is located in a directory
	 *         outside of the build directory.
	 */
	public String toBuildRelativePath(Project project, File sourceFile) {
		return toRelativePath(sourceFile, project.getBuildDir());
	}
	
	private String toRelativePath(File sourceFile, File dir) {
		String sourceFilePath = sourceFile.getAbsolutePath();
		String dirPath = dir.getAbsolutePath();
		if (!sourceFilePath.startsWith(dirPath) || sourceFilePath.equals(dirPath)) {
			throw new IllegalArgumentException("File " + sourceFilePath + 
					" is located outside of source directory");
		}
		return sourceFilePath.substring(dirPath.length() + 1);
	}
	
	public void prepareOutputFile(File outputFile) {
		outputFile.mkdirs();
		if (outputFile.exists()) {
			outputFile.delete();
		}
	}
	
	/**
	 * Finds all JavaScript source files in the specified project. The returned
	 * list will not contain any JavaScript files produced by the build itself,
	 * or files that have been excluded.
	 */
	public List<File> findJavaScriptFiles(Project project) {
		ConfigurableFileTree fileTree = project.fileTree(sourceDir);
		fileTree.include("**/*.js");
		fileTree.exclude(excludedJavaScriptFiles);
		
		List<File> jsFiles = new ArrayList<>();
		jsFiles.addAll(fileTree.getFiles());
		return jsFiles;
	}
}
