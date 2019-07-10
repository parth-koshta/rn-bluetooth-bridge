import React, { Component } from "react";
import { View, Text, NativeModules, TouchableHighlight, NativeEventEmitter, DeviceEventEmitter } from "react-native";
import { clear } from "sisteransi";



class BluetoothScreen extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isOn: false,
      bluetoothSupported: false,
      pairedDevices: null,
      selectedDeviceId: null
    };
  }
  startBluetooth() {
    // NativeModules.Bluetooth.startBluetooth().then(item=>{
    //     console.log(item);
    // });
    NativeModules.Bluetooth.startBluetooth();
    this.updateStatus();
  }
  updateStatus = () => {
    this.setState({ isOn: false });
    NativeModules.Bluetooth.getStatus((error, isOn) => {
      this.setState({ isOn: isOn });
    });
  };

  checkBluetoothSupport = () => {
    NativeModules.Bluetooth.checkBluetoothSupport(
      (error, bluetoothSupported) => {
        this.setState({ bluetoothSupported: bluetoothSupported });
      }
    );
  };

  getPairedDevices = () => {
    NativeModules.Bluetooth.getPairedDevices().then(pairedDevices => {
      this.setState({ pairedDevices: pairedDevices });
      console.log("state=>", this.state.pairedDevices);
    });
  };

  scan = () => {
    let tem = NativeModules.Bluetooth.scan();
    console.log(tem);
  };

  getDeviceId = (item) => {
 let index = item.indexOf(":") - 2;
 selectedDeviceId = item.slice(index).trim()
//  console.log(selectedDeviceId)
this.setState({selectedDeviceId})

if(selectedDeviceId != null){
    NativeModules.Bluetooth.startCreateThread(selectedDeviceId);
}




setInterval(() => {
    this.interval = NativeModules.Bluetooth.getReceivedData(item=>{
        console.log('====================================');
        console.log("printing received data from js");
        console.log('====================================');
        console.log(item);
    })
}, 100);

// this.listener = DeviceEventEmitter.addListener('myAwesomeEvent', e => console.log(e));

  }


  componentDidMount() {
    this.checkBluetoothSupport();
    this.startBluetooth();
    // this.getPairedDevices();
    // this.scan();
  }

  componentWillUnmount(){
      console.log("clearing");
    clearInterval(this.interval);
    console.log("cleared");
    
  }

  render() {
      console.log(typeof(this.state.selectedDeviceId))
    return (
      <View
        style={{
          height: "100%",
          width: "100%",
        }}
      >
        <View style={{alignSelf:"flex-start"}}>
          <Text>Bluetooth Screen</Text>
        </View>

        <View style={{width:"100%", justifyContent: "center",
          alignItems: "center"}}>
          <TouchableHighlight onPress={() => this.getPairedDevices()}
          style={{width:"40%", height:30, backgroundColor: "#202020"}}
          >
            <Text style={{color:"#fff", textAlign:"center"}}>Get Paired Devices</Text>
          </TouchableHighlight>
        </View>
        <TouchableHighlight
        onPress = {()=> this.disconnectSocket()}
        >
            <Text>disconnect socket</Text>
        </TouchableHighlight>
        <View>
         {   
 this.state.pairedDevices != null?<View>{
 this.state.pairedDevices.map(item=><TouchableHighlight key={item}
 onPress={()=>this.getDeviceId(item)}
 >
     <Text>{item}</Text></TouchableHighlight>)}
</View>:null
         }
        </View>
           
        
      </View>
    );
  }
}

export default BluetoothScreen;
