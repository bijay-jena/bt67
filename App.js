import React, { useState } from 'react';
import { NativeModules, Button, View, Text, TouchableOpacity, TextInput } from 'react-native';

const App = () => {
  const [text,setText] = useState('');
  const [str,setStr] = useState('');
  const [deviceList, setDeviceList] = useState({});
  const {bt} = NativeModules;

  setInterval(()=>{bt.getMessage((msg)=>{setStr(msg)})},100);

  const enableBluetooth = () => {
    bt.enable(
      (bluetoothStatus) => {
        console.log(bluetoothStatus);
      }
    );
  };

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

  const getMessage = () => {
    bt.getMessage((msg)=>{
      console.log(msg);
      setStr(msg);
    })
  }

  const renderList = () => {
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
    <>
      <Button
        title =  "Enable Bluetooth"
        color="#841584"
        onPress={enableBluetooth} />
      <Button
        title = "Get Paired Devices"
        onPress={getPairedDevices} />
      <Button
        title = "Accept Connection"
        color="#841584" onPress={acceptConnection} />
      <Button
        title =  "Receive Message"
        onPress={getMessage} />
      {renderList()}
      <Text>{str}</Text>
      <TextInput
        style={{height: 40}}
        placeholder="Message!"
        onChangeText={newText =>
          setText(newText)}
        value={text} />
      <Button title="Send Message"
              onPress={sendMessage} />
    </>
  );
};

export default App;
