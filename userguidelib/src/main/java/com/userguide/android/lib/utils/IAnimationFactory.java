package com.userguide.android.lib.utils;

import android.view.View;


public interface IAnimationFactory {

    void fadeInView(View target, long duration, AnimationStartListener listener);

    void fadeOutView(View target, long duration, AnimationEndListener listener);

    void enterRevealView(View target, int pointX, int pointY, long duration, AnimationStartListener listener);

    void exitRevealView(View target, long duration, AnimationEndListener listener);

    public interface AnimationStartListener {
        void onAnimationStart();
    }

    public interface AnimationEndListener {
        void onAnimationEnd();
    }
}

