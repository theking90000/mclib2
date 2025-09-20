package be.theking90000.mclib2.platform.gradle;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

public class DependencyResolver {

    private final Project project;
    private final MCLib2Extension MCLib2Extension;

    public DependencyResolver(Project project, MCLib2Extension MCLib2Extension) {
        this.project = project;
        this.MCLib2Extension = MCLib2Extension;
    }

    private boolean useProjectLinking() {
        return getVersion().equals("project");
    }

    private String getVersion() {
        return MCLib2Extension.getVersion().get();
    }

    public Dependency resolve(String dependency, String projectPath) {
        if (useProjectLinking()) {
            if (projectPath != null) {
                dependency = projectPath;
            } else {
                String[] parts = dependency.split(":");
                String[] pNames = parts[1].split("-");
                dependency = parts[0] + ':' + pNames[pNames.length - 1];
            }
        }

        return project.getDependencies().create(dependency + ":" + getVersion());
    }

    public Dependency resolve(String dependency) {
        return resolve(dependency, null);
    }

}
