package gr.kagklis.di.exceptions;

public class NoImplementationFoundException extends RuntimeException {
    public NoImplementationFoundException(String interfaceName) {
        super("No implementation found for interface: " + interfaceName);
    }
}