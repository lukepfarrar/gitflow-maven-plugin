package uk.co.tbp.maven.gitflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.model.io.ModelWriter;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import uk.co.tbp.gitflow.GitFlowException;
import uk.co.tbp.gitflow.GitFlowRepository;
import uk.co.tbp.gitflow.GitUtils;

/**
 * @goal release
 *
 * @author Luke Farrar <lfarrar@thebookpeople.co.uk>
 */
public class ReleaseMojo extends AbstractMojo {

    public static void main(String[] args) throws MojoExecutionException, MojoFailureException {
//        new ReleaseMojo().execute();
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String releaseVersion, developVersion, currentVersion;

        try {
            // check state
            GitFlowRepository gitFlowRepo = new GitFlowRepository(GitUtils.initGitRepository());
            Git git = gitFlowRepo.git();
            
            List<String> existingReleaseBranchNames = gitFlowRepo.getReleaseBranchNames();
            System.out.println("names:");
            for (String name : existingReleaseBranchNames) {
                System.out.println(name);
            }
            if (!existingReleaseBranchNames.isEmpty()) {
                String error = "Release branch(es) already exist:\n";
                for (String nextBranchName : existingReleaseBranchNames) {
                    error += '\t'+nextBranchName+'\n';
                }                
                throw new MojoFailureException(error+"There is release branch. Finish this first.");
            }
            
            if (!gitFlowRepo.ready()) {
                throw new MojoFailureException("Repository is in an inconsistent state. sort your commits out...");
            }

            final String currentBranch = git.getRepository().getBranch();
            try {
                gitFlowRepo.checkoutDevelop();
                final File pomFile = new File(".", "pom.xml");
                System.out.println("initial rent = "+pomFile.getParentFile());
                Model model;
                try {
                    model = readPom(pomFile);
                } catch (FileNotFoundException ex) {
                    throw new MojoExecutionException("Could not find POM file.", ex);
                }


                System.out.println("Repository is " + new File(".").getAbsolutePath());
                currentVersion = model.getVersion();
                DefaultVersionInfo versionInfo;
                versionInfo = new DefaultVersionInfo(currentVersion);

                releaseVersion = versionInfo.getReleaseVersionString();
                developVersion = versionInfo.getNextVersion().getSnapshotVersionString();

                System.out.println("Starting on: " + currentVersion);
                System.out.println("Release branch to be: " + releaseVersion);
                System.out.println("Develop branch to be: " + developVersion);

                final String releaseBranchName = gitFlowRepo.getReleasePrefix() + releaseVersion;
                if (GitUtils.branchExists(git, releaseBranchName)) {
                    throw new MojoFailureException("Release branch " + releaseBranchName + " already exists.");
                }

                // change SNAPSHOT dependencies to release versions.
                Map<Dependency, String> changedDependencyVersions = new HashMap<Dependency, String>();
                model.setVersion(releaseVersion);
                for (Dependency nextDependency : model.getDependencies()) {
                    DefaultVersionInfo dependencyVersionInfo;

                    dependencyVersionInfo = new DefaultVersionInfo(nextDependency.getVersion());

                    if (!dependencyVersionInfo.getReleaseVersionString().equals(nextDependency.getVersion())) {
                        changedDependencyVersions.put(nextDependency, nextDependency.getVersion());
                        nextDependency.setVersion(dependencyVersionInfo.getReleaseVersionString());
                        System.out.println("moving dep " + nextDependency.getArtifactId() + " from " + nextDependency.getVersion() + " to " + dependencyVersionInfo.getReleaseVersionString());
                    }
                }

                ModelWriter modelWriter = new DefaultModelWriter();
                System.out.println("pom "+pomFile.getParentFile());
                System.out.println("model "+model);
                modelWriter.write(pomFile, null, model);


                git.add().addFilepattern(pomFile.getName()).call();
                git.commit().setMessage("Setting POM version to " + releaseVersion + " for branch " + releaseBranchName).call();
                git.branchCreate().setName(releaseBranchName).call();

//        gitFlowRepo.createReleaseBranch(releaseVersion);
                model.setVersion(developVersion);
                for (Map.Entry<Dependency, String> nextEntry : changedDependencyVersions.entrySet()) {
                    nextEntry.getKey().setVersion(nextEntry.getValue());
                }
                modelWriter.write(pomFile, null, model);
                git.add().addFilepattern(pomFile.getName()).call();
                git.commit().setMessage("Setting POM version to " + developVersion).call();


            } finally {
                git.checkout().setName(currentBranch).call();
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

    private static Model readPom(final File pomFile) throws FileNotFoundException, IOException {
        FileReader fileReader = null;

        try {
            fileReader = new FileReader(pomFile);
            MavenXpp3Reader m = new MavenXpp3Reader();
            Model model;
            try {
                model = m.read(fileReader);
            } catch (XmlPullParserException ex) {
                throw new IOException("POM " + pomFile.getAbsolutePath() + " could not be parsed.", ex);
            }
//            model.setPomFile(pomFile);
            return model;
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }
}
