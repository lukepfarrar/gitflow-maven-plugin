package uk.co.theboo.maven.gitflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import uk.co.theboo.jgitflow.GitFlowException;
import uk.co.theboo.jgitflow.GitFlowRepository;
import uk.co.theboo.jgitflow.GitUtils;
import uk.co.theboo.maven.utils.PomUtils;

/**
 * @goal validate
 */
public class ValidateMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        GitFlowRepository gitFlowRepo;
        try {
            gitFlowRepo = new GitFlowRepository(GitUtils.initGitRepository());
            Git git = gitFlowRepo.git();

            final File pomFile = new File(".", "pom.xml");
            System.out.println("initial rent = " + pomFile.getParentFile());
            Model model;
            try {
                model = PomUtils.readPom(pomFile);
            } catch (FileNotFoundException ex) {
                throw new MojoExecutionException("Could not find POM file.", ex);
            }
            DefaultVersionInfo defaultVersionInfo = new DefaultVersionInfo(model.getVersion());
            final String masterBranchName = gitFlowRepo.getMasterBranchName();
            
            if (gitFlowRepo.isOnMaster()) {            
                if (defaultVersionInfo.isSnapshot()) {
                    StringBuilder message = new StringBuilder("Snapshot version '").append(defaultVersionInfo).append("' is not valid on branch ");
                    if ("master".equals(masterBranchName)) {
                        message.append("master.");
                    } else {
                        message.append(masterBranchName).append(" as this is the master branch.");
                    }
                    throw new MojoFailureException(message.toString());
                }
            }
            
            final String developBranchName = gitFlowRepo.getMasterBranchName();
            if (gitFlowRepo.isOnDevelop()) {
                if (!defaultVersionInfo.isSnapshot()) {
                    StringBuilder message = new StringBuilder("Release version '").append(defaultVersionInfo).append("' is not valid on branch ");
                    if ("develop".equals(masterBranchName)) {
                        message.append("develop.");
                    } else {
                        message.append(developBranchName).append(" as this is the develop branch.");
                    }
                    throw new MojoFailureException(message.toString());
                }
            }
        } catch (VersionParseException ex) {
            Logger.getLogger(ValidateMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ValidateMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GitFlowException ex) {
            Logger.getLogger(ValidateMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GitAPIException ex) {
            Logger.getLogger(ValidateMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
