package com.iodesystems.android.jsi.exceptions;

public class JsExecutionException {
    private final Throwable e;

    public JsExecutionException(Throwable e) {
        this.e = e;
    }

    public String getMessage() {
        return e.getMessage();
    }

    public String getType() {
        return e.getClass().getSimpleName();
    }

    public String getRootType() {
        if (e.getCause() == null) {
            return getType();
        } else {
            return e.getCause().getClass().getSimpleName();
        }
    }
}
