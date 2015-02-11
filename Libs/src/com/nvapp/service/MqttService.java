package com.nvapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Mqtt 服务
 */
public class MqttService extends Service {
	private IBinder mIBinder = new MqttBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mIBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return false;
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDestroy() {
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	private class MqttBinder extends IMqtt.Stub {

		@Override
		public void subscribe(String topic) throws RemoteException {

		}
	}
}
