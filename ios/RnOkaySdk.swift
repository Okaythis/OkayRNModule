import PSA
import FlutterCommunicationChannel
import FccAbstractCore

class OkayResourceProvider: ResourceProvider {
    var invalidPinRetryErrorText: String!
    
    var biometricAlertReasonText: NSAttributedString!
    
    var confirmButtonText: NSAttributedString!
    
    var confirmBiometricTouchButtonText: NSAttributedString!
    
    var confirmBiometricFaceButtonText: NSAttributedString!
    
    var cancelButtonText: NSAttributedString!
    
    var massPaymentDetailsButtonText: NSAttributedString!
    
    var massPaymentDetailsHeaderText: NSAttributedString!
    
    var feeLabelText: NSAttributedString!
    
    var recepientLabelText: NSAttributedString!
    
    var enrollmentTitleText: NSAttributedString!
    
    var enrollmentDescriptionText: NSAttributedString!
    
    func string(for transactionInfo: TransactionInfo!) -> NSAttributedString! {
        return NSAttributedString.init()
    }
    
    func string(forConfirmActionHeader transactionInfo: TransactionInfo!) -> NSAttributedString! {
        return NSAttributedString.init()
    }
}

@objc(RnOkaySdk)
class RnOkaySdk: NSObject {
    
    var tenantTheme: PSATheme?
    var resourceProvider: OkayResourceProvider = OkayResourceProvider()
    var okayUrlEndpoint: String?
    internal let pinTheme = PSAPINLoginTheme()
    
    
    internal func initResourceProvider(data: [String: String]) -> OkayResourceProvider {
        let provider = OkayResourceProvider()
        provider.invalidPinRetryErrorText = "You entered a wrong PIN."
        
        data["iosBiometricAlertReasonText"].map { text in provider.biometricAlertReasonText = NSAttributedString(string: text)}
        data["confirmButtonText"].map { text in provider.confirmButtonText = NSAttributedString(string: text)}
        data["iosConfirmBiometricTouchButtonText"].map { text in provider.confirmBiometricTouchButtonText = NSAttributedString(string: text)}
        data["iosConfirmBiometricFaceButtonText"].map { text in provider.confirmBiometricFaceButtonText = NSAttributedString(string: text)}
        data["cancelButtonText"].map { text in provider.cancelButtonText = NSAttributedString(string: text)}
        data["massPaymentDetailsButtonText"].map { text in provider.massPaymentDetailsButtonText = NSAttributedString(string: text)}
        data["iosMassPaymentDetailsHeaderText"].map { text in provider.massPaymentDetailsHeaderText = NSAttributedString(string: text)}
        data["feeLabelText"].map { text in provider.feeLabelText = NSAttributedString(string: text)}
        data["recipientLabelText"].map { text in provider.recepientLabelText = NSAttributedString(string: text)}
        data["enrollmentTitleText"].map { text in provider.enrollmentTitleText = NSAttributedString(string: text)}
        data["enrollmentDescriptionText"].map { text in provider.enrollmentDescriptionText = NSAttributedString(string: text)}
        data["invalidPinRetryErrorText"].map { text in provider.invalidPinRetryErrorText = text }
        
        return provider
    }
    
    
    @objc(initOkay:withResolver:withRejecter:)
    func initOkay(data: NSDictionary, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
        do {
            guard let okayUrlEndpoint = data["okayUrlEndpoint"] as? String,
                  let resourceDataMap = data["resourceProvider"] as? [String: String]
            else {
                try resolve(OkayInitResponse(status: false).toDictionary());
                return;
            }
            self.okayUrlEndpoint = okayUrlEndpoint
            self.resourceProvider = initResourceProvider(data: resourceDataMap)
            DispatchQueue.main.async {
                PSAModule.update(FccApiImpl.getInstance()  as FccAbstractCore.FccApi)
            }
            try resolve(OkayInitResponse(status: true).toDictionary())
        } catch {
            reject("Error", "Error", error)
        }
    }
    
    @objc(updateDeviceToken:withResolver:withRejecter:)
    func updateDeviceToken(deviceToken: String, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) -> Void {
        PSAModule.updateDeviceToken(deviceToken)
        resolve(true)
    }
    
    @objc(isEnrolled:withRejecter:)
    func isEnrolled(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        let isEnrolled = PSAModule.isEnrolled()
        resolve(isEnrolled)
    }
    
