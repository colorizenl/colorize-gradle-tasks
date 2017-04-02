//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2017 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
		String originalContents = readZipFileContents(tempOutputFile,
				Arrays.asList("combined.js", "-----", " files"));
		createTask().repackageWAR(tempOutputFile, new WebAppExtension());
		String outputFileContents = readZipFileContents(tempOutputFile, 
				Arrays.asList("combined.js", "-----", " files"));
		String today = new SimpleDateFormat("MM-dd-yyyy HH:mm").format(new Date());
		
		assertTrue(tempOutputFile.exists());
		assertEquals(originalContents.replaceAll("07-30-2007 \\d+:\\d+", today), outputFileContents);
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
	
	private String readZipFileContents(File zipFile, List<String> ignoreLines) throws IOException {
		if (!zipFile.exists()) {
			throw new AssertionError("ZIP file does not exist");
		}
		
		String output = runShellCommand("unzip", "-l", zipFile.getAbsolutePath());
		StringBuilder processed = new StringBuilder();
		for (String line : output.split("\n")) {
			if (!lineContains(line, ignoreLines)) {
				processed.append(line).append("\n");
			}
		}
		return processed.toString();
	}
	
	private boolean lineContains(String line, List<String> ignoreLines) {
		for (String pattern : ignoreLines) {
			if (line.contains(pattern)) {
				return true;
			}
		}
		return false;
	}
	
	private String runShellCommand(String... command) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process process = processBuilder.start();
		
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
