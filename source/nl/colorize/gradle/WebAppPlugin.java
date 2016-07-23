//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.TaskContainer;

/**
 * Gradle plugin for building HTML/CSS/JavaScript applications.
 */
public class WebAppPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		// Ensure that the base plugin has been applied, so that the
		// standard "clean" and "assemble" tasks are available.
		if (!project.getPlugins().hasPlugin(BasePlugin.class)) {
			project.getPlugins().apply(BasePlugin.class);
		}
		
		initConfiguration(project.getExtensions());
		initTasks(project.getTasks());
	
		integrateWithOtherPlugins(project);
	}

	private void initConfiguration(ExtensionContainer extensions) {
		extensions.create("webApp", WebAppExtension.class);
	}

	private void initTasks(TaskContainer tasks) {
		tasks.create("combineJavaScript", CombineJavaScriptTask.class);
		tasks.create("packageWebApp", PackageWebAppTask.class);
		
		tasks.getByName("assemble").dependsOn("packageWebApp");
		tasks.getByName("packageWebApp").dependsOn("combineJavaScript");
	}
	
	private void integrateWithOtherPlugins(Project project) {
		// If the WAR plugin is also used in the same project, make sure
		// the packaged web application is used instead of the original
		// source files.
		if (project.getPlugins().hasPlugin(WarPlugin.class)) {
			//TODO
		}
	}
}
