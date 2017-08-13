//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2017 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import groovy.lang.Closure;

public class TestPackageWebAppTask {
	
	private static final Charset CHARSET = Charset.forName("UTF-8");

	@Test
	public void testRewriteJavaScriptFileReferences() throws Exception {
		PackageWebAppTask task = createTask();
		List<String> references = Arrays.asList("first.js", "lib/second.js");
		String replacement = "combined.js";
		
		assertEquals("", task.rewriteSourceFileReferences("", references, replacement));
		assertEquals("Unrelated textual reference to first", 
				task.rewriteSourceFileReferences("Unrelated textual reference to first", 
				references, replacement));
		assertEquals("<script src=\"combined.js\"></script>", 
				task.rewriteSourceFileReferences("<script src=\"first.js\"></script>", 
				references, replacement));
		assertEquals("<script src=\"combined.js\"></script>", 
				task.rewriteSourceFileReferences("<script src=\"lib/second.js\"></script>", 
				references, replacement));
		assertEquals("<script src=\"combined.js\"></script>", 
				task.rewriteSourceFileReferences("<script src=\"lib/Second.js\"></script>", 
				references, replacement));
	}
	
	@Test
	public void testRewriteReferencesToCSS() {
		PackageWebAppTask task = createTask();
		
		assertEquals("<link rel=\"stylesheet\" href=\"combined.css\" />", 
				task.rewriteSourceFileReferences("<link rel=\"stylesheet\" href=\"css/test.css\" />",
				Arrays.asList("css/test.css"), "combined.css"));
	}
	
	@Test
	public void testReplacePreviousBuildDirContents() throws Exception {
		File tempDir = new File(System.getProperty("java.io.tmpdir") + "/tempbuild");
		tempDir.mkdir();
		Files.write(new File(tempDir, "test.txt").toPath(), Arrays.asList("first", "second"), 
				Charset.forName("UTF-8"));
		
		PackageWebAppTask task = createTask();
		WebAppExtension config = task.getProject().getExtensions().getByType(WebAppExtension.class);
		task.cleanBuildDir(tempDir, config);
		
		assertTrue(tempDir.exists());
		assertFalse(new File(tempDir, "test.txt").exists());
	}
	
	@Test
	public void testShouldCopySourceFile() {
		PackageWebAppTask task = createTask();
		WebAppExtension config = createConfig(new File("/tmp/test.js"));
		
		assertFalse(task.shouldCopySourceFile(new File("/tmp/test.js"), config));
		assertTrue(task.shouldCopySourceFile(new File("/tmp/lib/test.js"), config));
		assertTrue(task.shouldCopySourceFile(new File("/tmp/node_modules/test.js"), config));
	}
	
	@Test
	public void testCombineCSS() throws Exception {
		WebAppExtension config = new WebAppExtension();
		
		File firstFile = File.createTempFile("first", ".css");
		Files.write(firstFile.toPath(), Arrays.asList("first", "1"), CHARSET);
		
		File secondFile = File.createTempFile("second", ".css");
		Files.write(secondFile.toPath(), Arrays.asList("second", "2"), CHARSET);
		
		File combinedFile = File.createTempFile("combined", ".css");
		createTask().combineFiles(Arrays.asList(firstFile, secondFile), combinedFile, config, null);
		List<String> lines = Files.readAllLines(combinedFile.toPath(), CHARSET);
		
		assertEquals(6, lines.size());
		assertEquals("first", lines.get(0));
		assertEquals("1", lines.get(1));
		assertEquals("", lines.get(2));
		assertEquals("second", lines.get(3));
		assertEquals("2", lines.get(4));
		assertEquals("", lines.get(5));
	}
	
	@Test
	public void testCombinedCssFileHasProjectName() {
		Project project = createTask().getProject();
		WebAppExtension config = new WebAppExtension();
		File combinedFile = config.getCombinedCSSFile(project);
		
		assertEquals("test-" + config.getBuild() + ".css", combinedFile.getName());
	}
	
