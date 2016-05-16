package com.iodesystems.android.libs.view.transitions;

import android.content.Context;
import com.iodesystems.android.libs.R;

import static android.view.animation.AnimationUtils.loadAnimation;

public class TransitionInFromTop extends TransitionPair {

    public TransitionInFromTop(Context context) {
        super(loadAnimation(context, R.anim.slide_down_out),
              loadAnimation(context, R.anim.slide_down_in),
              loadAnimation(context, R.anim.slide_up_out),
              loadAnimation(context, R.anim.slide_up_in));
    }
}
