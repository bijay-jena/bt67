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
  StyleSheet
} from 'react-native';

export default function App() {
  const [text, setText] = useState('');
  const [str, setStr] = useState('');
  const [deviceList, setDeviceList] = useState([])
  const {bt} = NativeModules;

  useEffect(() => {
    enable();
    const eventEmitter = new NativeEventEmitter(NativeModules.bt);
    
    eventEmitter.addListener('rcvr', (rcvr: any) => {
      setStr(rcvr.msg);
    });

    eventEmitter.addListener('conn', (conn: any) => {
      console.log(conn.status);
    })

    eventEmitter.addListener('dvcFound', (dvc: any) => {
      // console.log(dvc);
      let temp = deviceList;
      temp.push(dvc);
      setDeviceList(temp);
    });
  }, [deviceList]);  

  const enable = () => {
    bt.enable((bluetoothStatus: any) => {
      console.log(bluetoothStatus);
    });
  };

  const sendMsg = () => { bt.sendMsg(text); };

  const discover = () => {
    bt.discover((cb: any) => {
      console.log(cb);
    });
  };

  const discoverable = () => {
    bt.discoverable(15, (cb: any) => {
      console.log(cb);
    });
    bt.accept();
  };

  const connect = (deviceAddress: string) => { bt.connect(deviceAddress); };

  const renderDeviceList = () => {
    console.log(deviceList);
    if (deviceList) {
      return Object.entries(deviceList).map(([key, value]) => {
        return (
          <TouchableOpacity onPress={() => connect(key)} style={styles.buttonCss} key={key}>
            <View> <Text style={{color: '#fff'}}>{value}</Text> </View>
          </TouchableOpacity>
        );
      });
    }
  };

  return (
    <ScrollView>
      <Button title={'Start Discovery'} onPress={discover} />
      <Button
        title={'Make Discoverable'}
        onPress={discoverable}
        color="#841584"
      />
      {renderDeviceList()}
      <Text>{str}</Text>
      <TextInput
        style={{height: 40}} placeholder="Type your message"
        onChangeText={newText => setText(newText)}
        value={text}
      />
      <Button title="Send Message" onPress={sendMsg} />
      <Button title="Get List" onPress={() => {console.log(deviceList)}} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  buttonCss: {
    backgroundColor: '#153484',
    marginVertical: 5,
    marginHorizontal: 20,
    padding: 10,
  },
})
