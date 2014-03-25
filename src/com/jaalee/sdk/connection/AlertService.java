package com.jaalee.sdk.connection;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
public class AlertService
  implements BluetoothService
{
	private final HashMap<UUID, BluetoothGattCharacteristic> characteristics = new HashMap();
	private final HashMap<UUID, BeaconConnection.WriteCallback> writeCallbacks = new HashMap();
	
	public void processGattServices(List<BluetoothGattService> services)
	{
		for (BluetoothGattService service : services)
			if (JaaleeUuid.BEACON_ALERT.equals(service.getUuid())) {
				this.characteristics.put(JaaleeUuid.BEACON_ALERT_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_ALERT_CHAR));
			}
	}

	public boolean hasCharacteristic(UUID uuid)
	{
		return this.characteristics.containsKey(uuid);
	}
	
	public void update(BluetoothGattCharacteristic characteristic)
	{
		this.characteristics.put(characteristic.getUuid(), characteristic);
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

	public Collection<BluetoothGattCharacteristic> getAvailableCharacteristics() {
		List chars = new ArrayList(this.characteristics.values());
		chars.removeAll(Collections.singleton(null));
		return chars;
	}

	private static String getStringValue(byte[] bytes) {
		int indexOfFirstZeroByte = 0;
		while (bytes[indexOfFirstZeroByte] != 0) {
			indexOfFirstZeroByte++;
		}

		byte[] strBytes = new byte[indexOfFirstZeroByte];
		for (int i = 0; i != indexOfFirstZeroByte; i++) {
			strBytes[i] = bytes[i];
		}
		
		return new String(strBytes);
	}
}
