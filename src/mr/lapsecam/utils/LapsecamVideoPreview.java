package mr.lapsecam.utils;


import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LapsecamVideoPreview extends SurfaceView implements SurfaceHolder.Callback {
	private Size 		mOptimalPreviewSize;
	private Size 		mOnMeasureSize;
    private Camera 		mCamera;
    private SurfaceHolder mHolder;
    private boolean     isPreviewON = false;
    public boolean      mIsSurfaceCreated = false;
    public int mBestChoiseWidth = 0;
    public int mBestChoiseHeight = 0;
    public int mBestChoiseFPS = 0;
    public int mForcedWidth = 0;
    public int mForcedHeight = 0;
    
    public LapsecamVideoPreview(Context context, Camera camera) {
        super(context);
    	Log.d(" ","Constructor 1");
        mCamera = camera;
        mIsSurfaceCreated = false;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mOnMeasureSize = mCamera.getParameters().getPreviewSize();
    }

    public void setdifferentMode (String value) {
    	Camera.Parameters parameters = mCamera.getParameters();
    	parameters.setColorEffect (value);
    	restartPreviewWithNewParams (parameters);
    }
    
    public void zoomin ()
    {
    	Camera.Parameters parameters = mCamera.getParameters();
    	if (parameters.isZoomSupported())
    	{
    		int maxZoom = parameters.getMaxZoom();
    		int currentZoom = parameters.getZoom();
    		int nextZoomValue = currentZoom + 2;
    		if (nextZoomValue >= maxZoom)
    		{
    			nextZoomValue = maxZoom;
    		}
    		if (currentZoom == nextZoomValue)
    		{
    			return;
    		}
    		Log.d(" ","setting zoom in value = " + nextZoomValue);
    		parameters.setZoom(nextZoomValue);
    	}
    	restartPreviewWithNewParams (parameters);
    }
    
    public void zoomout ()
    {
    	Camera.Parameters parameters = mCamera.getParameters();
    	if (parameters.isZoomSupported())
    	{
    		//int maxZoom = parameters.getMaxZoom();
    		int currentZoom = parameters.getZoom();
    		int nextZoomValue = currentZoom - 2;
    		if (nextZoomValue <= 0)
    		{
    			nextZoomValue = 0;
    		}
    		if (currentZoom == nextZoomValue)
    		{
    			return;
    		}
    		Log.d(" ","setting zoom out value = " + nextZoomValue);
    		parameters.setZoom(nextZoomValue);
    	}
    	restartPreviewWithNewParams (parameters);
    }
    
	public void setFlashMode(String flashMode) {
		Camera.Parameters parameters = mCamera.getParameters();
		String currentFlashMode = parameters.getFlashMode ();
		if (currentFlashMode == flashMode)
		{
			return;
		}
		List <String> flashList = parameters.getSupportedFlashModes();
		if (flashList.contains(flashMode))
		{
			Log.d(" ","New flash Mode = " + flashMode);
			parameters.setFlashMode(flashMode);
			restartPreviewWithNewParams (parameters);
		} else {
			return;//Dont try to set Unsupported flash mode.
		}
		
	}
	
    /** This Method will take care of Setting camera parameters for newly created camera object. 
     * Say camera switch happens, new surfacechanged will not be called. But all camera parameters needs to set
     **/
    @SuppressLint("NewApi")
	public void refreshCameraPreview (Camera camera) {
    	Log.d(" ","Start Preview");
    	mCamera = camera;
    	
        // Android Guideline. set preview size and make any resize, rotate or reformatting changes here
        Camera.Parameters parameters = mCamera.getParameters();
        
        SupportedCameraParams mSupportedCameraParams = new SupportedCameraParams (parameters);
//        logCameraInfoParams();
       	
    	/*----------------- Enable Custom Video preview Size ---------------*/
        if (mOnMeasureSize != null) {
            mOptimalPreviewSize = getOptimalPreviewSize(mOnMeasureSize.width, mOnMeasureSize.height);
	        Log.d(" ","set previewSize Height = " + mOptimalPreviewSize.height + " Width = " + mOptimalPreviewSize.width);
	        parameters.setPreviewSize(mOptimalPreviewSize.width, mOptimalPreviewSize.height);
        }
        
    	/*----------------- Enable Video Autofocus ---------------*/
    	String autoFocusMode = getAutoFocusMode(parameters);
    	if (autoFocusMode != null) {
	    	parameters.setFocusMode(autoFocusMode);
	        Log.d(" ","Setting AutoFocus Mode to = " + parameters.getFocusMode());
    	}
    	
    	/*----------------- Enable Video Stabilisation ---------------*/
    	if (android.os.Build.VERSION.SDK_INT >= 15)
    	{
        	if (parameters.isVideoStabilizationSupported()) {
        		Log.d(" ","Enable Video Stabilization");     
        		parameters.setVideoStabilization(true);
        	}
    	}
    	
    	restartPreviewWithNewParams (parameters);
    }
    
    public void setCamera (Camera camera) {
    	Log.d(" ","Set new Camera Object");
    	mCamera = camera;
    }
    
    public void restartPreviewWithNewParams (Camera.Parameters parameters) {
    	// stop preview before making changes like setPreviewSize, setFocusMode, setPreviewFpsRange
    	Log.d(" ","Stop Preview before setting new preview settings");
    	stopPreview ();
     
        try {
        	Log.d(" ","Set Camera parameters");
        	mCamera.setParameters(parameters);
        	Log.d(" ","Current Camera Params: " + parameters.flatten()); 
        } catch (RuntimeException RE) {
			RE.printStackTrace();
		}
        
        Log.d(" ","Now start the camera preview");
	
    	try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
			isPreviewON = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void stopPreview () {
    	if (isPreviewON == true)
    	{
	    	Log.d(" ","Stop preview called");
	    	isPreviewON = false;
	    	try {
	    		mCamera.stopPreview();
	    	} catch (Exception e){
	            // ignore: tried to stop a non-existent preview
	        }
    	}
    }
    
    public void startPreview () {
    	if (isPreviewON == false)
    	{
            Log.d(" ","Start the camera preview");
            isPreviewON = true;
        	try {
    			mCamera.setPreviewDisplay(mHolder);
    			mCamera.startPreview();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	Log.d(" ","OnMeasure called");
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        Log.d(" ","OnMeasure Width = " + width + " x Height = " + height);
        mOnMeasureSize.width = width;
   		mOnMeasureSize.height = height;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	Log.d(" ","OnLayout called");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
    	Log.d(" ","OnSizeChanged called");
    }
    
    public void surfaceCreated(SurfaceHolder holder) {
    	Log.d(" ","Surface Created");
    	// Start preview in surface changed itself
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.d(" ","Surface Destroyed");
        // Android Guideline. empty. Take care of releasing the Camera preview in your activity.
    }

    @SuppressLint("NewApi")
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	Log.d(" ","Surface Changed with camera object = " + mCamera + " width = " + w + " x Height = " + h);
    	if (mCamera != null)
    	{
    		// If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.
    		if (mHolder.getSurface() == null) {
    			return;
    		}
    		
         refreshCameraPreview (mCamera);// start preview with new camera parameters set
    	}
    	mIsSurfaceCreated = true;
    }

    public void setPreviewCallback (Camera.PreviewCallback cb) {
    	if (mCamera != null)
    	{
    		Log.d(" ","set Camera Preview Callback");
    		mCamera.setPreviewCallback(cb);
    	}
    }
    
    private Size getOptimalPreviewSize(int w, int h) {
    	Log.d(" ","Trying to get optimal preview size");
    	List<Size> sizes = null;
       	if (mCamera != null) 
    	{
       		sizes = mCamera.getParameters().getSupportedPreviewSizes();
    		Log.d(" ","List of supported preview sizes");       
    		for (Size size : sizes) {
    			Log.d(" ","Height = " + size.height + " Width = " + size.width);            	
		  }
    	}
       	
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;
        
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        mBestChoiseWidth = optimalSize.width;
   		mBestChoiseHeight = optimalSize.height;
        return optimalSize;
    }
    
    private String getAutoFocusMode (Camera.Parameters parameters)
    {
    	List<String> autoFocusModesList = parameters.getSupportedFocusModes ();
    	if ((autoFocusModesList != null) && (autoFocusModesList.size() > 0)) {
	    	Log.d(" ","Supported autofocus modes ");
	    	for (String AFM : autoFocusModesList) {
	    		Log.d(" ","autofocus mode: " + AFM);
	    	}
	    	
	    	String autoFocusMode = parameters.getFocusMode();
	        Log.d(" ","current AutoFocus Mode = " + autoFocusMode);
	        
	        if (android.os.Build.VERSION.SDK_INT >= 9) {
	        	if (autoFocusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
	        		//Log.d(" ","suggested autofocus mode = " + Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
	        		return Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
	        	} else {
	        		if (autoFocusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
	        			//Log.d(" ","suggested autofocus mode = " + Camera.Parameters.FOCUS_MODE_AUTO);
		                return Camera.Parameters.FOCUS_MODE_AUTO;
	        		} else {
	        			//Log.d(" ","return current AutoFocus Mode = " + autoFocusMode);
	        			return parameters.getFocusMode();
	        		}
	        	}
	        } else {
        		if (autoFocusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
        			//Log.d(" ","suggested autofocus mode = " + Camera.Parameters.FOCUS_MODE_AUTO);
	                return Camera.Parameters.FOCUS_MODE_AUTO;
        		} else {
        			//Log.d(" ","return current AutoFocus Mode = " + autoFocusMode);
        			return parameters.getFocusMode();
        		}
	        } 
        } else {
        	Log.d(" ","Autofocus is not supported for this camera");
        	return null;
        }
    }
    
    @SuppressLint("NewApi")
	private void logCameraInfoParams() {
    	if (android.os.Build.VERSION.SDK_INT >= 9) 
    	{
	   	    int numOfcameras = Camera.getNumberOfCameras ();
	   	    for (int camID = 0; camID < numOfcameras; camID++)
	   	    {
		    	Log.d(" ","Camera Info ID = " + camID);
	   	    	Camera.CameraInfo camInfo = new Camera.CameraInfo();
	   	    	Camera.getCameraInfo(camID, camInfo);
	   	    	if (camInfo != null)
	   	    	{
	   	    		String camFacing = "front"; 
	   	    		if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
	   	    		{
	   	    			camFacing = "Back";
	   	    		} else {
	   	    			camFacing = "Front";
	   	    		}
	   	    		Log.d(" ","Camera Info Facing = " + camFacing);
	   	    		Log.d(" ","Camera Info Orientation = " + camInfo.orientation);
	 	    		//Log.d(" ","Camera Info can disableShutterSound = " + camInfo.canDisableShutterSound);
	   	    	}
	   	    }
    	}
    	
    	if (android.os.Build.VERSION.SDK_INT >= 16) {
            List<Integer> PreviewList = mCamera.getParameters().getSupportedPreviewFormats();
            Log.d(" ","Supported Preview image formats");       
            for (Integer prev : PreviewList) {
            	 Log.d(" ","Supported Image Format = " + prev);            	
            }
    	}
	}
    
    @SuppressLint("NewApi")
    /** This method determines camera parameters used while setting up MediRecorder API.
     * This method will be used till Android <= ICS */
	public Camera.Parameters determineCameraParamsforMediaRecorder (Camera camera)
    {
    	Camera.Parameters parameters = camera.getParameters();
		/*----------------- Enable recording hint ---------------*/
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			Log.d(" ","Enable Recording hint");
			parameters.setRecordingHint(true);
		}
		
    	/*----------------- Enable Video Autofocus ---------------*/
    	String autoFocusMode = getAutoFocusMode(parameters);
    	if (autoFocusMode != null) {
	    	parameters.setFocusMode(autoFocusMode);
	        Log.d(" ","Setting AutoFocus Mode to = " + parameters.getFocusMode());
    	}
    	
    	/*----------------- Enable Video Stabilisation ---------------*/
    	if (android.os.Build.VERSION.SDK_INT >= 15)
    	{
        	if (parameters.isVideoStabilizationSupported()) {
        		Log.d(" ","Enable Video Stabilization");     
        		parameters.setVideoStabilization(true);
        	}
    	}
		Log.d(" ","Current Camera Params: " + parameters.flatten());
		return parameters;
    }
}