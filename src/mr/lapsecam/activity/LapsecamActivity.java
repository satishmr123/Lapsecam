package mr.lapsecam.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import mr.lapsecam.R;
import mr.lapsecam.utils.LapsecamApplication;
import mr.lapsecam.utils.LapsecamVideoPreview;
import mr.lapsecam.utils.TimeLapseVideoEncoder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LapsecamActivity extends Activity implements OnTouchListener {

	private Camera mCamera;
	private LapsecamVideoPreview customVideoView;
	private int mCameraIDToOpen = 0;
	private boolean mIsRecordingStarted = false;
	private TimeLapseVideoEncoder mVideoEncoder = null;
	private File mOutputVideoFile = null;
	
	private ImageButton startStopButton;
	private ImageButton zoominButton;
	private ImageButton zoomoutButton;
	private ImageButton backButton;
	private ImageButton pauseButton;
	private ImageButton settingsButton;
	private RelativeLayout mBottomBar;
	
	private Runnable mRunnableforBottomBar;
	private Handler mTimerHandler = new Handler();
	
	private ListView mainSettingsListView;  
    private ArrayAdapter<String> mainsettings_Adapter;
    private LinearLayout main_settings_Layout;
    private LinearLayout sub_settings_Layout;
	
    private ListView effects_settingsListView;
    private ArrayAdapter<String> effects_Adapter;
    
    private ListView autofocus_settingsListView;
    private ArrayAdapter<String> autofocus_Adapter;
    
    private ListView scene_settingsListView;
    private ArrayAdapter<String> scene_Adapter;
    
    private ListView whitebalalnce_settingsListView;
    private ArrayAdapter<String> whitebalalnce_Adapter;
    
    private ListView flash_settingsListView;
    private ArrayAdapter<String> flash_Adapter;
    
    private ListView size_settingsListView;
    private ArrayAdapter<String> size_Adapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_lapsecam);
		
		
		openCamera ();
		FrameLayout framelayout = (FrameLayout) findViewById(R.id.videoPreview);
		
		customVideoView = new LapsecamVideoPreview(this, mCamera);
		framelayout.addView(customVideoView);
		framelayout.setOnTouchListener(this);
		
		mBottomBar = (RelativeLayout)findViewById(R.id.bottom_bar);
		mBottomBar.setVisibility(View.VISIBLE);
		
		mRunnableforBottomBar = new Runnable() {
			@Override
			public void run() {
				mBottomBar.setVisibility(View.INVISIBLE);
				main_settings_Layout.setVisibility(View.GONE);
				sub_settings_Layout.setVisibility(View.GONE);
			}
		};
		
		startStopButton = (ImageButton) findViewById(R.id.start_stop_button);
		startStopButton.setBackgroundResource(R.drawable.start_video_recording);
		startStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsRecordingStarted == false) {
					Log.d("","start Video Recording");
					startStopButton.setBackgroundResource(R.drawable.stop_video_recording);
					mIsRecordingStarted = true;
					try {
						mVideoEncoder.startVideoRecording();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					showExitAlert ("Want to Stop Video Recording?");
				}
			}
		});
		
		backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showExitAlert("Want to Stop Video Recording?");
			}
		});
		
		settingsButton = (ImageButton) findViewById(R.id.settings_button);
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (main_settings_Layout.isShown()) {
					main_settings_Layout.setVisibility(View.GONE);
					sub_settings_Layout.setVisibility(View.GONE);
				} else {
					main_settings_Layout.setVisibility(View.VISIBLE);
					main_settings_Layout.setFocusable(false);
				}
			}
		});
		
		pauseButton = (ImageButton) findViewById(R.id.pause_button);
		pauseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		
		
		zoominButton = (ImageButton) findViewById(R.id.zoomin_button);
		zoominButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				customVideoView.zoomin();
			}
		});
		
		zoomoutButton = (ImageButton) findViewById(R.id.zoomout_button);
		zoomoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				customVideoView.zoomout();
			}
		});
		
		mOutputVideoFile = new File(LapsecamApplication.getFilePath("testLapse", ".mp4"));
		mVideoEncoder = new TimeLapseVideoEncoder (mCamera, customVideoView, 2, CamcorderProfile.QUALITY_TIME_LAPSE_1080P, mOutputVideoFile.getAbsolutePath());
		
		/*-------------- main settings -----------------------*/
		main_settings_Layout = (LinearLayout) findViewById(R.id.main_settings_list);
		sub_settings_Layout = (LinearLayout) findViewById(R.id.sub_settings_list);
		
		mainSettingsListView = (ListView) findViewById( R.id.main_settings_listview); 
		
		effects_settingsListView = (ListView) findViewById(R.id.effects_listview);
		autofocus_settingsListView = (ListView) findViewById(R.id.autofocus_listview);
		scene_settingsListView = (ListView) findViewById(R.id.scene_listview);
		whitebalalnce_settingsListView = (ListView) findViewById(R.id.whitebalance_listview);
		flash_settingsListView = (ListView) findViewById(R.id.flash_listview);
		size_settingsListView = (ListView) findViewById(R.id.size_listview);
		
		String[] settings = new String[] {"Effects", "AutoFocus", "Scene", "Whitebalance", "Flash", "Size"};    
		ArrayList<String> settingsList = new ArrayList<String>();  
		settingsList.addAll(Arrays.asList(settings));
		
		mainsettings_Adapter = new ArrayAdapter<String>(this, R.layout.simplerow, settingsList);
		mainSettingsListView.setAdapter(mainsettings_Adapter);
		mainSettingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				Log.d("","click on position = " + position + " id = " + id);
				switch (position) {
				case 0://Effects
					autofocus_settingsListView.setVisibility(View.GONE);
					scene_settingsListView.setVisibility(View.GONE);
					whitebalalnce_settingsListView.setVisibility(View.GONE);
					flash_settingsListView.setVisibility(View.GONE);
					size_settingsListView.setVisibility(View.GONE);
					effects_settingsListView.setVisibility(View.VISIBLE);
					sub_settings_Layout.setVisibility(View.VISIBLE);
					break;
				case 1://AutoFocus
					scene_settingsListView.setVisibility(View.GONE);
					whitebalalnce_settingsListView.setVisibility(View.GONE);
					flash_settingsListView.setVisibility(View.GONE);
					effects_settingsListView.setVisibility(View.GONE);
					size_settingsListView.setVisibility(View.GONE);
					autofocus_settingsListView.setVisibility(View.VISIBLE);
					sub_settings_Layout.setVisibility(View.VISIBLE);
					break;
				case 2://Scene
					whitebalalnce_settingsListView.setVisibility(View.GONE);
					flash_settingsListView.setVisibility(View.GONE);
					effects_settingsListView.setVisibility(View.GONE);
					autofocus_settingsListView.setVisibility(View.GONE);
					size_settingsListView.setVisibility(View.GONE);
					scene_settingsListView.setVisibility(View.VISIBLE);
					sub_settings_Layout.setVisibility(View.VISIBLE);
					break;
				case 3://Whitebalance
					flash_settingsListView.setVisibility(View.GONE);
					effects_settingsListView.setVisibility(View.GONE);
					autofocus_settingsListView.setVisibility(View.GONE);
					scene_settingsListView.setVisibility(View.GONE);
					size_settingsListView.setVisibility(View.GONE);
					whitebalalnce_settingsListView.setVisibility(View.VISIBLE);
					sub_settings_Layout.setVisibility(View.VISIBLE);
					break;
				case 4://Flash
					effects_settingsListView.setVisibility(View.GONE);
					autofocus_settingsListView.setVisibility(View.GONE);
					scene_settingsListView.setVisibility(View.GONE);
					whitebalalnce_settingsListView.setVisibility(View.GONE);
					size_settingsListView.setVisibility(View.GONE);
					flash_settingsListView.setVisibility(View.VISIBLE);
					sub_settings_Layout.setVisibility(View.VISIBLE);
					break;
				case 5://Size
					effects_settingsListView.setVisibility(View.GONE);
					autofocus_settingsListView.setVisibility(View.GONE);
					scene_settingsListView.setVisibility(View.GONE);
					whitebalalnce_settingsListView.setVisibility(View.GONE);
					flash_settingsListView.setVisibility(View.GONE);
					size_settingsListView.setVisibility(View.VISIBLE);
					sub_settings_Layout.setVisibility(View.VISIBLE);
					break;
					
				default:
					break;
				}
			}
		});
		
		/*-------------- effects settings -----------------------*/
		String[] effects = new String[] {"None", "Aqua","Blackboard", "Mono", "Negative", "Posterize", "Sepia", "Solarize", "Whiteboard"};    
		ArrayList<String> effectssList = new ArrayList<String>();  
		effectssList.addAll(Arrays.asList(effects));
		
		effects_Adapter = new ArrayAdapter<String>(this, R.layout.simplerow, effectssList);
		effects_settingsListView.setAdapter(effects_Adapter);
		effects_settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				Log.d("","effects click on position = " + position + " id = " + id);
				SetColorEffects (position, id);
			}
		});
		
		/*-------------- autofocus settings -----------------------*/
		String[] autofocusmodes = new String[] {"auto", "Fixed"};    
		ArrayList<String> autofocusmodesList = new ArrayList<String>();  
		autofocusmodesList.addAll(Arrays.asList(autofocusmodes));
		
		autofocus_Adapter = new ArrayAdapter<String>(this, R.layout.simplerow, autofocusmodesList);
		autofocus_settingsListView.setAdapter(autofocus_Adapter);
		autofocus_settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				Log.d("","autofocus click on position = " + position + " id = " + id);
				SetFocusEffects (position, id);
			}
		});
		
		/*-------------- scene settings -----------------------*/
		String[] scenes = new String[] {"Auto", "Action",/*"Barcode",*/ "Beach", "Candlelight", "Fireworks", /*HDR*/"Landscape", "Night", "Night Portrait", "Party", "Portrait", "Snow", "Sports", "Steady", "Sunset", "Theatre"};    
		ArrayList<String> scenesList = new ArrayList<String>();  
		scenesList.addAll(Arrays.asList(scenes));
		
		scene_Adapter = new ArrayAdapter<String>(this, R.layout.simplerow, scenesList);
		scene_settingsListView.setAdapter(scene_Adapter);
		scene_settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				Log.d("","scene click on position = " + position + " id = " + id);
