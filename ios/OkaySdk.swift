import PSA

class Test: ResourceProvider {
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

    @objc(multiply:withB:withResolver:withRejecter:)
    func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) -> Void {
        resolve(a*b)
    }
    @objc(updateDeviceToken:withResolver:withRejecter:)
    func updateDeviceToken(deviceToken: String, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) -> Void {
        do {
            try PSA.updateDeviceToken(deviceToken)
            resolve("Success")
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

    @objc(enrollProcedure:withResolver:withRejecter:)
    func enrollProcedure(spaEnrollData: NSDictionary, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        do {
            let resourceProvider: ResourceProvider = Test()
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
                                    invisibly: true,
                                    installationId: installationId,
                                    resourceProvider: resourceProvider,
                                    pubPssBase64: pubPss,
                                    idCompletion: { status in
                    resolve(status)
                })
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
                resolve(status)
            })
        } catch {
            reject("Error", "Failed to link tenant", error)
        }
    }

    @objc(unlinkTenant:withResolver:withRejecter:)
    func unlinkTenant(tenantId: NSNumber, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        do {
            PSA.unlinkTenant(withTenantId: tenantId) { status, id in
                resolve(status)
            }
        } catch {
            reject("Error", "Failed to link tenant", error)
        }
    }

    @objc(isReadyForAuthorization:withRejecter:)
    func isReadyForAuthorization(resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) -> Void {
        do {
            let isReady = try PSA.isReadyForAuthorization()
            resolve(isReady)
        } catch {
            reject("Error", "Error", error)
        }
    }

    @objc(authorization:withResolver:withRejecter:)
    func authorization(authorizationData: NSDictionary, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) -> Void {
        resolve("Method authorization is not implemented yet");
    }
}
