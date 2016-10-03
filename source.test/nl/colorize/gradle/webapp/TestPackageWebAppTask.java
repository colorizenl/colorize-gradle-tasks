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
	
	@Test
	public void testSyncPackagedWebAppToDirs() throws Exception {
		File syncSourceDir = new File(System.getProperty("java.io.tmpdir") + "/sync-source");
		syncSourceDir.mkdir();
		Files.write(new File(syncSourceDir, "a.txt").toPath(), Arrays.asList("test"), Charset.forName("UTF-8"));
		Files.write(new File(syncSourceDir, "b.txt").toPath(), Arrays.asList("test"), Charset.forName("UTF-8"));
		
		File syncDestDir = new File(System.getProperty("java.io.tmpdir") + "/sync-dest");
		syncDestDir.mkdir();
		Files.write(new File(syncDestDir, "b.txt").toPath(), Arrays.asList("test"), Charset.forName("UTF-8"));
		Files.write(new File(syncDestDir, "c.txt").toPath(), Arrays.asList("test"), Charset.forName("UTF-8"));
		
		PackageWebAppTask task = createTask();
		WebAppExtension config = task.getProject().getExtensions().getByType(WebAppExtension.class);
		task.sync(syncSourceDir, syncDestDir, config);
		
		File[] contents = syncDestDir.listFiles();
		assertEquals(2, contents.length);
		assertEquals("a.txt", contents[0].getName());
		assertEquals("b.txt", contents[1].getName());
	}
	
	private PackageWebAppTask createTask() {
		Project project = ProjectBuilder.builder().withProjectDir(new File("testbuild")).build();
		WebAppPlugin plugin = new WebAppPlugin();
		plugin.apply(project);
		return (PackageWebAppTask) project.getTasks().getByName("packageWebApp");
	}
}
