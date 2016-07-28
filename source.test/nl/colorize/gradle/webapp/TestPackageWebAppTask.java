//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import nl.colorize.gradle.webapp.PackageWebAppTask;
import nl.colorize.gradle.webapp.WebAppPlugin;

public class TestPackageWebAppTask {

	@Test
	public void testRewriteJavaScriptFileReferences() throws Exception {
		PackageWebAppTask task = createTask();
		List<String> references = Arrays.asList("first.js", "lib/second.js");
		String replacement = "combined.js";
		
		assertEquals("", task.rewriteJavaScriptFileReferences("", references, replacement));
		assertEquals("Unrelated textual reference to first", 
				task.rewriteJavaScriptFileReferences("Unrelated textual reference to first", 
				references, replacement));
		assertEquals("<script src=\"combined.js\"></script>", 
				task.rewriteJavaScriptFileReferences("<script src=\"first.js\"></script>", 
				references, replacement));
		assertEquals("<script src=\"combined.js\"></script>", 
				task.rewriteJavaScriptFileReferences("<script src=\"lib/second.js\"></script>", 
				references, replacement));
	}
	
	private PackageWebAppTask createTask() {
		Project project = ProjectBuilder.builder().build();
		WebAppPlugin plugin = new WebAppPlugin();
		plugin.apply(project);
		return (PackageWebAppTask) project.getTasks().getByName("packageWebApp");
	}
}
