package uk.co.tbp.gitflow;

import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitUtils {

    public static boolean branchExists(Git git, final String branchName) {
        if (branchName == null) {
            return false;
        }
        for (Ref nextRef : git.branchList().call()) {
            if (nextRef.getName().equals("refs/heads/" + branchName)) {
                return true;
            }
        }
        return false;
    }

    public static FileRepository initGitRepository() throws GitFlowException, IOException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.findGitDir();
        if (repositoryBuilder.getGitDir() == null) {            
            throw new GitFlowException("Not a git repository");
        }
        return repositoryBuilder.build();
    }
}
