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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
	private List<String> excludes;
	private String charset;
	private List<String> syncDirs;
	
	private boolean combineJavaScriptEnabled;
	private String combinedJavaScriptFileName;
	private List<String> combineJavaScriptExcludes;
	private boolean combineJavaScriptLibraries;
	private Closure<String> rewriteJavaScriptFilter;
	
	private boolean combineCSSEnabled;
	private String combinedCSSFileName;
	private List<String> combineCSSExcludes;
	
	private String build;
	
	private static final List<String> JAVASCRIPT_LIBRARY_PATTERNS = Arrays.asList(
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
		excludes = new ArrayList<>();
		charset = "UTF-8";
		syncDirs = new ArrayList<>();
		
		combineJavaScriptEnabled = true;
		combineJavaScriptExcludes = new ArrayList<>();
		combineJavaScriptLibraries = false;
		
		combineCSSEnabled = false;
		combineCSSExcludes = new ArrayList<>();
		
		build = UUID.randomUUID().toString();
	}
	
	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}
	
	public String getSourceDir() {
		return sourceDir;
	}
	
	public File getSourceDir(Project project) {
		return project.file(sourceDir);
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
	
	public List<String> getExcludes() {
		return excludes;
	}
	
	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
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
	
	public boolean getCombineJavaScriptEnabled() {
		return combineJavaScriptEnabled;
	}
	
	public void setCombineJavaScriptEnabled(boolean combineJavaScriptEnabled) {
		this.combineJavaScriptEnabled = combineJavaScriptEnabled;
	}

	public void setCombinedJavaScriptFileName(String combinedJavaScriptFileName) {
		this.combinedJavaScriptFileName = combinedJavaScriptFileName;
	}
	
	public String getCombinedJavaScriptFileName() {
		return combinedJavaScriptFileName;
	}

	public File getCombinedJavaScriptFile(Project project) {
		String fileName = combinedCSSFileName;
		if (fileName == null) {
			Project rootProject = (Project) project.getProperties().get("rootProject");
			fileName = toGeneratedFileName(rootProject.getName(), "js");
		}
		return new File(getBuildDir(project), fileName);
	}
	
	public List<String> getCombineJavaScriptExcludes() {
		return combineJavaScriptExcludes;
	}
	
	public void setCombineJavaScriptExcludes(List<String> combineJavaScriptExcludes) {
		this.combineJavaScriptExcludes = combineJavaScriptExcludes;
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
	
	public boolean getCombineCSSEnabled() {
		return combineCSSEnabled;
	}
	
	public void setCombineCSSEnabled(boolean combineCSSEnabled) {
		this.combineCSSEnabled = combineCSSEnabled;
	}

	public String getCombinedCSSFileName() {
		return combinedCSSFileName;
	}
	
	public File getCombinedCSSFile(Project project) {
		String fileName = combinedCSSFileName;
		if (fileName == null) {
			Project rootProject = (Project) project.getProperties().get("rootProject");
			fileName = toGeneratedFileName(rootProject.getName(), "css");
		}
		return new File(getBuildDir(project), fileName);
	} 

	public void setCombinedCSSFileName(String combinedCSSFileName) {
		this.combinedCSSFileName = combinedCSSFileName;
	}
	
	public List<String> getCombineCSSExcludes() {
		return combineCSSExcludes;
	}
	
	public void setCombineCSSExcludes(List<String> combineCSSExcludes) {
		this.combineCSSExcludes = combineCSSExcludes;
	}

	/**
	 * Returns the path of the specified file relative to the project directory.
	 * @throws IllegalArgumentException if the file is located in a directory
	 *         outside of the source directory.
	 */
	public String toRelativePath(Project project, File sourceFile) {
		return toRelativePath(sourceFile, project.file(sourceDir));
	}
	
	private String toGeneratedFileName(String name, String ext) {
		if (name == null) {
			throw new NullPointerException();
		}
		
		String normalizedName = name;
		normalizedName = normalizedName.toLowerCase();
		normalizedName = normalizedName.replaceAll("\\s+", "-");
		return normalizedName + "-" + build + "." + ext;
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
	 * Finds all JavaScript source files in the project that are in scope, and
	 * that should be combined. The returned list will not contain any JavaScript 
	 * files produced by the build itself, files that have been excluded, or
	 * files that should not be combined.
	 */
	public List<File> findCombinableJavaScriptFiles(Project project) {
		if (!combineJavaScriptEnabled) {
			return Collections.emptyList();
		}
		
		ConfigurableFileTree fileTree = project.fileTree(sourceDir);
		fileTree.include("**/*.js");
		fileTree.exclude(getExcludedJavaScriptPatterns());
		
		return new ArrayList<File>(fileTree.getFiles());
	}
	
	private List<String> getExcludedJavaScriptPatterns() {
		List<String> excluded = new ArrayList<>();
		excluded.addAll(DEFAULT_EXCLUDES);
		excluded.addAll(excludes);
		if (!combineJavaScriptLibraries) {
			excluded.addAll(JAVASCRIPT_LIBRARY_PATTERNS);
		}
		excluded.addAll(combineJavaScriptExcludes);
		return excluded;
	}
	
	protected List<File> findJavaScriptLibraryFiles(Project project) {
		ConfigurableFileTree fileTree = project.fileTree(sourceDir);
		fileTree.include(JAVASCRIPT_LIBRARY_PATTERNS);
		fileTree.exclude(DEFAULT_EXCLUDES);
		fileTree.exclude(excludes);
		
		return new ArrayList<File>(fileTree.getFiles());
	}
	
	/**
	 * Finds all CSS source files that should be combined.
	 */
	public List<File> findCombinableCSSFiles(Project project) {
		if (!combineCSSEnabled) {
			return Collections.emptyList();
		}
		
		List<String> cssExcludes = new ArrayList<>();
		cssExcludes.addAll(DEFAULT_EXCLUDES);
		cssExcludes.addAll(excludes);
		cssExcludes.addAll(combineCSSExcludes);
		// Some JavaScript libraries come with their own stylesheets.
		// By default these will not end up in the combined CSS file.
		cssExcludes.addAll(JAVASCRIPT_LIBRARY_PATTERNS);
		
		ConfigurableFileTree fileTree = project.fileTree(sourceDir);
		fileTree.include("**/*.css");
		fileTree.exclude(cssExcludes);
		return new ArrayList<File>(fileTree.getFiles());
	}
	
	protected String getBuild() {
		return build;
	}
}
