package com.github.maxstupo.jflatlog.exception;

import com.github.maxstupo.jflatlog.JFlatLog;

/**
 * This exception is thrown by {@link JFlatLog} when log-to-file is attempted to be reinitialized.
 * 
 * @author Maxstupo
 */
public class AlreadyInitializedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * {@link RuntimeException#RuntimeException()}
     */
    public AlreadyInitializedException() {
    }

    /**
     * {@link RuntimeException#RuntimeException(String)}
     */
    public AlreadyInitializedException(String message) {
        super(message);
    }

    /**
     * {@link RuntimeException#RuntimeException(Throwable)}
     */
    public AlreadyInitializedException(Throwable cause) {
        super(cause);
    }

    /**
     * {@link RuntimeException#RuntimeException(String,Throwable)}
     */
    public AlreadyInitializedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@link RuntimeException#RuntimeException(String,Throwable,Boolean,Boolean)}
     */
    public AlreadyInitializedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
