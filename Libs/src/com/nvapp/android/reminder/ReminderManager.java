package com.nvapp.android.reminder;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ReminderManager {
	private Context mContext;
	private AlarmManager mAlarmManager;

	public ReminderManager(Context context) {
		this.mContext = context;
		this.mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	public void setReminder(Calendar when) {
		Intent i = new Intent(mContext, OnAlermReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT);

		mAlarmManager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
	}
}
