import * as React from 'react';

import { StyleSheet, View, Text, TouchableOpacity, Alert } from 'react-native';
import {
  initAPI,
  activateReader,
} from 'react-native-honey-well-scanner-reader';

export default function App() {
  const [barcodeText, setBarcodeText] = React.useState('');

  const initBarcodeReader = async () => {
    try {
      const initResult = await initAPI();
      console.log('initResult', initResult);
    } catch (ex) {
      Alert.alert('Error', 'Error while initialization. ' + ex, undefined, {
        cancelable: true,
      });
      console.log(ex);
    }
  };

  React.useEffect(() => {
    initBarcodeReader();
  }, []);

  const onBarcodeReadCallback = (text: string) => {
    setBarcodeText(text);
  };

  const onPressScanButton = () => {
    activateReader(onBarcodeReadCallback);
  };

  return (
    <View style={styles.container}>
      <Text>Barcode: {barcodeText}</Text>
      <View style={styles.button}>
        <TouchableOpacity onPress={onPressScanButton}>
          <Text style={styles.buttonText}>Scan</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  button: {
    padding: 10,
    backgroundColor: 'blue',
    marginVertical: 10,
  },
  buttonText: {
    color: 'white',
  },
});
