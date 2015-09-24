package com.example.syncdrawing.pen;

public abstract class BasePen {
	public float x, y, c1x, c1y, c2x, c2y, velocity;
	public long time;
	protected float mDensity;
	protected float mSmoothingRatio;

	public abstract float velocityTo(BasePen p);
	public abstract void findControlPoints(BasePen prev, BasePen next);
	
	public BasePen reset(float x, float y, long time) {
		this.x = x;
		this.y = y;
		this.time = time;
		velocity = 0f;

		c1x = x;
		c1y = y;
		c2x = x;
		c2y = y;

		return this;
	}
	
	public boolean equals(BasePen p) {
		return equals(p.x, p.y);
	}

	public boolean equals(float x, float y) {
		return this.x == x && this.y == y;
	}
	
	public float distanceTo(BasePen p) {
		float dx = p.x - x;
		float dy = p.y - y;

		return (float) Math.sqrt(dx * dx + dy * dy);
	}
}
