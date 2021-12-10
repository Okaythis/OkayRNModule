//
//  PSAResponse.swift
//  react-native-okay-sdk
//
//  Created by e.brusnev on 12/10/21.
//

import Foundation

public class OkayResponse: Encodable {
    func toString() throws -> String {
        let response = try JSONEncoder().encode(self)
        return String(data: response, encoding: .utf8)!
    }
}

public class OkayEnrollmentResponse: OkayResponse {
    let enrollmentStatus: Bool
    init(status: Bool) {
        enrollmentStatus = status
    }
}

public class OkayAuthResponse: OkayResponse {
    let authSessionStatus: Bool
    init(status: Bool) {
        authSessionStatus = status
    }
}

public class OkayLinkResponse: OkayResponse {
    let linkingSuccessStatus: Bool
    let data: [String : Any]?
    init(status: Bool, tenantData: [String : Any]?) {
        linkingSuccessStatus = status
        data = tenantData
    }
}

public class OkayUnLinkResponse: OkayResponse {
    let unlinkingSuccessStatus: Bool
    init(status: Bool) {
        unlinkingSuccessStatus = status
    }
}

public class OkayInitResponse: OkayResponse {
    let initStatus: Bool
    init(status: Bool) {
        initStatus = status
    }
}
