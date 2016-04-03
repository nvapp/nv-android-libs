package com.nvapp.service.mqtt.interfaces;

import com.nvapp.service.mqtt.impl.MqttException;

public interface IMqttMessage
{
	public int getQoS();
	public byte[] getPayload() throws MqttException;
	public boolean isRetained();	
	public boolean isDuplicate();
}
