//
//  PSAResponse.swift
//  react-native-okay-sdk
//
//  Created by e.brusnev on 12/10/21.
//

import Foundation

public protocol OkayResponse: Encodable {
    func toString() throws -> String
}

extension OkayResponse {
    public func toString() throws -> String {
        let response = try JSONEncoder().encode(self)
        return String(data: response, encoding: .utf8)!
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

public struct OkayInitResponse: OkayResponse {
    let initStatus: Bool
    init(status: Bool) {
        self.initStatus = status
    }
}
