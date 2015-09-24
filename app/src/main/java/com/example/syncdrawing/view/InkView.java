package com.example.syncdrawing.view;

import java.util.*;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;
import android.view.*;


import com.example.syncdrawing.R;
import com.example.syncdrawing.packet.PacketID;
import com.example.syncdrawing.pen.BasePen;
import com.example.syncdrawing.pen.InkPen;

public class InkView extends DrawingView {
	/**
	 * The default maximum stroke width (dp)<br/>
	 * Will be used as the standard stroke width if FLAG_RESPONSIVE_WIDTH is
	 * removed
	 */
	public static final float DEFAULT_MAX_STROKE_WIDTH = 10f;
	/**
	 * The default minimum stroke width (dp)
	 */
	public static final float DEFAULT_MIN_STROKE_WIDTH = 0.5f;

	/**
	 * The default smoothing ratio for calculating the control points for the
	 * bezier curves<br/>
	 * Will be ignored if FLAG_INTERPOLATION is removed
	 */
	public static final float DEFAULT_SMOOTHING_RATIO = 0.75f;

	/**
	 * When this flag is added, paths will be drawn as cubic-bezier curves
	 */
	public static final int FLAG_INTERPOLATION = 1;

	/**
	 * When present, the width of the paths will be responsive to the velocity
	 * of the stroke<br/>
	 * When missing, the width of the path will be the the max stroke width
	 */
	public static final int FLAG_RESPONSIVE_WIDTH = 1 << 1;

	/**
	 * When present, the data points for the path are drawn with their
	 * respective control points
	 */
	public static final int FLAG_DEBUG = 1 << 2;

	// constants
	private static final float THRESHOLD_VELOCITY = 7f; // in/s
	private static final float THRESHOLD_ACCELERATION = 3f; // in/s^2
	private static final float FILTER_RATIO_MIN = 0.22f;
	private static final float FILTER_RATIO_ACCEL_MOD = 0.1f;
	private static final int DEFAULT_FLAGS = FLAG_INTERPOLATION | FLAG_RESPONSIVE_WIDTH;

	// settings
	private int mFlags;
	private float mMaxStrokeWidth;
	private float mMinStrokeWidth;

	// points
	private ArrayList<BasePen> mPointQueue = new ArrayList<BasePen>();
	private ArrayList<BasePen> mPointRecycle = new ArrayList<BasePen>();

	// misc

	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Paint mPaint;

	// debug
	private boolean mHasDebugLayer = false;
	private Bitmap mDebugBitmap;
	private Canvas mDebugCanvas;
	private Paint mDebugPointPaint;
	private Paint mDebugControlPaint;
	private Paint mDebugLinePaint;

	private int mBackgroundColor = Color.TRANSPARENT;

	private int mPenColor = android.R.color.black;
	private float mPenThickness = DEFAULT_MAX_STROKE_WIDTH;
	private boolean isErase = false;

	public int width;
	public int height;

	private float mDensity;
	private float mSmoothingRatio;

