package com.nosari20.bleconnect.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nosari20.bleconnect.R;


public class JoystickWidget extends View implements Runnable {
    // Constants
    protected final double RAD = 57.2957795;
    public final static long DEFAULT_LOOP_INTERVAL = 100; // 100 ms
    private final static double ANGLE_STEP = 45;
    public enum Direction {
        FRONT_LEFT,
        FRONT,
        FRONT_RIGHT,
        RIGHT,
        BOTTOM_LEFT,
        BOTTOM,
        BOTTOM_RIGHT,
        LEFT,
        NONE,
    }

    // Variables
    protected OnJoystickMoveListener onJoystickMoveListener; // Listener
    protected IsJoystickChanged isJoystickChanged;
    protected Thread thread = new Thread(this);
    protected long loopInterval = DEFAULT_LOOP_INTERVAL;
    protected int xPosition = 0; // Touch x position
    protected int yPosition = 0; // Touch y position
    protected double centerX = 0; // Center view x position
    protected double centerY = 0; // Center view y position
    protected Paint mainCircle;
    protected Paint secondaryCircle;
    protected Paint button;
    protected int joystickRadius;
    protected int buttonRadius;
    protected double lastAngle = 0;
    protected int lastPower = 0;
    protected Direction lastDirection;

    protected int stick_color = Color.argb((int)(0.5*255),0,0,0);
    protected int main_circle_color = Color.argb((int)(0.2*255),255,255,255);
    protected int second_circle_color = Color.argb((int)(0.6*255),255,255,255);

    public JoystickWidget(Context context) {
        super(context);
    }

    public JoystickWidget(Context context, AttributeSet attrs) {
        super(context, attrs);


        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.JoystickWidget,
                0, 0);

