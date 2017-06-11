//-----------------------------------------------------------------------------
// Colorize Gradle tasks
// Copyright 2010-2017 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.gradle.webapp;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.plugins.BasePlugin;
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
		
		project.getExtensions().create("webApp", WebAppExtension.class);
		
		initTasks(project.getTasks());
	
		// Integrate with the WAR plugin.
		if (hasTask(project, "war")) {
			project.getTasks().getByName("war").dependsOn("packageWebApp");
		}
		
		// Integrate with the Client Dependencies plugin.
		if (hasTask(project, "clientRefresh")) {
			project.getTasks().getByName("packageWebApp").dependsOn("clientRefresh");
		}
	}

	private void initTasks(TaskContainer tasks) {
		tasks.create("combineJavaScript", CombineJavaScriptTask.class);
		tasks.create("combineCSS", CombineCSSTask.class);
		tasks.create("packageWebApp", PackageWebAppTask.class);
		tasks.create("syncWebApp", SyncWebAppTask.class);
		
		tasks.getByName("packageWebApp").dependsOn("combineJavaScript", "combineCSS");
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
}
