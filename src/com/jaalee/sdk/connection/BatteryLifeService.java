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
public class BatteryLifeService
  implements BluetoothService
{
	private final HashMap<UUID, BluetoothGattCharacteristic> characteristics = new HashMap();

	public void processGattServices(List<BluetoothGattService> services)
	{
		for (BluetoothGattService service : services)
			if (JaaleeUuid.BEACON_BATTERY_LIFE.equals(service.getUuid())) {
				this.characteristics.put(JaaleeUuid.BEACON_BATTERY_LIFE_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_BATTERY_LIFE_CHAR));
			}
	}
	
	public Integer getBatteryPercent() {
		return this.characteristics.containsKey(JaaleeUuid.BEACON_BATTERY_LIFE_CHAR) ? Integer.valueOf(getUnsignedByte(((BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.BEACON_BATTERY_LIFE_CHAR)).getValue())) : null;
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
	
	private static int getUnsignedByte(byte[] bytes) {
		return unsignedByteToInt(bytes[0]);
	}
	private static int unsignedByteToInt(byte value)
	{
		return value & 0xFF;
	}
	
	private static int getUnsignedInt16(byte[] bytes) {
		return unsignedByteToInt(bytes[0]) + (unsignedByteToInt(bytes[1]) << 8);
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