    @objc(startEnrollment:withResolver:withRejecter:)
    func startEnrollment(spaEnrollData: NSDictionary, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        guard let pubPss = spaEnrollData["pubPss"] as? String,
              let installationId = spaEnrollData["installationId"] as? String
        else {
            reject("Error", "Wrong data passed", nil)
            return
        }
        if PSAModule.isReadyForEnrollment() {
            if (self.okayUrlEndpoint == nil) {
                reject("Error", "Okay Url enpoint is not defined", nil)
            } else {
                DispatchQueue.main.async {
                    PSAModule.startEnrollment(withHost: self.okayUrlEndpoint,
                                              invisibly: false,
                                              installationId: installationId,
                                              resourceProvider: self.resourceProvider,
                                              pubPssBase64: pubPss) { status in
                        do {
                            if status.rawValue == 1 {
                                let enrollmentId = PSACommonData.enrollmentId()
                                let externalId = PSACommonData.externalId()
                                try resolve(OkayEnrollmentResponse(status: true, enrollmentId: enrollmentId, externalId: externalId).toDictionary())
                            } else {
                                try reject("", OkayEnrollmentResponse(status: false, enrollmentId: nil, externalId: nil).toString(), nil)
                            }
                        } catch {
                            reject("Error", "Failed to enroll", error)
                        }
                    }
                }
            }
        } else {
            reject("Error", "PSA is not ready for enrollment", nil)
        }
    }
    
    @objc(linkTenant:spaStorageData:withResolver:withRejecter:)
    func linkTenant(linkingCode: String, _spaStorageData: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        PSAModule.linkTenant(withLinkingCode: linkingCode, completion: { status, tenant in
            do {
                if status.rawValue == 1 {
                    self.tenantTheme = tenant.theme
                    let id = (tenant.tenantId != nil) ? Int(tenant.tenantId!) : nil
                    try resolve(OkayLinkResponse(status: true, tenantId: id).toDictionary())
                } else {
                    try reject("Error", OkayLinkResponse(status: false, tenantId: nil).toString(), nil)
                }
            } catch {
                reject("Error", "Failed to link tenant", error)
            }
        })
    }
    
    @objc(unlinkTenant:spaStorageData:withResolver:withRejecter:)
    func unlinkTenant(tenantId: NSNumber, _spaStorageData: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        PSAModule.unlinkTenant(withTenantId: tenantId) { status, id in
            do {
                if status.rawValue == 1 {
                    try resolve(OkayUnLinkResponse(status: true).toDictionary())
                } else {
                    try reject("Error", OkayUnLinkResponse(status: false).toString(), nil)
                }
            } catch {
                reject("Error", "Failed to unlink tenant", error)
            }
        }
    }
    
    @objc(isReadyForAuthorization:withRejecter:)
    func isReadyForAuthorization(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        let isReady = PSAModule.isReadyForAuthorization()
        resolve(isReady)
    }
    
    @objc(startAuthorization:withResolver:withRejecter:)
    func startAuthorization(data: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        do {
            if PSAModule.isReadyForAuthorization() {
                guard let sessionId = data["sessionId"] as? NSNumber
                else {
                    reject("Error", "Wrong data passed", nil)
                    return
                }
                DispatchQueue.main.async {
                    PSAModule.startAuthorization(with: self.tenantTheme, sessionId: sessionId, resourceProvider: self.resourceProvider, loaderViewController: nil) {isCancelled, status, info in
                        do {
                            if !isCancelled && status.rawValue == 1 {
                                try resolve(OkayAuthResponse(status: true).toDictionary())
                            } else {
                                try reject("Error", OkayAuthResponse(status: false).toString(), nil)
                            }
                        } catch {
                            reject("Error", "Failed to start authorization", error)
                        }
                    }
                }
            }
        } catch {
            reject("Error", "Failed to start authorization", error)
        }
    }
    
    @objc(startBiometricLogin:withResolver:withRejecter:)
    func startBiometricLogin(data: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock){
        PSAModule.startBiometricLogin(){ payload, statusCode  in
            if statusCode.rawValue == 900, let payload = payload as? [String: Any] {
                do{
                    try resolve(OkayStartBiometricLoginResponse(
                        payload: String(describing: payload["payload"] ?? ""),
                        protectedAlgo: String(describing: payload["protectedAlgo"] ?? ""),
                        header: String(describing: payload["header"] ?? ""),
                        signature: String(describing: payload["signature"] ?? ""),
                        biometricLoginStatus: true,
                        statusCode: Int(statusCode.rawValue)).toString())
                } catch {
                    reject("Error","There was an error decoding the result", error)
                }
                
            } else {
                do{
                    try resolve(OkayStartBiometricLoginResponse(
                        payload: "",
                        protectedAlgo: "",
                        header: "",
                        signature: "",
                        biometricLoginStatus: false,
                        statusCode: Int(statusCode.rawValue)).toString())
                } catch {
                    reject("Error","There was an error decoding the result", error)
                }
            }
        }
    }
    
