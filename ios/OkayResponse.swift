//
//  OkayResponse.swift
//  react-native-okay-sdk
//
//  Created by e.brusnev on 12/10/21.
//

import Foundation

public protocol OkayResponse: Codable {
    func toString() throws -> String
    func toDictionary() throws -> NSDictionary
}

extension OkayResponse {
    public func toString() throws -> String {
        let response = try JSONEncoder().encode(self)
        return String(data: response, encoding: .utf8)!
    }
    public func toDictionary() throws -> NSDictionary {
        let data = try JSONEncoder().encode(self)
        let dictionary = try JSONSerialization.jsonObject(with: data, options: [])
        return (dictionary as? [String: Any] ?? [:]) as NSDictionary
    }
}

public struct OkayEnrollmentResponse: OkayResponse {
    let enrollmentStatus: Bool
    let enrollmentId: String?
    let externalId: String?
    init(status: Bool, enrollmentId: String?, externalId: String?) {
        self.enrollmentStatus = status
        self.enrollmentId = enrollmentId
        self.externalId = externalId
    }
}

public struct OkayAuthResponse: OkayResponse {
    let authSessionStatus: Bool
    init(status: Bool) {
        self.authSessionStatus = status
    }
}

public struct OkayLinkResponse: OkayResponse {
    let linkingSuccessStatus: Bool
    let tenantId: Int?
    init(status: Bool, tenantId: Int?) {
        self.linkingSuccessStatus = status
        self.tenantId = tenantId
    }
}

public struct OkayUnLinkResponse: OkayResponse {
    let unlinkingSuccessStatus: Bool
    init(status: Bool) {
        self.unlinkingSuccessStatus = status
    }
}

public struct OkayStartBiometricLoginResponse: OkayResponse {
    let payload: String
    let protectedAlgo: String
    let header: String
    let signature: String
    let biometricLoginStatus: Bool
    let statusCode: Int

    init(payload: String, protectedAlgo: String, header: String, signature: String, biometricLoginStatus: Bool, statusCode: Int) {
        self.payload = payload
        self.protectedAlgo = protectedAlgo
        self.header = header
        self.signature = signature
        self.biometricLoginStatus = biometricLoginStatus
        self.statusCode = statusCode
    }
}

public struct OkayStartPINLoginResponse: OkayResponse {
    let payload: String
    let protectedAlgo: String
    let header: String
    let signature: String
    let biometricLoginStatus: Bool
    let message: String
    let statusCode: Int

    init(payload: String, protectedAlgo: String, header: String, signature: String, biometricLoginStatus: Bool, statusCode: Int, message: String) {
        self.payload = payload
        self.protectedAlgo = protectedAlgo
        self.header = header
        self.signature = signature
        self.biometricLoginStatus = biometricLoginStatus
        self.message = message
        self.statusCode = statusCode
    }
}

public struct OkayInitResponse: OkayResponse {
    let initStatus: Bool
    init(status: Bool) {
        self.initStatus = status
    }
}
