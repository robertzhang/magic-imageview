package com.robert.imageview;

import java.security.acl.LastOwnerException;

import com.robert.wrapper.MagicImageView;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;

public class ZoomImageView extends MagicImageView implements OnScaleGestureListener, OnTouchListener, 
			ViewTreeObserver.OnGlobalLayoutListener{

	//获取类名称作为log标签
	private static final String LOG_TAG = ZoomImageView.class.getSimpleName();
	
	//尺寸的最大值
	public static final float SCALE_MAX = 4.0f;
	
	//初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
	private float initScale = 1.0f;
	
	private final float[] matrixValues = new float[9];
	
	private boolean once = true;
	
	//缩放的手势检测
	private ScaleGestureDetector mScaleGestureDetector = null;
	
	private GestureDetector mGestureDetector = null;
	
	private final Matrix mScaleMatrix = new Matrix();
	
	private boolean isAutoScale = false;
	
	public ZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setScaleType(ScaleType.MATRIX);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		mGestureDetector = new GestureDetector(context, new SimpleOnGestureListener());
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		this.setOnTouchListener(this);
	}

	private class SimpleOnGestureListener implements OnGestureListener{
		public boolean onDoubleTap(MotionEvent e){
			if (isAutoScale == true)
				return true;
			
			float x = e.getX();
			float y = e.getY();
			if (getScale() < SCALE_MAX){
				ZoomImageView.this.postDelayed(new AutoScaleRunnable(SCALE_MAX, x, y), 16);
				isAutoScale = true;
			}else{
				ZoomImageView.this.postDelayed(new AutoScaleRunnable(initScale, x, y), 16);
				isAutoScale = true;
			}
			return true;
		}

		@Override
		public boolean onDown(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onLongPress(MotionEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onShowPress(MotionEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	private class AutoScaleRunnable implements Runnable{
		static final float BIGGER = 1.05f;
		static final float SMALLER = 0.95f;
		
		private float mTargetScale;
		private float tempScale;
		private float x;
		private float y;
		
		public AutoScaleRunnable(float targetScale, float x, float y){
			this.mTargetScale = targetScale;
			this.x = x;
			this.y = y;
			if (getScale() < mTargetScale){
				tempScale = BIGGER;
			}else{
				tempScale = SMALLER;
			}
		}
		
		@Override
		public void run() {
			mScaleMatrix.postSkew(tempScale, tempScale, x, y);
			checkBorderAndCenterWhenScale();
			setImageMatrix(mScaleMatrix);
			
			final float currentScale = getScale();
			if ((tempScale > 1f && currentScale < mTargetScale) || (tempScale < 1f && mTargetScale < currentScale)){
				ZoomImageView.this.postDelayed(this, 16);
			}else{
				final float deltaScale = mTargetScale / currentScale;
				mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
				checkBorderAndCenterWhenScale();
				setImageMatrix(mScaleMatrix);
				isAutoScale = false;
			}
			
		}
		
	}
	
	int lastPointerCount = 0;
	float mLastX = 0, mLastY = 0;
	boolean isCanDrag = false;
	boolean isCheckTopAndBottom = false;
	boolean isCheckLeftAndRight = false;
	float mTouchSlop = 0;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//return mScaleGestureDetector.onTouchEvent(event);//只能让图片的中心在中间
		if (mScaleGestureDetector.onTouchEvent(event))
			return true;
		
		float x = 0, y = 0;
		final int pointerCount = event.getPointerCount();//获取触摸点个数
		//获取几个触摸点的中心点
		for (int i = 0; i < pointerCount; i++){
			x += event.getX();
			y += event.getY();
		}
		x = x / pointerCount;
		y = y / pointerCount;
		
		//触摸点发生变化，重置mlastx，mLasty
		if (pointerCount != lastPointerCount){
			isCanDrag = false;
			mLastX = x;
			mLastY = y;
		}
		
		lastPointerCount = pointerCount;
		
		switch(event.getAction()){
		case MotionEvent.ACTION_MOVE:
			float dx = x - mLastX;
			float dy = y - mLastY;
			
			if (!isCanDrag){
				isCanDrag = isCanDrag(dx, dy);
			}
			if (isCanDrag){
				RectF rectF = getMatrixRectF();
				if (getDrawable() != null){
					isCheckLeftAndRight = isCheckTopAndBottom = true;
					if (rectF.width() < getWidth()){
						dx = 0;
						isCheckLeftAndRight = false;
					}
					if (rectF.height() < getHeight()){
						dy = 0;
						isCheckTopAndBottom = false;
					}
					mScaleMatrix.postTranslate(dx, dy);
					checkMatrixBounds();
					setImageMatrix(mScaleMatrix);
				}
			}
			mLastX = x;
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			lastPointerCount = 0;
		}
		
		return true;
	}

	/**
	 * 移动时进行边界判断，判断宽或者高大于屏幕
	 */
	private void checkMatrixBounds(){
		RectF rect = getMatrixRectF();
		
		float deltaX = 0, deltaY = 0;
		final float viewHeight = getHeight();
		final float viewWidth = getWidth();
		//判断移动或者缩放后，图片显示是否超出屏幕边界
		if (rect.top > 0 && isCheckTopAndBottom){
			deltaY = -rect.top;
		}
		if (rect.bottom < viewHeight && isCheckTopAndBottom){
			deltaY = viewHeight - rect.bottom;
		}
		if (rect.left > 0 && isCheckLeftAndRight){
			deltaX = -rect.left;
		}
		if (rect.right < viewWidth && isCheckLeftAndRight){
			deltaX = viewWidth - rect.right;
		}
		mScaleMatrix.postTranslate(deltaX, deltaY);
	}
	
	/**
	 * 判断是否需要拖动
	 */
	private boolean isCanDrag(float dx, float dy){
		return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
	}
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale();
		/*
		 * 返回从前一个伸缩事件至当前伸缩事件的伸缩比率。该值定义为 (getCurrentSpan() / getPreviousSpan())，
		 * getCurrentSpan():用于返回当前手势的两个触点的距离；
		 * getPreviousSpan():返回手势过程中，组成该手势的两个触点的前一次距离
		 */
		float scaleFactor = detector.getScaleFactor();
		
		if (getDrawable() == null){
			return true;
		}
		
		//缩放范围控制
		if ((scale<SCALE_MAX && scaleFactor > 1.0f) || (scale > initScale && scaleFactor < 1.0f)){
			if (scaleFactor * scale < initScale){
				scaleFactor = initScale / scale;
			}
			if (scaleFactor * scale > SCALE_MAX){
				scaleFactor = SCALE_MAX / scale;
			}
//			mScaleMatrix.postScale(scaleFactor, scaleFactor, getWidth() / 2, getHeight() /2);//中心在屏幕中间
			mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
			checkBorderAndCenterWhenScale();
			setImageMatrix(mScaleMatrix);
		}
		return true;
	}
	
	public final float getScale(){
		mScaleMatrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
	}

	/**
	 * 控制缩放时图片的显示范围
	 */
	private void checkBorderAndCenterWhenScale(){
		RectF rect = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;
		
		int width = getWidth();
		int height = getHeight();
		
		if (rect.width() >= width){
			if (rect.left > 0){
				deltaX = -rect.left;
			}
			if (rect.right < width){
				deltaX = width - rect.right;
			}
		}
		if (rect.height() >= height)  
        {  
            if (rect.top > 0)  
            {  
                deltaY = -rect.top;  
            }  
            if (rect.bottom < height)  
            {  
                deltaY = height - rect.bottom;  
            }  
        }  
        // 如果宽或高小于屏幕，则让其居中  
        if (rect.width() < width)  
        {  
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();  
        }  
        if (rect.height() < height)  
        {  
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();  
        }  
        mScaleMatrix.postTranslate(deltaX, deltaY);  
	}
	
	/**
	 * 根据当前图片的Matrix获取图片的范围
	 */
	private RectF getMatrixRectF(){
		Matrix matrix = mScaleMatrix;
		RectF rect = new RectF();
		Drawable d = getDrawable();
		if (null != d){
			rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			matrix.mapRect(rect);
		}
		return rect;
	}
	
	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	@Override
	public void onGlobalLayout() {
		// TODO Auto-generated method stub
		if (once){
			Drawable d = getDrawable();
			if (d == null){
				return;
			}
			//获取屏幕宽和高
			int width = getWidth();
			int height = getHeight();
			//获取图片宽和高
			int dw = d.getIntrinsicWidth();
			int dh = d.getIntrinsicHeight();
			float scale = 1.0f;
			if (dw > width && dh <= height){
				scale = width * 1.0f / dw;
			} else if (dh > height && dw <= width){
				scale = height * 1.0f / dh;
			} else if (dw > width && dh > height){	
				scale = Math.min(dw * 1.0f / width, dh * 1.0f / height);
			}
			initScale = scale;
			//屏幕移植中间
			mScaleMatrix.postScale(scale, scale, getWidth()/2, getHeight()/2);
			setImageMatrix(mScaleMatrix);
			once = false;
		}
	}
	
	



}
