package org.example.dtos;

public class Response {
    private int status;
    private Object data;

    public Response() {}

    public void status(int statusCode) {
        this.status = statusCode;
    }

    public void json(Object data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }
}