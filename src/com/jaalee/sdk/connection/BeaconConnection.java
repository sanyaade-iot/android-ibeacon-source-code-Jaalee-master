package com.jaalee.sdk.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Utils;
import com.jaalee.sdk.internal.HashCode;
import com.jaalee.sdk.internal.Objects;
import com.jaalee.sdk.internal.UnsignedInteger;
import com.jaalee.sdk.utils.AuthMath;
import com.jaalee.sdk.utils.L;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
public class BeaconConnection
{
	public static Set<Integer> ALLOWED_POWER_LEVELS = Collections.unmodifiableSet(new HashSet(Arrays.asList(new Integer[] { Integer.valueOf(-30), Integer.valueOf(-20), Integer.valueOf(-16), Integer.valueOf(-12), Integer.valueOf(-8), Integer.valueOf(-4), Integer.valueOf(0), Integer.valueOf(4) })));
	private final Context context;
	private final BluetoothDevice device;
	private final ConnectionCallback connectionCallback;
	private final Handler handler;
	private final BluetoothGattCallback bluetoothGattCallback;
//	private final Runnable timeoutHandler;
	private final BeaconNameService mNameService;
	private final BatteryLifeService mBatteryService;	
	private final JaaleeService mBeaconService;
	private final AlertService mAlertService;
	private final Map<UUID, BluetoothService> uuidToService;
	private boolean didReadCharacteristics;
	private LinkedList<BluetoothGattCharacteristic> toFetch;
	private long aAuth;
	private long bAuth;
	private BluetoothGatt bluetoothGatt;

	public BeaconConnection(Context context, Beacon beacon, ConnectionCallback connectionCallback)
	{
		this.context = context;
		this.device = deviceFromBeacon(beacon);
		this.toFetch = new LinkedList<BluetoothGattCharacteristic>();
		this.handler = new Handler();
		this.connectionCallback = connectionCallback;
		this.bluetoothGattCallback = createBluetoothGattCallback();
//		this.timeoutHandler = createTimeoutHandler();
		this.mNameService = new BeaconNameService();
		
		this.mBatteryService = new BatteryLifeService();
		this.mBeaconService = new JaaleeService();
		this.mAlertService = new AlertService();
		this.uuidToService = new HashMap<UUID, BluetoothService>();
		this.uuidToService.put(JaaleeUuid.BEACON_BATTERY_LIFE, mBatteryService);
		this.uuidToService.put(JaaleeUuid.JAALEE_BEACON_SERVICE, mBeaconService);
		this.uuidToService.put(JaaleeUuid.BEACON_ALERT, mAlertService);
		this.uuidToService.put(JaaleeUuid.BEACON_NAME, mNameService);		
	}

	private BluetoothDevice deviceFromBeacon(Beacon beacon)
	{
	     BluetoothManager bluetoothManager = (BluetoothManager)this.context.getSystemService("bluetooth");
	     BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
	     return bluetoothAdapter.getRemoteDevice(beacon.getMacAddress());
	}

	public void authenticate()
	{
		L.d("Trying to connect to GATT");
		this.didReadCharacteristics = true;
		this.bluetoothGatt = this.device.connectGatt(this.context, false, this.bluetoothGattCallback);
//		this.handler.postDelayed(this.timeoutHandler, TimeUnit.SECONDS.toMillis(10L));
	}

	public void close()
	{
		if (this.bluetoothGatt != null) {
			this.bluetoothGatt.disconnect();
			this.bluetoothGatt.close();
		}
//		this.handler.removeCallbacks(this.timeoutHandler);
	}

	public boolean isConnected() {
		BluetoothManager bluetoothManager = (BluetoothManager)this.context.getSystemService("bluetooth");
		int connectionState = bluetoothManager.getConnectionState(this.device, 7);
//		return (connectionState == 2) && (this.didReadCharacteristics);
		return (connectionState == 2);
	}
	
	public void CallBeacon(WriteCallback writeCallback)
	{
		if ((!isConnected()) || (!this.mAlertService.hasCharacteristic(JaaleeUuid.BEACON_ALERT_CHAR))) 
		{
			L.w("Not connected to beacon. Discarding changing proximity UUID.");
			writeCallback.onError();
			return;
		}
		BluetoothGattCharacteristic AlertChar = this.mAlertService.beforeCharacteristicWrite(JaaleeUuid.BEACON_ALERT_CHAR, writeCallback);
	    byte[] arrayOfByte = new byte[1];
	    arrayOfByte[0] = 1;
	    AlertChar.setValue(arrayOfByte);
		this.bluetoothGatt.writeCharacteristic(AlertChar);		
	}
	
