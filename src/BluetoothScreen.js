import React, { Component } from 'react';
import { View, Text, NativeModules } from 'react-native';


class BluetoothScreen extends Component {
    constructor(props){
        super(props);
        this.state = {
            isOn: false,
            bluetoothSupported: false,
            paired: null
        }
        

    }
    startBluetooth(){
        let temp = NativeModules.Bluetooth.startBluetooth().then(item=>{
            console.log(item);
        });
        
        this.updateStatus();
    }
    updateStatus = () => {
        this.setState({isOn:false})
        NativeModules.Bluetooth.getStatus((error, isOn) => {
          this.setState({ isOn: isOn });
        });
      };

      checkBluetoothSupport = () => {
          NativeModules.Bluetooth.checkBluetoothSupport((error, bluetoothSupported)=> {
              this.setState({bluetoothSupported: bluetoothSupported});
          })
      }

      getPairedDevices = () => {
          NativeModules.Bluetooth.getPairedDevices((paired)=>{
              console.log(paired)
              this.setState({paired: paired})
          })
      }

      scan = () => {
          let tem = NativeModules.Bluetooth.scan();
          console.log(tem);
      }




    componentDidMount(){
        this.checkBluetoothSupport();
        this.startBluetooth();
        // this.getPairedDevices();
        this.scan();
    }

  
    render() {
        // console.log(this.state.paired);
        return (
            <View>
                <Text>Bluetooth Screen</Text>
            </View>
        );
    }
}

export default BluetoothScreen;