	@Test
	public void testWriteCombinedJavaScriptFile() throws Exception {
		WebAppExtension config = new WebAppExtension();
		
		File firstFile = File.createTempFile("first", ".js");
		Files.write(firstFile.toPath(), Arrays.asList("first", "1"), CHARSET);
		
		File secondFile = File.createTempFile("first", ".js");
		Files.write(secondFile.toPath(), Arrays.asList("second", "2"), CHARSET);
		
		File combinedFile = File.createTempFile("combined", ".js");
		createTask().combineFiles(Arrays.asList(firstFile, secondFile), combinedFile, config, null);
		List<String> lines = Files.readAllLines(combinedFile.toPath(), CHARSET);
		
		assertEquals(6, lines.size());
		assertEquals("first", lines.get(0));
		assertEquals("1", lines.get(1));
		assertEquals("", lines.get(2));
		assertEquals("second", lines.get(3));
		assertEquals("2", lines.get(4));
		assertEquals("", lines.get(5));
	}
	
	@Test
	public void testExcludeJavaScriptFiles() throws Exception {
		Project project = ProjectBuilder.builder().withProjectDir(new File("testbuild")).build();
		WebAppExtension config = new WebAppExtension();
		config.setSourceDir("resources");
		List<File> jsFiles = config.findCombinableJavaScriptFiles(project);
		
		assertEquals(2, jsFiles.size());
		assertEquals("first.js", jsFiles.get(0).getName());
		assertEquals("second.js", jsFiles.get(1).getName());
		
		config.setExcludes(Arrays.asList("*fir*"));
		jsFiles = config.findCombinableJavaScriptFiles(project);
		
		assertEquals(1, jsFiles.size());
		assertEquals("second.js", jsFiles.get(0).getName());
	}
	
	@Test
	public void testRewriteJavaScriptFilter() throws Exception {
		WebAppExtension config = new WebAppExtension();
		config.setRewriteJavaScriptFilter(new Closure<String>(this) {
			@Override
			public String call(Object s) {
				return s.equals("second") ? "2" : s.toString();
			}
		});
		
		File testFile = File.createTempFile("first", ".js");
		Files.write(testFile.toPath(), Arrays.asList("first", "second", "third"), CHARSET);
		
		File combinedFile = File.createTempFile("combined", ".js");
		createTask().combineFiles(Arrays.asList(testFile), combinedFile, config, 
				config.getRewriteJavaScriptFilter());
		List<String> lines = Files.readAllLines(combinedFile.toPath(), CHARSET);
		
		assertEquals(4, lines.size());
		assertEquals("first", lines.get(0));
		assertEquals("2", lines.get(1));
		assertEquals("third", lines.get(2));
		assertEquals("", lines.get(3));
	}
	
	@Test
	public void testDoNotCopyTypeScriptFiles() throws Exception {
		PackageWebAppTask task = createTask();
		WebAppExtension config = new WebAppExtension();
		
		assertFalse(task.shouldCopySourceFile(new File("test.ts"), config));
		assertFalse(task.shouldCopySourceFile(new File("sub/test.ts"), config));
	}
	
	private Project createProject() {
		return ProjectBuilder.builder().withProjectDir(new File("/tmp")).build();
	}
	
	private PackageWebAppTask createTask() {
		Project project = createProject();
		WebAppPlugin plugin = new WebAppPlugin();
		plugin.apply(project);
		return (PackageWebAppTask) project.getTasks().getByName("packageWebApp");
	}
	
	private WebAppExtension createConfig(File... jsFiles) {
		return new WebAppExtension() {
			@Override
			public List<File> findCombinableJavaScriptFiles(Project project) {
				List<File> result = new ArrayList<>();
				result.addAll(Arrays.asList(jsFiles));
				result.removeAll(findJavaScriptLibraryFiles(project));
				return result;
			}
			
			@Override
			protected List<File> findJavaScriptLibraryFiles(Project project) {
				return Arrays.asList(jsFiles).stream()
					.filter(f -> f.getAbsolutePath().contains("/node_modules/"))
					.collect(Collectors.toList());
			}
		};
	}
}
