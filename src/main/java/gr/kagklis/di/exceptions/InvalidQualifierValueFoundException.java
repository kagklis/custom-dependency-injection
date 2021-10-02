package gr.kagklis.di.exceptions;

public class InvalidQualifierValueFoundException extends RuntimeException {
    public InvalidQualifierValueFoundException(String qualifier) {
        super("Could not find implementation specified in qualifier with value: " + qualifier + ". " +
                "Make sure you have no typos or that you provided a proper qualifier: packageName.className");
    }
}
