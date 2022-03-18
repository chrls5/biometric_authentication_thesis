package com.example.thesis_final;

public class Response {
    private String message;
    private boolean success;

    public Response(boolean success, String message) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}