        try {
            String c = a.getString(R.styleable.JoystickWidget_stick_color);
            if(c!=null) this.stick_color = Color.parseColor(c);

            c = a.getString(R.styleable.JoystickWidget_main_circle_color);
            if(c!=null) this.main_circle_color = Color.parseColor(c);

            c = a.getString(R.styleable.JoystickWidget_second_circle_color);
            if(c!=null) this.second_circle_color = Color.parseColor(c);


        } finally {
            a.recycle();
        }
        initJoystick();

    }

    public JoystickWidget(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);


        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.JoystickWidget,
                0, 0);

        try {
            String c = a.getString(R.styleable.JoystickWidget_stick_color);
            if(c!=null) this.stick_color = Color.parseColor(c);

            c = a.getString(R.styleable.JoystickWidget_main_circle_color);
            if(c!=null) this.main_circle_color = Color.parseColor(c);

            c = a.getString(R.styleable.JoystickWidget_second_circle_color);
            if(c!=null) this.second_circle_color = Color.parseColor(c);


        } finally {
            a.recycle();
        }

        //do something with str
        initJoystick();
    }

    protected void initJoystick() {
        this.setIsJoystickChanged(new IsJoystickChanged() {
            @Override
            public boolean compare(double old_angle, int old_power, JoystickWidget.Direction old_direction, double new_angle, int new_power, JoystickWidget.Direction new_direction) {
                if(old_angle == new_angle && old_power == new_power) return false;
                return true;
            }
        });

        mainCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainCircle.setColor(main_circle_color);
        mainCircle.setStyle(Paint.Style.FILL_AND_STROKE);

        secondaryCircle = new Paint();
        secondaryCircle.setColor(second_circle_color);
        secondaryCircle.setStyle(Paint.Style.STROKE);

        button = new Paint(Paint.ANTI_ALIAS_FLAG);
        button.setColor(stick_color);
        button.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        // before measure, get the center of view
        xPosition = (int) getWidth() / 2;
        yPosition = (int) getHeight() / 2;
        int d = Math.min(xNew, yNew);
        buttonRadius = (int) (d / 2 * 0.25);
        joystickRadius = (int) (d / 2 * 0.75);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and
        // height
        int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));

        setMeasuredDimension(d,d);
    }

    protected int measure(int measureSpec) {
        int result = 0;

        // Decode the measurement specifications.
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified.
            result = 200;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            result = specSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;

        // painting the main circle
        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius,
                mainCircle);

        // painting the secondary circle
        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius / 2,
                secondaryCircle);


        // painting the move button
        canvas.drawCircle(xPosition, yPosition, buttonRadius, button);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xPosition = (int) event.getX();
        yPosition = (int) event.getY();
        double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (abs > joystickRadius) {
            xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
            yPosition = (int) ((yPosition - centerY) * joystickRadius / abs + centerY);
        }
        invalidate();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            xPosition = (int) centerX;
            yPosition = (int) centerY;
            thread.interrupt();
            if (onJoystickMoveListener != null)
                onJoystickMoveListener.onValueChanged(getAngle(), getPower(),
                        getDirection());
        }
        if (onJoystickMoveListener != null
                && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(this);
            thread.start();
            if (onJoystickMoveListener != null)
                onJoystickMoveListener.onValueChanged(getAngle(), getPower(),
                        getDirection());
        }
        return true;
    }

    public double getAngle() {
        if (xPosition > centerX) {
            if (yPosition < centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY)
                        / (xPosition - centerX))
                        * RAD + 90);
            } else if (yPosition > centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY)
                        / (xPosition - centerX)) * RAD) + 90;
            } else {
                return lastAngle = 90;
            }
        } else if (xPosition < centerX) {
            if (yPosition < centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY)
                        / (xPosition - centerX))
                        * RAD - 90);
            } else if (yPosition > centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY)
                        / (xPosition - centerX)) * RAD) - 90;
            } else {
                return lastAngle = -90;
            }
        } else {
            if (yPosition <= centerY) {
                return lastAngle = 0;
            } else {
                if (lastAngle < 0) {
                    return lastAngle = -180;
                } else {
                    return lastAngle = 180;
                }
            }
        }
    }

    public int getPower() {

        lastPower = (int) Math.ceil(100 * Math.sqrt((xPosition - centerX)
                * (xPosition - centerX) + (yPosition - centerY)
                * (yPosition - centerY)) / (float)joystickRadius);
        if(lastPower > 100 ) lastPower = 100;
        return lastPower;
    }

    public Direction getDirection() {
        if (getPower() == 0) {
            lastDirection = Direction.NONE;
            return this.lastDirection;
        }

        double abs_angle = Math.abs(lastAngle);

        if(lastAngle > 0){
            if(abs_angle < ANGLE_STEP/2) { lastDirection = Direction.FRONT; return this.lastDirection;}
            if(abs_angle < ANGLE_STEP/2 + ANGLE_STEP) { lastDirection = Direction.FRONT_RIGHT; return this.lastDirection;}
            if(abs_angle < ANGLE_STEP/2 + 2*ANGLE_STEP) { lastDirection = Direction.RIGHT; return this.lastDirection;}
            if(abs_angle < ANGLE_STEP/2 + 3*ANGLE_STEP) { lastDirection = Direction.BOTTOM_RIGHT; return this.lastDirection;}
            this.lastDirection = Direction.BOTTOM;
            return this.lastDirection;
        }else{
            if(abs_angle < ANGLE_STEP/2) { lastDirection = Direction.FRONT;  return this.lastDirection;}
            if(abs_angle < ANGLE_STEP/2 + ANGLE_STEP) { lastDirection = Direction.FRONT_LEFT; return this.lastDirection;};
            if(abs_angle < ANGLE_STEP/2 + 2*ANGLE_STEP) { lastDirection = Direction.LEFT; return this.lastDirection;};
            if(abs_angle < ANGLE_STEP/2 + 3*ANGLE_STEP) { lastDirection = Direction.BOTTOM_LEFT; return this.lastDirection;}
            lastDirection = Direction.BOTTOM;
            return this.lastDirection;
        }
    }

    public void setOnJoystickMoveListener(OnJoystickMoveListener listener,
                                          long repeatInterval) {
        this.onJoystickMoveListener = listener;
        this.loopInterval = repeatInterval;
    }

    public void setIsJoystickChanged(IsJoystickChanged isJoystickChanged) {
        this.isJoystickChanged = isJoystickChanged;
    }


    @Override
    public void run() {
        while (!Thread.interrupted()) {
            post(new Runnable() {
                public void run() {
                    if (onJoystickMoveListener != null)
                        if(isJoystickChanged != null){
                            if(isJoystickChanged.compare(lastAngle,lastPower, lastDirection, getAngle(), getPower(), getDirection()))
                                onJoystickMoveListener.onValueChanged(getAngle(),getPower(), getDirection());
                        }else{
                            onJoystickMoveListener.onValueChanged(getAngle(),getPower(), getDirection());
                        }
                }
            });
            try {
                Thread.sleep(loopInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }


    public interface OnJoystickMoveListener {
        public void onValueChanged(double angle, int power, Direction direction);
    }

    public interface IsJoystickChanged {
        public boolean compare(double old_angle, int old_power, Direction old_direction, double new_angle, int new_power, Direction new_direction);
    }
}