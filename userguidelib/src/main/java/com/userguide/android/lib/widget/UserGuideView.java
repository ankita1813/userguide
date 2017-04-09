package com.userguide.android.lib.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.userguide.android.lib.R;
import com.userguide.android.lib.entity.CircleShape;
import com.userguide.android.lib.entity.RectangleShape;
import com.userguide.android.lib.entity.Shape;
import com.userguide.android.lib.entity.Target;
import com.userguide.android.lib.entity.TargetView;
import com.userguide.android.lib.utils.AnimationFactory;
import com.userguide.android.lib.utils.IAnimationFactory;

/**
 * Created by deepak on 7/4/17.
 */

public class UserGuideView extends FrameLayout implements View.OnClickListener, View.OnTouchListener {

    private int mOldHeight;
    private int mOldWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mEraser;
    private Target mTarget;
    private Shape mShape;
    private int mXPosition;
    private int mYPosition;
    private int mShapePadding = 10;
    private View mContentBox;
    private TextView mTitleTextView;
    private TextView mContentTextView;
    private TextView mDismissButton;
    private int mGravity;
    private int mContentBottomMargin;
    private int mContentTopMargin;
    private boolean mShouldRender = false;
    private boolean mRenderOverNav = false;
    private int mMaskColour;
    private AnimationFactory mAnimationFactory;
    private boolean mShouldAnimate = true;
    private long mFadeDurationInMillis = 300;
    private Handler mHandler;
    private long mDelayInMillis = 0;
    private int mBottomMargin = 0;
    private UpdateOnGlobalLayout mLayoutListener;

    public UserGuideView(Context context) {
        super(context);
        init(context);
    }

