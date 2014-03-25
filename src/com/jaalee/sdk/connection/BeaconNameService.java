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
 public class BeaconNameService implements BluetoothService
 {
	 private final HashMap<UUID, BluetoothGattCharacteristic> characteristics = new HashMap<UUID, BluetoothGattCharacteristic>();
 
	 public void processGattServices(List<BluetoothGattService> services)
	 {
		 for (BluetoothGattService service : services)
			 if (JaaleeUuid.BEACON_NAME.equals(service.getUuid())) {
				 this.characteristics.put(JaaleeUuid.BEACON_NAME_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_NAME_CHAR));
			 }
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
	 
	 public String getBeaconName()
	 {
		 return this.characteristics.containsKey(JaaleeUuid.BEACON_NAME_CHAR) ?
				 getStringValue(((BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.BEACON_NAME_CHAR)).getValue()) : null;		
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
 
//	 public boolean isAuthSeedCharacteristic(BluetoothGattCharacteristic characteristic) {
//		 return characteristic.getUuid().equals(JaaleeUuid.AUTH_SEED_CHAR);
//	 }
// 
//	 public boolean isAuthVectorCharacteristic(BluetoothGattCharacteristic characteristic) {
//		 return characteristic.getUuid().equals(JaaleeUuid.AUTH_VECTOR_CHAR);
//	 }
// 
//	 public BluetoothGattCharacteristic getAuthSeedCharacteristic() {
//		 return (BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.AUTH_SEED_CHAR);
//	 }
// 
//	 public BluetoothGattCharacteristic getAuthVectorCharacteristic() {
//		 return (BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.AUTH_VECTOR_CHAR);
//	 }
 }
