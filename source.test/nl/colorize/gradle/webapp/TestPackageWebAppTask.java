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

public class TestPackageWebAppTask {

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
	
	private PackageWebAppTask createTask() {
		Project project = ProjectBuilder.builder().withProjectDir(new File("/tmp")).build();
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