    @objc(setLoginTheme:withResolver:withRejecter:)
    func setLoginTheme(loginTheme: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock){
        guard let args = loginTheme as? [String: Any] else {
            reject("Error", "Invalid Login Theme", nil)
            return
        }
        
        
        pinTheme.screenTitleText = args["pinTitleText"] as? String ?? ""
        pinTheme.screenSubtitleText = args["pinSubTitleText"] as? String ?? ""
        pinTheme.screenForgotPinText = args["forgotPinText"] as? String ?? ""
        
        
        let screenBackgroundColor = args["pinScreenBackgroundColor"] as? String
        let screenTitleTextColor = args["pinTitleTextColor"] as? String
        let screenSubtitleErrorTextColor = args["pinSubTitleTextColor"] as? String
        let pinTextColor = args["pinFilledColor"] as? String
        let pinButtonTextColor = args["pinPadTextColor"] as? String
        let pinButtonBackgroundColor = args["pinPadBackgroundColor"] as? String
        let screenSubtitleErrorText = args["pinSubTitleErrorText"] as? String
        let serverErrorText = args["severErrorText"] as? String
        
        pinTheme.screenBackgroundColor = UIColor(hexColorString: screenBackgroundColor ?? "#000000")
        pinTheme.screenTitleTextColor = UIColor(hexColorString: screenTitleTextColor ?? "#000000")
        pinTheme.screenSubtitleErrorTextColor = UIColor(hexColorString: screenSubtitleErrorTextColor ?? "#000000")
        pinTheme.pinTextColor = UIColor(hexColorString: pinTextColor ?? "#000000")
        pinTheme.pinButtonTextColor = UIColor(hexColorString: pinButtonTextColor ?? "#000000")
        pinTheme.pinButtonBackgroundColor = UIColor(hexColorString: pinButtonBackgroundColor ?? "#000000")
        pinTheme.shuffleKeyPad = false
        
        let errorMessages = PSAPINLoginCustomErrorMessages()
        
        errorMessages.setErrorMessage(screenSubtitleErrorText ?? "Invalid Pin, Please try again.", forError: PSAPINLoginErrorCode.invalidPinError)
        errorMessages.setErrorMessage(serverErrorText ?? "Server error, Please try later.", forError: PSAPINLoginErrorCode.serverError)
        
        resolve("Login theme was set successfully")
    }
    
    
    @objc(startPINLogin:withResolver:withRejecter:)
    func startPINLogin(pinLoginData: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock){
        guard let args = pinLoginData as? [String: Any] else {
            reject("Error", "Invalid Login Data", nil)
            return
        }
        
        let publicKeyBase64 = args["publicKeyInBase64"] as! String
        let clientVerificationServerUrl = args["clientVerificationServerURL"] as! String
        let numberOfPINRetries = args["wrongPinRetries"] as! Int
        let userExternalId = args["userExternalId"] as! String
        
        PSAModule.startPinLogin(publicKeyBase64, clientVerificationServerURL: clientVerificationServerUrl, numberOfPINRetries: numberOfPINRetries, userExternalId: userExternalId, pinScreenTheme: pinTheme){ payload, statusCode, message   in
            if statusCode.rawValue == 900, let payload = payload as? [String: Any] {
                do{
                    try resolve(OkayStartPINLoginResponse(
                        payload: String(describing: payload["payload"] ?? "") ,
                        protectedAlgo: String(describing: payload["protectedAlgo"] ?? ""),
                        header: String(describing: payload["header"] ?? ""),
                        signature: String(describing: payload["signature"] ?? ""),
                        biometricLoginStatus: true,
                        statusCode: Int(statusCode.rawValue),
                        message: message ?? "").toString())
                } catch {
                    reject("Error","There was an error decoding the result", error)
                }
            } else {
                do{
                    try resolve(OkayStartPINLoginResponse(
                        payload: "",
                        protectedAlgo: "",
                        header: "",
                        signature: "",
                        biometricLoginStatus: true,
                        statusCode: Int(statusCode.rawValue),
                        message: message ?? ""
                    ).toString())
                } catch {
                    reject("Error","There was an error decoding the result", error)
                }
            }
        }
    }
}

extension UIColor {
    convenience init(hexColorString: String) {
        let hexColorString: String = hexColorString.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
        let scanner = Scanner(string: hexColorString)
        if (hexColorString.hasPrefix("#")) {
            scanner.scanLocation = 1
        }
        
        var color: UInt32 = 0
        scanner.scanHexInt32(&color)
        let mask = 0x000000FF
        
        let red   = CGFloat(Int(color >> 16) & mask) / 255.0
        let green = CGFloat(Int(color >> 8) & mask) / 255.0
        let blue  = CGFloat(Int(color) & mask) / 255.0
        
        self.init(red:red, green:green, blue:blue, alpha:1)
    }
}
