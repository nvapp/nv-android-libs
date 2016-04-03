package com.nvapp.activity;

import com.nvapp.service.LocThreadService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public abstract class FormWithLocServiceActivity extends FormActivity {
	private boolean mBound = false;
	private Messenger mRemoteService = null;
	private ServiceConnection mRemoteConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mRemoteService = new Messenger(service);
			mBound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			mRemoteService = null;
			mBound = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		System.out.println("FormWithLocServiceActivity...");

		Intent intent = new Intent(this, LocThreadService.class);
		bindService(intent, mRemoteConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		unbindService(mRemoteConnection);
		super.onDestroy();
	}

	/**
	 * 发送定位消息
	 * 
	 * @param message
	 *            定位消息
	 */
	public void sendLocationMessage(Message message) {
		try {
			if (mBound) {
				this.mRemoteService.send(message);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
