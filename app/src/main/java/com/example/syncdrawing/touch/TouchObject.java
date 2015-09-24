package com.example.syncdrawing.touch;

import android.graphics.*;


public class TouchObject implements Comparable<TouchObject> {
	public PointF mPoint;
	public long mEventTime = 0;
	public short mEvent = 0;
	public int mPenColor = 0;
	public int mPenThickness = 0;
	public TouchObject(long eventTime, short pEvent,  PointF pPoint, int pPenColor, int pPenThickness){
		mEvent = pEvent;
		mPoint = pPoint;
		mEventTime =eventTime;
		mPenColor = pPenColor;
		mPenThickness = pPenThickness;
	}
	

	@Override
	public int compareTo(TouchObject another) {
		// TODO Auto-generated method stub
		 return (int) (this.mEventTime - another.mEventTime);
	}
}
