package com.nvapp.service;

import java.io.Serializable;

public class LocInfo implements Serializable {
	/**
	 * UID
	 */
	private static final long serialVersionUID = -8301117934624389177L;
	private double lat;
	private double lng;
	private String address;

	public LocInfo(double lat, double lng, String address) {
		this.lat = lat;
		this.lng = lng;
		this.address = address;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
