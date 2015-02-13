package com.nvapp.service.mqtt.interfaces;

import com.nvapp.service.mqtt.impl.MqttException;

public interface IMqttClientFactory {
	public IMqttClient create(String host, int port, String clientId,
			IMqttPersistence persistence) throws MqttException;
}
