package com.iodesystems.android.jsi;

import com.iodesystems.android.jsi.handlers.Invokable;

public class Deferred {
    private Invokable<Object> successInvokable;
    private Invokable<Exception> errorInvokable;

    public void resolve(Object success) {
        if (successInvokable != null) successInvokable.invoke(success);
    }

    public void reject(Exception error) {
        if (errorInvokable != null) errorInvokable.invoke(error);
    }

    public void then(Invokable<Object> success, Invokable<Exception> onError) {
        this.successInvokable = success;
        this.errorInvokable = onError;
    }

    public void then(Invokable<Object> success) {
        this.successInvokable = success;
    }
}
