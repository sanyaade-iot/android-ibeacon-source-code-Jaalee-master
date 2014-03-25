package com.jaalee.sdk.connection;

import java.util.UUID;
/**
 * http://www.jaalee.com/
 * Jaalee, Inc.
 * This project is for developers, not for commercial purposes.
 * For the source codes which can be used for commercial purposes, please contact us directly.
 * 
 * @author Alvin.Bert
 * 
 * Alvin.Bert.hu@gmail.com
 * 
 * Service@jaalee.com
 * 
 */
public class JaaleeUuid
{
	public static final UUID JAALEE_BEACON_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
	public static final UUID BEACON_KEEP_CONNECT_CHAR = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
	public static final UUID BEACON_UUID_CHAR = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
	public static final UUID MAJOR_CHAR = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
	public static final UUID MINOR_CHAR = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
	public static final UUID POWER_CHAR = UUID.fromString("0000fff5-0000-1000-8000-00805f9b34fb");
	public static final UUID CHANGE_PASSWORD_CHAR = UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");	
	public static final UUID ADVERTISING_INTERVAL_CHAR = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");
	
	public static final UUID BEACON_ALERT = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	public static final UUID BEACON_ALERT_CHAR = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

	public static final UUID BEACON_NAME = UUID.fromString("0000ff80-0000-1000-8000-00805f9b34fb");
	public static final UUID BEACON_NAME_CHAR = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");

	
	public static final UUID BEACON_BATTERY_LIFE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
	public static final UUID BEACON_BATTERY_LIFE_CHAR = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
}
