import React, { useState } from 'react';
import { NativeModules, Button, View, Text, TouchableOpacity, TextInput, ScrollView } from 'react-native';

const App = () => {
  const [text,setText] = useState('');
  const [str,setStr] = useState('');
  const [deviceList, setDeviceList] = useState({});
  const [discoveredDeviceList, setDiscoveredDeviceList] = useState({});
  const { bt } = NativeModules;

  setInterval(()=>{bt.getMessage((msg)=>{setStr(msg)})},100);

  const enableBluetooth = () => { bt.enable( (bluetoothStatus) => { console.log(bluetoothStatus); } ); };

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

  const acceptConnection = () => { bt.acceptConnection(); }

  const initiateConnection = (deviceAddress) => { bt.initiateConnection(deviceAddress); }

  const sendMessage = () => { bt.sendMessage(text); }

  const getMessage = () => { bt.getMessage((msg)=>{ setStr(msg); }) }

  const discoverDevices = () => {
    bt.doDiscovery(
      // cb -> callback
      (cb) => {
        console.log(cb);
      }
    );
  }

  const getDiscoveredDevices = () => {
    bt.getDiscoveredDevices(
      (devices) => {
        console.log(devices);
        setDiscoveredDeviceList(devices);
      }
    );
  }

  const makeDeviceDiscoverable = () => {
    bt.makeDeviceDiscoverable(15,
      (cb) => {
        console.log(cb);
      });
  }

  const initiateDiscoveredConnection = (deviceAddress) => { bt.initiateDiscoveredConnection(deviceAddress); }

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

  return (
    <ScrollView>
      <Button title = {   "Enable Bluetooth"   }  onPress={    enableBluetooth   }  color="#841584" />
      <Button title = {  "Get Paired Devices"  }  onPress={   getPairedDevices   } />
      <Button title = {   "Accept Connection"  }  onPress={   acceptConnection   }  color="#841584" />
      {/* <Button title = {   "Receive Message"    }  onPress={      getMessage      } /> */}
      <Button title=  {   "Start Discovery"    }  onPress={   discoverDevices    } color="#841584" />
      <Button title=  {  "Make Discoverable"   }  onPress={makeDeviceDiscoverable} />
      <Button title=  {"Get Discovered Devices"}  onPress={ getDiscoveredDevices } color="#841584" />
      {renderDiscoveredDeviceList()}
      {renderPairedDeviceList()}
      <Text>{str}</Text>
      <TextInput
        style={{height: 40}}
        placeholder="Message!"
        onChangeText={newText =>
          setText(newText)}
        value={text} />
      <Button title="Send Message"
              onPress={sendMessage} />
    </ScrollView>
  );
};

export default App;
