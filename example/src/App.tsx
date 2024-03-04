import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  SafeAreaView,
  TextInput,
  ActivityIndicator,
  TouchableOpacity,
  Platform,
} from 'react-native';
import {
  startAuthorization,
  startEnrollment,
  isEnrolled,
  isReadyForAuthorization,
  linkTenant,
  unlinkTenant,
  updateDeviceToken,
  OkayLinkResponse,
  initOkay,
} from 'react-native-okay-sdk';

import messaging from '@react-native-firebase/messaging';
import { useEffect, useState } from 'react';
// import firebase from '@react-native-firebase/app';

let installationID = Platform.OS === 'android' ? '9990' : '9980';
const pubPssBase64 =
  'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxgyacF1NNWTA6rzCrtK60se9fVpTPe3HiDjHB7MybJvNdJZIgZbE9k3gQ6cdEYgTOSG823hkJCVHZrcf0/AK7G8Xf/rjhWxccOEXFTg4TQwmhbwys+sY/DmGR8nytlNVbha1DV/qOGcqAkmn9SrqW76KK+EdQFpbiOzw7RRWZuizwY3BqRfQRokr0UBJrJrizbT9ZxiVqGBwUDBQrSpsj3RUuoj90py1E88ExyaHui+jbXNITaPBUFJjbas5OOnSLVz6GrBPOD+x0HozAoYuBdoztPRxpjoNIYvgJ72wZ3kOAVPAFb48UROL7sqK2P/jwhdd02p/MDBZpMl/+BG+qQIDAQAB';

let appAPNT= ''

async function requestUserPermission(callback: (token: string) => void) {
  const authStatus = await messaging().requestPermission();
  const enabled =
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL;
  if (enabled) {
    console.log('Authorization status:', authStatus);
    messaging()
      .getToken()
      .then((token) => {
        console.log('token: ', token);
        updateDeviceToken(token || '');
        callback(token);
        appAPNT = token;
      });
  }
}

async function initSdk(callback: (token: string) => void) {
  try {
    requestUserPermission(callback);
    const response = await initOkay({
      okayUrlEndpoint: 'https://stage.okaythis.com',
      resourceProvider: {
        iosBiometricAlertReasonText: 'Test Alert',
      },
    });
    console.log('Init sdk status: ', response.initStatus);
  } catch (error) {
    console.error('Error init sdk', error);
  }
}

