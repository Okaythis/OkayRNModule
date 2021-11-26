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

export function updateDeviceToken(token: string): void {
  return OkaySdk.updateDeviceToken(token).then(console.log).catch(console.error)
}
export function isEnrolled(): Promise<string> {
  return OkaySdk.isEnrolled().then(console.log).catch(console.error)
}
export function linkTenant(code: string): Promise<string> {
  return OkaySdk.linkTenant(code).then(console.log).catch(console.error)
}
export function unlinkTenant(id: number): Promise<string> {
  return OkaySdk.unlinkTenant(id).then(console.log).catch(console.error)
}
export function enrollProcedure(): Promise<string> {
  return OkaySdk.enrollProcedure({
    SpaEnrollData: {
      host: 'https://demostand.okaythis.com',
      pubPss: pubPssBase64,
      installationId: "9980"
    }
  })
  .then(console.log)
  .catch(console.error)
}
export function isReadyForAuthorization(): Promise<string> {
  return OkaySdk.isReadyForAuthorization().then(console.log).catch(console.error)
}
export function authorization(sessionId: number): Promise<string> {
  return OkaySdk.authorization(sessionId).then(console.log).catch(console.error)
}