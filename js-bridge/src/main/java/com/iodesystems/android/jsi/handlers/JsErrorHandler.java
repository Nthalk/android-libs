package com.iodesystems.android.jsi.handlers;

import com.iodesystems.android.jsi.JsExecutor;

public interface JsErrorHandler {
    void handleJsError(Exception e, String callbackName, JsExecutor jsExecutor, Chain<Exception> chain);
}
