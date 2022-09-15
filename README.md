# Bluetooth Module

Android Bluetooth Module for React Native

Your need to add:

```Javascript
import { NativeModules } from 'react-native'
```

Then create instance for the module:

```Javascript
const {bt} = NativeModules;
```

Creating the Native Module (Calling the constructor) enables the bluetooth
## Functions Available with Example

### bt.enable() / bt.enable(Callback callback)

```Javascript
const enableBluetooth = () => {
  bt.enable();
};
```
---
```Javascript
const enableBluetooth = () => {
  bt.enable(
  (bluetoothStatus) => {
    console.log(bluetoothStatus);
  );
};
```

### bt.getPairedDevices(Callback callback)

```Javascript
const getPairedDevices = async () => {
  try {
    bt.getPairedDevices ( Devices => {
      console.log('Device List', Devices);
      setDeviceList(Devices);
    });
  } catch (e) {
    console.error('getPairedDevices', e);
  }
};
```

### bt.doDiscovery(Callback callback)

```Javascript
const discoverDevices = () => {
  bt.doDiscovery(
    // cb -> callback
    (cb) => {
      console.log(cb);
    }
  );
}
```

### bt.getDiscoveredDevices(Callback callback)

```Javascript
const getDiscoveredDevices = () => {
  bt.getDiscoveredDevices(
    (devices) => {
      console.log(devices);
      setDiscoveredDeviceList(devices);
    }
  );
}
```

### bt.makeDeviceDiscoverable(int duration, @NonNull Callback callback)

```Javascript
const makeDeviceDiscoverable = () => {
  bt.makeDeviceDiscoverable(15, //duration in seconds
    (cb) => {
      console.log(cb);
    });
}
```

### bt.initiateDiscoveredConnection(String deviceHardwareAddress)

```Javascript
const initiateDiscoveredDvcConnection = (deviceAddress) => {
  bt.initiateDiscoveredConnection(deviceAddress);
}
```

### bt.acceptConnection()

```Javascript
const acceptConnection = () => { bt.acceptConnection(); }
```

### bt.initiateConnection(String deviceHardwareAddress)

```Javascript
const initiateConnection = (deviceAddress) => { bt.initiateConnection(deviceAddress); }
```

### bt.sendMessage(String string)

```Javascript
const sendMessage = () => { bt.sendMessage(text); }
```

### bt.getMessage(Callback callback)

```Javascript
const getMessage = () => {
  bt.getMessage((msg)=>{
    console.log(msg);
    setStr(msg);
  })
}
```

## Some extra functions for listing the bluetooth devices

```Javascript
  const renderDiscoveredDeviceList = () => {
    if (discoveredDeviceList==null) { return; }
    return Object.entries(discoveredDeviceList).map(([key, value]) => {
      return (
        <TouchableOpacity
          onPress={() => initiateDiscoveredConnection(key)}
          style={{
            backgroundColor: '#153484',
            marginVertical: 5,
            marginHorizontal: 10,
            padding: 10,
            borderRadius: 20,
          }}>
          <View key={key}>
            <Text style={{color: '#fff'}}>{value}</Text>
          </View>
        </TouchableOpacity>
      );
    });
  };

  const renderPairedDeviceList = () => {
    return Object.entries(deviceList).map(([key, value]) => {
      return (
        <TouchableOpacity
          onPress={() => initiateConnection(key)}
          style={{
            backgroundColor: '#153484',
            marginVertical: 5,
            marginHorizontal: 10,
            padding: 10,
            borderRadius: 20,
          }}>
          <View key={key}>
            <Text style={{color: '#fff'}}>{value}</Text>
          </View>
        </TouchableOpacity>
      );
    });
  };
```