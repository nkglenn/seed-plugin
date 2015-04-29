package net.nemerosa.seed.jenkins.pipeline

import hudson.Extension
import hudson.Launcher
import hudson.model.*
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.Builder
import net.nemerosa.seed.jenkins.support.SeedDSLHelper
import net.nemerosa.seed.jenkins.support.SeedProjectEnvironment
import org.kohsuke.stapler.DataBoundConstructor

/**
 * This step prepares the DSL environment for the {@link PropertiesPipelineGenerator}.
 *
 * Reads the property file and gets:
 *
 * - the list of dependencies.
 * - the JAR containing the bootstrap script
 *
 * Those properties are read from the property file first, then from the configuration, and have a default value if
 * not defined anywhere.
 *
 * The output of this build will be a set of environment variables:
 *
 * - a comma separated list of Gradle dependency notations
 * - a new line separated list of JAR paths to use for the DSL
 * - a JAR file name and a path inside it to get the DSL bootstrap script
 */
class PropertiesPipelineGeneratorBuilder extends Builder {

    public static final String SEED_DSL_LIBRARIES = 'SEED_DSL_LIBRARIES'
    public static final String SEED_DSL_SCRIPT_JAR = 'SEED_DSL_SCRIPT_JAR'
    public static final String SEED_DSL_SCRIPT_LOCATION = 'SEED_DSL_SCRIPT_LOCATION'

    private final String project
    private final String projectClass
    private final String projectScmType
    private final String projectScmUrl
    private final String branch
    private final String propertyPath

    @DataBoundConstructor
    PropertiesPipelineGeneratorBuilder(String propertyPath, String project, String projectClass, String projectScmType, String projectScmUrl, String branch) {
        this.propertyPath = propertyPath
        this.project = project
        this.projectClass = projectClass
        this.projectScmType = projectScmType
        this.projectScmUrl = projectScmUrl
        this.branch = branch
    }

    @Override
    boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        // Getting the project environment
        SeedProjectEnvironment projectEnvironment = new SeedDSLHelper().getProjectEnvironment(
                project,
                projectClass,
                projectScmType,
                projectScmUrl
        )

        // Reads the property file
        listener.logger.println("Reading properties from ${propertyPath}")
        Properties properties = new Properties()
        build.workspace.child(propertyPath).read().withStream { properties.load(it) }

        // Gets the list of dependencies from the property file
        List<String> dependencies
        String dependenciesValue = properties['seed.dsl.libraries']
        if (dependenciesValue) {
            dependencies = dependenciesValue.split(',')
        }
        // ... or from the configuration
        else {
            dependencies = projectEnvironment.getConfigurationList('pipeline-generator-libraries')
        }
        listener.logger.println("List of DSL dependencies:")
        dependencies.each {
            listener.logger.println("* ${it}")
        }

        // Gets the dependency source for the bootstrap script
        String dslBootstrapDependency = properties['seed.dsl.script.jar']
        if (!dslBootstrapDependency) {
            dslBootstrapDependency = projectEnvironment.getConfigurationValue('pipeline-generator-script-jar', 'pipeline.jar')
        }
        listener.logger.println("DSL script JAR: ${dslBootstrapDependency}")

        // Gets the location of the bootstrap script
        String dslBootstrapLocation = properties['seed.dsl.script.location']
        if (!dslBootstrapLocation) {
            dslBootstrapLocation = projectEnvironment.getConfigurationValue('pipeline-generator-script-location', '/seed.groovy')
        }
        listener.logger.println("DSL script location: ${dslBootstrapLocation}")

        // Injects the environment variables
        build.addAction(new ParametersAction(
                new StringParameterValue(SEED_DSL_LIBRARIES, dependencies.join('/n')),
                new StringParameterValue(SEED_DSL_SCRIPT_JAR, dslBootstrapDependency),
                new StringParameterValue(SEED_DSL_SCRIPT_LOCATION, dslBootstrapLocation),
        ))

        // OK
        return true
    }

    String getProject() {
        return project
    }

    String getProjectClass() {
        return projectClass
    }

    String getProjectScmType() {
        return projectScmType
    }

    String getProjectScmUrl() {
        return projectScmUrl
    }

    String getBranch() {
        return branch
    }

    String getPropertyPath() {
        return propertyPath
    }

    @Extension
    public static class PropertiesPipelineGeneratorBuilderDescription extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Branch pipeline generator preparation";
        }
    }
}