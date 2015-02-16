package com.nvapp.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

/**
 * 定位线程服务
 */
public class LocThreadService extends Service {
	private LocThread mLocThread;
	private Messenger mWorkerMessager;
	private CountDownLatch cdl = new CountDownLatch(1);

	public void onCreate() {
		super.onCreate();
		mLocThread = new LocThread();
		mLocThread.start();
		initLocation();
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		mWorkerMessager = new Messenger(mLocThread.mWorkerHandler);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mWorkerMessager.getBinder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		mLocThread.quit();
		mLocationClient.stop();

		super.onDestroy();
	}

	private ArrayBlockingQueue<Messenger> replayToQueue = new ArrayBlockingQueue<Messenger>(4);

	private class LocThread extends HandlerThread {
		public LocThread() {
			super(LocThread.class.getName());
		}

		Handler mWorkerHandler;

		@Override
		protected void onLooperPrepared() {
			super.onLooperPrepared();

			mWorkerHandler = new Handler(this.getLooper()) {
				public void handleMessage(Message msg) {
					try {
						replayToQueue.put(msg.replyTo);
						mLocationClient.requestLocation();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};

			cdl.countDown();

			Looper.loop();
		}
	}

	private LocationClient mLocationClient;

	private void initLocation() {
		System.out.println("初始化定位...");
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(new MyLocationListener());

		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("gcj02");// 返回的定位结果是百度经纬度，默认值gcj02
		// option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);

		mLocationClient.start();
	}

	/**
	 * 实现实位回调监听
	 */
	private class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// Receive Location
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				sb.append("\ndirection : ");
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append(location.getDirection());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				// 运营商信息
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
			}

			System.out.println("定位结果:" + sb.toString());
			Messenger messenger = replayToQueue.poll();
			if (messenger != null) {
				try {
					LocInfo locInfo = new LocInfo(location.getLatitude(), location.getLongitude(),
							location.getAddrStr());

					messenger.send(Message.obtain(null, 999, 0, 0, locInfo));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
