package com.iodesystems.android.libs.view.transitions;

import android.view.View;
import android.view.animation.Animation;

public class TransitionPair {

    private final Animation enterIn;
    private final Animation enterOut;

    private final Animation exitIn;
    private final Animation exitOut;


    public TransitionPair(Animation enterOut,
                          Animation enterIn,
                          Animation exitOut,
                          Animation exitIn) {
        this.enterIn = enterIn;
        this.enterOut = enterOut;
        this.exitIn = exitIn;
        this.exitOut = exitOut;
    }

    public void enter(View out, View in) {
        perform(out, in, enterOut, enterIn);
    }

    public void exit(View out, View in) {
        perform(out, in, exitOut, exitIn);
    }

    private void perform(final View outView, View inView, Animation outAnimation, Animation inAnimation) {
        if(outView instanceof OnExitListener){
            ((OnExitListener) outView).onViewnExit();
        }

        outView.clearAnimation();
        outView.startAnimation(outAnimation);
        outAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                outView.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {

            }
        });

        if(inView instanceof OnEnterListener){
            ((OnEnterListener) inView).onViewnEnter();
        }
        
        inView.clearAnimation();
        inView.startAnimation(inAnimation);
        inView.setVisibility(View.VISIBLE);
    }

    public interface OnEnterListener{
        void onViewnEnter();
    }

    public interface OnExitListener{
        void onViewnExit();
    }
}
