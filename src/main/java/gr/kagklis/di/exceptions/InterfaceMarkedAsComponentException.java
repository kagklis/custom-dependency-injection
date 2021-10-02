package gr.kagklis.di.exceptions;

public class InterfaceMarkedAsComponentException extends RuntimeException {
    public InterfaceMarkedAsComponentException(String className) {
        super("Found @CustomComponent annotation on interface " + className + ", which is not allowed.");
    }
}