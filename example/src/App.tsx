import * as React from 'react';

import { StyleSheet, View, Text, SafeAreaView, TextInput, TouchableOpacity } from 'react-native';
import { authorization, enrollProcedure, isEnrolled, isReadyForAuthorization, linkTenant, unlinkTenant, updateDeviceToken } from 'react-native-okay-sdk';
import messaging from '@react-native-firebase/messaging';


interface Tenant {
  tenantId: number,
  name: string,
  theme: any
}

async function requestUserPermission() {
  const authStatus = await messaging().requestPermission();
  const enabled =
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL;
  if (enabled) {
    console.log('Authorization status:', authStatus);
    messaging().getToken().then(token => {
      updateDeviceToken(token || '');
    })
  }
}

export default function App() {
  const [linkingCode, setLinkingCode] = React.useState('');
  const [sessionId, setSessionId] = React.useState('');
  const [tenant, setTenant] = React.useState<Tenant>({} as Tenant);

  React.useEffect(() => {
    requestUserPermission();
    const unsubscribe = messaging().onMessage(async remoteMessage => {
      console.log('A new FCM message received!', remoteMessage.data?.data);
      const id = JSON.parse(remoteMessage?.data?.data ?? '')?.sessionId
      setSessionId(id?.toString() ?? '');

    });

    return unsubscribe
  }, []);

  const onLinkTenantClick = () => {
    linkTenant(linkingCode).then((t: Tenant) => {
      setTenant(t)
    })
  }

  const onUnlinkTenantClick = () => {
    unlinkTenant(tenant.tenantId)
  }

  const onAuthClick = () => {
    authorization(Number(sessionId));
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
          value={tenant?.tenantId?.toString() ?? ''}
          onChangeText={(val) => setTenant(prevState => ({
            ...prevState,
            tenantId: +val
          }))}
        />
        <TouchableOpacity style={styles.button} onPress={onUnlinkTenantClick}>
          <Text style={styles.buttonText}>Unlink tenant</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={isReadyForAuthorization}>
          <Text style={styles.buttonText}>isReadyForAuthorization</Text>
        </TouchableOpacity>
        <TextInput
          style={styles.textInput}
          placeholder="Enter session id"
          value={sessionId}
          onChangeText={setSessionId}
        />
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
