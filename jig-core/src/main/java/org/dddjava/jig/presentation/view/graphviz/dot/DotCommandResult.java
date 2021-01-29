package org.dddjava.jig.presentation.view.graphviz.dot;


public class DotCommandResult {

    DotCommandResultStatus status;
    String message;

    public DotCommandResult(DotCommandResultStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public static DotCommandResult success() {
        return new DotCommandResult(DotCommandResultStatus.SUCCESS, "");
    }

    public static DotCommandResult failure() {
        return new DotCommandResult(DotCommandResultStatus.FAILURE, "");
    }

    public boolean succeed() {
        return this.status == DotCommandResultStatus.SUCCESS;
    }

    public boolean failed() {
        return !succeed();
    }

    public DotCommandResult withMessage(String message) {
        return new DotCommandResult(this.status, message);
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return "DotCommandResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }

}
