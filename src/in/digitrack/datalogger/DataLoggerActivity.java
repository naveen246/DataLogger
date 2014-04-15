package in.digitrack.datalogger;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.util.Date;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DataLoggerActivity extends IOIOActivity {

	private TextView dateTextView;
	private TextView timeTextView;
	private TextView voltTextView;
	private EditText intervalEditText;
	private Button dataLogButton;
	
	private boolean isLoggingInProgress;
	private String currentData;
	DataLog dataLog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_logger);
		dateTextView = (TextView) findViewById(R.id.dateTextView);
		timeTextView = (TextView) findViewById(R.id.timeTextView);
		voltTextView = (TextView) findViewById(R.id.voltTextView);
		intervalEditText = (EditText) findViewById(R.id.intervalEditText);
		dataLogButton = (Button) findViewById(R.id.dataLogButton);
		
		isLoggingInProgress = false;
		
		dataLog = new DataLog("data_log.txt", getApplicationContext());
		
		dataLogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isLoggingInProgress) {					
					isLoggingInProgress = false;
					dataLogButton.setText(getString(R.string.start_data_log_text));
				} else {
					isLoggingInProgress = true;
					dataLogButton.setText(getString(R.string.stop_data_log_text));
				}
			}
		});
	}

	class Looper extends BaseIOIOLooper {
		private AnalogInput voltInput;

		@Override
		protected void setup() throws ConnectionLostException {
			voltInput = ioio_.openAnalogInput(39);
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			float volts = voltInput.getVoltage();
			setText(volts);
			if(isLoggingInProgress) {
				long curTimeInSec = System.currentTimeMillis() / 1000;
				int intervalSec = 20;
				try {
					intervalSec = Integer.parseInt(intervalEditText.getText().toString());
				} catch(NumberFormatException nfe) {
					Toast.makeText(getApplicationContext(), nfe.getMessage(), Toast.LENGTH_SHORT).show();
				} 
				if(curTimeInSec % intervalSec == 0 && dataLog.getLastLogTime() != curTimeInSec) {
					dataLog.appendData(currentData);
					dataLog.setLastLogTime(curTimeInSec);
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
}