//				SetSceneEffects (position, id);
			}
		});
		
		/*-------------- whitebalance settings -----------------------*/
		String[] wb = new String[] {"Auto", "Cloudy","Daylight", "Fluorescent", "Incandescent", "Shade", "Twilight", "Warm Fluorescent"};    
		ArrayList<String> WBList = new ArrayList<String>();  
		WBList.addAll(Arrays.asList(wb));
		
		whitebalalnce_Adapter = new ArrayAdapter<String>(this, R.layout.simplerow, WBList);
		whitebalalnce_settingsListView.setAdapter(whitebalalnce_Adapter);
		whitebalalnce_settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				Log.d("","whitebalance click on position = " + position + " id = " + id);
//				SetWhitebalanceEffects (position, id);
			}
		});
		
		/*-------------- flash settings -----------------------*/
		String[] flash = new String[] {"Auto", "OFF", "ON", "RedEye", "Torch"};    
		ArrayList<String> flashList = new ArrayList<String>();  
		flashList.addAll(Arrays.asList(flash));
		
		flash_Adapter = new ArrayAdapter<String>(this, R.layout.simplerow, flashList);
		flash_settingsListView.setAdapter(flash_Adapter);
//		if (!(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)))
//		{
//			flash_settingsListView.setVisibility(View.GONE);
//		}
		
		flash_settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				Log.d("","flash click on position = " + position + " id = " + id);
				SetFlashEffects (position, id);
			}
		});
		
		/*-------------- size settings -----------------------*/
		String[] size = new String[] {"Qcif", "Cif", "480p", "720p", "1080p"};    
		ArrayList<String> sizeList = new ArrayList<String>();  
		sizeList.addAll(Arrays.asList(size));
		
		size_Adapter = new ArrayAdapter<String>(this, R.layout.simplerow, sizeList);
		size_settingsListView.setAdapter(size_Adapter);
		size_settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				Log.d("","size click on position = " + position + " id = " + id);
