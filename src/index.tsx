import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'rn-okay-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const RnOkaySdk = NativeModules.RnOkaySdk
  ? NativeModules.RnOkaySdk
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
export interface PSATheme {
  actionBarBackgroundColor?: string;
  actionBarTextColor?: string;
  buttonTextColor?: string;
  paymentDetailsButtonTextColor?: string;
  buttonBackgroundColor?: string;
  screenBackgroundColor?: string;
  progressColor?: string;
  progressMessageTextColor?: string;
  nameTextColor?: string;
  titleTextColor?: string;
  messageTextColor?: string;
  tenantLogoPath?: string;
  tenantName?: string;
  inputTextColor?: string;
  inputSelectionColor?: string;
  inputErrorColor?: string;
  inputDefaultColor?: string;
  questionMarkColor?: string;
  transactionTypeTextColor?: string;
  infoSectionTitleColor?: string;
  infoSectionValueColor?: string;
  infoSectionValueSecondaryColor?: string;
  fromTextColor?: string;
  authInfoBackgroundColor?: string;
  showDetailsTextColor?: string;
  confirmButtonBackgroundColor?: string;
  confirmButtonTextColor?: string;
  cancelButtonBackgroundColor?: string;
  cancelButtonTextColor?: string;
  authConfirmationBackgroundColor?: string;
  authConfirmationTitleColor?: string;
  authConfirmationMessageColor?: string;
  authConfirmationThumbColor?: string;
  authConfirmationApostropheColor?: string;
  authConfirmationButtonBackgroundColor?: string;
  authConfirmationButtonTextColor?: string;
  authCancellationBackgroundColor?: string;
  authCancellationTitleColor?: string;
  authCancellationMessageColor?: string;
  authCancellationThumbColor?: string;
  authCancellationApostropheColor?: string;
  authCancellationButtonBackgroundColor?: string;
  authCancellationButtonTextColor?: string;
  pinTitleTextColor?: string;
  pinFilledValueTextColor?: string;
  pinValueTextColor?: string;
  pinNumberButtonTextColor?: string;
  pinNumberButtonBackgroundColor?: string;
  pinRemoveButtonTextColor?: string;
  pinRemoveButtonBackgroundColor?: string;
  sessionTimerProgressColor?: string;
  sessionTimerTextColor?: string;
}

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

export interface FontVariants {
  fontVariant: string;
  fontAssetPath: string;
}

export interface InitData {
  okayUrlEndpoint: string;
  resourceProvider: ResourceProvider;
  fontConfig?: Array<FontVariants>;
}

export interface SpaEnrollData {
  pubPss: string;
  installationId: string;
  appPns?: string;
  pageTheme?: PSATheme;
  enrollInBackground?: boolean;
}

export interface SpaAuthData {
  deviceUiType: string;
  sessionId: number;
  extSessionId: string;
  clientServerUrl: string;
  userExternalId: string;
  isDisableMultipleRetry: boolean;
  appPns: string;
  pageTheme?: PSATheme;
}
export interface SpaStorageData {
  externalId?: string;
  enrollmentId?: string;
  appPns: string;
  pubPss: string;
  installationId: string;
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
  _tenantId: number;
}

export interface OkayUnLinkResponse {
  unlinkingSuccessStatus: boolean;
}

export interface OkayBiometricLoginResponse {
  biometricLoginStatus: boolean;
  payload?: string;
  header?: string;
  signature?: string;
  protectedAlgo?: string;
  status?: string;
  message?: string;
  sessionRemainingSeconds?: number;
}

export interface OkayPINLogin {
  publicKeyInBase64: string;
  clientVerificationServerURL: string;
  wrongPinRetries: number;
  userExternalId: string;
}

export interface OkayPINLoginResponse {
  pinLoginStatus: boolean;
  payload?: string;
  header?: string;
  signature?: string;
  protectedAlgo?: string;
  statusCode?: number;
  message?: string;
}

export function initOkay(initData: InitData): Promise<OkayInitResponse> {
  return RnOkaySdk.initOkay(initData);
}

export function updateDeviceToken(token: string): Promise<boolean> {
  return RnOkaySdk.updateDeviceToken(token);
}

export function isEnrolled(): Promise<boolean> {
  return RnOkaySdk.isEnrolled();
}

export function linkTenant(
  code: string,
  spaStorageData?: SpaStorageData
): Promise<OkayLinkResponse> {
  return RnOkaySdk.linkTenant(code, spaStorageData);
}

export function unlinkTenant(
  id: number | string,
  spaStorageData?: SpaStorageData
): Promise<OkayUnLinkResponse> {
  return RnOkaySdk.unlinkTenant(+id, spaStorageData);
}

export function startEnrollment(
  enrollData: SpaEnrollData
): Promise<OkayEnrollmentResponse> {
  return RnOkaySdk.startEnrollment(enrollData);
}
export function isReadyForAuthorization(): Promise<boolean> {
  return RnOkaySdk.isReadyForAuthorization();
}
export function startAuthorization(
  spaAuthData: SpaAuthData
): Promise<OkayAuthResponse> {
  return RnOkaySdk.startAuthorization(spaAuthData);
}

export function startBiometricLogin(): Promise<OkayBiometricLoginResponse> {
  return RnOkaySdk.startBiometricLogin(null);
}

export function setLoginTheme(loginTheme: OkayLoginTheme): Promise<String> {
  return RnOkaySdk.setLoginTheme(loginTheme);
}

export function startPINLogin(
  pinLoginData: OkayPINLogin
): Promise<OkayPINLoginResponse> {
  return RnOkaySdk.startPINLogin(pinLoginData);
}
