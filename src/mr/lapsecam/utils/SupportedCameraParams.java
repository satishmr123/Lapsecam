
package mr.lapsecam.utils;

import java.util.List;

import android.hardware.Camera;
import android.util.Log;

public class SupportedCameraParams {
	
	public String Camera_facing = "back";
	public List<String> Color_modes_List;
	public List<String> AutoFocus_Modes_List;
	public List<String> Scene_Modes_List;
	public List<String> Whitebalance_List;
	public List<String> FlashMode_List;
	
	
	public SupportedCameraParams (Camera.Parameters camParamas) {
		
		/*----------------Color Effects -----------------------------*/
		Color_modes_List = camParamas.getSupportedColorEffects();
		if (Color_modes_List != null)
		{
			Log.d(" ","Supported " +  Color_modes_List.size() + " color effects are " + Color_modes_List.toString());
		} else {
			Log.d(" ","Color Modes are not supported");
		}
		
		/*----------------- Video Autofocus Modes ---------------*/
    	AutoFocus_Modes_List = camParamas.getSupportedFocusModes ();
		if (AutoFocus_Modes_List != null)
		{
			Log.d(" ","Supported " +  AutoFocus_Modes_List.size() + " Autofocus modes are " + AutoFocus_Modes_List.toString());
		} else {
			Log.d(" ","Autofocus modes are not supported");
		}
		
		/*----------------- Video Scene Modes ---------------*/
		Scene_Modes_List = camParamas.getSupportedSceneModes  ();
		if (Scene_Modes_List != null)
		{
			Log.d(" ","Supported " +  Scene_Modes_List.size() + " Scene modes are " + Scene_Modes_List.toString());
		} else {
			Log.d(" ","Scene modes are not supported");
		}
		
		/*----------------- whiteBalance Modes ---------------*/
		Whitebalance_List = camParamas.getSupportedWhiteBalance ();
		if (Whitebalance_List != null)
		{
			Log.d(" ","Supported " +  Whitebalance_List.size() + " Whitebalance modes are " + Whitebalance_List.toString());
		} else {
			Log.d(" ","Whitebalance modes are not supported");
		}
		
		/*----------------- Flash Modes ---------------*/
		FlashMode_List = camParamas.getSupportedFlashModes ();
		if (FlashMode_List != null)
		{
			Log.d(" ","Supported " +  FlashMode_List.size() + " FlashModes are " + FlashMode_List.toString());
		} else {
			Log.d(" ","FlashModes are not supported");
		}
	}
	
}