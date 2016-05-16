package com.iodesystems.android.libs.view.transitions;

import android.content.Context;
import com.iodesystems.android.libs.R;

import static android.view.animation.AnimationUtils.loadAnimation;

public class TransitionInFromRight extends TransitionPair {

    public TransitionInFromRight(Context context) {
        super(loadAnimation(context, R.anim.slide_left_out),
              loadAnimation(context, R.anim.slide_left_in),
              loadAnimation(context, R.anim.slide_right_out),
              loadAnimation(context, R.anim.slide_right_in));
    }
}
