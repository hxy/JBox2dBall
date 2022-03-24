package com.aruba.jbox2dapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.jbox2d.dynamics.Body;

/**
 * 碰撞view
 */
public class CollisionView extends FrameLayout implements SensorEventListener {
    private CollisionPresenter collisionPresenter;
    private float currentOrientation = 0;
    private SensorManager sensorManager;
    private Sensor defaultSensor;

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
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        defaultSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
        if(currentOrientation < 0){
            //如果当前手机头朝下，则从底部生成小球
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

    /**
     * 删除小球
     * @param index
     */
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
                //动画结束后删除view和刚体
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


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //注册传感器监听
        sensorManager.registerListener(this, defaultSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //取消传感器监听
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1] * 2.0f;
            onSensorChanged(-x, y);
            currentOrientation = y;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
