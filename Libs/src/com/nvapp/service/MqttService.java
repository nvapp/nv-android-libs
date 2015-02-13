package com.nvapp.service;

import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.nvapp.service.mqtt.impl.MqttConnectOptions;
import com.nvapp.service.mqtt.impl.MqttException;
import com.nvapp.service.mqtt.impl.MqttMessage;
import com.nvapp.service.mqtt.impl.MqttPersistenceException;
import com.nvapp.service.mqtt.impl.MqttTopic;
import com.nvapp.service.mqtt.impl.paho.PahoMqttClientFactory;
import com.nvapp.service.mqtt.interfaces.IMqttCallback;
import com.nvapp.service.mqtt.interfaces.IMqttClient;
import com.nvapp.service.mqtt.interfaces.IMqttClientFactory;
import com.nvapp.service.mqtt.interfaces.IMqttConnectOptions;
import com.nvapp.service.mqtt.interfaces.IMqttMessage;
import com.nvapp.service.mqtt.interfaces.IMqttPersistence;
import com.nvapp.service.mqtt.interfaces.IMqttTopic;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings.Secure;

public class MqttService extends Service implements IMqttCallback {
	/************************************************************************/
	/* CONSTANTS */
	/************************************************************************/
	public static final String APP_ID = "com.nvapp.mqtt";

	// constants used to notify the Activity UI of received messages
	public static final String MQTT_MSG_RECEIVED_INTENT = "com.qonect.services.mqtt.MSGRECVD";
	public static final String MQTT_MSG_RECEIVED_TOPIC = "com.qonect.services.mqtt.MSGRECVD_TOPIC";
	public static final String MQTT_MSG_RECEIVED_MSG = "com.qonect.services.mqtt.MSGRECVD_MSG";
	//
	// // constants used to notify the Service of messages to send
	public static final String MQTT_PUBLISH_MSG_INTENT = "com.qonect.services.mqtt.SENDMSG";
	public static final String MQTT_PUBLISH_MSG_TOPIC = "com.qonect.services.mqtt.SENDMSG_TOPIC";
	public static final String MQTT_PUBLISH_MSG = "com.qonect.services.mqtt.SENDMSG_MSG";

	// constant used internally to schedule the next ping event
	public static final String MQTT_PING_ACTION = "nv.app.af.mqtt.PING";
	public static final String MQTT_STATUS_INTENT = "com.qonect.services.mqtt.STATUS";
	public static final String MQTT_STATUS_CODE = "com.qonect.services.mqtt.STATUS_CODE";
	public static final String MQTT_STATUS_MSG = "com.qonect.services.mqtt.STATUS_MSG";

	// // constants used by status bar notifications
	// public static final int MQTT_NOTIFICATION_ONGOING = 1;
	// public static final int MQTT_NOTIFICATION_UPDATE = 2;

	// MQTT连接状态
	public enum ConnectionStatus {
		INITIAL, // 初始状态
		CONNECTING, // 连接中
		CONNECTED, // 连接完成
		NOTCONNECTED_WAITINGFORINTERNET, // 没有网络连接，缺少网络连接
		NOTCONNECTED_USERDISCONNECT, // 用户断开连接
		NOTCONNECTED_DATADISABLED, // 不能进行数据连接
		NOTCONNECTED_UNKNOWNREASON // 未知原因
	}

	// MQTT 常量
	public static final int MAX_MQTT_CLIENTID_LENGTH = 22;

	/************************************************************************/
	/* VARIABLES used to maintain state */
	/************************************************************************/

	// status of MQTT client connection
	private ConnectionStatus connectionStatus = ConnectionStatus.INITIAL;
	private Timestamp connectionStatusChangeTime;

	/************************************************************************/
	/* VARIABLES used to configure MQTT connection */
	/************************************************************************/
	private String brokerHostName = "192.168.1.200";
	private List<IMqttTopic> topics = new ArrayList<IMqttTopic>();

	private int brokerPortNumber = 1883;
	private IMqttPersistence usePersistence = null;
	private boolean cleanStart = false;
	private String username = "guest";
	private char[] password = "guest".toCharArray();

	// how often should the app ping the server to keep the connection alive?
	//
	// too frequently - and you waste battery life
	// too infrequently - and you wont notice if you lose your connection
	// until the next unsuccessfull attempt to ping
	//
	// it's a trade-off between how time-sensitive the data is that your
	// app is handling, vs the acceptable impact on battery life
	//
	// it is perhaps also worth bearing in mind the network's support for
	// long running, idle connections. Ideally, to keep a connection open
	// you want to use a keep alive value that is less than the period of
	// time after which a network operator will kill an idle connection
	private short keepAliveSeconds = 20 * 60;

