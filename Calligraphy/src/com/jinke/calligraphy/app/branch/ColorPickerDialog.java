/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jinke.calligraphy.app.branch;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerDialog extends Dialog {

	private static final String TAG = "ColorPickerDialog";
	
    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    private OnColorChangedListener mListener;
    private int mInitialColor;

    private static class ColorPickerView extends View {
        private Paint mPaint;
        private Paint mCenterPaint;
        private final int[] mColors;
        private final int[] mGrayColors;
        private OnColorChangedListener mListener;
        
        private int[][] gridColors;
        private Paint   gridPaint;
        
        ColorPickerView(Context c, OnColorChangedListener l, int color) {
            super(c);
            mListener = l;
//            mColors = new int[] {
//            	0xFF000000/*black */ , 0xFFFF0000/* red */,   0xFFFF00FF/* magenta */, 0xFF0000FF/* blue */, 0xFF00FFFF/* cyan */, 0xFF00FF00/* green */,
//                0xFFFFFF00/* yellow */, 0xFFFFFFFF/* white*/, /*0xFF000000 black */
//            };
            mColors = new int[] {Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN, 
            		Color.GREEN, Color.YELLOW, Color.WHITE, Color.BLACK};
            mGrayColors = new int[] {Color.WHITE, Color.BLACK};
//            Shader s = new SweepGradient(0, 0, mColors, null);
            Shader s = new LinearGradient(START_X, START_Y, END_X, END_Y, mColors, null, Shader.TileMode.CLAMP);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(32);
            
            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(color);
            mCenterPaint.setStrokeWidth(5);
            
            gridColors = new int[COLOR_NUM_X][COLOR_NUM_Y];
            gridPaint = new Paint();
    		gridPaint.setStyle(Paint.Style.FILL);
            fillGridColors(mGrayColors, gridColors);
        }
        
        private boolean mTrackingCenter;
        private boolean mHighlightCenter;

        @Override 
        protected void onDraw(Canvas canvas) {
            float r = CENTER_X - mPaint.getStrokeWidth()*0.5f;
            
//            canvas.translate(CENTER_X, CENTER_X);
//            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
//            canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
            canvas.drawColor(Color.WHITE);
            canvas.drawRect(new RectF(50, 50, 4*100 + 50, 50 + 30), mPaint);
            canvas.drawCircle(CENTER_X, CENTER_Y, CENTER_RADIUS, mCenterPaint);
            
            canvas.save();
            canvas.translate(POS_X, POS_Y);
            for(int i=0;i<COLOR_NUM_X;i++) {
            	for(int j=0;j<COLOR_NUM_Y;j++) {
            		gridPaint.setColor(gridColors[j][i]);
            		canvas.drawRect(new Rect(i*DISTANCE_INTERVAL, j*DISTANCE_INTERVAL, 
            				(i+1)*DISTANCE_INTERVAL, (j+1)*DISTANCE_INTERVAL), gridPaint);
            	}
            }
            gridPaint.setColor(Color.BLACK);
            for(int i=0;i<=COLOR_NUM_X;i++){
            	canvas.drawLine(0, i*DISTANCE_INTERVAL, COLOR_NUM_X * DISTANCE_INTERVAL, 
            			i*DISTANCE_INTERVAL, gridPaint);
            }
            for(int i=0;i<=COLOR_NUM_Y;i++){
            	canvas.drawLine(i*DISTANCE_INTERVAL, 0, i*DISTANCE_INTERVAL, 
            			COLOR_NUM_Y * DISTANCE_INTERVAL, gridPaint);
            }
            canvas.restore();
            
            if (mTrackingCenter) {
                int c = mCenterPaint.getColor();
                mCenterPaint.setStyle(Paint.Style.STROKE);
                
                if (mHighlightCenter) {
                    mCenterPaint.setAlpha(0xFF);
                } else {
                    mCenterPaint.setAlpha(0x80);
                }
                canvas.drawCircle(CENTER_X, CENTER_Y,
                                  CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
                                  mCenterPaint);
                
                mCenterPaint.setStyle(Paint.Style.FILL);
                mCenterPaint.setColor(c);
            }
            
        }
        
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(DISTANCE_X, DISTANCE_Y);
//            setMeasuredDimension(CENTER_X*2, CENTER_Y*2);
        }
        
        private static final int CENTER_X = 250;
        private static final int CENTER_Y = 150;
        private static final int CENTER_RADIUS = 32;

        private static final int UNIT = 100;
        private static final int DISTANCE_X = UNIT * 5;
        private static final int DISTANCE_Y = UNIT * 6;
        private static final int DISTANCE_COLOR = UNIT * 4;
        private static final int START_X = 50;
        private static final int START_Y = 0;
        private static final int END_X = START_X + DISTANCE_COLOR;
        private static final int END_Y = 30;
        private static final int COLOR_NUM_X = 4;
        private static final int COLOR_NUM_Y = 4;
        private static final int DISTANCE_INTERVAL = 80;
        private static final int POS_X = 90;
        private static final int POS_Y = 200;

        private int floatToByte(float x) {
            int n = java.lang.Math.round(x);
            return n;
        }
        private int pinToByte(int n) {
            if (n < 0) {
                n = 0;
            } else if (n > 255) {
                n = 255;
            }
            return n;
        }
        
        private int ave(int s, int d, float p) {
            return s + java.lang.Math.round(p * (d - s));
        }
        
        private int interpColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }
            
            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);
            
            return Color.argb(a, r, g, b);
        }
        
        private int rotateColor(int color, float rad) {
            float deg = rad * 180 / 3.1415927f;
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            
            ColorMatrix cm = new ColorMatrix();
            ColorMatrix tmp = new ColorMatrix();

            cm.setRGB2YUV();
            tmp.setRotate(0, deg);
            cm.postConcat(tmp);
            tmp.setYUV2RGB();
            cm.postConcat(tmp);
            
            final float[] a = cm.getArray();

            int ir = floatToByte(a[0] * r +  a[1] * g +  a[2] * b);
            int ig = floatToByte(a[5] * r +  a[6] * g +  a[7] * b);
            int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);
            
            return Color.argb(Color.alpha(color), pinToByte(ir),
                              pinToByte(ig), pinToByte(ib));
        }
        
        private void fillGridColors(int colors[], int gridColors[][]){
        	float unit = 0f;
        	for(int i=0;i<COLOR_NUM_X;i++)
        		for(int j=0;j<COLOR_NUM_Y;j++){
        			unit = (i * COLOR_NUM_X + j) * 1.0f / (COLOR_NUM_X * COLOR_NUM_Y);
        			gridColors[i][j] = interpColor(mGrayColors, unit);
        			Log.i(TAG, "unit:" + unit + " color:" + i + "," + "j" + j + ":" + gridColors[i][j]);
        		}
        }
        
        private static final float PI = 3.1415926f;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - CENTER_X;
            float y = event.getY() - CENTER_Y;
        	Log.i(TAG, "x:" + event.getX() + " y:" + event.getY());
            boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= CENTER_RADIUS;
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTrackingCenter = inCenter;
                    if (inCenter) {
                        mHighlightCenter = true;
                        invalidate();
                        break;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (mTrackingCenter) {
                        if (mHighlightCenter != inCenter) {
                            mHighlightCenter = inCenter;
                            invalidate();
                        }
                    } else {
                        float angle = (float)java.lang.Math.atan2(y, x);
                        // need to turn angle [-PI ... PI] into unit [0....1]
                        Log.i(TAG, "angle:" + angle);
                        float unit = angle/(2*PI);
                        if (unit < 0) {
                            unit += 1;
                        }
                        unit = (event.getX() - START_X) / DISTANCE_COLOR;
                        mCenterPaint.setColor(interpColor(mColors, unit));
                        if(event.getX()>POS_X && event.getX() < POS_X + COLOR_NUM_X * DISTANCE_INTERVAL &&
                        		event.getY() > POS_Y && event.getY()< POS_Y + COLOR_NUM_Y * DISTANCE_INTERVAL) {
                        	mCenterPaint.setColor(
                        			gridColors[(int)(event.getY()- POS_Y) / DISTANCE_INTERVAL]
                        			           [(int)((event.getX()- POS_X) / DISTANCE_INTERVAL)]);
                        }
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTrackingCenter) {
                        if (inCenter) {
                            mListener.colorChanged(mCenterPaint.getColor());
                        }
                        mTrackingCenter = false;    // so we draw w/o halo
                        invalidate();
                    }
                    break;
            }
            return true;
        }
    }

    public ColorPickerDialog(Context context,
                             OnColorChangedListener listener,
                             int initialColor) {
        super(context);
        
        mListener = listener;
        mInitialColor = initialColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color) {
                mListener.colorChanged(color);
                dismiss();
            }
        };

        setContentView(new ColorPickerView(getContext(), l, mInitialColor));
        setTitle("Pick a Color");
        
    }
}
