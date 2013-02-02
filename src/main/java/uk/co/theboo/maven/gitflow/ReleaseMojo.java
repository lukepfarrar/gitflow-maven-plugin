package uk.co.theboo.maven.gitflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import uk.co.theboo.jgitflow.GitFlowException;
import uk.co.theboo.jgitflow.GitFlowRepository;
import uk.co.theboo.jgitflow.GitUtils;
import uk.co.theboo.maven.utils.PomUtils;

@Mojo(name = "release")
public class ReleaseMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String releaseVersion, developVersion, currentVersion, releaseBranchName;
        boolean failed = true;

        try {
            GitFlowRepository gitFlowRepo = new GitFlowRepository(GitUtils.initGitRepository());
            Git git = gitFlowRepo.git();

            // Check for unfinished releases
            List<String> existingReleaseBranchNames = gitFlowRepo.getReleaseBranchNames();
            if (!existingReleaseBranchNames.isEmpty()) {
                getLog().error("");
                getLog().error("Cannot start a new release branch whilst others exist.");
                getLog().error("");
                getLog().error("Existing release branch(es):");
                for (String nextBranchName : existingReleaseBranchNames) {
                    getLog().error("- " + nextBranchName);
                }
                getLog().error("");
                throw new MojoFailureException("Existing release branch(es) must be finished or deleted first.");
            }

            // Check for uncommited work
            if (!gitFlowRepo.hasConsistentState()) {
                throw new MojoFailureException("The repository is in an inconsistent state. Please stash or commit your work.");
            }

            final String currentBranch = git.getRepository().getBranch();
            try {
                gitFlowRepo.checkoutDevelop();
                
                final File pomFile = new File("pom.xml");
                Model model;
                try {
                    model = PomUtils.readPom(pomFile);
                } catch (FileNotFoundException ex) {
                    throw new MojoExecutionException("Could not find pom.xml", ex);
                }

                currentVersion = model.getVersion();
                DefaultVersionInfo versionInfo = new DefaultVersionInfo(currentVersion);
                releaseVersion = versionInfo.getReleaseVersionString();
                developVersion = versionInfo.getNextVersion().getSnapshotVersionString();

                getLog().info("");
                getLog().info("Current development version is: " + currentVersion);

                final String releaseBranchName = gitFlowRepo.getReleasePrefix() + releaseVersion;

                // set release version
                model.setVersion(releaseVersion);

                // change SNAPSHOT dependencies to release versions.
                List<String> dependencyLog = removeSnapshotVersions(model);

                // Write release pom to the develop branch, to provide common ancestry.
                final String messagePrefix = "mvn gitflow:release setting POM version to ";
                RevCommit relasePomOnDevelopCommit = writeAndCommitPom(model, pomFile, git, messagePrefix + releaseVersion);

                // Create the new release branch from this commit
                git.branchCreate().setName(releaseBranchName).call();

                // Whilst still on the develop branch, revert the commit off which release is branched.
                git.revert().include(relasePomOnDevelopCommit).call();
                
                // read the pom again, (should be same as at first
                try {
                    model = PomUtils.readPom(pomFile);
                } catch (FileNotFoundException ex) {
                    throw new MojoExecutionException("Could not find pom.xml, inconsistencies may exist.", ex);
                }

                model.setVersion(developVersion);
                writeAndCommitPom(model, pomFile, git, messagePrefix + developVersion);

                // Checkout release branch
                git.checkout().setName(releaseBranchName).call();

                getLog().info("");
                getLog().info("Summary of actions:");
                getLog().info("- A new branch '" + releaseBranchName + "' was created, based on '" + gitFlowRepo.getDevelopBranchName() + "'");
                getLog().info("- The project version in branch '" + releaseBranchName + "' is '" + releaseVersion + "'");
                getLog().info("- The project version in branch '" + gitFlowRepo.getDevelopBranchName() + "' is '" + developVersion + "'");
                if (!dependencyLog.isEmpty()) {
                    getLog().info("- The following dependency versions have changed on branch '" + releaseBranchName + "':");
                    for (String nextDependencyLogEntry : dependencyLog) {
                        getLog().info("---- " + nextDependencyLogEntry);
                    }
                }
                getLog().info("- You are now on branch '" + releaseBranchName + "'");
                getLog().info("");
                getLog().info("Follow-up actions:");
                getLog().info("- Start committing last-minute fixes in preparing your release");
                getLog().info("");
                getLog().info("When done, run:");
                getLog().info("");
                getLog().info("- git flow release finish " + releaseVersion);
                getLog().info("");
                failed = false;
            } finally {
                if (failed) {
                    git.checkout().setName(currentBranch).call();
                }
            }
        } catch (GitFlowException e) {
            throw new MojoExecutionException("git-flow problem", e);
        } catch (IOException e) {
            throw new MojoExecutionException("git-flow problem", e);
        } catch (GitAPIException e) {
            throw new MojoExecutionException("git-flow problem", e);
        } catch (VersionParseException ex) {
            throw new MojoExecutionException("Could not parse version", ex);
        }
    }

    private void oneSecondDelay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private RevCommit writeAndCommitPom(Model model, final File pomFile, Git git, final String message) throws IOException, GitAPIException {
        // Write the final develop pom
        PomUtils.writePom(model, pomFile);
        git.add().addFilepattern(pomFile.getName()).call();
        return git.commit().setMessage(message).call();
    }

    private List<String> removeSnapshotVersions(Model model) throws VersionParseException {
        List<String> dependencyLog = new ArrayList<String>();
        for (Dependency nextDependency : model.getDependencies()) {
            DefaultVersionInfo dependencyVersionInfo = new DefaultVersionInfo(nextDependency.getVersion());
            final String currentDependencyVersion = nextDependency.getVersion();
            final String releaseDependencyVersion = dependencyVersionInfo.getReleaseVersionString();
            if (!releaseDependencyVersion.equals(currentDependencyVersion)) {
                dependencyLog.add(nextDependency.getArtifactId() + " has gone from '" + currentDependencyVersion + "' to '" + releaseDependencyVersion + "'");
                nextDependency.setVersion(dependencyVersionInfo.getReleaseVersionString());
            }
        }
        return dependencyLog;
    }
}
