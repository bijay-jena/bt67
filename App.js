import React, {useState, useEffect} from 'react';
import {
  NativeEventEmitter,
  NativeModules,
  Button,
  View,
  Text,
  TouchableOpacity,
  TextInput,
  ScrollView,
} from 'react-native';

export default function App() {
  const [text, setText] = useState('');
  const [str, setStr] = useState('');
  const [discoveredDeviceList, setDiscoveredDeviceList] = useState({});
  const [disable, setDisable] = useState(true);
  const {bt} = NativeModules;

  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(NativeModules.bt);
    eventListener = eventEmitter.addListener('msgReceiver', e => {
      // console.log(e.eventProperty);
      setStr(e.Message);
    });
  }, []);

  const enableBluetooth = () => {
    bt.enable(bluetoothStatus => {
      console.log(bluetoothStatus);
    });
  };

  const sendMessage = () => {
    bt.sendMessage(text);
  };

  const discoverDevices = () => {
    bt.doDiscovery(
      // cb -> callback
      cb => {
        console.log(cb);
      },
    );
    setDisable(false);
  };

  const getDiscoveredDevices = () => {
    bt.getDiscoveredDevices(devices => {
      console.log(devices);
      setDiscoveredDeviceList(devices);
    });
    setDisable(true);
  };

  const makeDeviceDiscoverable = () => {
    bt.makeDeviceDiscoverable(15, cb => {
      console.log(cb);
    });
    bt.acceptConnection();
  };

  const initiateDiscoveredConnection = deviceAddress => {
    bt.initiateDiscoveredConnection(deviceAddress);
  };

  const renderDiscoveredDeviceList = () => {
    if (discoveredDeviceList == null) {
      return;
    }
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
  //   return Object.entries(deviceList).map(([key, value]) => {
  //     return (
  //       <TouchableOpacity
  //         onPress={() => initiateConnection(key)}
  //         style={{
  //           backgroundColor: '#153484',
  //           marginVertical: 5,
  //           marginHorizontal: 10,
  //           padding: 10,
  //           borderRadius: 20,
  //         }}>
  //         <View key={key}>
  //           <Text style={{color: '#fff'}}>{value}</Text>
  //         </View>
  //       </TouchableOpacity>
  //     );
  //   });
  // };

  return (
    <ScrollView>
      <Button
        title={'Enable Bluetooth'}
        onPress={enableBluetooth}
        color="#841584"
        style={buttonDesign}
      />
      <Button title={'Start Discovery'} onPress={discoverDevices} />
      <Button
        title={'Make Discoverable'}
        onPress={makeDeviceDiscoverable}
        color="#841584"
      />
      <Button
        title={'Get Discovered Devices'}
        onPress={getDiscoveredDevices}
        disabled={disable}
      />
      {renderDiscoveredDeviceList()}
      <Text>{str}</Text>
      <TextInput
        style={{height: 40}}
        placeholder="Message!"
        onChangeText={newText => setText(newText)}
        value={text}
      />
      <Button title="Send Message" onPress={sendMessage} />
    </ScrollView>
  );
}

const buttonDesign= {

}