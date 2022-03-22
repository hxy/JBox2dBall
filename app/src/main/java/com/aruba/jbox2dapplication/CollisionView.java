package com.aruba.jbox2dapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.jbox2d.dynamics.Body;

/**
 * 碰撞view
 */
public class CollisionView extends FrameLayout {
    private CollisionPresenter collisionPresenter;
    private int currentOrientation = 0;
    private CollisionOrientationListener orientationListener;

    public CollisionView(@NonNull Context context) {
        this(context, null);
    }

    public CollisionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollisionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //开启ondraw方法回调
        setWillNotDraw(false);
        orientationListener = new CollisionOrientationListener(context);
        collisionPresenter = new CollisionPresenter();
        collisionPresenter.setDensity(getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        collisionPresenter.updateBounds(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d("yue.huang","onLayoutonLayoutonLayout");
        if (changed) {
            //子viwe设置body
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                collisionPresenter.bindBody(view);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        collisionPresenter.startWorld();

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            collisionPresenter.drawView(view);
        }
        invalidate();
    }

    public void onSensorChanged(float x, float y) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            collisionPresenter.applyLinearImpulse(x, y, view);
        }
    }

    /**
     * 添加小球
     * @param view
     */
    public void addBall(final ImageView view){
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        if(currentOrientation == 180){
            //如果当前手机
            layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        }
        addView(view,layoutParams);
        view.post(new Runnable() {
            @Override
            public void run() {
                collisionPresenter.bindBody(view);
            }
        });
    }

    public void removeBall(int index){
        final View child = getChildAt(index);
        Animator scaleX = ObjectAnimator.ofFloat(child,"scaleX",1,0);
        Animator scaleY = ObjectAnimator.ofFloat(child,"scaleY",1,0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX,scaleY);
        animatorSet.setDuration(200);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                collisionPresenter.destroyBody((Body) child.getTag(R.id.view_body_tag));
                removeView(child);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }




    private class CollisionOrientationListener extends OrientationEventListener {

        public CollisionOrientationListener(Context context) {
            super(context);
        }

        public CollisionOrientationListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if(orientation == OrientationEventListener.ORIENTATION_UNKNOWN){return;}
            int newOrientation = ((orientation+45)/90*90)%360;
            if(currentOrientation!=newOrientation){
                currentOrientation = newOrientation;
            }
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(orientationListener.canDetectOrientation()){orientationListener.enable();}
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        orientationListener.disable();
    }
}
