package ph.com.cdu.cherryradio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class SingleItemView extends Activity {
	
	// SESSION CLASS
    TextView txtfromTime;
    TextView txttoTime;
    TextView txtsessionDesc;	
    TextView txtsongName;
	String fromTime;
	String toTime;
	String sessionDesc;
	String songName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singleitemview);
		// Retrieve data from MainActivity on item click event
		Intent i = getIntent();
		// Get the results of description
		fromTime = i.getStringExtra("fromTime");
		toTime = i.getStringExtra("toTime");
		sessionDesc = i.getStringExtra("sessionDesc");
		songName = i.getStringExtra("songName");

		// Locate the TextViews in singleitemview.xml
		txtfromTime = (TextView) findViewById(R.id.fromTime);
		txttoTime = (TextView) findViewById(R.id.toTime);
		txtsessionDesc = (TextView) findViewById(R.id.sessionDesc);
		txtsongName = (TextView) findViewById(R.id.songName);

		// Load the results into the TextViews
		txtfromTime.setText(fromTime);
		txttoTime.setText(toTime);
		txtsessionDesc.setText(sessionDesc);
		txtsongName.setText(songName);
	}
}