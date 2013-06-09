package org.docear.syncdaemon.indexdb;

import java.io.IOException;

public class PersistenceException extends IOException {

    public PersistenceException() {
    }

    public PersistenceException(Throwable cause) {
        super(cause);
    }

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
