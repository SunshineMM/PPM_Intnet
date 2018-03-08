package com.example.npttest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ViewfinderViewBaidu extends View {

	private Paint paint;
	private Paint paintLine;
	private int width, height;
	private int minLength = 0;
	public int length;
	public int top, bottom, left, right;
	public Rect frame;

	public ViewfinderViewBaidu(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paintLine = new Paint();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		
		super.onDraw(canvas);

		width = canvas.getWidth();
		height = canvas.getHeight();
		
		
		if(width <= height){
			minLength = width;
		}else{
			minLength = height;
		}
		
		if(minLength<1080||minLength>1620){
			length = minLength/4;
		}else{
			length = 250;
		}
		
		left = width/2-length;
		right = width/2+length;
		top = height/2-length;
		bottom = height/2+length;
		frame = new Rect(left, top, right, bottom);
		
		paint.setColor(Color.argb(128, 0, 0, 0));
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);
		paintLine.setColor(Color.rgb(0, 255, 0));
		paintLine.setStrokeWidth(4);
		paintLine.setAntiAlias(true);
		canvas.drawLine(left, top, left +50, top, paintLine);
		canvas.drawLine(left, top, left, top + 50, paintLine);
		canvas.drawLine(right, top, right - 50, top, paintLine);
		canvas.drawLine(right, top, right, top + 50, paintLine);
		canvas.drawLine(left, bottom, left + 50, bottom, paintLine);
		canvas.drawLine(left, bottom, left, bottom - 50, paintLine);
		canvas.drawLine(right, bottom, right - 50, bottom, paintLine);
		canvas.drawLine(right, bottom, right, bottom - 50, paintLine);
	}

}
