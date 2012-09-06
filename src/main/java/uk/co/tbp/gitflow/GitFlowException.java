
package uk.co.tbp.gitflow;

import org.eclipse.jgit.api.errors.RefAlreadyExistsException;

public class GitFlowException extends Exception {

    GitFlowException(String message) {
        super(message);
    }

    GitFlowException(Throwable throwable) {
        super(throwable);
    }
}
