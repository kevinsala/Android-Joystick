package com.example.joystick;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnTouchListener {
	
	/* Joystick size */
	private static final int IN_JOYSTICK_SIZE = 80;
	private static final int EX_JOYSTICK_SIZE = 150;
	
	/* Screen size */
	private int screenWidth;
	private int screenHeight;
	
	/* Visual elements */
	private TextView degreeTextView;
	private ImageView externalJoystickView;
	private ImageView internalJoystickView;
	
	/* Joystick attributes */
	private int joystickDefaultX;
	private int joystickDefaultY;
	private boolean started;
	private double degree;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		/* Set onTouchListener (look at activity_main.xml layout) */
		RelativeLayout mFrame = (RelativeLayout) findViewById(R.id.frame);
		mFrame.setOnTouchListener(this);
		
		degreeTextView = (TextView) findViewById(R.id.degreeText);
		degreeTextView.setText("Degree: -");
		
		/* Get the screen size */
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		screenWidth = point.x;
		screenHeight = point.y;
		
		initControls();
	}
	
	private void initControls() {
		/* Joystick configuration */
		externalJoystickView = (ImageView) findViewById(R.id.externalJoystickView);
		externalJoystickView.setLayoutParams(new RelativeLayout.LayoutParams(EX_JOYSTICK_SIZE, EX_JOYSTICK_SIZE));
		externalJoystickView.setX(screenWidth/2 - EX_JOYSTICK_SIZE/2);
		externalJoystickView.setY(screenHeight/2 - EX_JOYSTICK_SIZE/2);
		
		joystickDefaultX = screenWidth/2 - IN_JOYSTICK_SIZE/2;
		joystickDefaultY = screenHeight/2 - IN_JOYSTICK_SIZE/2;
		started = false;
		
		internalJoystickView = (ImageView) findViewById(R.id.internalJoystickView);
		internalJoystickView.setLayoutParams(new RelativeLayout.LayoutParams(IN_JOYSTICK_SIZE, IN_JOYSTICK_SIZE));
		internalJoystickView.setX(joystickDefaultX);
		internalJoystickView.setY(joystickDefaultY);
	}
	
	private boolean isInsideJoystick(float x, float y) {
		float xCenter = joystickDefaultX + IN_JOYSTICK_SIZE/2, yCenter = joystickDefaultY + IN_JOYSTICK_SIZE/2;
		return (Math.sqrt(Math.pow(Math.abs(xCenter - x),2) + Math.pow(Math.abs(yCenter - y),2)) <= EX_JOYSTICK_SIZE/2);
	}
	
	private double getDegree(float px, float py) {
		float xCentre = screenWidth/2;
		float yCentre = screenHeight/2;
		float dx = Math.abs(xCentre - px);
		float dy = Math.abs(yCentre - py);
		
		double degree = Math.atan(dy/dx);
		if (px < xCentre && py <= yCentre) return Math.PI - degree;
		else if (px < xCentre && py > yCentre) return Math.PI + degree;
		else if (px >= xCentre && py > yCentre) return 2*Math.PI - degree;
		else return degree;
	}
	
	private PointF getIntersectionPoint(float px, float py) {
		float radius = EX_JOYSTICK_SIZE/2;
		float xCentre = screenWidth/2;
		float yCentre = screenHeight/2;
		
		PointF point = new PointF();
		double degree = getDegree(px, py);
		point.x = xCentre + ((float) Math.cos(degree) * radius);
		point.y = yCentre - ((float) Math.sin(degree) * radius);
		return point;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean isInside = isInsideJoystick(event.getX(),event.getY());
		int action = event.getAction();
		
		if (action == MotionEvent.ACTION_DOWN) {
			if (isInside) {
				internalJoystickView.setX(event.getX() - IN_JOYSTICK_SIZE/2);
				internalJoystickView.setY(event.getY() - IN_JOYSTICK_SIZE/2);
				degree = getDegree(event.getX(), event.getY());
				started = true;
			}
		}
		else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			internalJoystickView.setX(joystickDefaultX);
			internalJoystickView.setY(joystickDefaultY);
			started = false;
		}
		else if (started && action == MotionEvent.ACTION_MOVE) {
			if (isInside) {
				internalJoystickView.setX(event.getX() - IN_JOYSTICK_SIZE/2);
				internalJoystickView.setY(event.getY() - IN_JOYSTICK_SIZE/2);
			}
			else {
				PointF pointF = getIntersectionPoint(event.getX(), event.getY());
				internalJoystickView.setX(pointF.x - IN_JOYSTICK_SIZE/2);
				internalJoystickView.setY(pointF.y - IN_JOYSTICK_SIZE/2);
			}
			degree = getDegree(event.getX(), event.getY());
		}
		
		if (started) degreeTextView.setText("Degree: " + String.format("%.2f", Math.toDegrees(degree)));
		else degreeTextView.setText("Degree: -");
		return true;
	}
}
