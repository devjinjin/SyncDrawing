package com.example.syncdrawing.view;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class AlphaPatternDrawable extends Drawable {

	private int mRectangleSize = 10;

	private Paint mPaint = new Paint();
	private Paint mPaintWhite = new Paint();
	private Paint mPaintGray = new Paint();

	private int mNumRectanglesHorizontal;
	private int mNumRectanglesVertical;

	/**
	 * Bitmap in which the pattern will be cahched.
	 */
	private Bitmap mBitmap;

	public AlphaPatternDrawable(int pRectangleSize) {
		mRectangleSize = pRectangleSize;
		mPaintWhite.setColor(0xffffffff);
		mPaintGray.setColor(0xffcbcbcb);
	}

	@Override
	public void draw(Canvas pCanvas) {
		if(mBitmap!=null){
			pCanvas.drawBitmap(mBitmap, null, getBounds(), mPaint);
		}
	}

	@Override
	public int getOpacity() {
		return 0;
	}

	@Override
	public void setAlpha(int pAlpha) {
		throw new UnsupportedOperationException(
				"Alpha is not supported by this drawwable.");
	}

	@Override
	public void setColorFilter(ColorFilter pCf) {
		throw new UnsupportedOperationException(
				"ColorFilter is not supported by this drawwable.");
	}

	@Override
	protected void onBoundsChange(Rect pBounds) {
		super.onBoundsChange(pBounds);
		final int height = pBounds.height();
		final int width = pBounds.width();

		mNumRectanglesHorizontal = (int) Math.ceil((width / mRectangleSize));
		mNumRectanglesVertical = (int) Math.ceil(height / mRectangleSize);

		generatePatternBitmap();
	}

	/**
	 * This will generate a bitmap with the pattern as big as the rectangle we
	 * were allow to draw on. We do this to chache the bitmap so we don't need
	 * to recreate it each time draw() is called since it takes a few
	 * milliseconds.
	 */
	private void generatePatternBitmap() {

		if (getBounds().width() <= 0 || getBounds().height() <= 0) {
			return;
		}

		mBitmap = Bitmap.createBitmap(getBounds().width(),
				getBounds().height(), Config.ARGB_8888);
		final Canvas canvas = new Canvas(mBitmap);

		final Rect r = new Rect();
		boolean verticalStartWhite = true;
		for (int i = 0; i <= mNumRectanglesVertical; i++) {

			boolean isWhite = verticalStartWhite;
			for (int j = 0; j <= mNumRectanglesHorizontal; j++) {

				r.top = i * mRectangleSize;
				r.left = j * mRectangleSize;
				r.bottom = r.top + mRectangleSize;
				r.right = r.left + mRectangleSize;

				canvas.drawRect(r, isWhite ? mPaintWhite : mPaintGray);

				isWhite = !isWhite;
			}
			verticalStartWhite = !verticalStartWhite;
		}
	}
}
