package edu.missouri.nimh.emotion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryInfoBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			Utilities.curBatt = String.valueOf(level * 100 / scale);
			// Log.d("BatteryInfoBroadcastReceiver", Utilities.curBatt);
		}

	}
}