	public void BeaconKeepConnect(WriteCallback writeCallback)
	{
		if ((!isConnected()) || (!this.mBeaconService.hasCharacteristic(JaaleeUuid.BEACON_KEEP_CONNECT_CHAR))) 
		{
			L.w("Not connected to beacon. Discarding changing proximity UUID.");
			writeCallback.onError();
			return;
		}
		BluetoothGattCharacteristic uuidChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.BEACON_KEEP_CONNECT_CHAR, writeCallback);

	    byte[] arrayOfByte = new byte[1];
	    arrayOfByte[0] = 1;
	    
		uuidChar.setValue(arrayOfByte);
		
		this.bluetoothGatt.writeCharacteristic(uuidChar);
	}


	public void writeProximityUuid(String proximityUuid, WriteCallback writeCallback)
	{
		if ((!isConnected()) || (!this.mBeaconService.hasCharacteristic(JaaleeUuid.BEACON_UUID_CHAR))) {
			L.w("Not connected to beacon. Discarding changing proximity UUID.");
			writeCallback.onError();
			return;
		}
		byte[] uuidAsBytes = HashCode.fromString(proximityUuid.replaceAll("-", "").toLowerCase()).asBytes();
		BluetoothGattCharacteristic uuidChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.BEACON_UUID_CHAR, writeCallback);

		uuidChar.setValue(uuidAsBytes);
		this.bluetoothGatt.writeCharacteristic(uuidChar);
	}

	public void writeAdvertisingInterval(String BroadcastRate, WriteCallback writeCallback)
	{
		if ((!isConnected()) || (!this.mBeaconService.hasCharacteristic(JaaleeUuid.ADVERTISING_INTERVAL_CHAR))) {
			L.w("Not connected to beacon. Discarding changing advertising interval.");
			writeCallback.onError();
			return;
		}
		
		byte[] BroadcastRateAsBytes = HashCode.fromString(BroadcastRate.replaceAll("-", "").toLowerCase()).asBytes();
		
		BluetoothGattCharacteristic intervalChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.ADVERTISING_INTERVAL_CHAR, writeCallback);

		intervalChar.setValue(BroadcastRateAsBytes);
		this.bluetoothGatt.writeCharacteristic(intervalChar);
	}

	public void writeBroadcastingPowerValue(String powerDBM, WriteCallback writeCallback)
	{
		if ((!isConnected()) || (!this.mBeaconService.hasCharacteristic(JaaleeUuid.POWER_CHAR))) {
			L.w("Not connected to beacon. Discarding changing broadcasting power.");
			writeCallback.onError();
			return;
		}
		
//		if (!ALLOWED_POWER_LEVELS.contains(Integer.valueOf(powerDBM))) {
//			L.w("Not allowed power level. Discarding changing broadcasting power.");
//			writeCallback.onError();
//			return;
//		}
		
		byte[] powerDBMAsBytes = HashCode.fromString(powerDBM.replaceAll("-", "").toLowerCase()).asBytes();
		BluetoothGattCharacteristic powerChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.POWER_CHAR, writeCallback);

		powerChar.setValue(powerDBMAsBytes);
		this.bluetoothGatt.writeCharacteristic(powerChar);
	}

	public void writeMajor(String major, WriteCallback writeCallback)
	{
		if (!isConnected()) {
			L.w("Not connected to beacon. Discarding changing major.");
			writeCallback.onError();
			return;
		}
		
		byte[] MajorAsBytes = HashCode.fromString(major.replaceAll("-", "").toLowerCase()).asBytes();
		BluetoothGattCharacteristic majorChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.MAJOR_CHAR, writeCallback);

		majorChar.setValue(MajorAsBytes);
		this.bluetoothGatt.writeCharacteristic(majorChar);
	}

	public void writeMinor(String minor, WriteCallback writeCallback)
	{
		if (!isConnected()) {
			L.w("Not connected to beacon. Discarding changing minor.");
			writeCallback.onError();
			return;
		}
//		minor = Utils.normalize16BitUnsignedInt(minor);
		byte[] MinorAsBytes = HashCode.fromString(minor.replaceAll("-", "").toLowerCase()).asBytes();
		BluetoothGattCharacteristic minorChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.MINOR_CHAR, writeCallback);

		minorChar.setValue(MinorAsBytes);
		this.bluetoothGatt.writeCharacteristic(minorChar);
	}

	private Runnable createTimeoutHandler() 
	{
		return new Runnable()
		{
			public void run() {
				L.v("Timeout while authenticating");
				if (!BeaconConnection.this.didReadCharacteristics) {
					if (BeaconConnection.this.bluetoothGatt != null) {
						BeaconConnection.this.bluetoothGatt.disconnect();
						BeaconConnection.this.bluetoothGatt.close();
						BeaconConnection.this.bluetoothGatt = null;
					}
					BeaconConnection.this.notifyAuthenticationError();
				}
			}
		};
	}

	private BluetoothGattCallback createBluetoothGattCallback() {
		return new BluetoothGattCallback()
		{
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
				if (newState == 2) {
					L.d("Connected to GATT server, discovering services: " + gatt.discoverServices());
				} else if ((newState == 0) && (!BeaconConnection.this.didReadCharacteristics)) {
					L.w("Disconnected from GATT server");
					BeaconConnection.this.notifyAuthenticationError();
				} else if (newState == 0) {
					L.w("Disconnected from GATT server");
					BeaconConnection.this.notifyDisconnected();
				}
			}

			public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
			{
//				if (BeaconConnection.this.mNameService.isAuthSeedCharacteristic(characteristic)) {
//					if (status == 0) {
//						BeaconConnection.this.onBeaconSeedResponse(gatt, characteristic);
//					} else {
//						L.w("Auth failed: could not read beacon's response to seed");
//						BeaconConnection.this.notifyAuthenticationError();
//					}
//					return;
//				}
				

				
				if (status == 0) {
					BluetoothService temp = ((BluetoothService)BeaconConnection.this.uuidToService.get(characteristic.getService().getUuid()));
					if (temp != null)
					{
						temp.update(characteristic);
					}
					BeaconConnection.this.readCharacteristics(gatt);
				} else {
					L.w("Failed to read characteristic");
					BeaconConnection.this.toFetch.clear();
					BeaconConnection.this.notifyAuthenticationError();
				}
			}

			public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
			{
//				if (BeaconConnection.this.mNameService.isAuthSeedCharacteristic(characteristic)) {
//					if (status == 0) {
//						BeaconConnection.this.onSeedWriteCompleted(gatt, characteristic);
//					} else {
//						L.w("Authentication failed: seed not negotiated");
//						BeaconConnection.this.notifyAuthenticationError();
//					}
//				} 
//				else if (BeaconConnection.this.mNameService.isAuthVectorCharacteristic(characteristic)) {
//					if (status == 0) {
//						BeaconConnection.this.onAuthenticationCompleted(gatt);
//					} else {
//						L.w("Authentication failed: auth source write failed");
//						BeaconConnection.this.notifyAuthenticationError();
//					}
//				} 
				
//				((BluetoothService)BeaconConnection.this.uuidToService.get(characteristic.getService().getUuid())).onCharacteristicWrite(characteristic, status);
				
				if (JaaleeUuid.JAALEE_BEACON_SERVICE.equals(characteristic.getService().getUuid())){
					BeaconConnection.this.mBeaconService.onCharacteristicWrite(characteristic, status);
					BeaconConnection.this.onAuthenticationCompleted(gatt);
				}
				else if (JaaleeUuid.BEACON_NAME.equals(characteristic.getService().getUuid()))
				{
					//do something
				}
			}

			public void onServicesDiscovered(BluetoothGatt gatt, int status)
			{
				if (status == 0) {
					L.v("Services discovered");
					BeaconConnection.this.processDiscoveredServices(gatt.getServices());
//					BeaconConnection.this.startAuthentication(gatt);
					//keep connect
					BeaconConnection.this.handler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							BeaconConnection.this.BeaconKeepConnect(new WriteCallback() {
								
								@Override
								public void onSuccess() {
									// TODO Auto-generated method stub
									Log.i("BeaconConnection", "Keep Connect Successful");
								}
								
								@Override
								public void onError() {
									// TODO Auto-generated method stub
									Log.i("BeaconConnection", "Keep Connect Failed");
								}
							});	
							
						}
					}, TimeUnit.SECONDS.toMillis(1L));

				} else {
					L.w("Could not discover services, status: " + status);
					BeaconConnection.this.notifyAuthenticationError();
				}
			}
		};
	}

	private void notifyAuthenticationError() {
//		this.handler.removeCallbacks(this.timeoutHandler);
		this.connectionCallback.onAuthenticationError();
	}

	private void notifyDisconnected() {
		this.connectionCallback.onDisconnected();
	}

	private void processDiscoveredServices(List<BluetoothGattService> services) {
		this.mNameService.processGattServices(services);
		this.mBeaconService.processGattServices(services);
		this.mAlertService.processGattServices(services);

		this.toFetch.clear();
		this.toFetch.addAll(this.mBeaconService.getAvailableCharacteristics());
		this.toFetch.addAll(this.mNameService.getAvailableCharacteristics());
		this.toFetch.addAll(this.mAlertService.getAvailableCharacteristics());
		this.toFetch.addAll(this.mBatteryService.getAvailableCharacteristics());
	}

