package com.rnokaysdk.data

import com.rnokaysdk.data.serializer.AnySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class NativeResponse(val data: Map<String, @Serializable(with = AnySerializer::class) Any?>)
