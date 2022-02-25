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
export interface PSATheme {
  actionBarBackgroundColor?: number;
  actionBarTextColor?: number;
  buttonTextColor?: number;
  paymentDetailsButtonTextColor?: number;
  buttonBackgroundColor?: number;
  screenBackgroundColor?: number;
  progressColor?: number;
  progressMessageTextColor?: number;
  nameTextColor?: number;
  titleTextColor?: number;
  messageTextColor?: number;
  tenantLogoPath?: string;
  tenantName?: string;
  inputTextColor?: number;
  inputSelectionColor?: number;
  inputErrorColor?: number;
  inputDefaultColor?: number;
  questionMarkColor?: number;
  transactionTypeTextColor?: number;
  infoSectionTitleColor?: number;
  infoSectionValueColor?: number;
  infoSectionValueSecondaryColor?: number;
  fromTextColor?: number;
  authInfoBackgroundColor?: number;
  showDetailsTextColor?: number;
  confirmButtonBackgroundColor?: number;
  confirmButtonTextColor?: number;
  cancelButtonBackgroundColor?: number;
  cancelButtonTextColor?: number;
  authConfirmationBackgroundColor?: number;
  authConfirmationTitleColor?: number;
  authConfirmationMessageColor?: number;
  authConfirmationThumbColor?: number;
  authConfirmationApostropheColor?: number;
  authConfirmationButtonBackgroundColor?: number;
  authConfirmationButtonTextColor?: number;
  authCancellationBackgroundColor?: number;
  authCancellationTitleColor?: number;
  authCancellationMessageColor?: number;
  authCancellationThumbColor?: number;
  authCancellationApostropheColor?: number;
  authCancellationButtonBackgroundColor?: number;
  authCancellationButtonTextColor?: number;
  pinTitleTextColor?: number;
  pinFilledValueTextColor?: number;
  pinValueTextColor?: number;
  pinNumberButtonTextColor?: number;
  pinNumberButtonBackgroundColor?: number;
  pinRemoveButtonTextColor?: number;
  pinRemoveButtonBackgroundColor?: number;
  sessionTimerProgressColor?: number;
  sessionTimerTextColor?: number;
}
export interface ResourceProvider {
  confirmButtonText?: string;
  cancelButtonText?: string;
  massPaymentDetailsButtonText?: string;
  feeLabelText?: string;
  recipientLabelText?: string;
  enrollmentTitleText?: string;
  enrollmentDescriptionText?: string;
  androidScreenhotsNotificationIconId?: string;
  androidScreenshotsChannelName?: string;
  androidTransactionDetails?: string;
  androidBiometricPromptSubTitle?: string;
  androidScreenshotsNotificationText?: string;
  androidAuthScreenTitle?: string;
  androidBiometricPromptTitle?: string;
  androidConfirmBiometricButtonText?: string;
  androidAuthorizationProgressViewText?: string;
  androidBiometricPromptDescription?: string;
  iosBiometricAlertReasonText?: string;
  iosConfirmBiometricTouchButtonText?: string;
  iosConfirmBiometricFaceButtonText?: string;
  iosMassPaymentDetailsHeaderText?: string;
}

export interface InitData {
  initData: {
    okayUrlEndpoint: string;
    resourceProvider: ResourceProvider;
  };
}

export interface SpaEnrollData {
  SpaEnrollData: {
    pubPss: string;
    installationId: string;
    appPns?: string;
    pageTheme?: PSATheme;
    enrollInBackground?: boolean;
  };
}

export interface SpaAuthData {
  SpaAuthorizationData: {
    sessionId: number;
    appPns?: string;
    pageTheme?: PSATheme;
  };
}
export interface SpaStorageData {
  SpaStorageData: {
    externalId?: string;
    enrollmentId?: string;
    appPns: string;
    pubPss: string;
    installationId: string;
  };
}

export interface OkayInitResponse {
  initStatus: boolean;
}

export interface OkayEnrollmentResponse {
  enrollmentStatus: boolean;
  enrollmentId: string;
  externalId: string;
}

export interface OkayAuthResponse {
  authSessionStatus: boolean;
}

export interface OkayLinkResponse {
  linkingSuccessStatus: boolean;
  tenantId: number;
}

export interface OkayUnLinkResponse {
  unlinkingSuccessStatus: boolean;
}

export function initOkay(initData: InitData): Promise<OkayInitResponse> {
  return OkaySdk.initOkay(initData);
}

export function updateDeviceToken(token: string): Promise<boolean> {
  return OkaySdk.updateDeviceToken(token);
}

export function isEnrolled(): Promise<boolean> {
  return OkaySdk.isEnrolled();
}

export function linkTenant(
  code: string,
  spaStorageData?: SpaStorageData
): Promise<OkayLinkResponse> {
  return OkaySdk.linkTenant(code, spaStorageData);
}

export function unlinkTenant(
  id: number | string,
  spaStorageData?: SpaStorageData
): Promise<OkayUnLinkResponse> {
  return OkaySdk.unlinkTenant(+id, spaStorageData);
}

export function startEnrollment(
  enrollData: SpaEnrollData
): Promise<OkayEnrollmentResponse> {
  return OkaySdk.startEnrollment(enrollData);
}
export function isReadyForAuthorization(): Promise<boolean> {
  return OkaySdk.isReadyForAuthorization();
}
export function startAuthorization(
  spaAuthData: SpaAuthData
): Promise<OkayAuthResponse> {
  return OkaySdk.startAuthorization(spaAuthData);
}
