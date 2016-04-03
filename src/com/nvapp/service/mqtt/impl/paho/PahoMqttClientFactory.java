package com.nvapp.service.mqtt.impl.paho;

import com.nvapp.service.mqtt.impl.MqttException;
import com.nvapp.service.mqtt.interfaces.IMqttClient;
import com.nvapp.service.mqtt.interfaces.IMqttClientFactory;
import com.nvapp.service.mqtt.interfaces.IMqttPersistence;

public class PahoMqttClientFactory implements IMqttClientFactory {
	@Override
	public IMqttClient create(String host, int port, String clientId,
			IMqttPersistence persistence) throws MqttException {
		PahoMqttClientPersistence persistenceImpl = null;
		if (persistence != null) {
			persistenceImpl = new PahoMqttClientPersistence(persistence);
		}

		return new PahoMqttClientWrapper("tcp://" + host + ":" + port,
				clientId, persistenceImpl);
	}
}
