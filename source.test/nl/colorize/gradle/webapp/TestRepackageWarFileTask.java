//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.gradle.api.Project;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class TestRepackageWarFileTask {
	
	private static final File SAMPLE_WAR_FILE = new File("testbuild/resources/sample.war");
	
	@Test
	public void testExtractAndRecreateWarFile() throws IOException {
		File tempOutputFile = File.createTempFile("sample-war-file", ".war");
		FileUtils.copyFile(SAMPLE_WAR_FILE, tempOutputFile);
		String originalContents = readZipFileContents(tempOutputFile);
		createTask().repackageWAR(tempOutputFile, new WebAppExtension());
		String outputFileContents = readZipFileContents(tempOutputFile);
		
		assertTrue(tempOutputFile.exists());
		assertEquals(originalContents, outputFileContents);
	}
	
	@Test
	public void testReplaceJavaScriptSourceFilesWithCombinedFile() throws IOException {
		File tempOutputFile = File.createTempFile("sample-combined-file", ".war");
		tempOutputFile.delete();
		runShellCommand("zip", "-r", tempOutputFile.getAbsolutePath(), 
				new File("testbuild/resources").getAbsolutePath());
		String originalContents = readZipFileContents(tempOutputFile);
		
		WebAppExtension config = new WebAppExtension();
		config.setSourceDir("resources");
		config.setBuildDir("build");
		config.setCombinedJavaScriptFileName("combined.js");
		
		RepackageWarFileTask task = createTask();
		task.repackageWAR(tempOutputFile, config);
		String repackagedContents = readZipFileContents(tempOutputFile);
		
		assertTrue(originalContents.contains("lines.html"));
		assertTrue(originalContents.contains("first.js"));
		assertTrue(originalContents.contains("second.js"));
		assertFalse(originalContents.contains("combined.js"));
		
		assertTrue(repackagedContents.contains("lines.html"));
		assertFalse(repackagedContents.contains("first.js"));
		assertFalse(repackagedContents.contains("second.js"));
		assertTrue(repackagedContents.contains("combined.js"));
	}
	
	private Project createProject() {
		Project project = ProjectBuilder.builder().withProjectDir(new File("testbuild")).build();
		project.setBuildDir("build");
		return project;
	}

	private RepackageWarFileTask createTask() {
		Project project = createProject();
		new WarPlugin().apply(project);
		new WebAppPlugin().apply(project);
		return (RepackageWarFileTask) project.getTasks().getByName("repackageWAR");
	}
	
	private String readZipFileContents(File zipFile) throws IOException {
		if (!zipFile.exists()) {
			throw new AssertionError("ZIP file does not exist");
		}
		
		String output = runShellCommand("unzip", "-l", zipFile.getAbsolutePath());
		// Remove the date/time column to be able to compare the result.
		Pattern dateTimePattern = Pattern.compile("\\d\\d-\\d\\d-\\d\\d \\d\\d[:]\\d\\d");
		return dateTimePattern.matcher(output).replaceAll("");
	}
	
	private String runShellCommand(String... command) throws IOException {
		Process process = Runtime.getRuntime().exec(command);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
		StringBuilder buffer = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			buffer.append(line).append("\n");
		}
		reader.close();
		
		return buffer.toString();
	}
}
