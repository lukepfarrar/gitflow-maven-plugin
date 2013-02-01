package uk.co.theboo.maven.gitflow;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo (name="help")
public class HelpMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("");
        getLog().info("This plugin simplifies gitflow releases with maven projects.");
        getLog().info("See https://github.com/lukepfarrar/maven-gitflow-plugin for more details.");
        getLog().info("");
        getLog().info("mvn gitflow:release is equivalent to git flow release start v?.??, with a few advantages:");
        getLog().info("- The project version is automatically incremented.");
        getLog().info("- SNAPSHOT dependencies are replaced by the equivalent release versions.");
        getLog().info("- The release pom is commited to develop, then reverted. This stops merge conflicts when the branch is finished.");
    }
}
