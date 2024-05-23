# Installing Okay React Native Module


####  Create a folder called **custom_modules** in your React Native project root folder:

```sh
$ mkdir ~/project_dir/custom_modules
```

#####  Copy the downloaded React Native module from this repository to custom_modules folder

```sh
$ cp ~/Downloads/RNOkaySDK ~/project_dir/custom_modules
```
#####  Add RNOKaySDK as a local dependency to your package.json file:

```sh
"react-native-okay-sdk": "file:custom_modules/OkayRNModule"
```

##### Install node_modules:

Run the following command from your project root folder

```sh
$ yarn install
```

##### Link library with react-native:

Run the following command from your project root folder

```sh
$ react-native link react-native-okay-sdk
```
<br>

## Android
### Configure Android project:

Locate your ***project_dir/android/app/build.gradle*** file in your project workspace, then set your minSDKVersion in the *build.gradle* to API 21.

```groovy
buildscript {
  ext {
    buildToolsVersion = "34.0.0"
    minSdkVersion = 23
    compileSdkVersion = 34
    targetSdkVersion = 34
    ndkVersion = "26.1.10909125"
    kotlinVersion = "1.9.22"
  }
}
```

Add Okay maven repository to your Android ***project_dir/android/build.gradle***  file

```groovy
allprojects {
  repositories {
    mavenLocal()
    google()
    jcenter()
    maven {
      // All of React Native (JS, Obj-C sources, Android binaries) is installed from npm
      url "$rootDir/../node_modules/react-native/android"

    }
    // Begin: Add This
    maven {
      url "https://gitlab.okaythis.com/api/v4/projects/15/packages/maven"
      name "GitLab"
    }
    // End:
  }
}
```

#### Add permissions to AndroidManifest.xml:
Locate your *project_dir/android/src/main/AndroidManifest.xml* file, then add these Android permissions to the file.

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

##### Add **databinding** and **multidex** for android:

Add the following to your Android *project_dir/android/app/build.gradle* file

```groovy
android {
  compileSdkVersion rootProject.ext.compileSdkVersion

  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }

  // End
  defaultConfig {
    ...
    multiDexEnabled true // Add this line
  }
  ...
}
```
## Install react-native Firebase:

The Okay service uses Firebase push notification service to deliver 2FA push notifications to your app. Please see the following link for further instructions on how to install Firebase for React Native https://rnfirebase.io/.
<br>


### Enable Push Notifications

Please visit this link to enable Push Notification for iOS devices when using React Native: https://facebook.github.io/react-native/docs/pushnotificationios


## **API Usage**
- initOkay(initData: InitData): Promise<OkayInitResponse> (Android Only)
- updateDeviceToken(token: string): Promise<boolean> (iOS only)
- linkTenant(code: string, spaStorageData?: SpaStorageData): Promise<OkayLinkResponse>;
- unlinkTenant(id: number | string, spaStorageData?: SpaStorageData): Promise<OkayUnLinkResponse>;
- isEnrolled(): Promise<boolean>
- startEnrollment(enrollData: SpaEnrollData): Promise<OkayEnrollmentResponse>
- isReadyForAuthorization(): Promise<boolean>;
- startAuthorization(spaAuthData: SpaAuthData): Promise<OkayAuthResponse>;
- startPINLogin(loginData: OkayPINLogin): Promise<OkayPINLoginResponse>
- startBiometricLogin(): Promise<OkayBiometricLoginResponse>
- setLoginTheme(loginTheme: OkayLoginTheme): Promise<String>

### **SDK Initialization:**

We will need to call the initOkay(object) on the SDK to properly initialize the Okay SDK. For example we pass in 'https://demostand.okaythis.com/' as our Okay server endpoint.

```javascript
  initOkay({
      okayUrlEndpoint: 'https://demostand.okaythis.com',
  })
```
### **Using Custom Fonts With Okay Flutter based UI**
In order to be able to use your custom font with the Okay React Native module, you will be required to add your fonts manually to your app.
i.e you will be required to bundle the font file as part of your application either as assets in the Android app or adding the fonts
as part of your iOS bundle. When you have successfully added the font to the native part of your app, you can now add the font configuration to the
Okay initialisation method as can be seen below.

```javascript

  initOkay({
    okayUrlEndpoint: 'https://demostand.okaythis.com',
    fontConfig: [
      {
        fontVariant: 'FiraGO_200italic',
        fontAssetPath: 'fonts/firago_eightitalic.ttf',
      },
    ],
  })
    .then(response => {
      console.log('init: ', response);
    })
    .catch(error => {
      console.error('error: ', error);
    })
```

The fontConfig property takes an array of the FontVariants datatype.

```javascript
export interface FontVariants {
  fontVariant: string;
  fontAssetPath: string;
}
```

The FontVariant datatype consists of the following properties:

- **fontVariant**: The font variant is made up of the name of the font followed by an underscore, the size and the style of the font.

