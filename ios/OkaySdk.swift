import PSA

class OkayResourceProvider: ResourceProvider {
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

@objc(OkaySdk)
class OkaySdk: NSObject {

    var tenantTheme: PSATheme?
    var resourceProvider: OkayResourceProvider = OkayResourceProvider()
    
    internal func initResourceProvider(data: [String: String]) -> OkayResourceProvider {
        let provider = OkayResourceProvider()
        data["biometricAlertReasonText"].map { text in provider.biometricAlertReasonText = NSAttributedString(string: text)}
        data["confirmButtonText"].map { text in provider.confirmButtonText = NSAttributedString(string: text)}
        data["confirmBiometricTouchButtonText"].map { text in provider.confirmBiometricTouchButtonText = NSAttributedString(string: text)}
        data["confirmBiometricFaceButtonText"].map { text in provider.confirmBiometricFaceButtonText = NSAttributedString(string: text)}
        data["cancelButtonText"].map { text in provider.cancelButtonText = NSAttributedString(string: text)}
        data["massPaymentDetailsButtonText"].map { text in provider.massPaymentDetailsButtonText = NSAttributedString(string: text)}
        data["massPaymentDetailsHeaderText"].map { text in provider.massPaymentDetailsHeaderText = NSAttributedString(string: text)}
        data["feeLabelText"].map { text in provider.feeLabelText = NSAttributedString(string: text)}
        data["recepientLabelText"].map { text in provider.recepientLabelText = NSAttributedString(string: text)}
        data["enrollmentTitleText"].map { text in provider.enrollmentTitleText = NSAttributedString(string: text)}
        data["enrollmentDescriptionText"].map { text in provider.enrollmentDescriptionText = NSAttributedString(string: text)}
        
        return provider
    }
    
    
    @objc(initOkay:withResolver:withRejecter:)
    func initOkay(data: NSDictionary, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
        do {
            guard let initData = data["initData"] as? [String: Any?],
                  let resourceDataMap = initData["resourceProvider"] as? [String: String]
            else {
                try resolve(OkayInitResponse(status: false).toString());
                return;
            }
            self.resourceProvider = initResourceProvider(data: resourceDataMap)
            try resolve(OkayInitResponse(status: true).toString())
        } catch {
            reject("Error", "Error", error)
        }
    }

    @objc(updateDeviceToken:withResolver:withRejecter:)
    func updateDeviceToken(deviceToken: String, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) -> Void {
        do {
            try PSA.updateDeviceToken(deviceToken)
            resolve(true)
        } catch {
            reject("Error", "Failed to update token", error)
        }
    }

    @objc(isEnrolled:withRejecter:)
    func isEnrolled(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        do {
            let isEnrolled = try PSA.isEnrolled()
            resolve(isEnrolled)
        } catch {
            reject("Error", "Error", error)
        }
    }

    @objc(startEnrollment:withResolver:withRejecter:)
    func startEnrollment(spaEnrollData: NSDictionary, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        do {
            guard let enrollData = spaEnrollData["SpaEnrollData"] as? [String: String],
                let host = enrollData["host"],
                let pubPss = enrollData["pubPss"],
                let installationId = enrollData["installationId"]
            else {
                reject("Error", "Wrong data passed", nil)
                return
            }
            if try PSA.isReadyForEnrollment() {
                try PSA.startEnrollment(withHost: host,
                                    invisibly: false,
                                    installationId: installationId,
                                    resourceProvider: self.resourceProvider,
                                    pubPssBase64: pubPss) { status in
                    do {
                        if status.rawValue == 1 {
                            let enrollmentId = PSACommonData.enrollmentId()
                            let externalId = PSACommonData.externalId()
                            let a = OkayEnrollmentResponse(status: true, enrollmentId: enrollmentId, externalId: externalId)
                            let str = try a.toString()
                            try resolve(str)
                        } else {
                            try reject("", OkayEnrollmentResponse(status: false, enrollmentId: nil, externalId: nil).toString(), nil)
                        }
                    } catch {
                        reject("Error", "Failed to enroll", error)
                    }
                }
            } else {
                reject("Error", "PSA is not ready for enrollment", nil)
            }
        } catch {
            reject("Error", "Failed to enroll", error)
        }
    }

    @objc(linkTenant:withResolver:withRejecter:)
    func linkTenant(linkingCode: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        do {
            try PSA.linkTenant(withLinkingCode: linkingCode, completion: { status, tenant in
                do {
                    if status.rawValue == 1 {
                        self.tenantTheme = tenant.theme
                        let id = (tenant.tenantId != nil) ? Int(tenant.tenantId!) : nil
                        try resolve(OkayLinkResponse(status: true, tenantId: id).toString())
                    } else {
                        try reject("Error", OkayLinkResponse(status: false, tenantId: nil).toString(), nil)
                    }
                } catch {
                    reject("Error", "Failed to link tenant", error)
                }
            })
        } catch {
            reject("Error", "Failed to link tenant", error)
        }
    }

    @objc(unlinkTenant:withResolver:withRejecter:)
    func unlinkTenant(tenantId: NSNumber, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        do {
            PSA.unlinkTenant(withTenantId: tenantId) { status, id in
                if status.rawValue == 1 {
                    resolve("Success")
                } else {
                    reject("Error", "Failed with code: \(status.rawValue)", nil)
                }
            }
        } catch {
            reject("Error", "Failed to link tenant", error)
        }
    }

    @objc(isReadyForAuthorization:withRejecter:)
    func isReadyForAuthorization(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        do {
            let isReady = try PSA.isReadyForAuthorization()
            resolve(isReady)
        } catch {
            reject("Error", "Error", error)
        }
    }

    @objc(startAuthorization:withResolver:withRejecter:)
    func startAuthorization(sessionId: NSNumber, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        do {
            if PSA.isReadyForAuthorization() {
                PSA.startAuthorization(with: tenantTheme, sessionId: sessionId, resourceProvider: self.resourceProvider, loaderViewController: nil) {isCancelled, status, info in
                    do {
                        if !isCancelled && status.rawValue == 1 {
                            try resolve(OkayAuthResponse(status: true).toString())
                        } else {
                            try reject("Error", OkayAuthResponse(status: false).toString(), nil)
                        }
                    } catch {
                        reject("Error", "Failed to start authorization", error)
                    }
                }
            }
        } catch {
            reject("Error", "Failed to start authorization", error)
        }
    }
}
