package mr.lapsecam.utils;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

/*
 * Supports needed
 * 1. Sd card & no sdcard hases
 * 2. Move app to sdcard
 * 3. switch camera
 * 4. zoom
 * 5. 4.1 support
 * 6. assets
 * 7. Stop locking screen for long time video capture
 * 8. keep sceen on always
 * 9. Show diaong to user for connecting to charger & tripod
 * 10.sdcard size monitor
 * 11. Handle errors
 * 12. Test on Tablets
 * 13. Option to choose quality settings
 * 
 * 
 * */

public class LapsecamApplication extends Application {
	
	public static LapsecamApplication instance; 
	public static final String VIDEO_DIR = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
			.getAbsolutePath() + "/Lapsecam/";
	
	public static String getFilePath (String filename, String Extension) {
		File f = new File(get_Media_Dir());		
		if (!f.exists())
			f.mkdirs();
		
		final StringBuilder mediaFileBuilder = new StringBuilder(200);
		mediaFileBuilder.append(VIDEO_DIR).append(filename).append(Extension);
		return mediaFileBuilder.toString();		
	}
	
	public static String get_Media_Dir () {
		return VIDEO_DIR;		
	}
	
	public LapsecamApplication ()
	{
		LapsecamApplication.instance = this;
	}
	
    public static Context getContext() {
        return LapsecamApplication.instance;
    }
    
    @Override
    public void onCreate() {
    	super.onCreate();
    }
    
    
}