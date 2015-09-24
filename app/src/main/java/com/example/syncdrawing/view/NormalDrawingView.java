package com.example.syncdrawing.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.syncdrawing.packet.PacketID;

public class NormalDrawingView extends DrawingView {

	public int width;
	public int height;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Path mPath;
	private Paint mBitmapPaint;
	Context context;
	private Paint circlePaint;
	private Path circlePath;
	private Paint mPaint;
	private int mBackgroundColor = Color.TRANSPARENT;

	private int mPenColor = Color.GREEN;
	private float mPenThickness = 12;
	private boolean isErase = false;
	private MaskFilter  mEmboss;
	private MaskFilter mBlur;
	private MaskFilter mBlur2;
	private MaskFilter mBlur3;
	private MaskFilter mBlur4;
	private MaskFilter mBlur5;
	public NormalDrawingView(Context pContext, AttributeSet attrs, int defStyle) {
		super(pContext, attrs, defStyle);
		context = pContext;
		init();
	}

	public NormalDrawingView(Context pContext, AttributeSet attrs) {
		super(pContext, attrs);
		context = pContext;
		init();
	}
	public NormalDrawingView(Context pContext) {

		super(pContext);
		context = pContext;
		init();

	}

	@Override
	public void setBackgroundColor(int color) {
		mBackgroundColor = color;
	}
	@Override
	public boolean isEraseMode() {
		return isErase;
	}
	@Override
	public int getBackgroundColor() {
		return mBackgroundColor;
	}

	protected final void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(mPenColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(mPenThickness);
		// mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

		mPath = new Path();
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		circlePaint = new Paint();
		circlePath = new Path();
		circlePaint.setAntiAlias(true);
		circlePaint.setColor(Color.RED);
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setStrokeJoin(Paint.Join.MITER);
		circlePaint.setStrokeWidth(4f);
		initFilter();
	}

	private void initFilter(){
		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
		mBlur2 = new BlurMaskFilter(8, BlurMaskFilter.Blur.INNER);
		mBlur3 = new BlurMaskFilter(8, BlurMaskFilter.Blur.OUTER);
		mBlur4 = new BlurMaskFilter(8, BlurMaskFilter.Blur.SOLID);
		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
	}

	@Override
	public void setPenFilterType(int pType){
		if(pType == 0){
			mPaint.setMaskFilter(null);
		}else if(pType == 1){
			mPaint.setMaskFilter(mEmboss);
		}else if(pType == 2){
			mPaint.setMaskFilter(mBlur);
		}else if(pType == 3){
			mPaint.setMaskFilter(mBlur2);
		}else if(pType == 4){
			mPaint.setMaskFilter(mBlur3);
		}else if(pType == 5){
			mPaint.setMaskFilter(mBlur4);
		}
	}
	@Override
	public void setPenColor(int pPenColor) {

		if (mPenColor != pPenColor) {
			mPenColor = pPenColor;
			mPaint.setColor(mPenColor);
		}
	}
	@Override
	public void setThickness(int pPenThickness) {
		if (mPenThickness != pPenThickness) {
			mPenThickness = pPenThickness;
			mPaint.setStrokeWidth(mPenThickness);
		}
	}
	@Override
	public float getThickness() {
		return mPenThickness;
	}
	@Override
	public int getPenColor() {
		if (isErase) {
			return mBackgroundColor;
		}
		return mPenColor;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (mListener != null) {
			mListener.onSizeChange(widthSize, heightSize);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

		mCanvas = new Canvas(mBitmap);

	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mBitmap != null && !mBitmap.isRecycled()) {
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

			canvas.drawPath(mPath, mPaint);

			canvas.drawPath(circlePath, circlePaint);
		}

	}
	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 1;

	private void touch_start(float x, float y) {
		mPath.reset();
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		// if (mX != x || mY != y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);

		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);

			// mPath.lineTo(mX, mY);(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;

			circlePath.reset();
			circlePath.addCircle(mX, mY, 30, Path.Direction.CCW);
		}
		// }
	}

	private void touch_up() {
		mPath.lineTo(mX, mY);
		circlePath.reset();
		// commit the path to our offscreen
		mCanvas.drawPath(mPath, mPaint);
		// kill this so we don't double draw
		mPath.reset();
	}

	@Override
	public boolean onTouchEventBySync(MotionEvent event) {

		synchronized (event) {

			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN :
					touch_start(x, y);
					invalidate();

					break;
				case MotionEvent.ACTION_MOVE :
					touch_move(x, y);
					invalidate();

					break;
				case MotionEvent.ACTION_UP :
					touch_up();
					invalidate();

					break;
			}

			return true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :

				touch_start(x, y);
				invalidate();
				if (mListener != null) {
					float downX = (x / width);
					float downY = (y / height);
					mListener.onTouchEvent(PacketID.BC_DRAW_GESTURE_DOWN, downX, downY);
				}

				break;
			case MotionEvent.ACTION_MOVE :

				touch_move(x, y);
				invalidate();
				if (mListener != null) {
					float downX = (x / width);
					float downY = (y / height);
					mListener.onTouchEvent(PacketID.BC_DRAW_GESTURE_MOVE, downX, downY);
				}

				break;
			case MotionEvent.ACTION_UP :
				touch_up();
				invalidate();
				if (mListener != null) {
					float downX = (x / width);
					float downY = (y / height);
					mListener.onTouchEvent(PacketID.BC_DRAW_GESTURE_UP, downX, downY);
				}
				break;
		}
		return true;
	}

	@Override
	public void startEraser() {
		isErase = true;
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(mBackgroundColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(50);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

		circlePaint.setStrokeWidth(50);
		mListener.onChangeModeTitle();
	}

	@Override
	public void startDrawing() {
		isErase = false;
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(mPenColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(mPenThickness);
		mPaint.setXfermode(null);

		circlePaint.setStrokeWidth(4f);

		mListener.onChangeModeTitle();
	}

	@Override
	public void setDefaultCanvas(int color) {

		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
			Paint paint = new Paint();
			mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
			setBackgroundColor(color);
			paint.setColor(color);
			paint.setStyle(Paint.Style.FILL);
			mCanvas.drawRect(new Rect(0, 0, width, height), paint);
			invalidate();
		}
	}

	@Override
	public void setDefaultCanvas() {

		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
			Paint paint = new Paint();
			mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
			paint.setColor(getBackgroundColor());
			paint.setStyle(Paint.Style.FILL);
			mCanvas.drawRect(new Rect(0, 0, width, height), paint);
			invalidate();
		}
	}

	public void setDrawingPixel() {
		if (mBitmap != null) {
			int[] mPixels = new int[width * height];
			mBitmap.getPixels(mPixels, 0, width, 0, 0, width, height);

			// int startX = 200;
			// int endX = 500;
			//
			// int startY = 100;
			// int endY = 400;
			for (int i = 200; i < 500; i++) {
				int a = i * width;
				for (int j = 300; j < 600; j++) {
					int a1 = a+j;
					mPixels[a1] = Color.YELLOW;
				}
			}

			mBitmap.setPixels(mPixels, 0, width, 0, 0, width, height);

			invalidate();
		}
	}

}
