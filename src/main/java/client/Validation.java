package client;

/**
 * A class to validate input on the client.
 */
public class Validation {
    private boolean isValid;
    private String message;

    /**
     * Creates an instance of this class with no args.
     */
    public Validation() {
    }

    /**
     * Creates an instance of this class.
     *
     * @param isValid true if input is valid or false otherwise
     * @param message validation message
     */
    public Validation(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
