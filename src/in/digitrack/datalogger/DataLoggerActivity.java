package in.digitrack.datalogger;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DataLoggerActivity extends IOIOActivity {

	private ToggleButton button_;
	private TextView dateTextView;
	private TextView timeTextView;
	private TextView voltTextView;
	private EditText intervalEditText;
	private Button dataLogButton;
	private boolean isLoggingInProgress;
	private String currentData;
	private int lastLogTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_logger);
		button_ = (ToggleButton) findViewById(R.id.button);
		dateTextView = (TextView) findViewById(R.id.dateTextView);
		timeTextView = (TextView) findViewById(R.id.timeTextView);
		voltTextView = (TextView) findViewById(R.id.voltTextView);
		intervalEditText = (EditText) findViewById(R.id.intervalEditText);
		dataLogButton = (Button) findViewById(R.id.dataLogButton);
		
		isLoggingInProgress = false;
		
		dataLogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isLoggingInProgress) {					
					isLoggingInProgress = false;
					dataLogButton.setText("Start Data Log");
				} else {
					isLoggingInProgress = true;
					dataLogButton.setText("Stop Data Log");
				}
			}
		});
	}

	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput led_;
		private AnalogInput voltInput;

		@Override
		protected void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(0, true);
			voltInput = ioio_.openAnalogInput(39);
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			led_.write(!button_.isChecked());
			
			float volts = voltInput.getVoltage();
			setText(volts);
			if(isLoggingInProgress) {
				Calendar c = Calendar.getInstance(); 
				int seconds = c.get(Calendar.SECOND);
				int hours = c.get(Calendar.HOUR_OF_DAY);
				int minutes = c.get(Calendar.MINUTE);
				int curTimeInSec = (hours * 3600) + (minutes * 60) + seconds;
				if(seconds % 20 == 0 && lastLogTime != curTimeInSec) {
					appendToFile("data/data_log.txt", currentData);
					lastLogTime = curTimeInSec;
				}
			}
			Thread.sleep(100);
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
	
	private void setText(float volts) {
		final String date = DateFormat.format("dd-MM-yyyy", new Date()).toString();
		final String time = DateFormat.format("kk:mm:ss", new Date()).toString();
		final String voltStr = String.format("%.3f", volts);
		currentData = voltStr + "  " + date + "  " + time + "\n";
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				dateTextView.setText("Date: " + date);
				timeTextView.setText("Time: " + time);
				voltTextView.setText("Volts: " + voltStr);
			}
		});
	}
	
	private void appendToFile(String filename, String line) {
		 File root = Environment.getExternalStorageDirectory();
		 try {
			 FileOutputStream f = new FileOutputStream(new File(root, filename), true);
			 f.write(line.getBytes());
			 f.close();
		 } catch (Exception e) {
			 Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		 }
	}

}
