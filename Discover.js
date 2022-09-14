import React, {useState} from 'react'
import {Button, NativeModules, Text, TouchableOpacity, View} from "react-native";

const Discover = () => {
  const [deviceList, setDeviceList] = useState({});

  const { discover } = NativeModules;

  const discoverDevices = () => {
    discover.doDiscovery(
      // cb -> callback
      (cb) => {
        console.log(cb);
      }
    );
  }

  const getDiscoveredDevices = () => {
    discover.getDiscoveredDevices(
      (devices) => {
        console.log(devices);
        setDeviceList(devices);
      }
    );
  }

  const makeDeviceDiscoverable = () => {
    discover.makeDeviceDiscoverable(120,
      (cb) => {
        console.log(cb);
      });
  }

  const renderDiscoveredDeviceList = () => {
    if (deviceList==null) { return; }
    return Object.entries(deviceList).map(([key, value]) => {
      return (
        <TouchableOpacity
          // onPress={() => initiateConnection(key)}
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
      <Button title=  {   "Start Discovery"    }    onPress={   discoverDevices    } />
      <Button title=  {  "Make Discoverable"   }    onPress={makeDeviceDiscoverable} />
      <Button title=  {"Get Discovered Devices"}    onPress={ getDiscoveredDevices } />

      {renderDiscoveredDeviceList()}
    </>
  );
};

export default Discover;
