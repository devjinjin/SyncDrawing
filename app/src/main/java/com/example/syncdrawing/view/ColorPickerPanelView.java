package com.example.syncdrawing.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ColorPickerPanelView extends View {

	/**
	 * The width in pixels of the border surrounding the color panel.
	 */
	private final static float BORDER_WIDTH_PX = 1;

	private float mDensity = 1f;

	private int mBorderColor = 0xff6E6E6E;
	private int mColor = 0xff000000;

	private Paint mBorderPaint;
	private Paint mColorPaint;

	private RectF mDrawingRect;
	private RectF mColorRect;

	private AlphaPatternDrawable mAlphaPattern;

	public ColorPickerPanelView(Context pContext) {
		this(pContext, null);
	}

	public ColorPickerPanelView(Context pContext, AttributeSet pAttrs) {
		this(pContext, pAttrs, 0);
	}

	public ColorPickerPanelView(Context pContext, AttributeSet pAttrs,
			int pDefStyle) {
		super(pContext, pAttrs, pDefStyle);
		init();
	}

	private void init() {
		mBorderPaint = new Paint();
		mColorPaint = new Paint();
		mDensity = getContext().getResources().getDisplayMetrics().density;
	}

	@Override
	protected void onDraw(Canvas pCanvas) {

		final RectF rect = mColorRect;

		if (BORDER_WIDTH_PX > 0) {
			mBorderPaint.setColor(mBorderColor);
			pCanvas.drawRect(mDrawingRect, mBorderPaint);
		}

		if (mAlphaPattern != null) {
			mAlphaPattern.draw(pCanvas);
		}

		mColorPaint.setColor(mColor);

		pCanvas.drawRect(rect, mColorPaint);
	}

	@Override
	protected void onMeasure(int pWidthMeasureSpec, int pHeightMeasureSpec) {

		final int width = MeasureSpec.getSize(pWidthMeasureSpec);
		final int height = MeasureSpec.getSize(pHeightMeasureSpec);

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onSizeChanged(int pW, int pH, int pOldw, int pOldh) {
		super.onSizeChanged(pW, pH, pOldw, pOldh);

		mDrawingRect = new RectF();
		mDrawingRect.left = getPaddingLeft();
		mDrawingRect.right = pW - getPaddingRight();
		mDrawingRect.top = getPaddingTop();
		mDrawingRect.bottom = pH - getPaddingBottom();

		setUpColorRect();
	}

	private void setUpColorRect() {
		final RectF dRect = mDrawingRect;

		final float left = dRect.left + BORDER_WIDTH_PX;
		final float top = dRect.top + BORDER_WIDTH_PX;
		final float bottom = dRect.bottom - BORDER_WIDTH_PX;
		final float right = dRect.right - BORDER_WIDTH_PX;

		mColorRect = new RectF(left, top, right, bottom);

		mAlphaPattern = new AlphaPatternDrawable((int) (5 * mDensity));

		mAlphaPattern.setBounds(Math.round(mColorRect.left),
				Math.round(mColorRect.top), Math.round(mColorRect.right),
				Math.round(mColorRect.bottom));

	}

	/**
	 * Set the color that should be shown by this view.
	 * 
	 * @param color
	 */
	public void setColor(int pColor) {
		mColor = pColor;
		invalidate();
	}

	/**
	 * Get the color currently show by this view.
	 * 
	 * @return
	 */
	public int getColor() {
		return mColor;
	}

	/**
	 * Set the color of the border surrounding the panel.
	 * 
	 * @param color
	 */
	public void setBorderColor(int pColor) {
		mBorderColor = pColor;
		invalidate();
	}

	/**
	 * Get the color of the border surrounding the panel.
	 */
	public int getBorderColor() {
		return mBorderColor;
	}

}
