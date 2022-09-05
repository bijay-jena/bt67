import React from 'react';
import { 
  NativeModules,
  PermissionsAndroid,
  Button
} from 'react-native';

const requestBluetoothPermission = async () => {
  try {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      {
        title: "Bluetooth Permission",
        message:
          "Access Fine Location",
        buttonNegative: "Deny",
        buttonPositive: "Accept"
      }
    );
    if (granted === PermissionsAndroid.RESULTS.GRANTED) {
      console.log("You can use the Bluetooth");
    } else {
      console.log("Permission denied");
    }
  } catch (err) {
    console.warn(err);
  }
  try {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
      {
        title: "Bluetooth Permission",
        message:
          "Access Fine Location",
        buttonNegative: "Deny",
        buttonPositive: "Accept"
      }
    );
    if (granted === PermissionsAndroid.RESULTS.GRANTED) {
      console.log("You can use the Bluetooth");
    } else {
      console.log("Permission denied");
    }
  } catch (err) {
    console.warn(err);
  }
};

const App = () => {
  const { BluetoothModule } = NativeModules;
  const bluetoothSwitch = () => {
    BluetoothModule.bluetoothSwitch();
    console.log('Bluetooth Switched!');
  };

  const getPairedDevices = async () => {
    try {
      BluetoothModule.getPairedDevices();
      // const { PAIRED_DEVICES } = BluetoothModule.getConstants();
      // console.log(DEFAULT_EVENT_NAME);
      // console.log(`Check LogCat for Paired Devices! ${pairedDevices}`);
    } catch (e) {
      console.error("Paired Devices",e);
    }
  }
  
  const startBluetoothDeviceDiscovery = () => {
    BluetoothModule.startBluetoothDeviceDiscovery();
    console.log('Invoked Discovery');
  }

  const cancelDeviceDiscovery = () => {
    BluetoothModule.cancelDeviceDiscovery();
    console.log('Cancelled Discovery')
  }

  const enableDeviceDiscoverability = () => {
    BluetoothModule.enableDeviceDiscoverability();
    console.log('Device is Discoverable now');
  }

  const initiateConnection = () => {
    BluetoothModule.ConnectThreadRun()
  }

  const acceptConnection = () => {
    BluetoothModule.AcceptConnectionRun()
  }

  return (
    <>
      <Button
        title="Request Permissions"
        onPress={requestBluetoothPermission}
      />
      <Button
        title="Switch Bluetooth"
        color="#841584"
        onPress={bluetoothSwitch}
      />
      <Button
        title="Get Paired Devices"
        onPress={getPairedDevices}
      />
      <Button
        title="Enable Bluetooth Discoverability"
        color="#841584"
        onPress={enableDeviceDiscoverability}
      />
      <Button
        title="Start Bluetooth Discovery"
        onPress={startBluetoothDeviceDiscovery}
      />
      <Button
        title="Cancel Bluetooth Discovery"
        color="#841584"
        onPress={cancelDeviceDiscovery}
      />
      <Button
        title="Initiate Connection"
        onPress={initiateConnection}
      />
      <Button
        title="Accept Connection"
        color="#841584"
        onPress={acceptConnection}
      />
    </>
  );
};

export default App;