//	private void startAuthentication(BluetoothGatt gatt)
//	{
//		if (!this.mNameService.isAvailable()) {
//			L.w("Authentication service is not available on the beacon");
//			notifyAuthenticationError();
//			return;
//		}
//		this.aAuth = AuthMath.randomUnsignedInt();
//		BluetoothGattCharacteristic seedChar = this.mNameService.getAuthSeedCharacteristic();
//		seedChar.setValue(AuthMath.firstStepSecret(this.aAuth), 20, 0);
//		gatt.writeCharacteristic(seedChar);
//	}

	private void onSeedWriteCompleted(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic)
	{
		this.handler.postDelayed(new Runnable()
		{
			public void run() {
				gatt.readCharacteristic(characteristic);
			}
		}
		, 500L);
	}

	private void onBeaconSeedResponse(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
	{
//		Integer intValue = characteristic.getIntValue(20, 0);
//		this.bAuth = UnsignedInteger.fromIntBits(intValue.intValue()).longValue();
//		String macAddress = this.device.getAddress().replace(":", "");
//		BluetoothGattCharacteristic vectorChar = this.mNameService.getAuthVectorCharacteristic();
//		vectorChar.setValue(AuthMath.secondStepSecret(this.aAuth, this.bAuth, macAddress));
//		gatt.writeCharacteristic(vectorChar);
	}

	private void onAuthenticationCompleted(final BluetoothGatt gatt)
	{
		this.handler.postDelayed(new Runnable()
		{
			public void run() {
				BeaconConnection.this.readCharacteristics(gatt);
			}
		}
		, 500L);
	}

	private void readCharacteristics(BluetoothGatt gatt)
	{
		if (this.toFetch.size() != 0)
		{
			gatt.readCharacteristic((BluetoothGattCharacteristic)this.toFetch.poll());
		}
		else if (this.bluetoothGatt != null)
		{
			onAuthenticated();
		}
	}

	private void onAuthenticated()
	{
		L.v("Authenticated to beacon");
//		this.handler.removeCallbacks(this.timeoutHandler);
		this.didReadCharacteristics = true;
		this.connectionCallback.onAuthenticated(new BeaconCharacteristics(this.mBeaconService, this.mNameService));
	}

	public static abstract interface WriteCallback
	{
		public abstract void onSuccess();

		public abstract void onError();
	}

	public static abstract interface ConnectionCallback
	{
		public abstract void onAuthenticated(BeaconConnection.BeaconCharacteristics paramBeaconCharacteristics);

		public abstract void onAuthenticationError();

		public abstract void onDisconnected();
	}

	public static class BeaconCharacteristics
	{
		private final String BeaconUUID;
		private final String BeaconMajor;
		private final String BeaconMinor;
		private final String BeaconPowerValue;
    	private final String BeaconBroadcast;
    	private final String BeaconName;

    	public BeaconCharacteristics(JaaleeService JLService, BeaconNameService Name)
    	{
    		this.BeaconUUID = JLService.getBeaconUUID().replaceAll(" ", "");
    		this.BeaconMajor = JLService.getBeaconMajor().replaceAll(" ", "");
    		this.BeaconMinor = JLService.getBeaconMinor().replaceAll(" ", "");
    		this.BeaconPowerValue = JLService.getBeaconPower().replaceAll(" ", "");
    		this.BeaconBroadcast = JLService.getBeaconBroadcastRate().replaceAll(" ", "");
    		this.BeaconName = Name.getBeaconName().replaceAll(" ", "");
    	}

    	public String getBeaconUUID() {
    		return this.BeaconUUID;
    	}
    	public String getBroadcastingPower() {
    		return this.BeaconPowerValue;
    	}
    	public String getBroadcastRate() {
    		return this.BeaconBroadcast;
    	}
    	public String getMajor() {
    		return this.BeaconMajor;
    	}
    	public String getMinor() {
    		return this.BeaconMinor;
    	}
    	public String getBeaconName()
    	{
    		return this.BeaconName;
    	}

    	public String toString() {
        	
    		return Objects.toStringHelper(this)
    				.add("BeaconUUID", this.BeaconUUID)
    				.add("BeaconMajor", this.BeaconMajor)
    				.add("BeaconMinor", this.BeaconMinor)
    				.add("BeaconPowerValue", this.BeaconPowerValue)
    				.add("BeaconBroadcast", this.BeaconBroadcast)
    				.add("BeaconName", this.BeaconName).toString();
    	}
	}
}
