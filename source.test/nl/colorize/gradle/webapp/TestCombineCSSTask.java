package nl.colorize.gradle.webapp;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.internal.impldep.com.google.common.io.Files;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class TestCombineCSSTask {

	private static final Charset CHARSET = Charset.forName("UTF-8");
	
	@Test
	public void testCombineCSS() throws Exception {
		WebAppExtension config = new WebAppExtension();
		
		File firstFile = File.createTempFile("first", ".css");
		Files.write("first\n1", firstFile, CHARSET);
		
		File secondFile = File.createTempFile("second", ".css");
		Files.write("second\n2", secondFile, CHARSET);
		
		File combinedFile = File.createTempFile("combined", ".css");
		createTask().combineCSS(Arrays.asList(firstFile, secondFile), combinedFile, config);
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
	public void testCombinedFileHasProjectName() {
		Project project = createTask().getProject();
		WebAppExtension config = new WebAppExtension();
		File combinedFile = config.getCombinedCSSFile(project);
		
		assertEquals("test-" + config.getBuild() + ".css", combinedFile.getName());
	}
	
	private CombineCSSTask createTask() {
		Project project = ProjectBuilder.builder().withProjectDir(new File("testbuild")).build();
		new WebAppPlugin().apply(project);
		return (CombineCSSTask) project.getTasks().getByName("combineCSS");
	}
}
