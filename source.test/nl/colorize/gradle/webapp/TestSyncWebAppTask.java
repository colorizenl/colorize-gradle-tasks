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

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class TestSyncWebAppTask {

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
		
		SyncWebAppTask task = createTask();
		WebAppExtension config = task.getProject().getExtensions().getByType(WebAppExtension.class);
		task.sync(syncSourceDir, syncDestDir, config);
		
		File[] contents = syncDestDir.listFiles();
		assertEquals(2, contents.length);
		assertEquals("a.txt", contents[0].getName());
		assertEquals("b.txt", contents[1].getName());
	}
	
	private SyncWebAppTask createTask() {
		Project project = ProjectBuilder.builder().withProjectDir(new File("testbuild")).build();
		WebAppPlugin plugin = new WebAppPlugin();
		plugin.apply(project);
		return (SyncWebAppTask) project.getTasks().getByName("syncWebApp");
	}
}
