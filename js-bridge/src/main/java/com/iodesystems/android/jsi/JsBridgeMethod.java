package com.iodesystems.android.jsi;

import java.lang.reflect.Method;

public class JsBridgeMethod {
    private final Object target;
    private final Method method;
    private Boolean runOnUiThread;

    public JsBridgeMethod(Object target, Method method, boolean runOnUiThread) {
        this.target = target;
        this.method = method;
        this.runOnUiThread = runOnUiThread;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Boolean getRunOnUiThread() {
        return runOnUiThread;
    }
}
