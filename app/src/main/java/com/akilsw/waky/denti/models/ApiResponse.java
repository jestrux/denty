package com.akilsw.waky.denti.models;

/**
 * Created by WAKY on g3/16/2017.
 */
public class ApiResponse {
    Boolean success;
    String msg;
    Subject[] calls;

    public Subject[] getCalls() {
        return calls;
    }

    public void setCalls(Subject[] calls) {
        this.calls = calls;
    }

    public ApiResponse(Boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