    public UserGuideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UserGuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UserGuideView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private void init(Context context) {
        setWillNotDraw(false);
        // create our animation factory
        mAnimationFactory = new AnimationFactory();
        mLayoutListener = new UpdateOnGlobalLayout();
        getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);
        mMaskColour = Color.parseColor("#CC000000");
        setVisibility(INVISIBLE);
        setOnTouchListener(this);

        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.view_user_guide, this, true);
        mContentBox = contentView.findViewById(R.id.user_guide_layout);
        mTitleTextView = (TextView) contentView.findViewById(R.id.guide_title);
        mContentTextView = (TextView) contentView.findViewById(R.id.guide_content);
        mDismissButton = (TextView) contentView.findViewById(R.id.guide_gotit);
        mDismissButton.setOnClickListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        if (!mShouldRender) return;
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if(width <= 0 || height <= 0) return;

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;

        }

        if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {
           // if (mBitmap != null) mBitmap.recycle();
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        // save our 'old' dimensions
        mOldWidth = width;
        mOldHeight = height;

        // clear canvas
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // draw solid background
        mCanvas.drawColor(mMaskColour);

        // Prepare eraser Paint if needed
        if (mEraser == null) {
            mEraser = new Paint();
            mEraser.setColor(0xFFFFFFFF);
            mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        mShape.draw(mCanvas, mEraser, mXPosition, mYPosition, mShapePadding);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    @Override
    public void onClick(View v) {
        hide();
    }

    public void setTarget(Target target) {
        mTarget = target;
        updateDismissButton();

        if (mTarget != null) {


            if (!mRenderOverNav && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBottomMargin = getSoftButtonsBarSizePort((Activity) getContext());
                FrameLayout.LayoutParams contentLP = (LayoutParams) getLayoutParams();

                if (contentLP != null && contentLP.bottomMargin != mBottomMargin)
                    contentLP.bottomMargin = mBottomMargin;
            }

            // apply the target position
            Point targetPoint = mTarget.getPoint();
            Rect targetBounds = mTarget.getBounds();
            setPosition(targetPoint);

            // now figure out whether to put content above or below it
            int height = getMeasuredHeight();
            int midPoint = height / 2;
            int yPos = targetPoint.y;

            int radius = Math.max(targetBounds.height(), targetBounds.width()) / 2;
            if (mShape != null) {
                mShape.updateTarget(mTarget);
                radius = mShape.getHeight() / 2;
            }

            if (yPos > midPoint) {
                mContentTopMargin = 0;
                mContentBottomMargin = (height - yPos) + radius + mShapePadding;
                mGravity = Gravity.BOTTOM;
            } else {
                mContentTopMargin = yPos + radius + mShapePadding;
                mContentBottomMargin = 0;
                mGravity = Gravity.TOP;
            }
        }

        applyLayoutParams();
    }

    private void applyLayoutParams() {

        if (mContentBox != null && mContentBox.getLayoutParams() != null) {
            FrameLayout.LayoutParams contentLP = (LayoutParams) mContentBox.getLayoutParams();

            boolean layoutParamsChanged = false;

            if (contentLP.bottomMargin != mContentBottomMargin) {
                contentLP.bottomMargin = mContentBottomMargin;
                layoutParamsChanged = true;
            }

            if (contentLP.topMargin != mContentTopMargin) {
                contentLP.topMargin = mContentTopMargin;
                layoutParamsChanged = true;
            }

            if (contentLP.gravity != mGravity) {
                contentLP.gravity = mGravity;
                layoutParamsChanged = true;
            }


            if (layoutParamsChanged)
                mContentBox.setLayoutParams(contentLP);
        }
    }



    void setPosition(Point point) {
        setPosition(point.x, point.y);
    }

    void setPosition(int x, int y) {
        mXPosition = x;
        mYPosition = y;
    }

    private void setTitleText(CharSequence contentText) {
        if (mTitleTextView != null && !contentText.equals("")) {
            mContentTextView.setAlpha(0.5F);
            mTitleTextView.setText(contentText);
        }
    }

    private void setContentText(CharSequence contentText) {
        if (mContentTextView != null) {
            mContentTextView.setText(contentText);
        }
    }

    private void setDismissText(CharSequence dismissText) {
        if (mDismissButton != null) {
            mDismissButton.setText(dismissText);

            updateDismissButton();
        }
    }

    private void setTitleTextColor(int textColour) {
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(textColour);
        }
    }


    private void setShouldRender(boolean shouldRender) {
        mShouldRender = shouldRender;
    }





    public void setShape(Shape mShape) {
        this.mShape = mShape;
    }


    private void updateDismissButton() {
        // hide or show button
        if (mDismissButton != null) {
            if (TextUtils.isEmpty(mDismissButton.getText())) {
                mDismissButton.setVisibility(GONE);
            } else {
                mDismissButton.setVisibility(VISIBLE);
            }
        }
    }


    private class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {
            setTarget(mTarget);
        }
    }

    public static class Builder {
        private static final int CIRCLE_SHAPE = 0;
        private static final int RECTANGLE_SHAPE = 1;

        private boolean fullWidth = false;
        private int shapeType = CIRCLE_SHAPE;

        final UserGuideView userGuideView;

        private final Activity activity;

        public Builder(Activity activity) {
            this.activity = activity;

            userGuideView = new UserGuideView(activity);
        }


        public Builder setTarget(View target) {
            userGuideView.setTarget(new TargetView(target));
            return this;
        }




        public Builder setDismissText(CharSequence dismissText) {
            userGuideView.setDismissText(dismissText);
            return this;
        }


        public Builder setDiscriptionText(CharSequence text) {
            userGuideView.setContentText(text);
            return this;
        }

        public Builder setTitleText(CharSequence text) {
            userGuideView.setTitleText(text);
            return this;
        }


        public Builder setTitleTextColor(int textColour) {
            userGuideView.setTitleTextColor(textColour);
            return this;
        }

        public Builder setShape(Shape shape) {
            userGuideView.setShape(shape);
            return this;
        }


        public UserGuideView build() {
            if (userGuideView.mShape == null) {
                switch (shapeType) {
                    case RECTANGLE_SHAPE:
                        userGuideView.setShape(new RectangleShape(userGuideView.mTarget.getBounds(), fullWidth));
                        break;

                    case CIRCLE_SHAPE:
                        userGuideView.setShape(new CircleShape(userGuideView.mTarget));
                        break;

                    default:
                        throw new IllegalArgumentException("Unsupported shape type: " + shapeType);
                }
            }

            return userGuideView;
        }

        public UserGuideView show() {
            build().show(activity);
            return userGuideView;
        }

    }

    public void removeFromWindow() {
        if (getParent() != null && getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        mEraser = null;
        mAnimationFactory = null;
        mCanvas = null;
        mHandler = null;

        getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutListener);
        mLayoutListener = null;

    }

    public boolean show(final Activity activity) {
        if (getParent() != null && getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }
        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);

        setShouldRender(true);

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mShouldAnimate) {
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                        enterRevealView();
                    }else {
                        fadeIn();
                    }
                } else {
                    setVisibility(VISIBLE);
                }
            }
        }, mDelayInMillis);

        updateDismissButton();

        return true;
    }


    public void hide() {
        if (mShouldAnimate) {

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                exitRevealView();
            }else {
                fadeOut();
            }
        } else {
            removeFromWindow();
        }
    }

    public void fadeIn() {
        setVisibility(INVISIBLE);

        mAnimationFactory.fadeInView(this, mFadeDurationInMillis,
                new IAnimationFactory.AnimationStartListener() {
                    @Override
                    public void onAnimationStart() {
                        setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void enterRevealView() {
        setVisibility(INVISIBLE);
        int pointX = mTarget.getPoint().x;
        int pointY = mTarget.getPoint().y;
        mAnimationFactory.enterRevealView(this, pointX, pointY, mFadeDurationInMillis,
                new IAnimationFactory.AnimationStartListener() {
                    @Override
                    public void onAnimationStart() {
                        setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    public void fadeOut() {

        mAnimationFactory.fadeOutView(this, mFadeDurationInMillis, new IAnimationFactory.AnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                setVisibility(INVISIBLE);
                removeFromWindow();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void exitRevealView() {

        mAnimationFactory.exitRevealView(this, mFadeDurationInMillis, new IAnimationFactory.AnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                setVisibility(INVISIBLE);
                removeFromWindow();
            }
        });
    }



    public static int getSoftButtonsBarSizePort(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

}
