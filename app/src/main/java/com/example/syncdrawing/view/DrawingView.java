package com.example.syncdrawing.view;

import android.content.*;
import android.util.*;
import android.view.*;

public abstract class DrawingView extends View {
	
	protected IDrawingFragmentListener mListener;
	
	public interface IDrawingFragmentListener {
		void onSizeChange(int pWidth, int pHeight);
		void onTouchEvent(short EventId, float pSX, float pSY);
		void onChangeModeTitle();
	}
	
	public void setOnDrawingFragmentListener(IDrawingFragmentListener pListener) {
		mListener = pListener;
	}
	
	public DrawingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DrawingView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public abstract boolean onTouchEventBySync(MotionEvent motionEvent);
	public abstract void startDrawing();
	public abstract void startEraser();	
	public abstract void setDefaultCanvas(int color);
	public abstract void setDefaultCanvas();	
	public abstract void setPenColor(int pPenColor);
	public abstract void setThickness(int pPenThickness);
	public abstract float getThickness();
	public abstract int getPenColor();	
	public abstract void setBackgroundColor(int color);
	public abstract boolean isEraseMode();
	public abstract int getBackgroundColor();

	public abstract void setPenFilterType(int pType);
}
