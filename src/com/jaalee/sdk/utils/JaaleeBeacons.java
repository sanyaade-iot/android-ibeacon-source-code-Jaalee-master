package com.jaalee.sdk.utils;

import com.jaalee.sdk.Beacon;
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
public class JaaleeBeacons
{
	public static boolean isJaaleeBeacon(Beacon beacon)
	{
		return ("jaalee".equalsIgnoreCase(beacon.getName()) || "WWW.JAALEE.COM".equalsIgnoreCase(beacon.getName()) 
				|| "EBEFD083-70A2-47C8-9837-E7B5634DF524".equalsIgnoreCase(beacon.getProximityUUID()));
	}

	public static boolean isEstimoteBeacon(Beacon beacon)
	{
		return isJaaleeBeacon(beacon);
	}
}
