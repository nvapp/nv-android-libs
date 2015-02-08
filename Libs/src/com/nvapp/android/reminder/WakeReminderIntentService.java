package com.nvapp.android.reminder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public abstract class WakeReminderIntentService extends IntentService {
	public static final String LOCK_NAME_STATIC = "com.nvapp.android.reminder.Static";
	private static PowerManager.WakeLock lockStatic = null;

	public WakeReminderIntentService(String name) {
		super(name);
	}

	public abstract void doReminderWork(Intent intent);

	public static void acquireStaticLock(Context context) {
		getLock(context).acquire();
	}

	private static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic == null) {
			PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}

		return lockStatic;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			doReminderWork(intent);
		} finally {
			getLock(this).release();
		}
	}
}
