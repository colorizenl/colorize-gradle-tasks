//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2017 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Project;
import org.gradle.internal.impldep.com.google.common.io.Files;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import nl.colorize.gradle.webapp.CombineJavaScriptTask;
import nl.colorize.gradle.webapp.WebAppPlugin;

public class TestCombineJavaScriptTask {
	
	private static final Charset CHARSET = Charset.forName("UTF-8");
	
	@Test
	public void testWriteCombinedFile() throws Exception {
		WebAppExtension config = new WebAppExtension();
		
		File firstFile = File.createTempFile("first", ".js");
		Files.write("first\n1", firstFile, CHARSET);
		
		File secondFile = File.createTempFile("first", ".js");
		Files.write("second\n2", secondFile, CHARSET);
		
		File combinedFile = File.createTempFile("combined", ".js");
		createTask().createCombinedFile(Arrays.asList(firstFile, secondFile), combinedFile, config);
		List<String> lines = Files.readLines(combinedFile, CHARSET);
		
		assertEquals(6, lines.size());
		assertEquals("first", lines.get(0));
		assertEquals("1", lines.get(1));
		assertEquals("", lines.get(2));
		assertEquals("second", lines.get(3));
		assertEquals("2", lines.get(4));
		assertEquals("", lines.get(5));
	}
	
	@Test
	public void testLibraryFilesAreIncludedFirst() throws Exception {
		final List<File> files = Arrays.asList(new File("/tmp/a.js"), new File("/tmp/b.js"), 
				new File("/tmp/lib/c.js"), new File("/tmp/lib/d.js"), 
				new File("/tmp/subdir/e.js"), new File("/tmp/subdir/f.js"));
		
		CombineJavaScriptTask task = createTask();
		WebAppExtension config = new WebAppExtension() {
			@Override
			public List<File> findCombinableJavaScriptFiles(Project project) {
				return files;
			}
			
			@Override
			protected List<File> findJavaScriptLibraryFiles(Project project) {
				return files.stream()
					.filter(f -> f.getAbsolutePath().contains("/lib/"))
					.collect(Collectors.toList());
			}
		};
		
		List<File> ordered = task.getOrderedJavaScriptFiles(config, files);
		
		assertEquals(6, ordered.size());
		assertEquals("c.js", ordered.get(0).getName());
		assertEquals("d.js", ordered.get(1).getName());
		assertEquals("a.js", ordered.get(2).getName());
		assertEquals("e.js", ordered.get(4).getName());
		assertEquals("f.js", ordered.get(5).getName());
	}
	
	@Test
	public void testExcludeJavaScriptFiles() throws Exception {
		Project project = createProject();
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
	@SuppressWarnings("unchecked")
	public void testRewriteJavaScriptFilter() throws Exception {
		WebAppExtension config = new WebAppExtension();
		config.setRewriteJavaScriptFilter(new MethodClosure(this, "rewriteJavaScriptLine"));
		
		File testFile = File.createTempFile("first", ".js");
		Files.write("first\nsecond\nthird\n", testFile, CHARSET);
		
		File combinedFile = File.createTempFile("combined", ".js");
		createTask().createCombinedFile(Arrays.asList(testFile), combinedFile, config);
		List<String> lines = Files.readLines(combinedFile, CHARSET);
		
		assertEquals(4, lines.size());
		assertEquals("first", lines.get(0));
		assertEquals("2", lines.get(1));
		assertEquals("third", lines.get(2));
		assertEquals("", lines.get(3));
	}
	
	private Project createProject() {
		return ProjectBuilder.builder().withProjectDir(new File("testbuild")).build();
	}
	
	private CombineJavaScriptTask createTask() {
		Project project = createProject();
		new WebAppPlugin().apply(project);
		return (CombineJavaScriptTask) project.getTasks().getByName("combineJavaScript");
	}
	
	protected String rewriteJavaScriptLine(String line) {
		return line.replace("second", "2");
	}
}
