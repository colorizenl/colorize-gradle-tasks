//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class TestPackageWebAppTask {

	@Test
	public void testRewriteJavaScriptFileReferences() throws Exception {
		PackageWebAppTask task = createTask();
		List<String> references = Arrays.asList("first.js", "lib/second.js");
		String replacement = "combined.js";
		
		assertEquals("", task.rewriteJavaScriptSourceFileReferences("", references, replacement));
		assertEquals("Unrelated textual reference to first", 
				task.rewriteJavaScriptSourceFileReferences("Unrelated textual reference to first", 
				references, replacement));
		assertEquals("<script src=\"combined.js\"></script>", 
				task.rewriteJavaScriptSourceFileReferences("<script src=\"first.js\"></script>", 
				references, replacement));
		assertEquals("<script src=\"combined.js\"></script>", 
				task.rewriteJavaScriptSourceFileReferences("<script src=\"lib/second.js\"></script>", 
				references, replacement));
		assertEquals("<script src=\"combined.js\"></script>", 
				task.rewriteJavaScriptSourceFileReferences("<script src=\"lib/Second.js\"></script>", 
				references, replacement));
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
	
	private PackageWebAppTask createTask() {
		Project project = ProjectBuilder.builder().withProjectDir(new File("testbuild")).build();
		WebAppPlugin plugin = new WebAppPlugin();
		plugin.apply(project);
		return (PackageWebAppTask) project.getTasks().getByName("packageWebApp");
	}
}
