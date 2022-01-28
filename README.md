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
        buildToolsVersion = "28.0.3"
        minSdkVersion = 21 // use API 21
        compileSdkVersion = 30
        targetSdkVersion = 30
        ndkVersion = "21.4.7075529"
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
    // Begin Add DataBinding
    dataBinding {
        enabled = true
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
- initOkay(okayServerAdress: string): Promise<string> (Android Only)
- updateDeviceToken(token: string): void (iOS only)
- isEnrolled(): boolean
- startEnrollment(enrollData: any): Promise<string>;
- linkTenant(code: string, spaStorageData: any): Promise<string>;
- unlinkTenant(id: number | string, spaStorageData: any): Promise<string>;
- isReadyForAuthorization(): Promise<string>;
- startAuthorization(spaAuthData: any): Promise<string>;

### **SDK Initialization:**

We will need to call the initOkay(object) on the SDK to properly initialize the Okay SDK. For example we pass in 'https://demostand.okaythis.com/' as our Okay server endpoint.

```javascript

  OkaySdk.initOkay({
    initData: {
      okayUrlEndpoint: 'https://stage.okaythis.com',
    },
  })
```


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
      console.log('token: ', token);
      OkaySdk.updateDeviceToken(token || '');
    })
  }

```

### **Update Okay with Token received from PushNotificationsIOS (iOS ONLY)**
We will need to update Okay with the push notification token generated for iOS devices.

```javascript
// We can update iOS PNS token in this lifecycle method here

// If your are using APNS, your code will look similar to the following
PushNotificationIOS.addEventListener('register', token => {
    RNOkaySdk.updateDeviceToken(token);
});

// If you are using Firebase your code will look similar to this
messaging().getToken().then(token => {
    console.log('token: ', token);
    OkaySdk.updateDeviceToken(token || '');
})

```

### **How to enroll a user**
If the required permissiosn have been granted on the device, we can now proceed to enrolling the user. Okay SDK provides the *startEnrollment(enrollData: any)* method which takes a Json with "SpaEnrollData" as key.

```javascript
messaging().getToken().then(token => {
    RNOkaySdk.startEnrollment({
        SpaEnrollData: {
            host: "https://demostand.okaythis.com/", // Okay server address
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
          }
        }).then(response => console.log(response));
    })
```
SpaEnrollData contains several keys that are required for a secure communication with Okay servers.

*"appPns"*: This is your push notification token from Firebase(or Firebase registration token for iOS devices if you are using Firebase for push notification on iOS) or APNS token if you are using APNS. This allows us to send 2FA notifications to your apps. 

*"installationId"*: The installationId is a unique value that identifies unique installation keys for the Okay SDK in an app. For testing purposes we ask our users to use this value **9990** as their installationId

*"pubPss"*: This is a public key we provide to applications that use our SDK for secure communication with the Okay server. For testing purposes we ask our users to use the value below as their *"pubPss"* key.

 ```javascript
  const pubPssBase64 = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxgyacF1NNWTA6rzCrtK60se9fVpTPe3HiDjHB7MybJvNdJZIgZbE9k3gQ6cdEYgTOSG823hkJCVHZrcf0/AK7G8Xf/rjhWxccOEXFTg4TQwmhbwys+sY/DmGR8nytlNVbha1DV/qOGcqAkmn9SrqW76KK+EdQFpbiOzw7RRWZuizwY3BqRfQRokr0UBJrJrizbT9ZxiVqGBwUDBQrSpsj3RUuoj90py1E88ExyaHui+jbXNITaPBUFJjbas5OOnSLVz6GrBPOD+x0HozAoYuBdoztPRxpjoNIYvgJ72wZ3kOAVPAFb48UROL7sqK2P/jwhdd02p/MDBZpMl/+BG+qQIDAQAB'

 ```

*"pageTheme"*: This is a JSON object that allows you to customize the colors for our enrollment and authorization screens to suit your product branding. [Click here to see all valid color properties](https://github.com/Okaythis/okay-sdk-android/wiki/PageTheme-(Android))


### **How to link a user**
The linkTenant(linkingCode, SpaStorage) method links a user of your application with an existing tenant on the Okay secure server. When you make a linking request to the Okay server, it returns a linkingCode as part of its response (For more information on how to send a linking request please see this [documatation](https://okaythis.com/developer/documentation/v1/server#1.2)). The linking code can be passed directly to this method after a successful linking request to the Okay Service. 

The **externalId** can be retrieved from the *RNOkaySdk.startEnrollment(...).then( externalId => ...)* method, if the method was called and executed successfully.

```javascript
messaging().getToken().then(token => {
    RNOkaySdk.linkTenant(
        linkingCode,
        {
          SpaStorage: {
            appPns: token,
            pubPss: pubPssBase64,
            externalId: 'YOUR_EXTERNAL_ID',
            installationId: "9990",
            enrollmentId: null
          }
        })
    })
```

### **How to unlink a user**
If a user was successfully linked to a tenant and you now wish to unlink that user from your tenant on the Okay secure server, you can use the *unlinkTenant(tenantId, SpaStorage)* method to do this as shown below.

```javascript
messaging().getToken().then(token => {
    RNOkaySdk.unlinkTenant(
        tenantId,
        {
          SpaStorage: {
            appPns: token,
            pubPss: pubPssBase64,
            externalId: 'YOUR_EXTERNAL_ID',
            installationId: "9990",
            enrollmentId: null
          }
      })
    })
  
```


### **Authorizing a User's Action**
When there is a transaction that needs to be authorized by your application, Okay sends a push notification to your mobile app with the transaction details needed to complete that request. The body of the push notification has the following fields as it payload:

```json
{
  "tenantId": <int>,
  "sessionId": <int>
  ...
}
```

When the push notification is received on the client side, you should retrieve the **_sessionId_** from the push notification body. Pass in the **_sessionId_** value to *RNOkaySdk.authorization(SpaAuthorizationData)* method directly as shown in the code snippet below:

```javascript
messaging().onMessage(async message => {
    let data = JSON.parse(message.data.data);
    let response = await OkaySdk.startAuthorization({
        SpaAuthorizationData: {
            sessionId: data.sessionId,
            appPns: deviceToken,
            pageTheme: null,
        },
    });         
});

```

### Page Theme properies for Android
- https://github.com/Okaythis/okay-sdk-android/wiki/PageTheme-(Android)
