package net.nemerosa.seed.jenkins.step;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.nemerosa.seed.jenkins.strategy.naming.SeedNamingStrategyHelper;
import net.nemerosa.seed.jenkins.support.SeedProjectEnvironment;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Build step which creates a project folder and a project seed inside.
 */
public class ProjectSeedBuilder extends AbstractSeedBuilder {

    @DataBoundConstructor
    public ProjectSeedBuilder(String project, String projectClass, String projectScmType, String projectScmUrl) {
        super(project, projectClass, projectScmType, projectScmUrl);
    }

    @Override
    protected void configureEnvironment(EnvVars env, SeedProjectEnvironment projectEnvironment) {
        env.put("projectSeedFolder", SeedNamingStrategyHelper.getProjectSeedFolder(
                projectEnvironment.getNamingStrategy(),
                projectEnvironment.getId()
        ));
        env.put("projectSeedPath", projectEnvironment.getNamingStrategy().getProjectSeed(
                projectEnvironment.getId()
        ));
    }

    @Override
    protected String getScriptPath() {
        return "/project-seed-generator.groovy";
    }

    @Override
    protected String replaceExtensionPoints(String script, EnvVars env, SeedProjectEnvironment projectEnvironment) {
        return replaceExtensionPoint(script, "authorisations", new ProjectFolderAuthorisationsExtension(projectEnvironment).generate());
    }

    @Extension
    public static class ProjectSeedBuilderDescription extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Project seed generator";
        }
    }
}
