package mr.lapsecam.utils;

import java.io.IOException;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;

public class TimeLapseVideoEncoder implements MediaRecorder.OnErrorListener,MediaRecorder.OnInfoListener {
	
	private MediaRecorder 					mMediaRecorder;	
	private Camera 							mCamera = null;
	private CamcorderProfile 				mCamCorderProfile = null;
	private LapsecamVideoPreview			mVideoPreview = null;
	private boolean 						mMediaRecorderRecording = false;
	private int 							mVideoBitRate = 0;
	private int 							mVideoHeight;
	private int 							mVideoWidth;
	private int 							mVideoCodec;
	private int 							mVideoFPS;
	private double							mLapsedCaptureRate;
	private int 							mRecordingLength = 0;
	private boolean 						mTimeLimited = false;
    private String  						mOutputFilePath = null;
	
	
	public int getmVideoFPS() {
		return mVideoFPS;
	}
	
	public int getmVideoWidth() {
		return mVideoWidth;
	}
	
	public int getmVideoHeight() {
		return mVideoHeight;
	}

	public void setTimeLimit(int time) {
		mRecordingLength = time;
		mTimeLimited = true;
	}	
	
	public TimeLapseVideoEncoder(Camera camera, final LapsecamVideoPreview VideoPreview, 
								 double lapsedCaptureRate, int LapseQuality, String outputFilePath) {
        mCamera = camera;
        mVideoPreview = VideoPreview;
		mOutputFilePath = outputFilePath;
		mCamCorderProfile = CamcorderProfile.get(LapseQuality);
		mLapsedCaptureRate = lapsedCaptureRate;
		Log.d("","Video Encoder Profile Set with values: " +
				" Width = " + mCamCorderProfile.videoFrameWidth + " x Height = " + mCamCorderProfile.videoFrameHeight + 
				" O/p File Format = " + mCamCorderProfile.fileFormat + " Codec = " + mCamCorderProfile.videoCodec + " Bitrate = " + mCamCorderProfile.videoBitRate + 
				" quality = " + mCamCorderProfile.quality +   " FrameRate = " + mCamCorderProfile.videoFrameRate  + 
				" duration = " + mCamCorderProfile.duration);
		mVideoWidth = mCamCorderProfile.videoFrameWidth;
		mVideoHeight = mCamCorderProfile.videoFrameHeight;
		mVideoFPS = mCamCorderProfile.videoFrameRate;
	}

	public void startVideoRecording() throws IllegalStateException, IOException {
	    if (!mMediaRecorderRecording) {
	    	if (initializeVideo() == true) {
	    		Log.d("","Initialize Video Succeeded\n");
	            try {
	                Log.d("", "Starting Video Recorder");
	                mMediaRecorder.start();
	                mMediaRecorderRecording = true;
	            } catch (final IllegalStateException isException) {
	            	Log.d("","TLError: Illegal State Exception in Media Recorder Start");
	            	isException.printStackTrace();
	                freeMediaRecorder();
	                throw isException;
	            }
	    		Log.d("", "Video recording started");
	    		return;
	    	}
	    	else {
	    		Log.d("","TLError: Inititalize Video Failed\n");
	    		return;
	    	}
	    }
	}
		
	public void stopVideoRecording () 
	{
	    if (mMediaRecorderRecording) {
	        Log.d("", "Stop Video Recording");
            try {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.stop();    
                Log.d("","Media recorder stopped");
            } catch (final RuntimeException runtimeException) {
                Log.d("","TLError: Media recorder stop failed " + runtimeException);
            }
            mMediaRecorderRecording = false;
            freeMediaRecorder();

            //restart the preview after stoping mediarecorder
            mVideoPreview.startPreview();
	    }
	}
	
    private void freeMediaRecorder() {
        if (mMediaRecorder != null) 
        {
        	Log.d("","Freeing up media recorder.");
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
    
	private boolean initializeVideo() throws IllegalStateException, IOException {
		Log.d("", "Video Initialization Started");    
		
		if (mMediaRecorder == null) {
			mMediaRecorder = new MediaRecorder();
		} else {
			mMediaRecorder.reset();
		}
		mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOnInfoListener(this);

        mVideoPreview.stopPreview();

        if (!setupCamera()) {
			Log.d("", "TLError: failed inside setupCameraParams");
			return false;
		}
		
		// Setup the Media recorder with the camera found
		mMediaRecorder.setCamera(mCamera);
		Log.d("", "Camera set for Media Recorder");
		
		Log.d("", "Video Recorder profile settings : VBitrate = [" + mVideoBitRate + "] Width = [" + mVideoWidth + "] Height = [" + mVideoHeight + "] FPS = [" + mVideoFPS + "]");
		try {
			mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			
			mMediaRecorder.setProfile(mCamCorderProfile);
			mMediaRecorder.setCaptureRate(mLapsedCaptureRate);
			
			mMediaRecorder.setOutputFile(mOutputFilePath);
	        mMediaRecorder.setPreviewDisplay(mVideoPreview.getHolder().getSurface());
	        if (mTimeLimited)
	        	mMediaRecorder.setMaxDuration(mRecordingLength);
		}
		catch(IllegalStateException isException) {
			Log.d("", "TLError: Illegal State Exception in Media Recorder parameter settings" + isException);
			isException.printStackTrace();
			throw isException;
		}
		Log.d("","start Media Recorder prepare");

        try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException isException) {
			Log.d("","TLError: IllegalStateException in MediaRecorder Prepare " + isException);
			isException.printStackTrace();
			throw isException;
		} catch (IOException ioException) {
			Log.d("","TLError: IOException in MediaRecorder Prepare " + ioException);
			ioException.printStackTrace();
			throw ioException;
		}
		Log.d("","Media Recorder prepared");
		return true;
    }
		
	private boolean setupCamera() {
		final Camera.Parameters parameters = mVideoPreview.determineCameraParamsforMediaRecorder(mCamera); 
		mVideoFPS = mVideoPreview.mBestChoiseFPS;
    	mVideoHeight = mVideoPreview.mBestChoiseHeight;
    	mVideoWidth = mVideoPreview.mBestChoiseWidth;
		
		try {
			mCamera.setParameters(parameters);
		}
		catch (RuntimeException rtException) {
			Log.d("","TLError: Couldn't set some parameters in Camera's SetParams " + rtException);
			rtException.printStackTrace();
			return false;
		}
		Log.d("","Camera parameters set"); 

		try {
			mCamera.setPreviewDisplay(mVideoPreview.getHolder());
		} catch (IOException e) {
			Log.d("","TLError: Camera couldnt be assigned a surface holder " + e);
			e.printStackTrace();
			return false;
		}
		Log.d("","Camera assigned a surface holder");
		
		// We dont preview the frames directly 			
		try {
			mCamera.unlock();
		}
		catch (final RuntimeException rtException) {
			Log.d("","TLError: Runtime exception in camera unlock " + rtException);
			rtException.printStackTrace();
			return false;
		}
		
		Log.d("","Camera unlocked for MediaRecorder");
		return true;
    }
   
	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
	    Log.d("","Media Recorder error: MediaTestRecorder" + "Code:" + what + "Extra: " + extra);
	}
	
	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
	    Log.d("","Media Recorder Info: MediaTestRecorder" + "Code:" + what + "Extra: " + extra);
	    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
	    	Log.d("","Maximum Duration Reached  which is = " + mRecordingLength/1000 + " Seconds"); 
	    	stopVideoRecording();
		}
		
	}
	
}