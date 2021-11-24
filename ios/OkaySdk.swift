import PSA
@objc(OkaySdk)
class OkaySdk: NSObject {

    @objc(multiply:withB:withResolver:withRejecter:)
    func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve(a*b)
    }
    @objc(updateDeviceToken:withResolver:withRejecter:)
    func updateDeviceToken(deviceToken: String, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        PSA.updateDeviceToken(deviceToken)
    }

    @objc(isEnrolled:withRejecter:)
    func isEnrolled(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve("Method isEnrolled is not implemented yet");
    }

    @objc(enrollProcedure:withResolver:withRejecter:)
    func enrollProcedure(spaEnrollData: NSDictionary, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve("Method enrollProcedure is not implemented yet");
    }
    @objc(isEnrolled:withRejecter:)
    func isEnrolled(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve("Method isEnrolled is not implemented yet");
    }
    @objc(isEnrolled:withRejecter:)
    func isEnrolled(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve("Method isEnrolled is not implemented yet");
    }
    @objc(isEnrolled:withRejecter:)
    func isEnrolled(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve("Method isEnrolled is not implemented yet");
    }
}