	public InkView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// get flags from attributes
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.InkView, defStyle, 0);
		int flags = a.getInt(R.styleable.InkView_inkFlags, DEFAULT_FLAGS);
		a.recycle();

		init(flags);
	}

	public InkView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public InkView(Context context) {
		this(context, DEFAULT_FLAGS);
	}

	public InkView(Context context, int flags) {
		super(context);

		init(flags);
	}

	@Override
	protected void onSizeChanged(int widthSize, int heightSize, int oldw, int oldh) {
		super.onSizeChanged(widthSize, heightSize, oldw, oldh);
		width = widthSize;
		height = heightSize;
		setDefaultCanvas();
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

	public void setMaxStrokeWidth(float width) {
		mMaxStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics());
	}

	/**
	 * Sets the minimum stroke width
	 * 
	 * @param width
	 *            The width (in dp)
	 */
	public void setMinStrokeWidth(float width) {
		mMinStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics());
	}

	@Override
	public synchronized boolean onTouchEventBySync(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		// on down, initialize stroke point
		if (action == MotionEvent.ACTION_DOWN) {
			addPoint(getRecycledPoint(event.getX(), event.getY(), event.getEventTime()));
		} else if (action == MotionEvent.ACTION_MOVE) {
			if (mPointQueue.size() > 0) {
				final BasePen point = mPointQueue.get(mPointQueue.size() - 1);

				if (!point.equals(event.getX(), event.getY())) {
					addPoint(getRecycledPoint(event.getX(), event.getY(), event.getEventTime()));
				}
			}
		}

		// on up, draw remaining queue
		if (action == MotionEvent.ACTION_UP) {
			// draw final points
			if (mPointQueue.size() == 1) {
				draw(mPointQueue.get(0));

			} else if (mPointQueue.size() == 2) {
				mPointQueue.get(1).findControlPoints(mPointQueue.get(0), null);
				draw(mPointQueue.get(0), mPointQueue.get(1));
			}

			// recycle remaining points
			mPointRecycle.addAll(mPointQueue);
			mPointQueue.clear();
		}

		return true;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		// on down, initialize stroke point
		if (action == MotionEvent.ACTION_DOWN) {
			addPoint(getRecycledPoint(event.getX(), event.getY(), event.getEventTime()));
			if (mListener != null) {
				float downX = (x / width);
				float downY = (y / height);
				mListener.onTouchEvent(PacketID.BC_DRAW_GESTURE_DOWN, downX, downY);
			}
		}

		// on move, add next point
		else if (action == MotionEvent.ACTION_MOVE) {
			if (!mPointQueue.get(mPointQueue.size() - 1).equals(event.getX(), event.getY())) {
				addPoint(getRecycledPoint(event.getX(), event.getY(), event.getEventTime()));
				if (mListener != null) {

					float downX = (x / width);
					float downY = (y / height);
					mListener.onTouchEvent(PacketID.BC_DRAW_GESTURE_MOVE, downX, downY);
				}
			}
		}

		// on up, draw remaining queue
		if (action == MotionEvent.ACTION_UP) {
			// draw final points
			if (mPointQueue.size() == 1) {
				draw(mPointQueue.get(0));

			} else if (mPointQueue.size() == 2) {
				mPointQueue.get(1).findControlPoints(mPointQueue.get(0), null);
				draw(mPointQueue.get(0), mPointQueue.get(1));
			}

			if (mListener != null) {
				float downX = (x / width);
				float downY = (y / height);
				mListener.onTouchEvent(PacketID.BC_DRAW_GESTURE_UP, downX, downY);
			}

			// recycle remaining points
			mPointRecycle.addAll(mPointQueue);
			mPointQueue.clear();
		}

		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// simply paint the bitmap on the canvas
		canvas.drawBitmap(mBitmap, 0, 0, null);

		// draw debug layer if it has some data
		if (mHasDebugLayer) {
			canvas.drawBitmap(mDebugBitmap, 0, 0, null);
		}

		super.onDraw(canvas);
	}

	public void setFlags(int flags) {
		mFlags = flags;
	}

	public void addFlags(int flags) {
		mFlags |= flags;
	}

	public void addFlag(int flag) {
		addFlags(flag);
	}

	public void removeFlags(int flags) {
		mFlags &= ~flags;
	}

	public void removeFlag(int flag) {
		removeFlags(flag);
	}

	public boolean hasFlags(int flags) {
		return (mFlags & flags) > 0;
	}

	public boolean hasFlag(int flag) {
		return hasFlags(flag);
	}

	public void clearFlags() {
		mFlags = 0;
	}

	private void init(int flags) {

		// init flags
		setFlags(flags);

		// init screen density
		DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
		mDensity = (metrics.xdpi + metrics.ydpi) / 2f;

		// init paint
		mPaint = new Paint();
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setAntiAlias(true);

		// apply default settings
		setMaxStrokeWidth(mPenThickness);
		setMinStrokeWidth(DEFAULT_MIN_STROKE_WIDTH);
		mSmoothingRatio = Math.max(Math.min(DEFAULT_SMOOTHING_RATIO, 1f), 0f);

		// init debug paint
		mDebugPointPaint = new Paint();
		mDebugPointPaint.setAntiAlias(true);
		mDebugPointPaint.setStyle(Paint.Style.FILL);
		mDebugPointPaint.setColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
		mDebugControlPaint = new Paint();
		mDebugControlPaint.setAntiAlias(true);
		mDebugControlPaint.setStyle(Paint.Style.FILL);
		mDebugControlPaint.setColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
		mDebugLinePaint = new Paint();
		mDebugLinePaint.setAntiAlias(true);
		mDebugLinePaint.setStyle(Paint.Style.STROKE);
		mDebugLinePaint.setColor(getContext().getResources().getColor(android.R.color.darker_gray));

	}

	@Override
	public void startDrawing() {
		// TODO Auto-generated method stub
		isErase = false;
		mPaint = new Paint();
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setAntiAlias(true);

		// apply default settings
		setPenColor(getResources().getColor(android.R.color.black));
		setMaxStrokeWidth(DEFAULT_MAX_STROKE_WIDTH);
		setMinStrokeWidth(DEFAULT_MIN_STROKE_WIDTH);
		mSmoothingRatio = Math.max(Math.min(DEFAULT_SMOOTHING_RATIO, 1f), 0f);

		mListener.onChangeModeTitle();
	}

	@Override
	public void startEraser() {
		// TODO Auto-generated method stub
		isErase = true;

		mPaint = new Paint();
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		// apply default settings
		setPenColor(mBackgroundColor);
		mPaint.setStrokeWidth(50);

		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		mListener.onChangeModeTitle();
	}

	@Override
	public void setDefaultCanvas(int color) {
		// TODO Auto-generated method stub
		if (mBitmap != null) {
			mBitmap.recycle();
		}

		// cleanup debug bitmap
		if (mDebugBitmap != null) {
			mDebugBitmap.recycle();
		}

		Paint paint = new Paint();
		mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mBackgroundColor = color;
		setBackgroundColor(mBackgroundColor);
		paint.setColor(mBackgroundColor);
		paint.setStyle(Paint.Style.FILL);
		mCanvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), paint);

		// init debug bitmap cache
		mDebugBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		mDebugCanvas = new Canvas(mDebugBitmap);
		mHasDebugLayer = false;

		invalidate();
	}

	@Override
	public void setDefaultCanvas() {
		// TODO Auto-generated method stub
		// clean up existing bitmap
		if (mBitmap != null) {
			mBitmap.recycle();
		}

		// cleanup debug bitmap
		if (mDebugBitmap != null) {
			mDebugBitmap.recycle();
		}

		Paint paint = new Paint();
		mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		setBackgroundColor(mBackgroundColor);
		paint.setColor(mBackgroundColor);
		paint.setStyle(Paint.Style.FILL);
		mCanvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), paint);

		// init debug bitmap cache
		mDebugBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		mDebugCanvas = new Canvas(mDebugBitmap);
		mHasDebugLayer = false;

		invalidate();
	}

	@Override
	public void setPenColor(int pPenColor) {
		// TODO Auto-generated method stub
		mPenColor = pPenColor;
		mPaint.setColor(pPenColor);
	}

	@Override
	public void setThickness(int pPenThickness) {
		// TODO Auto-generated method stub
		mPenThickness = pPenThickness;
	}

	@Override
	public float getThickness() {
		// TODO Auto-generated method stub
		return mPenThickness;
	}

	@Override
	public int getPenColor() {
		// TODO Auto-generated method stub
		return mPenColor;
	}

	@Override
	public void setBackgroundColor(int color) {
		// TODO Auto-generated method stub
		mBackgroundColor = color;
	}

	@Override
	public boolean isEraseMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getBackgroundColor() {
		// TODO Auto-generated method stub
		return mBackgroundColor;
	}

	@Override
	public void setPenFilterType(int pType) {

	}

	public Bitmap getBitmap() {
		return getBitmap(mBackgroundColor);
	}

	public Bitmap getBitmap(int backgroundColor) {
		// create new bitmap
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas bitmapCanvas = new Canvas(bitmap);

		// draw background if not transparent
		if (backgroundColor != 0) {
			bitmapCanvas.drawColor(backgroundColor);
		}

		// draw bitmap
		bitmapCanvas.drawBitmap(mBitmap, 0, 0, null);

		return bitmap;
	}

	public void drawBitmap(Bitmap bitmap, float x, float y, Paint paint) {
		mCanvas.drawBitmap(bitmap, x, y, paint);

		invalidate();
	}

	private void addPoint(BasePen p) {
		mPointQueue.add(p);

		int queueSize = mPointQueue.size();
		if (queueSize == 1) {
			// compute starting velocity
			int recycleSize = mPointRecycle.size();
			p.velocity = (recycleSize > 0) ? mPointRecycle.get(recycleSize - 1).velocityTo(p) / 2f : 0f;

			// compute starting stroke width
			mPaint.setStrokeWidth(computeStrokeWidth(p.velocity));
		}
		if (queueSize == 2) {
			BasePen p0 = mPointQueue.get(0);

			// compute velocity for new point
			p.velocity = p0.velocityTo(p);

			// re-compute velocity for 1st point (predictive velocity)
			p0.velocity = p0.velocity + p.velocity / 2f;

			// find control points for first point
			p0.findControlPoints(null, p);

			// update starting stroke width
			mPaint.setStrokeWidth(computeStrokeWidth(p0.velocity));
		} else if (queueSize == 3) {
			BasePen p0 = mPointQueue.get(0);
			BasePen p1 = mPointQueue.get(1);

			// find control points for second point
			p1.findControlPoints(p0, p);

			// compute velocity for new point
			p.velocity = p1.velocityTo(p);

			// draw geometry between first 2 points
			draw(p0, p1);

			// recycle 1st point
			mPointRecycle.add(mPointQueue.remove(0));
		}
	}

	private BasePen getRecycledPoint(float x, float y, long time) {
		if (mPointRecycle.size() == 0) {
			return new InkPen(x, y, time, mDensity, mSmoothingRatio);
		}

		return mPointRecycle.remove(0).reset(x, y, time);
	}

	private float computeStrokeWidth(float velocity) {
		// compute responsive width
		if (hasFlags(FLAG_RESPONSIVE_WIDTH)) {
			return mMaxStrokeWidth - (mMaxStrokeWidth - mMinStrokeWidth) * Math.min(velocity / THRESHOLD_VELOCITY, 1f);
		}

		return mMaxStrokeWidth;
	}

	private void draw(BasePen p) {
		mPaint.setStyle(Paint.Style.FILL);

		// draw dot
		mCanvas.drawCircle(p.x, p.y, mPaint.getStrokeWidth() / 2f, mPaint);

		invalidate();
	}

	private void draw(BasePen p1, BasePen p2) {
		mPaint.setStyle(Paint.Style.STROKE);

		// adjust low-pass ratio from changing acceleration
		// using comfortable range of 0.2 -> 0.3 approx.
		float acceleration = Math.abs((p2.velocity - p1.velocity) / (p2.time - p1.time)); // in/s^2
		float filterRatio = Math.min(FILTER_RATIO_MIN + FILTER_RATIO_ACCEL_MOD * acceleration / THRESHOLD_ACCELERATION, 1f);

		// compute new stroke width
		float desiredWidth = computeStrokeWidth(p2.velocity);
		float startWidth = mPaint.getStrokeWidth();

		float endWidth = filterRatio * desiredWidth + (1f - filterRatio) * startWidth;
		float deltaWidth = endWidth - startWidth;

		// interpolate bezier curve
		if (hasFlags(FLAG_INTERPOLATION)) {

			// compute # of steps to interpolate in the bezier curve
			int steps = (int) (Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2)) / 5);

			// computational setup for differentials used to interpolate the
			// bezier curve
			float u = 1f / (steps + 1);
			float uu = u * u;
			float uuu = u * u * u;

			float pre1 = 3f * u;
			float pre2 = 3f * uu;
			float pre3 = 6f * uu;
			float pre4 = 6f * uuu;

			float tmp1x = p1.x - p1.c2x * 2f + p2.c1x;
			float tmp1y = p1.y - p1.c2y * 2f + p2.c1y;
			float tmp2x = (p1.c2x - p2.c1x) * 3f - p1.x + p2.x;
			float tmp2y = (p1.c2y - p2.c1y) * 3f - p1.y + p2.y;

			float dx = (p1.c2x - p1.x) * pre1 + tmp1x * pre2 + tmp2x * uuu;
			float dy = (p1.c2y - p1.y) * pre1 + tmp1y * pre2 + tmp2y * uuu;
			float ddx = tmp1x * pre3 + tmp2x * pre4;
			float ddy = tmp1y * pre3 + tmp2y * pre4;
			float dddx = tmp2x * pre4;
			float dddy = tmp2y * pre4;

			float x1 = p1.x;
			float y1 = p1.y;
			float x2, y2;

			// iterate over each step and draw the curve
			int i = 0;
			while (i++ < steps) {
				x2 = x1 + dx;
				y2 = y1 + dy;

				mPaint.setStrokeWidth(startWidth + deltaWidth * i / steps);
				mCanvas.drawLine(x1, y1, x2, y2, mPaint);

				x1 = x2;
				y1 = y2;
				dx += ddx;
				dy += ddy;
				ddx += dddx;
				ddy += dddy;
			}

			mPaint.setStrokeWidth(endWidth);
			mCanvas.drawLine(x1, y1, p2.x, p2.y, mPaint);
		}
		// no interpolation, draw line between points
		else {
			mCanvas.drawLine(p1.x, p1.y, p2.x, p2.y, mPaint);
			mPaint.setStrokeWidth(endWidth);
		}

		// draw debug layer
		if (hasFlags(FLAG_DEBUG)) {

			// draw control points if interpolating
			if (hasFlags(FLAG_INTERPOLATION)) {
				float controlRadius = mMaxStrokeWidth / 3f;

				mDebugCanvas.drawLine(p1.c1x, p1.c1y, p1.c2x, p1.c2y, mDebugLinePaint);
				mDebugCanvas.drawLine(p2.c1x, p2.c1y, p2.c2x, p2.c2y, mDebugLinePaint);
				mDebugCanvas.drawCircle(p1.c1x, p1.c1y, controlRadius, mDebugControlPaint);
				mDebugCanvas.drawCircle(p1.c2x, p1.c2y, controlRadius, mDebugControlPaint);
				mDebugCanvas.drawCircle(p2.c1x, p2.c1y, controlRadius, mDebugControlPaint);
				mDebugCanvas.drawCircle(p2.c2x, p2.c2y, controlRadius, mDebugControlPaint);
			}

			float pointRadius = mMaxStrokeWidth / 1.5f;

			mDebugCanvas.drawCircle(p1.x, p1.y, pointRadius, mDebugPointPaint);
			mDebugCanvas.drawCircle(p2.x, p2.y, pointRadius, mDebugPointPaint);

			mHasDebugLayer = true;
		}

		invalidate();
	}

	// public class InkPoint {
	// public float x, y, c1x, c1y, c2x, c2y, velocity;
	// public long time;
	//
	// public InkPoint() {
	// }
	//
	// public InkPoint(float x, float y, long time) {
	// reset(x, y, time);
	// }
	//
	// public InkPoint reset(float x, float y, long time) {
	// this.x = x;
	// this.y = y;
	// this.time = time;
	// velocity = 0f;
	//
	// c1x = x;
	// c1y = y;
	// c2x = x;
	// c2y = y;
	//
	// return this;
	// }
	//
	// public boolean equals(InkPoint p) {
	// return equals(p.x, p.y);
	// }
	//
	// public boolean equals(float x, float y) {
	// return this.x == x && this.y == y;
	// }
	//
	// public float distanceTo(InkPoint p) {
	// float dx = p.x - x;
	// float dy = p.y - y;
	//
	// return (float) Math.sqrt(dx * dx + dy * dy);
	// }
	//
	// public float velocityTo(InkPoint p) {
	// return (1000f * distanceTo(p)) / (Math.abs(p.time - time) *
	// getDensity()); // in/s
	// }
	//
	// public void findControlPoints(InkPoint prev, InkPoint next) {
	// if (prev == null && next == null) {
	// return;
	// }
	//
	// float r = getSmoothingRatio();
	//
	// // if start of a stroke, c2 control points half-way between this and
	// // next point
	// if (prev == null) {
	// c2x = x + r * (next.x - x) / 2f;
	// c2y = y + r * (next.y - y) / 2f;
	// return;
	// }
	//
	// // if end of a stroke, c1 control points half-way between this and
	// // prev point
	// if (next == null) {
	// c1x = x + r * (prev.x - x) / 2f;
	// c1y = y + r * (prev.y - y) / 2f;
	// return;
	// }
	//
	// // init control points
	// c1x = (x + prev.x) / 2f;
	// c1y = (y + prev.y) / 2f;
	// c2x = (x + next.x) / 2f;
	// c2y = (y + next.y) / 2f;
	//
	// // calculate control offsets
	// float len1 = distanceTo(prev);
	// float len2 = distanceTo(next);
	// float k = len1 / (len1 + len2);
	// float xM = c1x + (c2x - c1x) * k;
	// float yM = c1y + (c2y - c1y) * k;
	// float dx = x - xM;
	// float dy = y - yM;
	//
	// // inverse smoothing ratio
	// r = 1f - r;
	//
	// // translate control points
	// c1x += dx + r * (xM - c1x);
	// c1y += dy + r * (yM - c1y);
	// c2x += dx + r * (xM - c2x);
	// c2y += dy + r * (yM - c2y);
	// }
	// }

}
