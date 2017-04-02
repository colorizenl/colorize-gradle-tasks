//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2017 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileTree;

import groovy.lang.Closure;

/**
 * Configuration for the web application, including the locations of the source
 * files and information for how it should be packaged.
 */
public class WebAppExtension {

	private String sourceDir;
	private String buildDir;
	private String combinedJavaScriptFileName;
	private List<String> excludes;
	private boolean combineJavaScriptLibraries;
	private Closure<String> rewriteJavaScriptFilter;
	private String charset;
	private List<String> syncDirs;
	
	private static final List<String> JAVASCRIPT_LIBRARY_DIRECTORIES = Arrays.asList(
			"**/lib/**", 
			"**/node_modules/**", 
			"**/bower_components/**");
	
	private static final List<String> DEFAULT_EXCLUDES = Arrays.asList(
			"**/index.js", 
			"**/*.min.js", 
			"**/*.bundle.js",
			"Gruntfile.js",
			"gulpfile.js");
	
	public WebAppExtension() {
		sourceDir = "web";
		buildDir = "build/web";
		combinedJavaScriptFileName = "combined.js";
		excludes = new ArrayList<>();
		combineJavaScriptLibraries = false;
		charset = "UTF-8";
		syncDirs = new ArrayList<>();
	}
	
	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}
	
	public String getSourceDir() {
		return sourceDir;
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
	
	public List<String> getExcludes() {
		return excludes;
	}
	
	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}
	
	public void setCombineJavaScriptLibraries(boolean combineJavaScriptLibraries) {
		this.combineJavaScriptLibraries = combineJavaScriptLibraries;
	}
	
	public boolean getCombineJavaScriptLibraries() {
		return combineJavaScriptLibraries;
	}
	
	public Closure<String> getRewriteJavaScriptFilter() {
		return rewriteJavaScriptFilter;
	}
	
	public void setRewriteJavaScriptFilter(Closure<String> rewriteJavaScriptFilter) {
		this.rewriteJavaScriptFilter = rewriteJavaScriptFilter;
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
	
	public void setSyncDirs(List<String> syncDirs) {
		this.syncDirs = syncDirs;
	}
	
	public List<String> getSyncDirs() {
		return syncDirs;
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
	
	public String toRelativePath(File sourceFile, File dir) {
		String sourceFilePath = sourceFile.getAbsolutePath();
		String dirPath = dir.getAbsolutePath();
		
		try {
			if (!sourceFilePath.startsWith(dirPath) || sourceFilePath.equals(dirPath)) {
				sourceFilePath = sourceFile.getCanonicalFile().getAbsolutePath();
				dirPath = dir.getCanonicalFile().getAbsolutePath();
				if (!sourceFilePath.startsWith(dirPath) || sourceFilePath.equals(dirPath)) {
					throw new IllegalArgumentException("File " + sourceFilePath + 
							" is located outside of source directory " + dirPath);
				}
			}
			return sourceFilePath.substring(dirPath.length() + 1);
		} catch (IOException e) {
			throw new RuntimeException("Cannot determine relative path for " + sourceFile.getName());
		}
	}
	
	public void prepareOutputFile(File outputFile) {
		outputFile.mkdirs();
		if (outputFile.exists()) {
			outputFile.delete();
		}
	}
	
	/**
	 * Finds all web application files in the project that are in scope according
	 * to this configuration. Note that this includes JavaScript files. 
	 */
	public List<File> findWebAppFiles(Project project) {
		ConfigurableFileTree fileTree = project.fileTree(sourceDir);
		fileTree.exclude(DEFAULT_EXCLUDES);
		fileTree.exclude(excludes);
		
		return new ArrayList<File>(fileTree.getFiles());
	}
	
	/**
	 * Finds all JavaScript source files in the project that are in scope according
	 * to this configuration. The returned list will not contain any JavaScript 
	 * files produced by the build itself, or files that have been excluded.
	 */
	public List<File> findJavaScriptFiles(Project project) {
		ConfigurableFileTree fileTree = project.fileTree(sourceDir);
		fileTree.include("**/*.js");
		fileTree.exclude(getExcludedJavaScriptFiles());
		
		return new ArrayList<File>(fileTree.getFiles());
	}
	
	private List<String> getExcludedJavaScriptFiles() {
		List<String> excluded = new ArrayList<>();
		excluded.addAll(DEFAULT_EXCLUDES);
		excluded.addAll(excludes);
		if (!combineJavaScriptLibraries) {
			for (String jsLibDir : JAVASCRIPT_LIBRARY_DIRECTORIES) {
				excluded.add(jsLibDir);
			}
		}
		return excluded;
	}
	
	public boolean isJavaScriptLibrary(File file) {
		for (String jsLibDir : JAVASCRIPT_LIBRARY_DIRECTORIES) {
			if (file.getName().endsWith(".js") &&
					file.getAbsolutePath().contains(jsLibDir.replace("*", ""))) {
				return true;
			}
		}
		return false;
	}
}
