import PSA
import FlutterCommunicationChannel
import FccAbstractCore

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
    var okayUrlEndpoint: String?
    
    internal func initResourceProvider(data: [String: String]) -> OkayResourceProvider {
        let provider = OkayResourceProvider()
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
                PSA.update(FccApiImpl.getInstance()  as FccAbstractCore.FccApi)
            }
            try resolve(OkayInitResponse(status: true).toDictionary())
        } catch {
            reject("Error", "Error", error)
        }
    }

    @objc(updateDeviceToken:withResolver:withRejecter:)
    func updateDeviceToken(deviceToken: String, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) -> Void {
        PSA.updateDeviceToken(deviceToken)
        resolve(true)
    }

    @objc(isEnrolled:withRejecter:)
    func isEnrolled(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        let isEnrolled = PSA.isEnrolled()
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
        if PSA.isReadyForEnrollment() {
            if (self.okayUrlEndpoint == nil) {
                reject("Error", "Okay Url enpoint is not defined", nil)
            } else {
                PSA.startEnrollment(withHost: self.okayUrlEndpoint,
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
        } else {
            reject("Error", "PSA is not ready for enrollment", nil)
        }
    }

    @objc(linkTenant:spaStorageData:withResolver:withRejecter:)
    func linkTenant(linkingCode: String, _spaStorageData: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        PSA.linkTenant(withLinkingCode: linkingCode, completion: { status, tenant in
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
        PSA.unlinkTenant(withTenantId: tenantId) { status, id in
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
        let isReady = PSA.isReadyForAuthorization()
        resolve(isReady)
    }

    @objc(startAuthorization:withResolver:withRejecter:)
    func startAuthorization(data: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        do {
            if PSA.isReadyForAuthorization() {
                guard let sessionId = data["sessionId"] as? NSNumber
                else {
                    reject("Error", "Wrong data passed", nil)
                    return
                }
                DispatchQueue.main.async {
                    PSA.startAuthorization(with: self.tenantTheme, sessionId: sessionId, resourceProvider: self.resourceProvider, loaderViewController: nil) {isCancelled, status, info in
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
}
