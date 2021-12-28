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

export function initOkay(initData: object): void {
  return OkaySdk.initOkay(initData)
}

export function updateDeviceToken(token: string): void {
  return OkaySdk.updateDeviceToken(token)
}

export function isEnrolled(): Promise<string> {
  return OkaySdk.isEnrolled()
}

export function linkTenant(code: string, spaStorageData: any): Promise<string> {
  return OkaySdk.linkTenant(code, spaStorageData)
}
export function unlinkTenant(id: number | string, spaStorageData: any): Promise<string> {
  return OkaySdk.unlinkTenant(+id, spaStorageData)
}
export function startEnrollment(enrollData: any): Promise<string> {
  return OkaySdk.startEnrollment(enrollData)
}
export function isReadyForAuthorization(): Promise<string> {
  return OkaySdk.isReadyForAuthorization()
}
export function startAuthorization(spaAuthData: any): Promise<string> {
  return OkaySdk.startAuthorization(spaAuthData)
}
