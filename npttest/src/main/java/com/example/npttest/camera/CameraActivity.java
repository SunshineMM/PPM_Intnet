package com.example.npttest.camera;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.npttest.R;
import com.example.npttest.util.CameraUtils;
import com.example.npttest.view.ViewfinderView;
import com.ice.entity.PlateRecognitionParameter;
import com.ice.iceplate.RecogService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity implements SurfaceHolder.Callback,
		Camera.PreviewCallback, AnimationListener {
	private Camera camera;
	private SurfaceView surfaceView;
	private static final String PATH = Environment
			.getExternalStorageDirectory().toString() + "/DCIM/PlatePic/";
	private ImageButton back_btn, flash_btn, back, take_pic;
	private ViewfinderView myview;
	private int width, height;
	private TimerTask timer;
	private int preWidth = 0;
	private int preHeight = 0;
	private String number = "", color = "";
	private boolean isFatty = false;
	private SurfaceHolder holder;
	private int iInitPlateIDSDK = -1;
	private String[] fieldvalue = new String[10];
	private int rotation = 90;
	private static int tempUiRot = 0;
	private Bitmap bitmap1;
	private Vibrator mVibrator;
	private PlateRecognitionParameter prp = new PlateRecognitionParameter();;
	private byte[] tempData;
	private boolean isSuccess = false;
	private Animation scaleAnimation;
	private boolean isAnimationEnd = false;

	private boolean isCamera = true;// ???????????????????? true:?????? false:???????
	private boolean recogType = true;// ????????????????????????????? true:?????? false:???????
	private boolean isFirstPic = true;// ???????????????????
	
	private boolean isClick = false;

	public RecogService.MyBinder recogBinder;
	public ServiceConnection recogConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			recogConn = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			recogBinder = (RecogService.MyBinder) service;

			iInitPlateIDSDK = recogBinder.getInitPlateIDSDK();

			if (iInitPlateIDSDK != 0) {

				String[] str = { "" + iInitPlateIDSDK };
				getResult(str);
			}
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// DisplayMetrics ?????????????????????????????????????????????
			DisplayMetrics metric = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metric);
			width = metric.widthPixels; // ????????????
			height = metric.heightPixels; // ????????????
			switch (msg.what) {
			case 0:
				rotation = 90;
				break;
			case 1:
				rotation = 0;
				break;
			case 2:
				rotation = 270;
				break;
			case 3:
				rotation = 180;
				break;

			}
			setButton();
			initCamera(holder, rotation);
			super.handleMessage(msg);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		int uiRot = getWindowManager().getDefaultDisplay().getRotation();// ?????????????

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_camera);

		isCamera = getIntent().getBooleanExtra("camera", false);
		recogType = getIntent().getBooleanExtra("camera", false);

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		width = metric.widthPixels; // ????????????
		height = metric.heightPixels; // ????????????

		switch (uiRot) {
		case 0:// ?????????
			tempUiRot = 0;
			rotation = 90;
			break;
		case 1:// ????90??
			tempUiRot = 1;
			rotation = 0;
			break;
		case 2:// ????180??
			tempUiRot = 2;
			rotation = 270;
			break;
		case 3:// ????270??
			tempUiRot = 3;
			rotation = 180;
			break;
		}
		findView();
		if (width * 3 == height * 4) {
			isFatty = true;
		}
	}

	private void findView() {
		surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe_video);
		myview = (ViewfinderView) findViewById(R.id.myview);
		back_btn = (ImageButton) findViewById(R.id.back_camera);
		flash_btn = (ImageButton) findViewById(R.id.flash_camera);
		back = (ImageButton) findViewById(R.id.back);
		take_pic = (ImageButton) findViewById(R.id.take_pic_btn);
		setButton();
		holder = surfaceView.getHolder();
		holder.addCallback(CameraActivity.this);
		back_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent=new Intent();
				intent.putExtra("number", "null");
				intent.putExtra("color", "null");
				intent.putExtra("path", "null");
				setResult(0x128,intent);
				Log.e("TAG","0x128");
				//startActivity(intent);
				CameraActivity.this.finish();
			}
		});
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				//finish();
				Intent intent=new Intent();
				intent.putExtra("number", "null");
				intent.putExtra("color", "null");
				intent.putExtra("path", "null");
				setResult(0x127,intent);
				Log.e("TAG","0x127");
				//startActivity(intent);
				CameraActivity.this.finish();
			}
		});
		flash_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!getPackageManager().hasSystemFeature(
						PackageManager.FEATURE_CAMERA_FLASH)) {
					Toast.makeText(getApplicationContext(), "?????????????!",
							Toast.LENGTH_SHORT).show();
				} else {
					if (camera != null) {
						Camera.Parameters parameters = camera.getParameters();
						String flashMode = parameters.getFlashMode();
						if (flashMode
								.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
							parameters
									.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							parameters.setExposureCompensation(0);
						} else {
							parameters
									.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// ????????
							parameters.setExposureCompensation(-1);

						}
						try {
							camera.setParameters(parameters);
						} catch (Exception e) {
							Toast.makeText(
									getApplicationContext(),
									getResources().getString(
											getResources().getIdentifier(
													"no_flash", "string",
													getPackageName())),
									Toast.LENGTH_SHORT).show();
						}
						camera.startPreview();
					}
				}
			}

		});
		take_pic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				isClick = true;
				if(isClick){
					scaleAnimation = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
							width / 2, height / 2);
					scaleAnimation.setDuration(300);
					scaleAnimation.setRepeatMode(Animation.REVERSE);
					scaleAnimation.setRepeatCount(1);
					myview.startAnimation(scaleAnimation);
					scaleAnimation.setAnimationListener(CameraActivity.this);

					camera.autoFocus(new AutoFocusCallback() {
						public void onAutoFocus(boolean success, Camera camera) {
							isSuccess = success;
							//??????????????(??:?????????????success?????false),??????????????????if???;
							//???????????????????????????????
							if (!isSuccess) {
								isSuccess = true;
							}
						}
					});

					if (timer != null) {
						timer.cancel();
					}

					if (!isSuccess || !isAnimationEnd) {
						MyThread myThread = new MyThread();
						myThread.start();
					} else {
						isCamera = true;
					}
					
					isClick = false;
				}

				
			}
		});

	}

	private void setButton() {

		int back_w;
		int back_h;
		int flash_w;
		int flash_h;
		int take_h;
		int take_w;
		RelativeLayout.LayoutParams layoutParams;
		switch (rotation) {
		case 90:
		case 270:

			back.setVisibility(View.VISIBLE);
			back_btn.setVisibility(View.GONE);
			back_h = (int) (height * 0.067);
			back_w = (int) (back_h * 1);
			layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);

			layoutParams.topMargin = (int) (((height - width * 0.95) / 2 - back_h) / 2);
			layoutParams.leftMargin = (int) (width * 0.105);
			back.setLayoutParams(layoutParams);

			flash_h = (int) (height * 0.067);
			flash_w = (int) (flash_h * 1);
			layoutParams = new RelativeLayout.LayoutParams(flash_w, flash_h);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);

			layoutParams.topMargin = (int) (((height - width * 0.95) / 2 - flash_h) / 2);
			layoutParams.rightMargin = (int) (width * 0.105);
			flash_btn.setLayoutParams(layoutParams);

			take_h = (int) (height * 0.105);
			take_w = (int) (take_h * 1);
			layoutParams = new RelativeLayout.LayoutParams(take_w, take_h);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
					RelativeLayout.TRUE);

			layoutParams.bottomMargin = (int) (width * 0.105);
			take_pic.setLayoutParams(layoutParams);
			break;
		case 0:
		case 180:

			back_btn.setVisibility(View.VISIBLE);
			back.setVisibility(View.GONE);
			back_w = (int) (width * 0.0675);
			back_h = (int) (back_w * 1);
			layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
					RelativeLayout.TRUE);

			layoutParams.leftMargin = (int) (((width - height * 0.95) / 2 - back_h) / 2);
			layoutParams.bottomMargin = (int) (height * 0.105);
			back_btn.setLayoutParams(layoutParams);

			flash_w = (int) (width * 0.067);
			flash_h = (int) (flash_w * 1);
			layoutParams = new RelativeLayout.LayoutParams(flash_w, flash_h);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);

			layoutParams.leftMargin = (int) (((width - height * 0.95) / 2 - back_h) / 2);
			layoutParams.topMargin = (int) (height * 0.105);
			flash_btn.setLayoutParams(layoutParams);

			take_h = (int) (width * 0.105);
			take_w = (int) (take_h * 1);
			layoutParams = new RelativeLayout.LayoutParams(take_w, take_h);
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);

			layoutParams.rightMargin = (int) (height * 0.105);
			take_pic.setLayoutParams(layoutParams);
			break;
		}
		if (isCamera) {
			take_pic.setVisibility(View.GONE);
		} else {
			take_pic.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		if (camera == null) {
			try {
				camera = Camera.open();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			camera.setPreviewDisplay(holder);
			initCamera(holder, rotation);
			Timer time = new Timer();
			if (timer == null) {
				timer = new TimerTask() {
					public void run() {
						isSuccess = false;
						if (camera != null) {
							try {
								camera.autoFocus(new AutoFocusCallback() {
									public void onAutoFocus(boolean success,
											Camera camera) {
										isSuccess = success;
										//??????????????(??:?????????????success?????false),??????????????????if???;
										//???????????????????????????????
										if (!isSuccess) {
											isSuccess = true;
										}
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
				};
			}
			time.schedule(timer, 500, 2500);
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	int nums = 1;

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		if (data == null || isSuccess == false) {
//			Log.i("wu", "??????????");
			return;
		}
		// ???????????????
		int uiRot = getWindowManager().getDefaultDisplay().getRotation();// ?????????????

		if (uiRot != tempUiRot) {

			System.err.println("uiRot:" + uiRot);
			Message mesg = new Message();
			mesg.what = uiRot;
			handler.sendMessage(mesg);
			tempUiRot = uiRot;
		}

		if (isFirstPic) {
			Intent authIntent = new Intent(CameraActivity.this, RecogService.class);
			bindService(authIntent, recogConn, Service.BIND_AUTO_CREATE);
			isFirstPic = false;
		}

		while (iInitPlateIDSDK == -1) {
			return;
		}
		

		nums++;
		if (iInitPlateIDSDK == 0) {
			
			if(!take_pic.isEnabled()){
				take_pic.setEnabled(true);
			}

			if (nums == 3) {
				nums = 0;
				tempData = data;
				// prp = new PlateRecognitionParameter();
				prp.height = preHeight;
				prp.width = preWidth;
				prp.picByte = data;

				if (rotation == 0) {

					prp.plateIDCfg.bRotate = 0;
					prp.plateIDCfg.left = preWidth / 2 - myview.length
							* preWidth / width;
					prp.plateIDCfg.right = preWidth / 2 + myview.length
							* preWidth / width;
					prp.plateIDCfg.top = preHeight / 2 - myview.length
							* preHeight / height;
					prp.plateIDCfg.bottom = preHeight / 2 + myview.length
							* preHeight / height;

				} else if (rotation == 90) {

					prp.plateIDCfg.bRotate = 1;
					prp.plateIDCfg.left = preHeight / 2 - myview.length
							* preHeight / width;
					prp.plateIDCfg.right = preHeight / 2 + myview.length
							* preHeight / width;
					prp.plateIDCfg.top = preWidth / 2 - myview.length
							* preWidth / height;
					prp.plateIDCfg.bottom = preWidth / 2 + myview.length
							* preWidth / height;

				} else if (rotation == 180) {

					prp.plateIDCfg.bRotate = 2;
					prp.plateIDCfg.left = preWidth / 2 - myview.length
							* preWidth / width;
					prp.plateIDCfg.right = preWidth / 2 + myview.length
							* preWidth / width;
					prp.plateIDCfg.top = preHeight / 2 - myview.length
							* preHeight / height;
					prp.plateIDCfg.bottom = preHeight / 2 + myview.length
							* preHeight / height;
				} else if (rotation == 270) {

					prp.plateIDCfg.bRotate = 3;
					prp.plateIDCfg.left = preHeight / 2 - myview.length
							* preHeight / width;
					prp.plateIDCfg.right = preHeight / 2 + myview.length
							* preHeight / width;
					prp.plateIDCfg.top = preWidth / 2 - myview.length
							* preWidth / height;
					prp.plateIDCfg.bottom = preWidth / 2 + myview.length
							* preWidth / height;
				}

				if (isCamera) {
					fieldvalue = recogBinder.doRecogDetail(prp);
					getResult(fieldvalue);
				}

			}
		}else{
			if(take_pic.isEnabled()){
				take_pic.setEnabled(false);
			}
		}
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, int format,
                               int width, int height) {

		if (camera != null) {
			camera.autoFocus(new AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, final Camera camera) {
					if (success) {
						synchronized (camera) {
							new Thread() {
								public void run() {
									initCamera(holder, rotation);
									super.run();
								}
							}.start();
						}
					}
				}
			});
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			if (camera != null) {
				camera.setPreviewCallback(null);
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		} catch (Exception e) {
		}

	}

	/**
	 * 
	 * @Title: initCamera
	 * @Description: (????????)
	 * @param @param holder
	 * @param @param r ??????????
	 * @return void ????????
	 * @throws
	 */
	@TargetApi(14)
	private void initCamera(SurfaceHolder holder, int r) {

		if (camera != null) {
			// ???????????
			Camera.Parameters parameters = camera.getParameters();
			// ??????????????
			List<Camera.Size> list = parameters.getSupportedPreviewSizes();
			Camera.Size size;
			int length = list.size();
			int previewWidth = 640;
			int previewheight = 480;
			int second_previewWidth = 0;
			int second_previewheight = 0;
			if (length == 1) {
				size = list.get(0);
				previewWidth = size.width;
				previewheight = size.height;

			} else {
				for (int i = 0; i < length; i++) {
					size = list.get(i);

					if (isFatty) {
						if (size.height <= 960 || size.width <= 1280) {

							second_previewWidth = size.width;
							second_previewheight = size.height;

							if (previewWidth <= second_previewWidth
									&& second_previewWidth * 3 == second_previewheight * 4) {

							}
							previewWidth = second_previewWidth;
							previewheight = second_previewheight;
						}
					} else {

						if (size.height <= 960 || size.width <= 1280) {
							second_previewWidth = size.width;
							second_previewheight = size.height;
							if (previewWidth <= second_previewWidth) {
								previewWidth = second_previewWidth;
								previewheight = second_previewheight;
							}
						}
					}
				}
			}

			preWidth = previewWidth;
			preHeight = previewheight;
			System.out.println("?????????" + preWidth + "    " + preHeight);
			parameters.setPictureFormat(ImageFormat.JPEG);
			parameters.setPreviewSize(preWidth, preHeight);
			if (getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			}
			camera.setPreviewCallback(CameraActivity.this);
			camera.setParameters(parameters);

			if (rotation == 90 || rotation == 270) {
				if (width < 1080) {
					camera.stopPreview();
				}
			} else {
				if (height < 1080) {
					camera.stopPreview();
				}
			}

			camera.setDisplayOrientation(r);

			try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			camera.startPreview();

			if (rotation == 90 || rotation == 270) {
				if (width < 1080) {
					camera.setPreviewCallback(CameraActivity.this);
				}
			} else {
				if (height < 1080) {
					camera.setPreviewCallback(CameraActivity.this);
				}
			}
			camera.cancelAutoFocus();
		}
	}

	int[] fieldname = { R.string.plate_number, R.string.plate_color,
			R.string.plate_color_code, R.string.plate_type_code,
			R.string.plate_reliability, R.string.plate_leftupper_pointX,
			R.string.plate_leftupper_pointY, R.string.plate_rightdown_pointX,
			R.string.plate_rightdown_pointY, R.string.plate_car_color };
	/**
	 * 
	 * @Title: getResult
	 * @Description: (??????)
	 * @param @param fieldvalue ?????????????????
	 * @return void ????????
	 * @throws
	 */
	private void getResult(String[] fieldvalue) {

		if (iInitPlateIDSDK != 0) {

			String nretString = iInitPlateIDSDK + "";
			if (nretString.equals("1793")) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.failed_file_timeout),
						Toast.LENGTH_SHORT).show();
			} else if (nretString.equals("276")) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.failed_noFile_find),
						Toast.LENGTH_SHORT).show();
			} else if (nretString.equals("-10002")) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.failed_noAuth), Toast.LENGTH_SHORT)
						.show();
			} else if (nretString.equals("-10004")) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.failed_File_error),
						Toast.LENGTH_SHORT).show();
			} else if (nretString.equals("-10003")) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.failed_init_error),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"??????????????????" + iInitPlateIDSDK, Toast.LENGTH_SHORT)
						.show();
			}

		} else {
			String[] resultString;
			String boolString = "";
			boolString = fieldvalue[0];// ?????

			if (boolString != null && !boolString.equals("")) {
				resultString = boolString.split(";");
				int lenght = resultString.length;
				if (lenght > 0) {

					String[] strarray = fieldvalue[4].split(";");
					if (Float.valueOf(strarray[0]) > 14) {
						if (tempData != null) {
							// if(prp.picByte != null){
							int[] datas = CameraUtils.convertYUV420_NV21toARGB8888(
							// prp.picByte, preWidth, preHeight);
									tempData, preWidth, preHeight);

							BitmapFactory.Options opts = new BitmapFactory.Options();
							opts.inInputShareable = true;
							opts.inPurgeable = true;
							bitmap1 = Bitmap.createBitmap(datas, preWidth,
									preHeight,
									Bitmap.Config.ARGB_8888);
						}
						camera.stopPreview();
						camera.setPreviewCallback(null);
						if (lenght == 1) {
							if (null != fieldname) {
								// ?????????????????????????????
								Matrix matrix = new Matrix();
								matrix.reset();
								if (rotation == 90) {
									matrix.setRotate(90);
								} else if (rotation == 180) {
									matrix.setRotate(180);
								} else if (rotation == 270) {
									matrix.setRotate(270);
								}
								bitmap1 = Bitmap.createBitmap(bitmap1, 0, 0,
										bitmap1.getWidth(),
										bitmap1.getHeight(), matrix, true);

								String path = savePicture(bitmap1, null);
								if (bitmap1 != null) {
									if (!bitmap1.isRecycled()) {
										bitmap1.recycle();
										bitmap1 = null;
									}
								}

								 mVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
								 mVibrator.vibrate(100);
								Intent intent = new Intent();
								number = fieldvalue[0];
								color = fieldvalue[1];

								int left = Integer.valueOf(fieldvalue[5]);
								int top = Integer.valueOf(fieldvalue[6]) - 5;
								int w = Integer.valueOf(fieldvalue[7])
										- Integer.valueOf(fieldvalue[5]);
								int h = Integer.valueOf(fieldvalue[8])
										- Integer.valueOf(fieldvalue[6]) + 10;

								intent.putExtra("number", number);
								intent.putExtra("color", color);
								intent.putExtra("path", path);
								intent.putExtra("left", left);
								intent.putExtra("top", top);
								intent.putExtra("width", w);
								intent.putExtra("height", h);
								intent.putExtra("recogType", recogType);
								setResult(0x123,intent);
								Log.e("TAG","0x123");
								//startActivity(intent);
								 CameraActivity.this.finish();
							}

						} else {

							String path = null;
							if (null != fieldname) {
								// ?????????????????????????????
								Matrix matrix = new Matrix();
								matrix.reset();
								if (rotation == 90) {
									matrix.setRotate(90);
								} else if (rotation == 180) {
									matrix.setRotate(180);
								} else if (rotation == 270) {
									matrix.setRotate(270);

								}
								bitmap1 = Bitmap.createBitmap(bitmap1, 0, 0,
										bitmap1.getWidth(),
										bitmap1.getHeight(), matrix, true);

								path = savePicture(bitmap1, null);
								if (bitmap1 != null) {
									if (!bitmap1.isRecycled()) {
										bitmap1.recycle();
										bitmap1 = null;
									}

								}
							}

							String itemString = "";

							mVibrator = (Vibrator) getApplication()
									.getSystemService(Service.VIBRATOR_SERVICE);
							mVibrator.vibrate(100);
							Intent intent = new Intent();
							for (int i = 0; i < lenght; i++) {

								itemString = fieldvalue[0];
								resultString = itemString.split(";");
								number += resultString[i] + ";\n";

								itemString = fieldvalue[1];
								resultString = itemString.split(";");
								color += resultString[i] + ";";
							}

							int left = Integer.parseInt(fieldvalue[5]
									.split(";")[0]);
							int top = Integer
									.parseInt(fieldvalue[6].split(";")[0]);
							int w = Integer
									.parseInt(fieldvalue[7].split(";")[0])
									- Integer
											.parseInt(fieldvalue[5].split(";")[0]);
							int h = Integer
									.parseInt(fieldvalue[8].split(";")[0])
									- Integer
											.parseInt(fieldvalue[6].split(";")[0]);

							intent.putExtra("number", number);
							intent.putExtra("color", color);
							intent.putExtra("recogType", recogType);
							intent.putExtra("path", path);
							intent.putExtra("left", left);
							intent.putExtra("top", top);
							intent.putExtra("width", w);
							intent.putExtra("height", h);
							intent.putExtra("recogType", recogType);
							setResult(0x124,intent);
							Log.e("TAG","0x124");
							//startActivity(intent);
							CameraActivity.this.finish();
						}
					}

				}

			} else {

				if (!recogType) {
					if (tempData != null) {
						// if(prp.picByte != null){

						int[] datas = CameraUtils.convertYUV420_NV21toARGB8888(
						// prp.picByte, preWidth, preHeight);
								tempData, preWidth, preHeight);

						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inInputShareable = true;
						opts.inPurgeable = true;
						bitmap1 = Bitmap.createBitmap(datas, preWidth,
								preHeight,
								Bitmap.Config.ARGB_8888);
					}
					Matrix matrix = new Matrix();
					matrix.reset();
					if (rotation == 90) {
						matrix.setRotate(90);
					} else if (rotation == 180) {
						matrix.setRotate(180);
					} else if (rotation == 270) {
						matrix.setRotate(270);
					}
					bitmap1 = Bitmap.createBitmap(bitmap1, 0, 0,
							bitmap1.getWidth(), bitmap1.getHeight(), matrix,
							true);

					String path = savePicture(bitmap1, null);
					camera.stopPreview();
					camera.setPreviewCallback(null);
					if (bitmap1 != null) {
						if (!bitmap1.isRecycled()) {
							bitmap1.recycle();
							bitmap1 = null;
						}

					}

					if (null != fieldname) {
						mVibrator = (Vibrator) getApplication()
								.getSystemService(Service.VIBRATOR_SERVICE);
						mVibrator.vibrate(100);

						Intent intent = new Intent();
						number = fieldvalue[0];
						color = fieldvalue[1];
						if (fieldvalue[0] == null) {
							number = "null";
						}
						if (fieldvalue[1] == null) {
							color = "null";
						}
						int left = prp.plateIDCfg.left;
						int top = prp.plateIDCfg.top;
						int w = prp.plateIDCfg.right - prp.plateIDCfg.left;
						int h = prp.plateIDCfg.bottom - prp.plateIDCfg.top;

						intent.putExtra("number", number);
						intent.putExtra("color", color);
						intent.putExtra("path", path);
						intent.putExtra("left", left);
						intent.putExtra("top", top);
						intent.putExtra("width", w);
						intent.putExtra("height", h);
						intent.putExtra("recogType", recogType);
						setResult(0x125,intent);
						Log.e("TAG","0x125");
						//startActivity(intent);
						CameraActivity.this.finish();
					}

				}

			}
		}
		fieldvalue = null;
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (recogBinder != null) {
			unbindService(recogConn);
		}
		if (timer != null) {
			timer.cancel();
		}

		CameraActivity.this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			/*try {
				if (camera != null) {
					camera.setPreviewCallback(null);
					camera.stopPreview();
					camera.release();
					camera = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			finish();*/
			Intent intent=new Intent();
			intent.putExtra("number", "null");
			intent.putExtra("color", "null");
			intent.putExtra("path", "null");
			setResult(0x126,intent);
			Log.e("TAG","0x126");
			//startActivity(intent);
			CameraActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public static String savePicture(Bitmap bitmap, String a) {
		String strCaptureFilePath = PATH + "plateID_" + pictureName() + a
				+ ".jpg";
		File dir = new File(PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(strCaptureFilePath);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));

			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return strCaptureFilePath;
	}

	public static String pictureName() {
		String str = "";
		Time t = new Time();
		t.setToNow(); // ????????
		int year = t.year;
		int month = t.month + 1;
		int date = t.monthDay;
		int hour = t.hour; // 0-23
		int minute = t.minute;
		int second = t.second;
		if (month < 10)
			str = String.valueOf(year) + "0" + String.valueOf(month);
		else {
			str = String.valueOf(year) + String.valueOf(month);
		}
		if (date < 10)
			str = str + "0" + String.valueOf(date + "_");
		else {
			str = str + String.valueOf(date + "_");
		}
		if (hour < 10)
			str = str + "0" + String.valueOf(hour);
		else {
			str = str + String.valueOf(hour);
		}
		if (minute < 10)
			str = str + "0" + String.valueOf(minute);
		else {
			str = str + String.valueOf(minute);
		}
		if (second < 10)
			str = str + "0" + String.valueOf(second);
		else {
			str = str + String.valueOf(second);
		}
		return str;
	}

	class MyThread extends Thread {

		@Override
		public void run() {
			super.run();
			while (!isSuccess || !isAnimationEnd) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			isCamera = true;
		}
	}

	/**
	 * ??????????
	 */
	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		isAnimationEnd = true;
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

}
