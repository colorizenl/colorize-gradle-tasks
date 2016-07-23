//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Project;

/**
 * Configuration for the web application, including the locations of the source
 * files and information for how it should be packaged.
 */
public class WebAppExtension {

	private String sourceDir;
	private String buildDir;
	private String combinedJavaScriptFileName;
	private String charset;
	
	public WebAppExtension() {
		sourceDir = "web";
		buildDir = "web";
		combinedJavaScriptFileName = "combined.js";
		charset = "UTF-8";
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
	
	public void setCombinedJavaScriptFileName(String combinedJavaScriptFileName) {
		this.combinedJavaScriptFileName = combinedJavaScriptFileName;
	}
	
	public String getCombinedJavaScriptFileName() {
		return combinedJavaScriptFileName;
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
	 * Returns the path of the specified file relative to the source directory.
	 * @throws IllegalArgumentException if the file is located in a directory
	 *         outside of the source directory.
	 */
	public String toRelativePath(Project project, File sourceFile) {
		String sourceFilePath = sourceFile.getAbsolutePath();
		String root = project.file(sourceDir).getAbsolutePath();
		if (!sourceFilePath.startsWith(root) || sourceFilePath.equals(root)) {
			throw new IllegalArgumentException("File " + sourceFilePath + 
					" is located outside of source directory");
		}
		return sourceFilePath.substring(root.length() + 1);
	}
	
	public void prepareOutputFile(File outputFile) {
		outputFile.mkdirs();
		if (outputFile.exists()) {
			outputFile.delete();
		}
	}
	
	public List<File> findJavaScriptFiles(Project project) {
		List<File> jsFiles = new ArrayList<>();
		for (File file : project.fileTree(sourceDir).getFiles()) {
			if (file.getName().endsWith(".js")) {
				jsFiles.add(file);
			}
		}
		return jsFiles;
	}
}