- **fontAssetPath**: On iOS, this will be the full name of the font with the extension included in the iOS bundle or the asset path on Android.

### Permission Request
For Okay to work correctly, you are required to prompt the user to grant the notification permissions on iOS.

```javascript
import messaging from '@react-native-firebase/messaging';
// Snippet of permission request using Firebase SDK

  const authStatus = await messaging().requestPermission();
  console.log('status: ', authStatus);
  const enabled =
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL;
  if (enabled) {
    console.log('Authorization status:', authStatus);
    messaging().getToken().then(token => {
      updateDeviceToken(token || '');
    })
  }

```

### **Update Okay with Token received from PushNotificationsIOS (iOS ONLY)**
You will need to update Okay with the push notification token generated for iOS devices.

#### APNS
```javascript
// We can update iOS PNS token in this lifecycle method here

// If your are using APNS, your code will look similar to the following
PushNotificationIOS.addEventListener('register', token => {
    updateDeviceToken(token);
});

```

### **Update Okay with Token received from Firebase (Android and iOS)**
You will need to update Okay with the push notification token for an Android device or Firebase
registration token generated for the iOS device.

#### Firebase

```javascript
import messaging from '@react-native-firebase/messaging';

messaging().getToken().then(token => {
    updateDeviceToken(token || '');
})
```

### **How to enroll a user**
If the required permissions have been granted on the device, we can now proceed to enrolling the user. Okay SDK provides the *startEnrollment(enrollData: SpaEnrollData)* method which takes a JSON SpaEnrollData object.

```javascript
import messaging from '@react-native-firebase/messaging';

messaging().getToken().then(token => {
    startEnrollment({
            appPns: token,
            pubPss: pubPssBase64,
            installationId: "9990",
            pageTheme: {
              // Page Theme customization, if you don't want customization: pageTheme: null.
              actionBarTitle: "YOUR_ACTION_BAR_TITLE",
              actionBarBackgroundColor: "#ffffff",
              actionBarTextColor: "#ffffff",
              buttonTextColor: "#ffffff",
            }
        }).then(response => console.log(response));
    })
```
SpaEnrollData contains several keys that are required for a secure communication with Okay servers.

*"appPns"*: This is your push notification token from Firebase(or Firebase registration token for iOS devices if you are using Firebase for push notification on iOS) or APNS token if you are using APNS. This allows us to send 2FA notifications to your apps.

*"installationId"*: The installationId is a unique value that identifies unique installation keys for the Okay SDK in an app. For testing purposes we ask our users to use this value **9990** as their installationId on Android and **9980** on iOS.

*"pubPss"*: This is a public key we provide to applications that use our SDK for secure communication with the Okay server. For testing purposes we ask our users to use the value below as their *"pubPss"* key.

 ```javascript
  const pubPssBase64 = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxgyacF1NNWTA6rzCrtK60se9fVpTPe3HiDjHB7MybJvNdJZIgZbE9k3gQ6cdEYgTOSG823hkJCVHZrcf0/AK7G8Xf/rjhWxccOEXFTg4TQwmhbwys+sY/DmGR8nytlNVbha1DV/qOGcqAkmn9SrqW76KK+EdQFpbiOzw7RRWZuizwY3BqRfQRokr0UBJrJrizbT9ZxiVqGBwUDBQrSpsj3RUuoj90py1E88ExyaHui+jbXNITaPBUFJjbas5OOnSLVz6GrBPOD+x0HozAoYuBdoztPRxpjoNIYvgJ72wZ3kOAVPAFb48UROL7sqK2P/jwhdd02p/MDBZpMl/+BG+qQIDAQAB'
 ```

