

package mr.lapsecam.activity;

import mr.lapsecam.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class HomescreenActivity extends Activity {
	
	Button TL_Recorder_Button;
	Button TL_Creator_Button;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.homescreen);
		
		TL_Recorder_Button = (Button) findViewById(R.id.TimeLapserecorder);
		TL_Recorder_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent LTVideoRecorderIntent = new Intent(HomescreenActivity.this, LapsecamActivity.class);
				startActivity(LTVideoRecorderIntent);
			}
		});
		
		TL_Creator_Button = (Button) findViewById(R.id.TimeLapsecreator);
		TL_Creator_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
}