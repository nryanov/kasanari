package org.apache.iceberg.exceptions;

public class NonRetryableException extends RuntimeException {
    private final CommitFailedException wrapped;

    public NonRetryableException(CommitFailedException cause) {
        super(cause);
        this.wrapped = cause;
    }

    public CommitFailedException getWrapped() {
        return wrapped;
    }
}
