package gr.kagklis.di.exceptions;

public class MoreThanOneImplementationFoundException extends RuntimeException {
    public MoreThanOneImplementationFoundException(String interfaceName, Integer count) {
        super("Found " + count + " implementations of interface " +
                interfaceName + ". Expected single implementation, or make use of @CustomQualifier to resolve conflict");
    }

}
