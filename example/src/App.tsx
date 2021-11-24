import * as React from 'react';

import { StyleSheet, View, Text, SafeAreaView, TextInput, TouchableOpacity } from 'react-native';
import { authorization, enrollProcedure, isEnrolled, isReadyForAuthorization, linkTenant, multiply, unlinkTenant, updateDeviceToken } from 'react-native-okay-sdk';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);

  return (
    <SafeAreaView>
      {/* <Text>Your externalId: {externalId}</Text>
      <Text>Your deviceToken: {token}</Text> */}
      <View style={styles.container}>
        <TouchableOpacity style={styles.button} onPress={enrollProcedure}>
          <Text style={styles.buttonText}>Enroll device</Text>
        </TouchableOpacity>
        {/* <TextInput
          placeholder="Enter linking code"
          value={linkingCode}
          onChangeText={setCode}
        /> */}
        <TouchableOpacity style={styles.button} onPress={linkTenant}>
          <Text style={styles.buttonText}>Link device</Text>
        </TouchableOpacity>
        {/* <TextInput
          placeholder="Enter tenant ID"
          value={tenantId}
          onChangeText={setTenantId}
        /> */}
        <TouchableOpacity style={styles.button} onPress={unlinkTenant}>
          <Text style={styles.buttonText}>Unlink tenant</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={updateDeviceToken}>
          <Text style={styles.buttonText}>updateDeviceToken</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={isReadyForAuthorization}>
          <Text style={styles.buttonText}>isReadyForAuthorization</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={authorization}>
          <Text style={styles.buttonText}>authorization</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={isEnrolled}>
          <Text style={styles.buttonText}>isEnrolled</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    height: '100%'
  },
  button: {
    width: 200,
    height: 75,
    backgroundColor: 'blue',
    color: 'white',
    justifyContent: 'center',
    margin: 10
  },
  buttonText: {
    color: 'white',
    alignSelf: 'center'
  }
});
