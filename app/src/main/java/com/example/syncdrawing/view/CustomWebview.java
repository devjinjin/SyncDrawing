package com.example.syncdrawing.view;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.webkit.*;

public class CustomWebview extends WebView {

//	public int width;
//	public int height;
//	private Bitmap mBitmap;
//	private Canvas mCanvas;
//	private Path mPath;
//	private Paint mBitmapPaint;
//	Context context;
//	private Paint circlePaint;
//	private Path circlePath;
//	private Paint mPaint;
//	private int backgroundColor = Color.TRANSPARENT;
//	private IDrawingFragmentListener mListener;
//	private int mPenColor = Color.GREEN;
//	private int mPenThickness = 12;
//	private boolean isErase = false;
//	private float mX, mY;
//	private static final float TOUCH_TOLERANCE = 4;
	
	public CustomWebview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
//		init();
	}

	public CustomWebview(Context context, AttributeSet attrs) {
		super(context, attrs);
//		init();
	}

	public CustomWebview(Context context) {
		super(context);
//		init();
	}

	
	@Override
	public void loadUrl(String url) {
		// TODO Auto-generated method stub
		super.loadUrl(url);
//		super.loadUrl("rtsp://192.168.0.14/Resource/265/surfing.265");
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
//		if (mBitmap != null && !mBitmap.isRecycled()) {
//			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
//
//			canvas.drawPath(mPath, mPaint);
//
//			canvas.drawPath(circlePath, circlePaint);
//		}
	}

	
//	private void init() {
//		mPaint = new Paint();
//		mPaint.setAntiAlias(true);
//		mPaint.setDither(true);
//		mPaint.setColor(mPenColor);
//		mPaint.setStyle(Paint.Style.STROKE);
//		mPaint.setStrokeJoin(Paint.Join.ROUND);
//		mPaint.setStrokeCap(Paint.Cap.ROUND);
//		mPaint.setStrokeWidth(mPenThickness);
////	    mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
//	    
//		mPath = new Path();
//		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
//		circlePaint = new Paint();
//		circlePath = new Path();
//		circlePaint.setAntiAlias(true);
//		circlePaint.setColor(Color.RED);
//		circlePaint.setStyle(Paint.Style.STROKE);
//		circlePaint.setStrokeJoin(Paint.Join.MITER);
//		circlePaint.setStrokeWidth(4f);
//	}
//	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
//		width = w;
//		height = h;
//		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//
//		mCanvas = new Canvas(mBitmap);

	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		float x = event.getX();
//		float y = event.getY();
//
//		switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN :
//
//				touch_start(x, y);
//				invalidate();
//				if (mListener != null) {
//					float downX = (x / width);
//					float downY = (y / height);
//					mListener.onTouchEvent(PacketID.BC_DRAW_GESTURE_DOWN, downX, downY);
//				}
//
//				break;
//			case MotionEvent.ACTION_MOVE :
//
//				touch_move(x, y);
//				invalidate();
//				if (mListener != null) {
//					float downX = (x / width);
//					float downY = (y / height);
//					mListener.onTouchEvent(PacketID.BC_DRAW_GESTURE_MOVE, downX, downY);
//				}
//
//				break;
//			case MotionEvent.ACTION_UP :
//				touch_up();
//				invalidate();
//				if (mListener != null) {
//					float downX = (x / width);
//					float downY = (y / height);
//					mListener.onTouchEvent(PacketID.BC_DRAW_GESTURE_UP, downX, downY);
//				}
//				break;
//		}
//		return true;
//	}
//	
//	private void touch_start(float x, float y) {
//		mPath.reset();
//		mPath.moveTo(x, y);
//		mX = x;
//		mY = y;
//	}
//
//	private void touch_move(float x, float y) {
//		// if (mX != x || mY != y) {
//		float dx = Math.abs(x - mX);
//		float dy = Math.abs(y - mY);
//		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
//			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
//
//			// mPath.lineTo(mX, mY);//(mX, mY, (x + mX) / 2, (y + mY) / 2);
//			mX = x;
//			mY = y;
//
//			circlePath.reset();
//			circlePath.addCircle(mX, mY, 30, Path.Direction.CCW);
//		}
//		// }
//	}
//	private void touch_up() {
//		mPath.lineTo(mX, mY);
//		circlePath.reset();
//		// commit the path to our offscreen
//		mCanvas.drawPath(mPath, mPaint);
//		// kill this so we don't double draw
//		mPath.reset();
//	}
	


}
