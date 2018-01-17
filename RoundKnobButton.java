package com.example.rotaryknob;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Toast;

/*
File:              RoundKnobButton
Version:           1.0.0
Release Date:      November, 2013
License:           GPL v2
Description:	   A round knob button to control volume and toggle between two states

****************************************************************************
Copyright (C) 2013 Radu Motisan  <radu.motisan@gmail.com>

http://www.pocketmagic.net

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
****************************************************************************/

/* AMD - General, use new getAngle() function */

public class RoundKnobButton extends RelativeLayout implements OnGestureListener {

	private GestureDetector 	gestureDetector;
	private float 				mAngleDown , mAngleUp;
	private ImageView			ivRotor;
	private Bitmap 				bmpRotorOn , bmpRotorOff;
	private boolean 			mState = false;
	private int					m_nWidth = 0, m_nHeight = 0;
	private float 				startAngle = 0;
	private static double 		RotAngle[] = {90, 90};
	private ImageView ivBack;
	private int					Gplay, Gpause;
	private Context c;
	private Matrix matrix;
	
	interface RoundKnobButtonListener {
		public void onStateChange(boolean newstate) ;
		public void onRotate(int percentage);
	}
	
	private RoundKnobButtonListener m_listener;
	
	public void SetListener(RoundKnobButtonListener l) {
		m_listener = l;
	}
	
	public void SetState(boolean state) {
		mState = state;
		ivRotor.setImageBitmap(state?bmpRotorOn:bmpRotorOff);
		ivBack.setImageResource(state?Gpause:Gplay);
	}
	
	/* AMD - Now accept 3 bitmaps: backplay, backpause, rotor */
	public RoundKnobButton(Context context, int backplay, int backpause, int rotoron, int rotoroff, final int w, final int h) {		
		super(context);
		c = context;
		mState = false;
		Gplay = backplay;
		Gpause = backpause;
		
		// we won't wait for our size to be calculated, we'll just store out fixed size
		m_nWidth = w; 
		m_nHeight = h;
		// create stator
		ivBack = new ImageView(context);
		
		ivBack.setImageResource(Gplay);
		RelativeLayout.LayoutParams lp_ivBack = new RelativeLayout.LayoutParams(
				w,h);
		lp_ivBack.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(ivBack, lp_ivBack);
		// load rotor images
		Bitmap srcon = BitmapFactory.decodeResource(context.getResources(), rotoron);
		Bitmap srcoff = BitmapFactory.decodeResource(context.getResources(), rotoroff);
	    float scaleWidth = ((float) w) / srcon.getWidth();
	    float scaleHeight = ((float) h) / srcon.getHeight();
	    matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
		    
		bmpRotorOn = Bitmap.createBitmap(
				srcon, 0, 0, 
				srcon.getWidth(),srcon.getHeight() , matrix , true);
		bmpRotorOff = Bitmap.createBitmap(
				srcoff, 0, 0, 
				srcoff.getWidth(),srcoff.getHeight() , matrix , true);
		// create rotor
		ivRotor = new ImageView(context);
		ivRotor.setImageBitmap(bmpRotorOn);
		RelativeLayout.LayoutParams lp_ivKnob = new RelativeLayout.LayoutParams(w,h);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp_ivKnob.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(ivRotor, lp_ivKnob);
		// set initial state
		SetState(mState);
		// enable gesture detector
		gestureDetector = new GestureDetector(getContext(), this);
	}
	
	private float getAngle(double xTouch, double yTouch) {				
		double tx = (ivRotor.getLeft() + ivRotor.getRight()/2);
		double ty = (ivRotor.getTop() + ivRotor.getBottom()/2);
		double x1 = xTouch - tx;
		double y1 = ty - yTouch;
						
		if(yTouch > ty) {
			return -1;
		}
		
		float angle = (float) Math.toDegrees(Math.atan2(y1, x1));
	    //Toast.makeText(c, "" + (int)tx + "," + (int)ty + "," + (int)xTouch + "," + (int)yTouch + "," + (int)angle,  Toast.LENGTH_SHORT).show();
	    return angle;
	}
	
	private void rotateDialer(float degrees) {
		Toast.makeText(c, "Rotate :" + degrees,  Toast.LENGTH_SHORT).show();
		//ivRotor.setScaleType(ScaleType.MATRIX);   
		matrix.postRotate((float) degrees, m_nWidth/2, m_nHeight/2);
		ivRotor.setImageMatrix(matrix);
	}
		
	@Override public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) return true;
		else return super.onTouchEvent(event);
	}
	
	public boolean onDown(MotionEvent event) {
		/* AMD - Calculate startAngle */
		startAngle = getAngle(event.getX(), event.getY());
		if (startAngle == -1)
			return true;
		mAngleDown = startAngle;
		return true;
	}
	
	public boolean onSingleTapUp(MotionEvent e) {
		mAngleUp = getAngle(e.getX(), e.getY());

		// if we click up the same place where we clicked down, it's just a button press
		if (! Float.isNaN(mAngleDown) && ! Float.isNaN(mAngleUp) && Math.abs(mAngleUp-mAngleDown) < 10) {
			SetState(!mState);			
			/* AMD - Toggle BG */
			if (m_listener != null) m_listener.onStateChange(mState);
		}
		return true;
	}
	
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		/* AMD	-	Compute angle from currentAngle and startAngle and Rotate */
		/* 			Once rotated, calculate percent from RotAngle[0 & 1], call onRotate() */  
		
					float currentAngle = getAngle(e2.getX(), e2.getY());
					if (currentAngle == -1)
						return true;
					/* counter clockwise */										
					if((startAngle - currentAngle) < 0) {						
						if(currentAngle - startAngle > 10) 
							return true;						
						if (RotAngle[0] >= (currentAngle - startAngle)) {
							RotAngle[0] = RotAngle[0] - (currentAngle - startAngle);
							RotAngle[1] = RotAngle[1] + (currentAngle - startAngle);
							rotateDialer((float) (startAngle - currentAngle));
						} else {
							rotateDialer((float) (-RotAngle[0]));
							RotAngle[0] = 0;
							RotAngle[1] = 180;
						}							
					} 
					/* clockwise */
					else {
						if(startAngle - currentAngle > 10)
							return true;
						if (RotAngle[1] >= (startAngle - currentAngle)) {
							RotAngle[0] = RotAngle[0] + (startAngle - currentAngle);
							RotAngle[1] = RotAngle[1] - (startAngle - currentAngle);
							rotateDialer((float) (startAngle - currentAngle));
						} else {
							rotateDialer((float) (RotAngle[1]));
							RotAngle[0] = 180;
							RotAngle[1] = 0;
						}						
					}
					startAngle = currentAngle;
					
					int percent = (int) ((RotAngle[0] * 100)/ 180);
						if (m_listener != null) m_listener.onRotate(percent);
					return true;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) { return false; }

	public void onLongPress(MotionEvent e) {	}

}
