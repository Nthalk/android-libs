package com.iodesystems.android.libs.view;

import android.content.Context;
import android.view.View;
import com.iodesystems.android.libs.view.transitions.TransitionInFromRight;
import com.iodesystems.android.libs.view.transitions.TransitionPair;

import java.util.Deque;
import java.util.LinkedList;

public class Pager {

    private OnPageListener onPageTransitionListener = null;
    private final Deque<TransitionPair> transitionPairs = new LinkedList<TransitionPair>();
    private final Deque<View> views = new LinkedList<View>();
    private final TransitionPair defaultTransition;

    public Pager(TransitionPair defaultTransition) {
        this.defaultTransition = defaultTransition;
    }

    public Pager(Context context) {
        this(new TransitionInFromRight(context));
    }

    public void enter(View in) {
        enter(in, defaultTransition);
    }

    public void enter(View in, TransitionPair transitionPair) {
        if (!views.isEmpty()) {
            View out = views.peek();
            if (onPageTransitionListener != null) {
                onPageTransitionListener.onPageTransition(in, out);
            }
            transitionPair.enter(out, in);
        }
        views.push(in);
        transitionPairs.push(transitionPair);
    }

    public boolean exit() {
        if(views.isEmpty()){
            return false;
        }

        TransitionPair transitionPair = transitionPairs.pop();
        View out = views.pop();
        View in = views.peek();

        if (onPageTransitionListener != null) {
            onPageTransitionListener.onPageTransition(in, out);
        }

        transitionPair.exit(out, in);

        return true;
    }

    public TransitionPair getDefaultTransition() {
        return defaultTransition;
    }

    public OnPageListener getOnPageTransitionListener() {
        return onPageTransitionListener;
    }

    public void setOnPageTransitionListener(OnPageListener onPageTransitionListener) {
        this.onPageTransitionListener = onPageTransitionListener;
    }

    public static abstract class OnPageListener {
        public abstract void onPageTransition(View in, View out);
    }
}
