import * as React from 'react';

import {
  Platform,
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import {
  initOkay,
  linkTenant,
  type OkayBiometricLoginResponse,
  type OkayLinkResponse,
  type OkayPINLoginResponse,
  startAuthorization,
  startBiometricLogin,
  startEnrollment,
  startPINLogin,
  unlinkTenant,
  updateDeviceToken,
} from 'rn-okay-sdk';

import messaging from '@react-native-firebase/messaging';

let installationID = Platform.OS === 'android' ? '9990' : '9980';
const pubPssBase64 =
  'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxgyacF1NNWTA6rzCrtK60se9fVpTPe3HiDjHB7MybJvNdJZIgZbE9k3gQ6cdEYgTOSG823hkJCVHZrcf0/AK7G8Xf/rjhWxccOEXFTg4TQwmhbwys+sY/DmGR8nytlNVbha1DV/qOGcqAkmn9SrqW76KK+EdQFpbiOzw7RRWZuizwY3BqRfQRokr0UBJrJrizbT9ZxiVqGBwUDBQrSpsj3RUuoj90py1E88ExyaHui+jbXNITaPBUFJjbas5OOnSLVz6GrBPOD+x0HozAoYuBdoztPRxpjoNIYvgJ72wZ3kOAVPAFb48UROL7sqK2P/jwhdd02p/MDBZpMl/+BG+qQIDAQAB';

let appAPNT = '';

async function requestUserPermission(callback: (token: string) => void) {
  const authStatus = await messaging().requestPermission();
  const enabled =
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL;
  if (enabled) {
    console.log('Authorization status:', authStatus);
    messaging()
      .getToken()
      .then((token: string) => {
        console.log('token: ', token);
        updateDeviceToken(token || '');
        callback(token);
        appAPNT = token;
      });
  }
}

async function initSdk(callback: (token: string) => void) {
  try {
    await requestUserPermission(callback);

    const response = await initOkay({
      okayUrlEndpoint: 'https://stage.okaythis.com',
      resourceProvider: {
        androidBiometricPromptSubTitle: 'Biometric authorisation',
        androidConfirmBiometricButtonText: 'Authorise',
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        massPaymentDetailsButtonText: 'Test',
        feeLabelText: 'Test',
        recipientLabelText: 'Test',
        enrollmentTitleText: 'Test',
        enrollmentDescriptionText: 'Test',
        androidScreenhotsNotificationIconId: 'ic_menu_camera',
        androidScreenshotsChannelName: 'Test',
        androidTransactionDetails: 'Test',
        androidScreenshotsNotificationText: 'Test',
        androidAuthScreenTitle: 'Authorise',
        androidBiometricPromptTitle: 'Biometric Title',
        androidAuthorizationProgressViewText: 'Test',
        androidBiometricPromptDescription:
          'Please authenticate this transaction',
        iosBiometricAlertReasonText: 'Test Alert',
        iosConfirmBiometricTouchButtonText: 'Test',
        iosConfirmBiometricFaceButtonText: 'Test',
        iosMassPaymentDetailsHeaderText: 'Test',
      },
    });

    console.log('Init sdk status: ', response.initStatus);
  } catch (error) {
    console.error('Error init sdk', error);
  }
}

export default function App() {
  const [linkingCode, setLinkingCode] = React.useState('');
  const [tenantId, setTenantId] = React.useState<number>();
  const [deviceToken, setDeviceToken] = React.useState('');
  const [externalId] = React.useState('');

  React.useEffect(() => {
    initSdk(setDeviceToken).catch((e) => `${e}`);
    messaging().onMessage(async (remoteMessage: any) => {
      console.log('A new FCM message received!', remoteMessage.data?.data);
      const data = JSON.parse(remoteMessage?.data?.data as string);
      // const id = data?.sessionId;
      // console.log(data.params.DEVICE_UI_TYPE);

      let response = await startAuthorization({
        clientServerUrl: '',
        extSessionId: '',
        isDisableMultipleRetry: false,
        userExternalId: '',
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
          tenantLogoPath: '',
        },
      }).catch((e) => {
        console.log(e);
      });
      console.log(response);
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
      .then(({ linkingSuccessStatus, _tenantId }: OkayLinkResponse) => {
        if (linkingSuccessStatus) {
          setTenantId(_tenantId);
          console.log(`linking status: ${linkingSuccessStatus}`);
        }
      })
      .catch(console.error);
  };

  const onStartBiometricLoginClick = () => {
    startBiometricLogin()
      .then((response: OkayBiometricLoginResponse) => {
        console.log(response);
      })
      .catch(console.error);
  };

  const onStartPINLoginClick = () => {
    startPINLogin({
      publicKeyInBase64: 'tyty',
      clientVerificationServerURL: 'http://okay.com',
      wrongPinRetries: 3,
      userExternalId: 'string',
    })
      .then(
        ({
          pinLoginStatus,
          payload,
          header,
          signature,
          protectedAlgo,
          statusCode,
          message,
        }: OkayPINLoginResponse) => {
          if (pinLoginStatus) {
            console.log(` ${payload} ${header} ${signature} ${protectedAlgo}`);
            return;
          }
          console.log(`${statusCode} ${message}`);
        }
      )
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

  const onEnrollClick = () => {
    startEnrollment({
      appPns: deviceToken,
      pubPss: pubPssBase64,
      enrollInBackground: true,
      installationId: installationID,
    })
      .then((response) => {
        console.log(response);
        // setExternalId(response.externalId);
      })
      .catch((e) => {
        console.log(e);
      });
  };

  return (
    <SafeAreaView>
      <View style={styles.container}>
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

        <TouchableOpacity style={styles.button} onPress={onStartPINLoginClick}>
          <Text style={styles.buttonText}>PIN Login</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={onStartBiometricLoginClick}
        >
          <Text style={styles.buttonText}>Biometric Login</Text>
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
      </View>
    </SafeAreaView>
  );
}

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
