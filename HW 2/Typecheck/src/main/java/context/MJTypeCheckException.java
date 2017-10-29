package context;

/**
 * Convenience wrapper for a runtime exception with specific type check messages
 */
public class MJTypeCheckException extends RuntimeException {
    public MJTypeCheckException() {

    }

    public MJTypeCheckException(String msg) {
        super(msg);
    }
}