export default function App() {
  const [linkingCode, setLinkingCode] = React.useState('');
  const [sessionId, setSessionId] = React.useState('');
  const [tenantId, setTenantId] = React.useState<number>();
  const [deviceToken, setDeviceToken] = React.useState('');
  const [externalId, setExternalId] = React.useState('');
  const [showLoader, setShowLoader] = useState(false);


  React.useEffect(() => {

    // const timeoutId = setTimeout(() => {
    //   setShowLoader(false);
    // }, 400);

    initSdk(setDeviceToken).catch(e => `${e}`);
    messaging().onMessage(async (remoteMessage) => {
      console.log('A new FCM message received!', remoteMessage.data?.data);
      setShowLoader(true)
      const timeoutId =  setTimeout(() => {
        setShowLoader(false);
      }, 400);
      const data = JSON.parse(remoteMessage?.data?.data as string);
      const id = data?.sessionId;
      console.log(deviceToken);
      setSessionId(id?.toString() ?? '');
      let response = await startAuthorization({
        deviceUiType: data.params.DEVICE_UI_TYPE,
        sessionId: data.sessionId,
        appPns: appAPNT,
        pageTheme: {
          screenBackgroundColor: '#ffffff',
          actionBarBackgroundColor: '#004ba0',
          actionBarTextColor: '#ffd95a',
          pinNumberButtonTextColor: '#000000',
          pinNumberButtonBackgroundColor: '#ffd95a',
          pinRemoveButtonBackgroundColor: '#ffd95a',
          pinRemoveButtonTextColor: '#000000',
          pinTitleTextColor: '#ffffff',
          pinValueTextColor: '#ffffff',
          titleTextColor: '#ffd95a',
          questionMarkColor: '#63a4ff',
          transactionTypeTextColor: '#000000',
          authInfoBackgroundColor: '#ffd95a',
          infoSectionTitleColor: '#ffffff',
          infoSectionValueColor: '#000000',
          fromTextColor: '#000000',
          messageTextColor: '#000000',
          confirmButtonBackgroundColor: '#ffd95a',
          confirmButtonTextColor: '#000000',
          cancelButtonBackgroundColor: '#63a4ff',
          cancelButtonTextColor: '#ffffff',
          authConfirmationButtonBackgroundColor: '#f9a825',
          authConfirmationButtonTextColor: '#000000',
          authCancellationButtonBackgroundColor: '#1976d2',
          authCancellationButtonTextColor: '#ffffff',
          nameTextColor: '#000000',
          buttonBackgroundColor: '#ffffff',
          buttonTextColor: '#ffffff',
          inputTextColor: '#000000',
          inputSelectionColor: '#00FF00',
          inputErrorColor: '#FF0000',
          inputDefaultColor: '#888888',
          tenantName: '',
          tenantLogoPath: ''
        },
      }).catch(e => {
        console.log(e)
      });
      console.log(response);
      clearTimeout(timeoutId)
      setShowLoader(false)
    });

    return () => {
      // clearTimeout(timeoutId)
      // return unsubscribe
    };
  }, []);

  const onLinkTenantClick = () => {
    linkTenant(linkingCode, {
      appPns: deviceToken,
      pubPss: pubPssBase64,
      externalId: externalId,
      installationId: installationID,
    })
      .then(({ linkingSuccessStatus, tenantId }: OkayLinkResponse) => {
        if (linkingSuccessStatus) {
          setTenantId(tenantId);
          console.log(`linking status: ${linkingSuccessStatus}`)
        }
      })
      .catch(console.error);
  };

  const onUnlinkTenantClick = () => {
    if (tenantId) {
      unlinkTenant(tenantId, {
        appPns: deviceToken,
        pubPss: pubPssBase64,
        externalId: externalId,
        installationId: installationID,
      });
    }
  };

  const onAuthClick = () => {
    startAuthorization({
      sessionId: Number(sessionId),
      appPns: deviceToken,
    });
  };

  const onEnrollClick = () => {
    startEnrollment({
      appPns: deviceToken,
      pubPss: pubPssBase64,
      enrollInBackground: true,
      installationId: installationID,
    }).then((response) => {
      setExternalId(response.externalId);
      console.log(response)
    });
  };


  return (
    <SafeAreaView>
      {showLoader ? Loader() : <View style={styles.container}>
        <TouchableOpacity style={styles.button} onPress={onEnrollClick}>
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
          value={tenantId?.toString() ?? ''}
          onChangeText={(val) => setTenantId(+val)}
        />
        <TouchableOpacity style={styles.button} onPress={onUnlinkTenantClick}>
          <Text style={styles.buttonText}>Unlink tenant</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={isReadyForAuthorization}
        >
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
      </View>}
    </SafeAreaView>
  );
}

const Loader = () => {
  return (
        <View style={styles.loaderContainer}>
          <ActivityIndicator size="large" color="#0000ff" />
          <Text>Loading...</Text>
        </View>
  );
};


const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    height: '100%',
  },
  textInput: {
    borderBottomWidth: 1,
    marginTop: 20,
    marginBottom: 5,
    width: '60%',
    fontSize: 16,
    padding: 5,
  },
  button: {
    width: 200,
    height: 75,
    backgroundColor: 'blue',
    color: 'white',
    justifyContent: 'center',
    margin: 10,
  },
  buttonText: {
    color: 'white',
    alignSelf: 'center',
  },

  loader_container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loaderContainer: {
    alignItems: 'center',
  },
  contentContainer: {
    alignItems: 'center',
  },
});
