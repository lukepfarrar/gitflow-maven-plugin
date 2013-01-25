package uk.co.theboo.maven.gitflow;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal help
 *
 * @author Luke Farrar <lfarrar@thebookpeople.co.uk>
 */
public class HelpMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("\n\n\n\nHelp!\n\n\n\n");
    }
}