	// This is how the Android client app will identify itself to the
	// message broker.
	// It has to be unique to the broker - two clients are not permitted to
	// connect to the same broker using the same client ID.
	/**
	 * 客户端Id，不能2个客户端使用同一客户端Id连接到broker
	 */
	private String mqttClientId = null;

	/************************************************************************/
	/* VARIABLES - other local variables */
	/************************************************************************/
	// connection to the message broker
	private IMqttClient mqttClient = null;
	private IMqttClientFactory mqttClientFactory;

	// receiver that notifies the Service when the phone gets data connection
	private NetworkConnectionIntentReceiver netConnReceiver;

	// receiver that wakes the Service up when it's time to ping the server
	private PingSender pingSender;

	private ExecutorService executor;

	@Override
	public void onCreate() {
		super.onCreate();

		changeStatus(ConnectionStatus.INITIAL);

		mBinder = new LocalBinder<MqttService>(this);
		topics.add(new MqttTopic("test-topic"));

		mqttClientFactory = new PahoMqttClientFactory();

		executor = Executors.newFixedThreadPool(2);
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		doStart(intent, startId);
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, final int startId) {
		doStart(intent, startId);

		return START_STICKY;
	}

	private void doStart(final Intent intent, final int startId) {
		initMqttClient();

		executor.submit(new Runnable() {
			@Override
			public void run() {
				handleStart(intent, startId);
			}
		});
	}

	protected void onConnect() {
		subscribeToTopics();
	}

	synchronized void handleStart(Intent intent, int startId) {
		if (mqttClient == null) {

			stopSelf();
			return;
		}

		if (connectionStatus == ConnectionStatus.NOTCONNECTED_USERDISCONNECT) {

			return;
		}

		if (!isBackgroundDataEnabled()) {
			changeStatus(ConnectionStatus.NOTCONNECTED_DATADISABLED);

			broadcastServiceStatus("Not connected - background data disabled @ "
					+ getConnectionChangeTimestamp());

			return;
		}

		if (!isConnected()) {
			changeStatus(ConnectionStatus.CONNECTING);

			if (isOnline()) {
				if (connectToBroker()) {
					onConnect();
				}
			} else {
				changeStatus(ConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET);

				broadcastServiceStatus("Waiting for network connection @ "
						+ getConnectionChangeTimestamp());
			}
		}

		if (netConnReceiver == null) {
			netConnReceiver = new NetworkConnectionIntentReceiver();
			registerReceiver(netConnReceiver, new IntentFilter(
					ConnectivityManager.CONNECTIVITY_ACTION));
		}

		if (pingSender == null) {
			pingSender = new PingSender();
			registerReceiver(pingSender, new IntentFilter(MQTT_PING_ACTION));
		}

		if (!handleStartAction(intent)) {

			rebroadcastStatus();
		}
	}

	private boolean handleStartAction(Intent intent) {
		String action = intent.getAction();

		if (action == null) {
			return false;
		}

		if (action.equalsIgnoreCase(MQTT_PUBLISH_MSG_INTENT)) {
			handlePublishMessageIntent(intent);
		}

		return true;
	}

	@Override
	public void onDestroy() {
		// disconnect immediately
		disconnectFromBroker();

		// inform the app that the app has successfully disconnected
		broadcastServiceStatus("Disconnected @ "
				+ getConnectionChangeTimestamp());

		if (mBinder != null) {
			mBinder.close();
			mBinder = null;
		}

		super.onDestroy();
	}

	/************************************************************************/
	/* METHODS - broadcasts and notifications */
	/************************************************************************/

	// methods used to notify the Activity UI of something that has happened
	// so that it can be updated to reflect status and the data received
	// from the server

	private void broadcastServiceStatus(String statusDescription) {
		// inform the app (for times when the Activity UI is running /
		// active) of the current MQTT connection status so that it
		// can update the UI accordingly

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(MQTT_STATUS_INTENT);
		broadcastIntent.putExtra(MQTT_STATUS_CODE, connectionStatus.ordinal());
		broadcastIntent.putExtra(MQTT_STATUS_MSG, statusDescription);
		sendBroadcast(broadcastIntent);
	}

	private void broadcastReceivedMessage(String topic, byte[] message) {
		// pass a message received from the MQTT server on to the Activity UI
		// (for times when it is running / active) so that it can be displayed
		// in the app GUI

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(MQTT_MSG_RECEIVED_INTENT);
		broadcastIntent.putExtra(MQTT_MSG_RECEIVED_TOPIC, topic);
		broadcastIntent.putExtra(MQTT_MSG_RECEIVED_MSG, message);
		sendBroadcast(broadcastIntent);
	}

