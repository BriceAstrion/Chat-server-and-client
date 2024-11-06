package shared;

public class ParseError implements Sendable {
    private String errorMessage;

    public ParseError() { }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ParseError{" +
                "errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
