package com.iodesystems.android.libs.view.transitions;

import android.view.animation.TranslateAnimation;

public class TransitionInFromRight extends TransitionPair {

    public TransitionInFromRight() {
        super(new TranslateAnimation(0.0f, -1.0f, 0.0f, 0.0f),
              new TranslateAnimation(1.0f, 0.0f, 0.0f, 0.0f),
              new TranslateAnimation(0.0f, 1.0f, 0.0f, 0.0f),
              new TranslateAnimation(-1.0f, 0.0f, 0.0f, 0.0f));
    }
}