	/************************************************************************/
	/* METHODS - binding that allows access from the Actitivy */
	/************************************************************************/

	// trying to do local binding while minimizing leaks - code thanks to
	// Geoff Bruckner - which I found at
	// http://groups.google.com/group/cw-android/browse_thread/thread/d026cfa71e48039b/c3b41c728fedd0e7?show_docid=c3b41c728fedd0e7

	private LocalBinder<MqttService> mBinder;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder<S> extends Binder {
		private WeakReference<S> mService;

		public LocalBinder(S service) {
			mService = new WeakReference<S>(service);
		}

		public S getService() {
			return mService.get();
		}

		public void close() {
			mService = null;
		}
	}

	//
	// public methods that can be used by Activities that bind to the Service
	//

	public ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

	public void rebroadcastStatus() {
		String status = "";

		switch (connectionStatus) {
		case INITIAL:
			status = "Please wait";
			break;
		case CONNECTING:
			status = "Connecting @ " + getConnectionChangeTimestamp();
			break;
		case CONNECTED:
			status = "Connected @ " + getConnectionChangeTimestamp();
			break;
		case NOTCONNECTED_UNKNOWNREASON:
			status = "Not connected - waiting for network connection @ "
					+ getConnectionChangeTimestamp();
			break;
		case NOTCONNECTED_USERDISCONNECT:
			status = "Disconnected @ " + getConnectionChangeTimestamp();
			break;
		case NOTCONNECTED_DATADISABLED:
			status = "Not connected - background data disabled @ "
					+ getConnectionChangeTimestamp();
			break;
		case NOTCONNECTED_WAITINGFORINTERNET:
			status = "Unable to connect @ " + getConnectionChangeTimestamp();
			break;
		}

		// inform the app that the Service has successfully connected
		broadcastServiceStatus(status);
	}

	public void disconnect() {
		disconnectFromBroker();

		// set status
		changeStatus(ConnectionStatus.NOTCONNECTED_USERDISCONNECT);

		// inform the app that the app has successfully disconnected
		broadcastServiceStatus("Disconnected");
	}

	/************************************************************************/
	/* METHODS - MQTT methods inherited from MQTT classes */
	/************************************************************************/

	/*
	 * callback - method called when we no longer have a connection to the
	 * message broker server
	 */
	public void connectionLost(Throwable t) {
		// we protect against the phone switching off while we're doing this
		// by requesting a wake lock - we request the minimum possible wake
		// lock - just enough to keep the CPU running until we've finished
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
		wl.acquire();

		//
		// have we lost our data connection?
		//

		if (isOnline() == false) {
			changeStatus(ConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET);

			// inform the app that we are not connected any more
			broadcastServiceStatus("Connection lost - no network connection");
		} else {
			changeStatus(ConnectionStatus.NOTCONNECTED_UNKNOWNREASON);
			broadcastServiceStatus("Connection lost - reconnecting...");
		}

		wl.release();
	}

	@Override
	public void messageArrived(IMqttTopic topic, IMqttMessage message)
			throws Exception {
		// we protect against the phone switching off while we're doing this
		// by requesting a wake lock - we request the minimum possible wake
		// lock - just enough to keep the CPU running until we've finished
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
		wl.acquire();

		try {
			broadcastReceivedMessage(topic.getName(), message.getPayload());
		} catch (MqttException e) {
			e.printStackTrace();
		}

		scheduleNextPing();

		wl.release();
	}

	private void initMqttClient() {
		if (mqttClient != null) {
			return;
		}

		try {
			mqttClient = mqttClientFactory.create(brokerHostName,
					brokerPortNumber, getClientId(), usePersistence);
			mqttClient.setCallback(this);
		} catch (MqttException e) {
			mqttClient = null;
			changeStatus(ConnectionStatus.NOTCONNECTED_UNKNOWNREASON);
			broadcastServiceStatus("Invalid connection parameters");
		}
	}

	/*
	 * (Re-)connect to the message broker
	 */
	private boolean connectToBroker() {
		try {
			IMqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(cleanStart);
			options.setKeepAliveInterval(keepAliveSeconds);
			options.setUserName(username);
			options.setPassword(password);

			mqttClient.connect(options);

			changeStatus(ConnectionStatus.CONNECTED);

			broadcastServiceStatus("Connected @ "
					+ getConnectionChangeTimestamp());

			scheduleNextPing();

			return true;
		} catch (MqttException e) {
			changeStatus(ConnectionStatus.NOTCONNECTED_UNKNOWNREASON);

			broadcastServiceStatus("Unable to connect @ "
					+ getConnectionChangeTimestamp());

			scheduleNextPing();

			return false;
		}
	}

