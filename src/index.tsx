import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-okay-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const OkaySdk = NativeModules.OkaySdk
  ? NativeModules.OkaySdk
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const pubPssBase64 = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxgyacF1NNWTA6rzCrtK60se9fVpTPe3HiDjHB7MybJvNdJZIgZbE9k3gQ6cdEYgTOSG823hkJCVHZrcf0/AK7G8Xf/rjhWxccOEXFTg4TQwmhbwys+sY/DmGR8nytlNVbha1DV/qOGcqAkmn9SrqW76KK+EdQFpbiOzw7RRWZuizwY3BqRfQRokr0UBJrJrizbT9ZxiVqGBwUDBQrSpsj3RUuoj90py1E88ExyaHui+jbXNITaPBUFJjbas5OOnSLVz6GrBPOD+x0HozAoYuBdoztPRxpjoNIYvgJ72wZ3kOAVPAFb48UROL7sqK2P/jwhdd02p/MDBZpMl/+BG+qQIDAQAB'

export function multiply(a: number, b: number): Promise<number> {
  return OkaySdk.multiply(a, b);
}

export function updateDeviceToken(): void {
  return OkaySdk.updateDeviceToken('test').then(console.log)
}

export function isEnrolled(): Promise<string> {
  return OkaySdk.isEnrolled().then(console.log)
}

export function linkTenant(): Promise<string> {
  return OkaySdk.linkTenant("1").then(console.log)
}
export function unlinkTenant(): Promise<string> {
  return OkaySdk.unlinkTenant(1).then(console.log)
}
export function enrollProcedure(): Promise<string> {
  return OkaySdk.enrollProcedure({
    SpaEnrollData: {
      host: 'https://demostand.okaythis.com',
      pubPss: pubPssBase64,
      installationId: "9990"
    }
  }).then(console.log)
}
export function isReadyForAuthorization(): Promise<string> {
  return OkaySdk.isReadyForAuthorization().then(console.log)
}
export function authorization(): Promise<string> {
  return OkaySdk.authorization({}).then(console.log)
}