import React, {useState} from 'react';
import {
  NativeModules,
  PermissionsAndroid,
  Button,
  View,
  Text,
  TouchableHighlight,
  TouchableOpacity,
  TextInput,
} from 'react-native';

// const requestBluetoothPermission = async () => {
//   try {
//     const granted = await PermissionsAndroid.request(
//       PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
//       {
//         title: 'Bluetooth Permission',
//         message: 'Access Fine Location',
//         buttonNegative: 'Deny',
//         buttonPositive: 'Accept',
//       },
//     );
//     if (granted === PermissionsAndroid.RESULTS.GRANTED) {
//       console.log('You can use the Bluetooth');
//     } else {
//       console.log('Permission denied');
//     }
//   } catch (err) {
//     console.warn(err);
//   }
//   try {
//     const granted = await PermissionsAndroid.request(
//       PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
//       {
//         title: 'Bluetooth Permission',
//         message: 'Access Fine Location',
//         buttonNegative: 'Deny',
//         buttonPositive: 'Accept',
//       },
//     );
//     if (granted === PermissionsAndroid.RESULTS.GRANTED) {
//       console.log('You can use the Bluetooth');
//     } else {
//       console.log('Permission denied');
//     }
//   } catch (err) {
//     console.warn(err);
//   }
// };

const App = () => {
  const [text,setText] = useState('');
  const [message,setMessage] = useState('');
  const [deviceList, setDeviceList] = useState({});
  const {bt} = NativeModules;

  // const bluetoothSwitch = () => {
  //   BluetoothModule.bluetoothSwitch();
  //   console.log('Bluetooth Switched!');
  // };

  // const startBluetoothDeviceDiscovery = () => {
  //   BluetoothModule.startBluetoothDeviceDiscovery();
  //   console.log('Invoked Discovery');
  // };

  // const cancelDeviceDiscovery = () => {
  //   BluetoothModule.cancelDeviceDiscovery();
  //   console.log('Cancelled Discovery');
  // };

  // const enableDeviceDiscoverability = () => {
  //   BluetoothModule.enableDeviceDiscoverability();
  //   console.log('Device is Discoverable now');
  // };

  //   const acceptConnection = () => {
  //       BluetoothModule.AcceptThreadRun();
  //   };

  // const initiateConnection = (deviceAddress) => {
  //   BluetoothModule.ConnectThreadRun(deviceAddress);
  // };

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

  const runServer = () => {
    bt.runServer();
  }

  const runClient = (deviceAddress) => {
    bt.runClient(deviceAddress);
  }

  const sendMessage = () => {
    bt.sendMessage(text,
      (msg) =>{
        console.log(msg);
      });
    // console.log(text);
  }

  const renderList = () => {
    return Object.entries(deviceList).map(([key, value]) => {
      return (
        <TouchableOpacity
          onPress={() => runClient(key)}
          style={{
            backgroundColor: '#841584',
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

  return (
    <>

      {/* <Button title="Request Permissions" onPress={requestBluetoothPermission} />
      <Button title="Switch Bluetooth" color="#841584" onPress={bluetoothSwitch} />
      <Button title="Enable Bluetooth Discoverability" color="#841584" onPress={enableDeviceDiscoverability} />
      <Button title="Start Bluetooth Discovery" onPress={startBluetoothDeviceDiscovery} />
      <Button title="Cancel Bluetooth Discovery" color="#841584" onPress={cancelDeviceDiscovery} />
      <Button title="Accept Connection" onPress={acceptConnection} />
      <Button title="Initiate Connection" color="#841584" onPress={initiateConnection} /> */}

      <Button title="Get Paired Devices" onPress={getPairedDevices} />
      <Button title="Run Server" color="#841584" onPress={runServer} />

      {renderList()}
      
      <Text defaultValue={message}></Text>
      <TextInput
        style={{height: 40}}
        placeholder="Type Message!"
        onChangeText={newText => setText(newText)}
        defaultValue={text}
      />
      
      <Button title="Send Message" onPress={sendMessage} />
    </>
  );
};

export default App;
