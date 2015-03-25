package edu.missouri.nimh.emotion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class ShutdownReceiver extends BroadcastReceiver {
	String fileName = Utilities.RECORDING_CATEGORY + "." + MainActivity.ID + "." + Utilities.getFileDate();
	String toWrite = Utilities.getCurrentTimeStamp() + Utilities.LINEBREAK + "Device is shutting down."
			+ Utilities.LINEBREAK + Utilities.SPLIT;

	@Override
	public void onReceive(Context context, Intent intent) {
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
