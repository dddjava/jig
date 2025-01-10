package org.dddjava.jig.adapter.diagram;


public class ProcessResult {

    boolean success;
    String message;

    public ProcessResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ProcessResult success() {
        return new ProcessResult(true, "");
    }

    public static ProcessResult failure() {
        return new ProcessResult(false, "");
    }

    public static ProcessResult failureWithTimeout() {
        return new ProcessResult(false, "timeout");
    }

    public boolean succeed() {
        return success;
    }

    public boolean failed() {
        return !succeed();
    }

    public ProcessResult withMessage(String message) {
        return new ProcessResult(this.success, message);
    }

    public String message() {
        return message;
    }
}
