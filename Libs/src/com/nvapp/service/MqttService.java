package com.nvapp.service;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Mqtt 服务
 */
public class MqttService extends Service {
	public static final String ACTION_MQTT_MESSAGE_ARRIVE = "mqtt_all_message_action";

	/**
	 * 服务器监听主题
	 */
	// private static final String TOPIC_WEBRTC_CMD = "WebRTC/CMD";
	/**
	 * 服务质量
	 */
	private QoS qos = QoS.AT_MOST_ONCE;

	/**
	 * MQTT 服务器
	 */
	private MQTT mqtt = new MQTT();
	/**
	 * 连接
	 */
	private CallbackConnection connection;

	private IBinder mIBinder = new MqttBinder();

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("mqtt", "bind MqttService");

		return mIBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("mqtt", "unbind MqttService");

		return false;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d("mqtt", "create MqttService");
		start();
	}

	@Override
	public void onDestroy() {
		Log.d("mqtt", "destroy MqttService");

		super.onDestroy();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	/**
	 * 服务启动
	 */
	private void start() {
		try {
			mqtt.setHost("192.168.1.200", 1883);
			mqtt.setKeepAlive((short) 60);
			mqtt.setVersion("3.1.1");

			connection = mqtt.callbackConnection();
			connection.listener(new Listener() {

				public void onConnected() {
					System.out.println("连接到服务器");
				}

				public void onDisconnected() {
					System.out.println("服务器端口");
				}

				/**
				 * 订阅消息处理
				 */
				@Override
				public void onPublish(UTF8Buffer topic, Buffer body,
						Runnable ack) {
					try {
						String message = new String(body.toByteArray());
						System.out.println("收到消息:");
						System.out.println(message);

						ack.run();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				public void onFailure(Throwable value) {
				}
			});

			connection.resume();
			connection.connect(new Callback<Void>() {
				public void onFailure(Throwable value) {
					value.printStackTrace();
				}

				public void onSuccess(Void value) {
					final Topic[] tps = { new Topic("IM/7002/13060005663",
							QoS.AT_MOST_ONCE) };

					connection.subscribe(tps, new Callback<byte[]>() {
						public void onSuccess(byte[] value) {
							for (int i = 0; i < value.length; i++) {
								System.out.println("Subscribed to Topic: "
										+ tps[i].name() + " with QoS: "
										+ QoS.values()[value[i]]);
							}
						}

						public void onFailure(Throwable value) {
							System.out.println("Subscribe failed: " + value);
							value.printStackTrace();
						}
					});
				}
			});
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param topic
	 *            主题
	 * @param msg
	 *            消息内容
	 */
	public void publish(final String topic, final byte[] msg) {
		connection.publish(topic, msg, qos, false, new Callback<Void>() {

			public void onSuccess(Void value) {
				System.out.println("Sent message:" + new String(msg));
			}

			public void onFailure(Throwable value) {
				System.out.println("发送 message失败");
			}
		});
	}

	private class MqttBinder extends IMqtt.Stub {

		@Override
		public void sendMessage(String topic, final String message)
				throws RemoteException {
			try {
				connection.publish(topic, message.getBytes("UTF-8"), qos,
						false, new Callback<Void>() {

							public void onSuccess(Void value) {
								System.out.println("Sent message:" + message);
							}

							public void onFailure(Throwable value) {
								System.out.println("发送 message失败");
							}
						});
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
}
