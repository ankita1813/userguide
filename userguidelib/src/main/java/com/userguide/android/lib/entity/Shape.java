package com.userguide.android.lib.entity;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by ankita on 7/4/17.
 */

public interface Shape {

    void draw(Canvas canvas, Paint paint, int x, int y, int padding);
    int getWidth();
    int getHeight();
    void updateTarget(Target target);
}
