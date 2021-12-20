import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-okay-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

export const OkaySdk = NativeModules.OkaySdk
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
  return OkaySdk.initOkay(initData).then(console.log).catch(console.error);
}

export function updateDeviceToken(token: string): void {
  return OkaySdk.updateDeviceToken(token)
    .then(console.log)
    .catch(console.error);
}

export function isEnrolled(): Promise<string> {
  return OkaySdk.isEnrolled().then(console.log).catch(console.error);
}

export function linkTenant(code: string): Promise<string> {
  return OkaySdk.linkTenant(code).then(console.log).catch(console.error);
}
export function unlinkTenant(id: number): Promise<string> {
  return OkaySdk.unlinkTenant(id).then(console.log).catch(console.error);
}
export function startEnrollment(enrollData: any): Promise<string> {
  return OkaySdk.startEnrollment(enrollData)
    .then(console.log)
    .catch(console.error);
}
export function isReadyForAuthorization(): Promise<string> {
  return OkaySdk.isReadyForAuthorization()
    .then(console.log)
    .catch(console.error);
}
export function startAuthorization(sessionId: number): Promise<string> {
  return OkaySdk.startAuthorization(sessionId)
    .then(console.log)
    .catch(console.error);
}
