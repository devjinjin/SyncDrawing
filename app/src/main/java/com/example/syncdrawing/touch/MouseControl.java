package com.example.syncdrawing.touch;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;

import com.example.syncdrawing.packet.PacketID;
import com.example.syncdrawing.view.NormalDrawingView;

import java.util.concurrent.LinkedBlockingQueue;

public class MouseControl {
	private static String TAG = MouseControl.class.getSimpleName();
	public boolean mIsRunning = false;
	private LinkedBlockingQueue<TouchObject> mQueue = new LinkedBlockingQueue<TouchObject>();
	private TouchThread mThread;
	public int mFrame = 0;
	private NormalDrawingView mTouchView;

	public MouseControl(Context pContext, NormalDrawingView pTouchView) {
		mThread = new TouchThread();
		mThread.setPriority(Thread.NORM_PRIORITY);
		mIsRunning = true;
		mThread.start();
		mTouchView = pTouchView;
	}

	// Touch 포인트 발생시 Queue에 이벤트 객체 input
	public synchronized void setTouchAction(short packId, long eventTime, PointF pXY, int pColor, int pThickness) {
		try {
			mQueue.put(new TouchObject(eventTime, packId, pXY, pColor, pThickness));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void destroy() {
		mIsRunning = false;
		mThread.interrupt();
		mQueue.clear();
		mQueue = null;
	}

	// Queue에 입력된 마우스 이벤트를 꺼내 연결된 HID로 보내기 위한 Thread
	private class TouchThread extends Thread {

		public TouchThread() {

		}

		// 루프를 돌면서 Que에 값이 있을 경우 하나씩 처리 요청을 한다
		@Override
		public void run() {
			while (mIsRunning) {
				if (mTouchView != null && mQueue != null && !mQueue.isEmpty()) {
					final TouchObject touch = mQueue.poll();
					Message msg = Message.obtain();
					msg.what = touch.mEvent;
					msg.obj = touch;
					handler.sendMessage(msg);
				}
			}
		}

	}

	private Handler handler = new Handler() {
		float lastX = 0;
		float lastY = 0;
		// long beforeEventTime = 0;
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.obj.getClass().equals(TouchObject.class)) {
				TouchObject obj = ((TouchObject) msg.obj);
				if (obj != null) {
					switch (msg.what) {
						case PacketID.BC_DRAW_GESTURE_DOWN : // up
							sendTouchDownEvent(obj);
							break;
						case PacketID.BC_DRAW_GESTURE_MOVE : // up
							sendTouchMoveEvent(obj);
							break;
						case PacketID.BC_DRAW_GESTURE_UP : // up
							sendTouchUpEvent(obj);
							break;
						default :
							break;
					}
				}
			}
		}

		public void sendTouchMoveEvent(TouchObject touch) {
			long downTime = touch.mEventTime;
			long eventTime = touch.mEventTime;

			// if (beforeEventTime != touch.mEventTime) {
			PointF currentPoint = touch.mPoint;
			// beforeEventTime = eventTime;
			MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, currentPoint.x, currentPoint.y, 0);

			if (mTouchView != null) {
				mTouchView.setPenColor(touch.mPenColor);
				mTouchView.setThickness(touch.mPenThickness);
				mTouchView.onTouchEventBySync(motionEvent);
			}

			lastX = touch.mPoint.x;
			lastY = touch.mPoint.y;
			// }
		}
		public void sendTouchDownEvent(TouchObject touch) {

			long downTime = touch.mEventTime;
			long eventTime = touch.mEventTime;

			PointF currentPoint = touch.mPoint;
			MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, currentPoint.x, currentPoint.y, 0);

			if (mTouchView != null) {
				if (mTouchView.isEraseMode()) {

					mTouchView.startDrawing();
				}
				mTouchView.setPenColor(touch.mPenColor);
				mTouchView.setThickness(touch.mPenThickness);
				mTouchView.onTouchEventBySync(motionEvent);
			}

			lastX = touch.mPoint.x;
			lastY = touch.mPoint.y;
		}

		public void sendTouchUpEvent(TouchObject touch) {

			long downTime = touch.mEventTime;
			long eventTime = touch.mEventTime;
			// beforeEventTime = 0;

			MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, lastX, lastY, 0);

			if (mTouchView != null) {
				mTouchView.setPenColor(touch.mPenColor);
				mTouchView.setThickness(touch.mPenThickness);
				mTouchView.onTouchEventBySync(motionEvent);
			}
			lastX = 0;
			lastY = 0;
		}
	};
}
