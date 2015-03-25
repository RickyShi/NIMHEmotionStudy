package edu.missouri.nimh.emotion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class RecordingReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("RecordingReceiver", "received");
		String fileName = Utilities.RECORDING_CATEGORY + "." + MainActivity.ID + "." + Utilities.getFileDate();
		// Need to be modified like the format after merging
		// String prefix =
		// RECORDING_FILENAME+"."+phoneID+"."+getFileDate
		String toWrite = prepareData(context);

		try {
			Utilities.writeToFile(fileName + ".txt", toWrite);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String fileHead = getFileHead(fileName);
		// Log.d("RecordingReceiver", fileHead);
		String toSend = fileHead + toWrite;
		String enformattedData = null;
		try {
			enformattedData = Utilities.encryption(toSend);
		} catch (Exception e) {
			e.printStackTrace();
		}

		 TransmitData transmitData = new TransmitData();
		 if (Utilities.getConnectionState(context).equals("Connected")) {
			transmitData.execute(enformattedData);
		 }

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent it = new Intent(Utilities.ACTION_RECORD);
		PendingIntent piTrigger = PendingIntent.getBroadcast(context, 0, it, Intent.FLAG_ACTIVITY_NEW_TASK);

		am.setExact(AlarmManager.RTC_WAKEUP, getNextLongTime(), piTrigger);
	}

	private long getNextLongTime() {
		Calendar s = Calendar.getInstance();
		s.add(Calendar.MINUTE, 5);
		// s.add(Calendar.SECOND, 30);
		return s.getTimeInMillis();
	}

	private String prepareData(Context context) {
		String connectionState = Utilities.getConnectionState(context);
		return Utilities.getCurrentTimeStamp() + Utilities.LINEBREAK + "Connection: " + connectionState
				+ Utilities.LINEBREAK + "Battery: " + Utilities.curBatt + Utilities.LINEBREAK + Utilities.SPLIT;
	}

	private String getFileHead(String fileName) {
		StringBuilder prefix_sb = new StringBuilder(Utilities.PREFIX_LEN);
		prefix_sb.append(fileName);

		for (int i = fileName.length(); i <= Utilities.PREFIX_LEN; i++) {
			prefix_sb.append(" ");
		}
		return prefix_sb.toString();
	}

	private class TransmitData extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... strings) {
			String data = strings[0];
			// String fileName = strings[0];
			// String dataToSend = strings[1];

			HttpPost request = new HttpPost(Utilities.UPLOAD_ADDRESS);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("data", data));
			// // file_name
			// params.add(new BasicNameValuePair("file_name", fileName));
			// // data
			// params.add(new BasicNameValuePair("data", dataToSend));
			try {
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpResponse response = new DefaultHttpClient().execute(request);
				Log.d("Sensor Data Point Info", String.valueOf(response.getStatusLine().getStatusCode()));
				return true;
			} catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}

}
