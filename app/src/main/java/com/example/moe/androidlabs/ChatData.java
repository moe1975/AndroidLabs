package com.example.moe.androidlabs;

/**
 * Created by saleh on 3/16/2017.
 */

public class ChatData {
    private String message;
    private long _id;

    public ChatData() {
        message = "";
        _id = 0;
    }

    public ChatData(String message, long _id) {
        this.message = message;
        this._id = _id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }
}