	private void subscribeToTopics() {
		boolean subscribed = false;

		if (!isConnected()) {
			System.out.println("Unable to subscribe as we are not connected");
		} else {
			try {
				mqttClient.subscribe(topics.toArray(new IMqttTopic[topics
						.size()]));

				subscribed = true;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}

		if (subscribed == false) {
			broadcastServiceStatus("Unable to subscribe @ "
					+ getConnectionChangeTimestamp());
		}
	}

	/*
	 * Terminates a connection to the message broker.
	 */
	private void disconnectFromBroker() {
		// if we've been waiting for an Internet connection, this can be
		// cancelled - we don't need to be told when we're connected now
		try {
			if (netConnReceiver != null) {
				unregisterReceiver(netConnReceiver);
				netConnReceiver = null;
			}

			if (pingSender != null) {
				unregisterReceiver(pingSender);
				pingSender = null;
			}
		} catch (Exception eee) {
			eee.printStackTrace();
		}

		try {
			if (mqttClient != null && mqttClient.isConnected()) {
				try {
					mqttClient.disconnect();
				} catch (MqttException e) {
					e.printStackTrace();
				} catch (MqttPersistenceException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mqttClient = null;
		}

		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancelAll();
	}

	private void scheduleNextPing() {
		PendingIntent pendingIntent = PendingIntent
				.getBroadcast(this, 0, new Intent(MQTT_PING_ACTION),
						PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar wakeUpTime = Calendar.getInstance();
		wakeUpTime.add(Calendar.SECOND, keepAliveSeconds);

		AlarmManager aMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
		aMgr.set(AlarmManager.RTC_WAKEUP, wakeUpTime.getTimeInMillis(),
				pendingIntent);
	}

	/************************************************************************/
	/* METHODS - internal utility methods */
	/************************************************************************/
	private boolean isConnected() {
		return ((mqttClient != null) && (mqttClient.isConnected() == true));
	}

	@SuppressWarnings("deprecation")
	private boolean isBackgroundDataEnabled() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		// Only on pre-ICS platforms, backgroundDataSettings API exists
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return cm.getBackgroundDataSetting();
		}

		// On ICS platform and higher, define BackgroundDataSetting by checking
		// if
		// phone is online
		return isOnline();
	}

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		return netInfo != null && netInfo.isAvailable()
				&& netInfo.isConnected();
	}

	private String getClientId() {
		if (mqttClientId == null) {
			String android_id = Secure.getString(getContentResolver(),
					Secure.ANDROID_ID);
			mqttClientId = android_id;

			if (mqttClientId.length() > MAX_MQTT_CLIENTID_LENGTH) {
				mqttClientId = mqttClientId.substring(0,
						MAX_MQTT_CLIENTID_LENGTH);
			}
		}

		return mqttClientId;
	}

	private void changeStatus(ConnectionStatus newStatus) {
		connectionStatus = newStatus;
		connectionStatusChangeTime = new Timestamp(new Date().getTime());
	}

	private String getConnectionChangeTimestamp() {
		return connectionStatusChangeTime.toString();
	}

	private void handlePublishMessageIntent(Intent intent) {
		boolean isOnline = isOnline();
		boolean isConnected = isConnected();

		if (!isOnline || !isConnected) {
			return;
		}

		byte[] payload = intent.getByteArrayExtra(MQTT_PUBLISH_MSG);

		try {
			mqttClient.publish(new MqttTopic("test-topic"), new MqttMessage(
					payload));
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 网络连接失去时处理
	 */
	private class NetworkConnectionIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			WakeLock wl = pm
					.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
			wl.acquire();

			if (isOnline() && !isConnected()) {
				doStart(null, -1);
			}

			wl.release();
		}
	}

	/*
	 * keep-alive
	 */
	public class PingSender extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (isOnline() && !isConnected()) {
				doStart(null, -1);
			} else if (!isOnline()) {
				System.out.println("Waiting for network to come online again");
			} else {
				try {
					mqttClient.ping();
				} catch (MqttException e) {
					try {
						try {
							mqttClient.disconnect();
						} catch (MqttException e1) {
							e1.printStackTrace();
						} catch (MqttPersistenceException e1) {
							e1.printStackTrace();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}

					doStart(null, -1);
				}
			}

			scheduleNextPing();
		}
	}
}