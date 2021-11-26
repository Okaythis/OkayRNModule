import * as React from 'react';

import { StyleSheet, View, Text, SafeAreaView, TextInput, TouchableOpacity } from 'react-native';
import { authorization, enrollProcedure, isEnrolled, isReadyForAuthorization, linkTenant, multiply, unlinkTenant, updateDeviceToken } from 'react-native-okay-sdk';
import PushNotificationIOS from '@react-native-community/push-notification-ios';

export default function App() {
  const [linkingCode, setLinkingCode] = React.useState('');
  const [tenantId, setTenantId] = React.useState('1');
  const [sessionId, setSessionId] = React.useState('1');

  React.useEffect(() => {
    PushNotificationIOS.requestPermissions().then(console.log);
    PushNotificationIOS.addEventListener("register", (token) => {
      console.log('registered to push notif', token)
      updateDeviceToken(token)
    })
    PushNotificationIOS.addEventListener("notification", (notification) => {
      const data = notification.getData();
      setSessionId(data?.sessionId);
      console.log('received notification: ', notification, data);
    })
    return () => {
      PushNotificationIOS.removeEventListener("register")
      PushNotificationIOS.removeEventListener("notification")
    }
  }, []);

  const onLinkTenantClick = () => {
    linkTenant(linkingCode)
  }

  const onUnlinkTenantClick = () => {
    unlinkTenant(+tenantId)
  }

  const onAuthClick = () => {
    authorization(+sessionId);
  }

  return (
    <SafeAreaView>
      <View style={styles.container}>
        <TouchableOpacity style={styles.button} onPress={enrollProcedure}>
          <Text style={styles.buttonText}>Enroll device</Text>
        </TouchableOpacity>
        <TextInput
          style={styles.textInput}
          placeholder="Enter linking code"
          value={linkingCode}
          onChangeText={setLinkingCode}
        />
        <TouchableOpacity style={styles.button} onPress={onLinkTenantClick}>
          <Text style={styles.buttonText}>Link device</Text>
        </TouchableOpacity>
        <TextInput
        style={styles.textInput}
          keyboardType={'numeric'}
          placeholder="Enter tenant ID"
          value={tenantId}
          onChangeText={setTenantId}
        />
        <TouchableOpacity style={styles.button} onPress={onUnlinkTenantClick}>
          <Text style={styles.buttonText}>Unlink tenant</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={isReadyForAuthorization}>
          <Text style={styles.buttonText}>isReadyForAuthorization</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={onAuthClick}>
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
  textInput: {
    borderBottomWidth: 1,
    marginTop: 20,
    marginBottom: 5,
    width: '60%',
    fontSize: 16,
    padding: 5
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