//				SetSizes (position, id);
			}
		});
		//hideBottomBarLater();

	}

	private void SetColorEffects(int position, long id) {
		switch (position) {
		case 0://None
			customVideoView.setdifferentMode(Camera.Parameters.EFFECT_NONE);
			Toast.makeText(LapsecamActivity.this, "None" , Toast.LENGTH_SHORT).show();
			break;
		case 1://Aqua
			customVideoView.setdifferentMode(Camera.Parameters.EFFECT_AQUA);
			Toast.makeText(LapsecamActivity.this, "Aqua" , Toast.LENGTH_SHORT).show();
			break;
		case 2://Blackboard
			customVideoView.setdifferentMode(Camera.Parameters.EFFECT_BLACKBOARD);
			Toast.makeText(LapsecamActivity.this, "Blackboard" , Toast.LENGTH_SHORT).show();
			break;
		case 3://Mono
			customVideoView.setdifferentMode(Camera.Parameters.EFFECT_MONO);
			Toast.makeText(LapsecamActivity.this, "Mono" , Toast.LENGTH_SHORT).show();
			break;
		case 4://Negative
			customVideoView.setdifferentMode(Camera.Parameters.EFFECT_NEGATIVE);
			Toast.makeText(LapsecamActivity.this, "Negative" , Toast.LENGTH_SHORT).show();
			break;
		case 5://Posterize
			customVideoView.setdifferentMode(Camera.Parameters.EFFECT_POSTERIZE);
			Toast.makeText(LapsecamActivity.this, "Posterize" , Toast.LENGTH_SHORT).show();
			break;
		case 6://Sepia
			customVideoView.setdifferentMode(Camera.Parameters.EFFECT_SEPIA);
			Toast.makeText(LapsecamActivity.this, "Sepia" , Toast.LENGTH_SHORT).show();
			break;
		case 7://Solarize
			customVideoView.setdifferentMode(Camera.Parameters.EFFECT_SOLARIZE);
			Toast.makeText(LapsecamActivity.this, "Solarize" , Toast.LENGTH_SHORT).show();
			break;
		case 8://Whiteboard
			customVideoView.setdifferentMode(Camera.Parameters.EFFECT_WHITEBOARD);
			Toast.makeText(LapsecamActivity.this, "Whiteboard" , Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}
	
	
	private void SetFocusEffects(int position, long id) {
		switch (position) {
		case 0:
			customVideoView.setdifferentMode(Camera.Parameters.FOCUS_MODE_AUTO);
			break;
		case 1:
			customVideoView.setdifferentMode(Camera.Parameters.FOCUS_MODE_FIXED);
			break;
		default:
			break;
		}
	}

	
	private void SetFlashEffects(int position, long id) {
		switch (position) {
		case 0://Auto
			customVideoView.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			Toast.makeText(LapsecamActivity.this, "Flash Auto" , Toast.LENGTH_SHORT).show();
			break;
		case 1://off
			customVideoView.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			Toast.makeText(LapsecamActivity.this, "Flash Off" , Toast.LENGTH_SHORT).show();
			break;
		case 2://on
			customVideoView.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			Toast.makeText(LapsecamActivity.this, "Flash ON" , Toast.LENGTH_SHORT).show();
			break;
		case 3://RedEye
			customVideoView.setFlashMode(Camera.Parameters.FLASH_MODE_RED_EYE);
			Toast.makeText(LapsecamActivity.this, "Flash REDEYE" , Toast.LENGTH_SHORT).show();
			break;
		case 4://Torch
			customVideoView.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			Toast.makeText(LapsecamActivity.this, "Flash TORCH Mode" , Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}
	
	 private void showExitAlert(String message){
		 final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 builder.setCancelable(false);
		 builder.setMessage(message)
		        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
		            public void onClick(final DialogInterface dialog, final int id) {
						Log.d("","attempt to stop Video Recording");
						mIsRecordingStarted = false;
						mVideoEncoder.stopVideoRecording();
						finishWithCameraRealease();
		            }
		        })
		       .setNegativeButton("NO", new DialogInterface.OnClickListener() {
		    	   public void onClick(final DialogInterface dialog, final int id) {
		    		   return;
		    	   }
		 });
		 final AlertDialog alert = builder.create();
		 alert.show();
	 }	
	 
	@Override
	protected void onResume() {
		Log.d("","onResume");
		super.onResume();
		openCamera();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d("","onPause");
		releaseCamera();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.d("","onBackpressed");
		finishWithCameraRealease();
	}
	
	private void hideBottomBarLater(){
    	mTimerHandler.removeCallbacks(mRunnableforBottomBar, null);
    	mTimerHandler.postDelayed(mRunnableforBottomBar, 5000);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN){
			if (main_settings_Layout.isShown()) {
				//mBottomBar.setVisibility(View.INVISIBLE);
				main_settings_Layout.setVisibility(View.GONE);
				sub_settings_Layout.setVisibility(View.GONE);
			} //else {
				//mBottomBar.setVisibility(View.VISIBLE);
				//hideBottomBarLater();
			//}
		}
		return false;
	}
	
	@SuppressLint("NewApi")
	private void openCamera () {
		if (mCamera == null) {
			Log.d("","Open new Camera Object");
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				try {
					Log.d("","Trying to open camera with id = " + mCameraIDToOpen);
					mCamera = Camera.open(mCameraIDToOpen);
				} catch (RuntimeException RE) {
					RE.printStackTrace();
					finishWithCameraRealease ();
				}
			} else {
				mCamera = Camera.open();
			}
	        if (mCamera == null) {
	    		Log.e("","Error: Sorry your device does not have unused Camera.");
	    		finishWithCameraRealease ();
	    		return;
	        }
	        if (customVideoView != null) {
	        	Log.d("","Set preview with new camera object onPause");
	        	customVideoView.setCamera(mCamera);
	        }
		}
	}
	
	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
		    mCamera.release();
		    mCamera = null;
		    Log.d("","release preview Camera");
		}		
	}
	
	private void finishWithCameraRealease() {
		Log.d("","release cam & finish");
 		releaseCamera();
		finish ();
	}

}
