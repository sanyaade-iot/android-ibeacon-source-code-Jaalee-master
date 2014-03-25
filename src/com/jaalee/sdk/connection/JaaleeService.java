package com.jaalee.sdk.connection;
 
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
public class JaaleeService implements BluetoothService
{
	private final HashMap<UUID, BluetoothGattCharacteristic> characteristics = new HashMap();
 
	private final HashMap<UUID, BeaconConnection.WriteCallback> writeCallbacks = new HashMap();
 
	public void processGattServices(List<BluetoothGattService> services)
	{
		for (BluetoothGattService service : services)
			if (JaaleeUuid.JAALEE_BEACON_SERVICE.equals(service.getUuid())) {
				
				this.characteristics.put(JaaleeUuid.BEACON_KEEP_CONNECT_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_KEEP_CONNECT_CHAR));
				this.characteristics.put(JaaleeUuid.BEACON_UUID_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_UUID_CHAR));
				this.characteristics.put(JaaleeUuid.MAJOR_CHAR, service.getCharacteristic(JaaleeUuid.MAJOR_CHAR));
				this.characteristics.put(JaaleeUuid.MINOR_CHAR, service.getCharacteristic(JaaleeUuid.MINOR_CHAR));
				this.characteristics.put(JaaleeUuid.POWER_CHAR, service.getCharacteristic(JaaleeUuid.POWER_CHAR));
				this.characteristics.put(JaaleeUuid.CHANGE_PASSWORD_CHAR, service.getCharacteristic(JaaleeUuid.CHANGE_PASSWORD_CHAR));
				this.characteristics.put(JaaleeUuid.ADVERTISING_INTERVAL_CHAR, service.getCharacteristic(JaaleeUuid.ADVERTISING_INTERVAL_CHAR));
			}
	}
 
	public boolean hasCharacteristic(UUID uuid)
	{
		return this.characteristics.containsKey(uuid);
	}
	
	public String getBeaconUUID()
	{
		return this.characteristics.containsKey(JaaleeUuid.BEACON_UUID_CHAR) ?
				getStringValue(((BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.BEACON_UUID_CHAR)).getValue()) : null;
	}
	
	public String getBeaconMajor()
	{
		return this.characteristics.containsKey(JaaleeUuid.MAJOR_CHAR) ?
				getStringValue(((BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.MAJOR_CHAR)).getValue()) : null;		
	}
	
	public String getBeaconMinor()
	{
		return this.characteristics.containsKey(JaaleeUuid.MINOR_CHAR) ?
				getStringValue(((BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.MINOR_CHAR)).getValue()) : null;		
	}
	
	public String getBeaconPower()
	{
		return this.characteristics.containsKey(JaaleeUuid.POWER_CHAR) ?
				getStringValue(((BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.POWER_CHAR)).getValue()) : null;		
	}
	
	public String getBeaconBroadcastRate()
	{
		return this.characteristics.containsKey(JaaleeUuid.ADVERTISING_INTERVAL_CHAR) ?
				getStringValue(((BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.ADVERTISING_INTERVAL_CHAR)).getValue()) : null;		
	}
	
	
 
	
	public void update(BluetoothGattCharacteristic characteristic)
	{
		this.characteristics.put(characteristic.getUuid(), characteristic);
	}
 
	public Collection<BluetoothGattCharacteristic> getAvailableCharacteristics() {
		List chars = new ArrayList(this.characteristics.values());
		chars.removeAll(Collections.singleton(null));
		return chars;
	}
 
	public BluetoothGattCharacteristic beforeCharacteristicWrite(UUID uuid, BeaconConnection.WriteCallback callback) {
		this.writeCallbacks.put(uuid, callback);
		return (BluetoothGattCharacteristic)this.characteristics.get(uuid);
	}
 
	public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
		BeaconConnection.WriteCallback writeCallback = (BeaconConnection.WriteCallback)this.writeCallbacks.remove(characteristic.getUuid());
		if (status == 0)
			writeCallback.onSuccess();
		else
			writeCallback.onError();
	}
 
	private static String getStringValue(byte[] bytes) {
//		int indexOfFirstZeroByte = 0;
//		while (bytes[indexOfFirstZeroByte] != 0) {
//			indexOfFirstZeroByte++;
//		}

//		byte[] strBytes = new byte[indexOfFirstZeroByte];
//		String StrTemp = "";
//		for (int i = 0; i != indexOfFirstZeroByte; i++) {
//			strBytes[i] = bytes[i];
//		}
//		Log.i("TEST", "VALUE:"+strBytes);
//		
//		return new String(strBytes);
		
		String stmp="";  
        StringBuilder sb = new StringBuilder("");  
        for (int n = 0; n < bytes.length; n++)  
        {  
            stmp = Integer.toHexString(bytes[n] & 0xFF);  
            sb.append((stmp.length()==1)? "0"+stmp : stmp);  
            sb.append(" ");  
        }  
        return sb.toString().toUpperCase().trim();  
	}
	
	private static int unsignedByteToInt(byte value)
	{
		return value & 0xFF;
	}
	
	private static int getUnsignedInt16(byte[] bytes) {
		return unsignedByteToInt(bytes[0]) + (unsignedByteToInt(bytes[1]) << 8);
	}
}
