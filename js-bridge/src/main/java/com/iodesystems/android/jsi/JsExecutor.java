package com.iodesystems.android.jsi;

import com.google.gson.*;
import com.iodesystems.android.jsi.annotations.JsInterface;
import com.iodesystems.android.jsi.annotations.JsInterfaceMethod;
import com.iodesystems.android.jsi.exceptions.InvalidJsiUrlException;
import com.iodesystems.android.jsi.exceptions.JsExecutionException;
import com.iodesystems.android.jsi.exceptions.UnknownJsiMethodException;
import com.iodesystems.android.jsi.exceptions.UnreadableParameterWithinJsiUrlException;
import com.iodesystems.android.jsi.handlers.Chain;
import com.iodesystems.android.jsi.handlers.Invokable;
import com.iodesystems.android.jsi.handlers.JsErrorHandler;
import com.iodesystems.android.jsi.handlers.RequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class JsExecutor implements JsErrorHandler, RequestHandler {
    private final Gson gson;
    private final ExecutorService backgroundExecutor;
    private final ExecutorService uiExecutor;
    private List<JsErrorHandler> jsErrorHandlers = new ArrayList<JsErrorHandler>();
    private JsExecutorTarget target;
    private Map<String, Map<String, JsBridgeMethod>> jsInterfaces = new HashMap<String, Map<String, JsBridgeMethod>>();

    public JsExecutor(ExecutorService backgroundExecutor,
                      ExecutorService uiExecutor,
                      JsExecutorTarget target,
                      List<Object> interfaces) {

        this.backgroundExecutor = backgroundExecutor;
        this.uiExecutor = uiExecutor;
        this.target = target;
        this.gson = new Gson();

        target.setRequestHandler(this);
        target.setOnJsAvailable(new Runnable() {
            @Override
            public void run() {
                try {
                    setup();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        for (Object iface : interfaces) {
            Class<?> ifaceClass = iface.getClass();
            JsInterface jsi = ifaceClass.getAnnotation(JsInterface.class);
            if (jsi == null) {
                throw new IllegalArgumentException(ifaceClass.getSimpleName() + " should be annotated with @JsInterface");
            }
            String name = jsi.value().length() == 0 ? ifaceClass.getSimpleName() : jsi.value();
            HashMap<String, JsBridgeMethod> methods = new HashMap<String, JsBridgeMethod>();
            jsInterfaces.put(name, methods);
            for (Method method : ifaceClass.getMethods()) {
                JsInterfaceMethod annotation = method.getAnnotation(JsInterfaceMethod.class);
                if (annotation == null) continue;
                String methodName = annotation.name().length() == 0 ? method.getName() : annotation.name();
                methods.put(methodName, new JsBridgeMethod(iface, method, annotation.value() == JsInterfaceMethod.Mode.UI));
            }
        }
    }

    private void exec(String js) {
        target.exec(js);
    }

    @SuppressWarnings("WeakerAccess")
    public void event(String name) {
        event(name, null);
    }

    @SuppressWarnings("WeakerAccess")
    public void event(String name, Object data) {
        call("jsi.event", name, data);
    }

    private void call(String method, Object... args) {
        List<Object> objects = Arrays.asList(args);
        StringBuilder call = new StringBuilder();
        for (Object object : objects) {
            if (call.length() > 0) {
                call.append(",");
            }
            call.append(gson.toJson(object));
        }
        exec(method + "(" + call.toString() + ")");
    }

    private String callbackSuccess(String callbackName, Object success) {
        String callbackKey = gson.toJson(callbackName);
        return "if(jsi[" + callbackKey + "]) jsi[" + callbackKey + "][0](" + gson.toJson(success) + ")";
    }

    private String callbackFailure(String callbackName, Exception exception) {
        JsExecutionException message = new JsExecutionException(exception);
        if (callbackName == null) {
            return "console.error(" + gson.toJson(message) + ")";
        } else {
            String callbackKey = gson.toJson(callbackName);
            return "if(jsi[" + callbackKey + "]) jsi[" + gson.toJson(callbackName) + "][1](" + gson.toJson(message) + ")";
        }
    }

    private void setup() throws IOException {
        String jsi = getResourceString("jsi.js");
        exec(jsi);

        for (Map.Entry<String, Map<String, JsBridgeMethod>> jsEntry : jsInterfaces.entrySet()) {
            String jsInterfaceName = jsEntry.getKey();
            Object jsInterfaceObject = jsEntry.getValue();
            List<String> methods = new ArrayList<String>();
            for (Method method : jsInterfaceObject.getClass().getMethods()) {
                if (method.getAnnotation(JsInterfaceMethod.class) != null)
                    methods.add(method.getName());
            }
            call("jsi.define", jsInterfaceName, methods);
        }

        event("jsInterfaceLoaded");
    }

    private String getResourceString(String resource) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource);
        Reader in = new InputStreamReader(inputStream, "UTF-8");
        while (true) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }

    private void invokeUrlOnOnterfaces(String url) {
        URI uri = URI.create(url);
        List<String> pathSegments = Arrays.asList(uri.getPath().split("/"));

        if (pathSegments.size() != 2) {
            exec(callbackFailure(null, new InvalidJsiUrlException("jsi: Url malformed, only 3 segments accepted, jsi://name/method/callback?params, got:" + url)));
            return;
        }

        String interfaceName = uri.getHost();
        String interfaceAction = pathSegments.get(0);
        final String callbackName = pathSegments.get(1);

        if (!jsInterfaces.containsKey(interfaceName)) {
            exec(callbackFailure(callbackName, new UnknownJsiMethodException("jsi: No interface named " + interfaceName + " defined")));
        }
        Map<String, JsBridgeMethod> methods = jsInterfaces.get(interfaceName);

        if (!methods.containsKey(interfaceAction)) {
            exec(callbackFailure(callbackName, new UnknownJsiMethodException("jsi: No method named " + interfaceAction + " defined on " + interfaceName)));
        }
        JsBridgeMethod jsBridgeMethod = methods.get(interfaceAction);
        Method interfaceMethod = jsBridgeMethod.getMethod();
        Object jsInterface = jsBridgeMethod.getTarget();
        Boolean runOnUiThread = jsBridgeMethod.getRunOnUiThread();

        Type[] genericParameterTypes = interfaceMethod.getGenericParameterTypes();
        final Object[] arguments = new Object[genericParameterTypes.length];

        String args = uri.getQuery().split("args=", 2)[1];
        JsonArray jsonArgs = gson.fromJson(args, JsonArray.class);

        Deferred deferred = null;
        for (Integer argumentIndex = 0; argumentIndex < genericParameterTypes.length; argumentIndex++) {
            Object argument;
            if (argumentIndex == genericParameterTypes.length - 1 && genericParameterTypes[argumentIndex] == Deferred.class) {
                argument = deferred = new Deferred();
            } else {
                try {
                    final JsonElement queryParameterValue = jsonArgs.get(argumentIndex);
                    if (queryParameterValue instanceof JsonNull) {
                        argument = null;
                    } else if (genericParameterTypes[argumentIndex] == String.class) {
                        try {
                            argument = queryParameterValue.getAsString();
                        } catch (UnsupportedOperationException e) {
                            exec(callbackFailure(callbackName,
                                                 new UnreadableParameterWithinJsiUrlException("Could not read " + genericParameterTypes[argumentIndex].toString() + " parameter in: " + queryParameterValue)));
                            return;
                        }
                    } else {
                        try {
                            argument = gson.fromJson(queryParameterValue, genericParameterTypes[argumentIndex]);
                        } catch (JsonSyntaxException e) {
                            exec(callbackFailure(callbackName,
                                                 new UnreadableParameterWithinJsiUrlException("Could not read " + genericParameterTypes[argumentIndex].toString() + " parameter in: " + queryParameterValue)));
                            return;
                        }
                    }
                } catch (NoSuchElementException ignored) {
                    argument = null;
                }
            }
            arguments[argumentIndex] = argument;
        }

        handleInvocation(runOnUiThread, callbackName, interfaceMethod, jsInterface, arguments, deferred);
    }

    private void handleInvocation(Boolean runOnUiThread,
                                  final String callbackName,
                                  final Method interfaceMethod,
                                  final Object jsInterface,
                                  final Object[] arguments,
                                  final Deferred deferred) {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                if (deferred != null) {
                    deferred.then(new Invokable<Object>() {
                        @Override
                        public void invoke(Object arg) {
                            exec(callbackSuccess(callbackName, arg));
                        }
                    }, new Invokable<Exception>() {
                        @Override
                        public void invoke(Exception arg) {
                            handleInvocationException(callbackName, arg);
                        }
                    });
                }

                try {
                    final Object success = interfaceMethod.invoke(jsInterface, arguments);
                    if (deferred == null) exec(callbackSuccess(callbackName, success));
                } catch (Throwable e) {
                    handleInvocationException(callbackName, e);
                }
            }
        };
        if (runOnUiThread) {
            uiExecutor.submit(action);
        } else {
            backgroundExecutor.submit(action);
        }
    }

    private void handleInvocationException(final String callbackName, final Throwable e) {
        final Iterator<JsErrorHandler> iterator = jsErrorHandlers.iterator();
        JsErrorHandler current;
        if (iterator.hasNext()) {
            current = iterator.next();
        } else {
            current = this;
        }

        final Throwable cause = e.getCause() != null ? e.getCause() : e;

        if (cause instanceof Exception) {
            final Chain<Exception>[] next = new Chain[1];
            next[0] = new Chain<Exception>() {
                @Override
                public void next(Exception exception) {
                    if (iterator.hasNext()) {
                        iterator.next().handleJsError(exception, callbackName, JsExecutor.this, next[0]);
                    } else {
                        JsExecutor.this.handleJsError(exception, callbackName, JsExecutor.this, next[0]);
                    }
                }
            };

            current.handleJsError((Exception) cause, callbackName, this, next[0]);
        }
    }

    @SuppressWarnings("unused")
    public void addErrorHandler(JsErrorHandler jsErrorHandler) {
        jsErrorHandlers.add(jsErrorHandler);
    }

    @Override
    public void handleJsError(Exception e, String callbackName, JsExecutor jsExecutor, Chain<Exception> next) {
        exec(callbackFailure(callbackName, e));
    }

    @SuppressWarnings("unused")
    public void removeErrorHandler(JsErrorHandler jsErrorHandler) {
        if (jsErrorHandlers.contains(jsErrorHandler)) {
            jsErrorHandlers.remove(jsErrorHandler);
        }
    }

    @Override
    public boolean handleRequest(String url) {
        if (url.startsWith("jsi://")) {
            invokeUrlOnOnterfaces(url);
            return true;
        }
        return false;
    }
}
