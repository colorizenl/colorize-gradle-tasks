//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.internal.impldep.com.google.common.io.Files;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class TestCombineJavaScriptTask {
	
	private static final Charset CHARSET = Charset.forName("UTF-8");
	
	@Test
	public void testWriteCombinedFile() throws Exception {
		File firstFile = File.createTempFile("first", ".js");
		Files.write("first\n1", firstFile, CHARSET);
		
		File secondFile = File.createTempFile("first", ".js");
		Files.write("second\n2", secondFile, CHARSET);
		
		File combinedFile = File.createTempFile("combined", ".js");
		createTask().createCombinedFile(Arrays.asList(firstFile, secondFile), combinedFile, CHARSET);
		List<String> lines = Files.readLines(combinedFile, CHARSET);
		
		assertEquals(4, lines.size());
		assertEquals("first", lines.get(0));
		assertEquals("1", lines.get(1));
		assertEquals("second", lines.get(2));
		assertEquals("2", lines.get(3));
	}
	
	@Test
	public void testLibraryFilesAreIncludedFirst() throws Exception {
		List<File> files = Arrays.asList(new File("/tmp/a.js"), new File("/tmp/b.js"), 
				new File("/tmp/lib/c.js"), new File("/tmp/lib/d.js"), 
				new File("/tmp/subdir/e.js"), new File("/tmp/subdir/f.js"));
		
		CombineJavaScriptTask task = createTask();
		List<File> ordered = task.getOrderedJavaScriptFiles(files);
		
		assertEquals(6, ordered.size());
		assertEquals("c.js", ordered.get(0).getName());
		assertEquals("d.js", ordered.get(1).getName());
		assertEquals("a.js", ordered.get(2).getName());
		assertEquals("e.js", ordered.get(4).getName());
		assertEquals("f.js", ordered.get(5).getName());
	}
	
	private CombineJavaScriptTask createTask() {
		Project project = ProjectBuilder.builder().build();
		WebAppPlugin plugin = new WebAppPlugin();
		plugin.apply(project);
		return (CombineJavaScriptTask) project.getTasks().getByName("combineJavaScript");
	}
}