*"pageTheme"*: This is a JSON object that allows you to customize the colors for our enrollment and authorization **native** screens to suit your product branding. This properties may not be applicable to the Flutter UI templates [Click here to see all valid color properties](https://github.com/Okaythis/okay-sdk-android/wiki/PageTheme-(Android))


### **How to link a user**
The linkTenant(linkingCode, SpaStorage) method links a user of your application with an existing tenant on the Okay secure server. When you make a linking request to the Okay server, it returns a linkingCode as part of its response (For more information on how to send a linking request please see this [documatation](https://okaythis.com/developer/documentation/v1/server#1.2)). The linking code can be passed directly to this method after a successful linking request to the Okay Service.

The **externalId** can be retrieved from the *startEnrollment(...).then( externalId => ...)* method, if the method was called and executed successfully.

```javascript
import messaging from '@react-native-firebase/messaging';

messaging().getToken().then(token => {
    linkTenant(
        linkingCode,
        {
            appPns: token,
            pubPss: pubPssBase64,
            externalId: 'YOUR_EXTERNAL_ID',
            installationId: "9990",
        })
    })
```

### **How to unlink a user**
If a user was successfully linked to a tenant and you now wish to unlink that user from your tenant on the Okay secure server, you can use the *unlinkTenant(tenantId, SpaStorage)* method to do this as shown below.

```javascript

unlinkTenant(
  tenantId,
  {
    appPns: token,
    pubPss: pubPssBase64,
    externalId: 'YOUR_EXTERNAL_ID',
    installationId: '9990',
  }
);
```


### **Authorizing a user's transaction**
When there is a transaction that needs to be authorized by your application, Okay sends a push notification to your mobile app with the transaction details needed to complete that request. The body of the push notification has the following fields as it payload:

```json
{
  "tenantId": <int>,
  "sessionId": <int>
  ...
}
```


Push Notification Sample:

```javascript
  {
    "collapseKey": "auth",
    "data": {
      "data": {“tenantId”:10000,“sessionId”:2680005,“params”:{“DEVICE_UI_TYPE”:“NATIVE”},“expectedDeviceState”:“ipmHQ3zVtVE6LIssTWGb4dJXS+ExkZk0QIA7qsBePx8=“,”sessionExternalId”:“960002”,“clientServerUrl”:“http://192.168.18.96:8080”,“isDisableMultipleRetry”:false}
      "sender": "PSS",
      "type": "10"
    },
    "from": "0",
    "messageId": "0",
    "sentTime": 0,
    "ttl": 0
  }
```

When the push notification is received on the client side, you should retrieve the **_sessionId_** and **_DEVICE_UI_TYPE_** properties from the push notification body. Pass in the **_sessionId_** and **_DEVICE_UI_TYPE_** value to **startAuthorization(spaAuthData: SpaAuthData)** method as shown in the code snippet below:

```javascript
import messaging from '@react-native-firebase/messaging';

messaging().onMessage(async message => {
  console.log('message: ', message);
  let data = JSON.parse(message.data.data);
  let response = await startAuthorization({
    clientServerUrl: data.clientServerUrl,
    extSessionId: data.sessionExternalId,
    isDisableMultipleRetry: false,
    userExternalId: 'user-123445558999',
    deviceUiType: data.params.DEVICE_UI_TYPE,
    sessionId: data.sessionId,
    appPns: appPushNotificationToken,
  });
  console.log(response);
});

```


### The Okay In-App Login

The Okay SDK provides in-app APIs for PIN and Biometric login on app start.

#### PIN
The following snippet allows you to start the PIN login screen with the Okay API.

```javascript
startPINLogin({
  publicKeyInBase64: "your-public-key",
  clientVerificationServerURL: "http://your-server-verification-url.com",
  wrongPinRetries: 3,
  userExternalId: "user-123445558999",
})
  .then(({ pinLoginStatus, payload, header, signature, protectedAlgo, statusCode, message }: OkayPINLoginResponse) => {
    if (pinLoginStatus) {
      console.log(` ${payload} ${header} ${signature} ${protectedAlgo}`);
      return;
    }
    console.log(` ${statusCode} ${message}`);
  })
  .catch(console.error);

```

#### Customizing the Okay PIN Screen

The Okay SDk allow you to customize the look and feel of the PIN screen both on Android and iOS.
The SDK accepts the following properties that conforms to the OkayLoginTheme interface.

```javascript
export interface OkayLoginTheme {
  pinTitleText?: string;
  pinSubTitleText?: string;
  forgotPinText?: string;
  pinScreenBackgroundColor?: string;
  pinTitleTextColor?: string;
  pinSubTitleTextColor?: string;
  pinFilledColor?: string;
  pinPadTextColor?: string;
  pinPadBackgroundColor?: string;
  pinSubTitleErrorText?: string;
  severErrorText?: string;
  shuffleKeyPad?: boolean;
}
```

This is an example on how to customise the login screen

```javascript

await setLoginTheme({
  pinTitleText: "Login Title",
  pinSubTitleText: "Enter PIN to login",
  forgotPinText: "Forgot PIN?",
  pinScreenBackgroundColor: "#ffffff",
  pinTitleTextColor: "#ffd95a",
  pinSubTitleTextColor: "#ffd95a",
  pinFilledColor: "#ffd95a",
  pinPadTextColor: '#ffffff',
  pinPadBackgroundColor: '#ffffff',
  pinSubTitleErrorText: "You entered a wrong PIN. Please try again",
  severErrorText: "There was an error validating your PIN",
  shuffleKeyPad: false,
});

```

#### Biometric

The following snippet allows you to start the Biometric login prompt with the Okay API.

```javascript
startBiometricLogin()
  .then(({ biometricLoginStatus, payload, header, signature, protectedAlgo, status, message, sessionRemainingSeconds }: OkayBiometricLoginResponse) => {
    if (biometricLoginStatus) {
      console.log(` ${payload} ${header} ${signature} ${protectedAlgo}`);
      return;
    }
    console.log(` ${status} ${message}`);
  })
  .catch(console.error);
```

Please reach out to the Okay support team to provide more information about the specification of the PIN and biometric login verification server for In-App login response.
