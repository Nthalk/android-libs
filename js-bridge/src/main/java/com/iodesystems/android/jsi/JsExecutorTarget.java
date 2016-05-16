package com.iodesystems.android.jsi;

import com.iodesystems.android.jsi.handlers.RequestHandler;

public interface JsExecutorTarget {
    void exec(String js);

    void setOnJsAvailable(Runnable onJsAvailable);

    void setRequestHandler(RequestHandler requestHandler);
}
