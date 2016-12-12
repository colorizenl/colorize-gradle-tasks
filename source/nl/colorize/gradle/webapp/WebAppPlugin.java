//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
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
	
		// Integrate with the 'war' plugin if that has also been 
		// applied by the project.
		if (project.getPlugins().hasPlugin(WarPlugin.class) || hasTask(project, "war")) {
			integrateWithWarPlugin(project.getTasks());
		}
		
		// Ensure that all NPM and/or Bower libraries have been 
		// downloaded and refreshed.
		if (hasTask(project, "clientRefresh")) {
			integrateWithClientLibrariesPlugin(project.getTasks());
		}
	}

	private void initConfiguration(ExtensionContainer extensions) {
		extensions.create("webApp", WebAppExtension.class);
	}

	private void initTasks(TaskContainer tasks) {
		tasks.create("combineJavaScript", CombineJavaScriptTask.class);
		tasks.create("packageWebApp", PackageWebAppTask.class);
		tasks.create("syncWebApp", SyncWebAppTask.class);
		
		tasks.getByName("packageWebApp").dependsOn("combineJavaScript");
		tasks.getByName("syncWebApp").dependsOn("packageWebApp");
		tasks.getByName("assemble").dependsOn("packageWebApp", "syncWebApp");
	}
	
	private boolean hasTask(Project project, String taskName) {
		try {
			project.getTasks().getByName(taskName);
			return true;
		} catch (UnknownTaskException e) {
			return false;
		}
	}
	
	/**
	 * If the WAR plugin is also used in the same project, include the packaged 
	 * web application into the WAR file instead of the original source files.
	 */
	private void integrateWithWarPlugin(final TaskContainer tasks) {
		tasks.create("repackageWAR", RepackageWarFileTask.class);
		tasks.getByName("repackageWAR").dependsOn("war", "packageWebApp");
		tasks.getByName("assemble").dependsOn("repackageWAR");
	}
	
	private void integrateWithClientLibrariesPlugin(TaskContainer tasks) {
		tasks.getByName("packageWebApp").dependsOn("clientRefresh");
	}
}
