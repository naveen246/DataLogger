package in.digitrack.datalogger;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;


public class DataLog {
	private String fileName;
	private Context context;
	private long lastLogTime;
	
	public DataLog(String file, Context c) {
		fileName = file;
		context = c;
	}
	
	public void appendData(String line) {
		File root = Environment.getExternalStorageDirectory();
		 try {
			 FileOutputStream f = new FileOutputStream(new File(root, fileName), true);
			 f.write(line.getBytes());
			 f.close();
		 } catch (Exception e) {
			 Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
		 }
	}

	public long getLastLogTime() {
		return lastLogTime;
	}

	public void setLastLogTime(long lastLogTime) {
		this.lastLogTime = lastLogTime;
	}
}
