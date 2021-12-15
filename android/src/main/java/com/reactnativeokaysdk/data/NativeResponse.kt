package com.reactnativeokaysdk.data

import com.reactnativeokaysdk.data.serializer.AnySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class NativeResponse(val data: Map<String, @Serializable(with = AnySerializer::class) Any?>)
