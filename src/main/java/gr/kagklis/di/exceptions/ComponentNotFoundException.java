package gr.kagklis.di.exceptions;

public class ComponentNotFoundException extends RuntimeException {
    public ComponentNotFoundException(String className) {
        super("No component annotated class found with name: " + className + ". Error when trying to inject object.");
    }
}
