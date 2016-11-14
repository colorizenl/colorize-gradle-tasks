//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.War;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repackages a WAR file by replacing the JavaScript source files with the
 * combined JavaScript file. 
 */
public class RepackageWarFileTask extends DefaultTask {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RepackageWarFileTask.class);

	@TaskAction
	public void run() {
		WebAppExtension config = getProject().getExtensions().getByType(WebAppExtension.class);
		War warTask = (War) getProject().getTasks().getByName("war");
		File warFile = warTask.getArchivePath();
		
		repackageWAR(warFile, config);
	}

	protected void repackageWAR(File warFile, WebAppExtension config) {
		LOGGER.debug("Repacking WAR file " + warFile.getAbsolutePath());
		try {
			Map<String, byte[]> entries = extractEntries(warFile);
			excludeSourceFiles(entries, config);
			includeBuildFiles(entries, config);
			createWAR(entries, warFile);
		} catch (IOException e) {
			throw new RuntimeException("Cannot repackage WAR file", e);
		}
	}

	private Map<String, byte[]> extractEntries(File warFile) throws IOException {
		if (!warFile.exists()) {
			throw new IllegalStateException("WAR file does not exist: " + warFile.getAbsolutePath());
		}
		
		ZipFile zipFile = new ZipFile(warFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		Map<String, byte[]> contents = new LinkedHashMap<>();
		
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			String path = normalizeEntryPath(entry.getName(), warFile);
			contents.put(path, extractEntryContents(zipFile, entry));
		}
		
		zipFile.close();
		
		return contents;
	}
	
	private String normalizeEntryPath(String entryName, File warFile) {
		if (entryName.contains("Users/") || entryName.contains(":\\")) {
			LOGGER.warn("WAR file " + warFile.getName() + " contains entry with absolute " + 
					"path that cannot be preserved while repackaging: " + entryName);
			return entryName.substring(entryName.replace("\\", "/").lastIndexOf('/') + 1);
		}
		return entryName;
	}

	private byte[] extractEntryContents(ZipFile zipFile, ZipEntry entry) throws IOException {
		if (entry.isDirectory()) {
			return new byte[0];
		} else {
			InputStream entryStream = zipFile.getInputStream(entry);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			copy(entryStream, buffer);
			return buffer.toByteArray();
		}
	}
	
	private void excludeSourceFiles(Map<String, byte[]> entries, WebAppExtension config) {
		for (File jsSourceFile : config.findJavaScriptFiles(getProject())) {
			String relativePath = config.toRelativePath(getProject(), jsSourceFile);
			String matchingEntry = findMatchingEntry(entries, relativePath);
			if (matchingEntry != null) {
				LOGGER.debug("Removing web app source file from WAR file: " + matchingEntry);
				entries.remove(matchingEntry);
			}
		}
	}
	
	private void includeBuildFiles(Map<String, byte[]> entries, WebAppExtension config) {
		File combinedJavaScriptFile = config.getCombinedJavaScriptFile(getProject());
		String relativePath = config.toBuildRelativePath(getProject(), combinedJavaScriptFile);
		if (combinedJavaScriptFile.exists() && findMatchingEntry(entries, relativePath) == null) {
			LOGGER.debug("Including generated web app file into WAR file: " + 
					combinedJavaScriptFile.getName());
			entries.put(combinedJavaScriptFile.getName(), readFile(combinedJavaScriptFile));
		}
	}
	
	private String findMatchingEntry(Map<String, byte[]> entries, String path) {
		for (String entryName : entries.keySet()) {
			if (entryName.endsWith(path)) {
				return entryName;
			}
		}
		return null;
	}
	
	private void createWAR(Map<String, byte[]> entries, File warFile) throws IOException {
		LOGGER.debug("Recreating WAR file " + warFile.getAbsolutePath());
		warFile.delete();

		ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(warFile));
		for (String entryName : entries.keySet()) {
			ZipEntry entry = new ZipEntry(entryName);
			outputStream.putNextEntry(entry);
			copy(new ByteArrayInputStream(entries.get(entryName)), outputStream);
			outputStream.closeEntry();
		}
		outputStream.close();
	}
	
	private void copy(InputStream from, OutputStream to) throws IOException {
		byte[] readBuffer = new byte[1024];
		int count = 0;
		while ((count = from.read(readBuffer)) != -1) {
			to.write(readBuffer, 0, count);
		}
	}
	
	private byte[] readFile(File file) {
		try {
			FileInputStream stream = new FileInputStream(file);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			copy(stream, buffer);
			stream.close();
			return buffer.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read file " + file.getAbsolutePath());
		}
	}
}
