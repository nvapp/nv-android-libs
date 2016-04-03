package com.nvapp.service;

interface IMqtt {
	/*订阅主题*/
	//void subscribe(inout String[] topics);
	/*消息发送*/
	void sendMessage(String topic, String message);
}