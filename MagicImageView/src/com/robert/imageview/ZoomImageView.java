package com.robert.imageview;

import com.robert.wrapper.MagicImageView;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
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
	
	private final Matrix mScaleMatrix = new Matrix();
	
	public ZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setScaleType(ScaleType.MATRIX);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		this.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mScaleGestureDetector.onTouchEvent(event);
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
