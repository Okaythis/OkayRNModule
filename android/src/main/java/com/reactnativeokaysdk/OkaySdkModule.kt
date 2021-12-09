package com.reactnativeokaysdk

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.bridge.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.itransition.protectoria.psa_multitenant.data.SpaStorage
import com.itransition.protectoria.psa_multitenant.protocol.scenarios.linking.LinkingScenarioListener
import com.itransition.protectoria.psa_multitenant.protocol.scenarios.unlinking.UnlinkingScenarioListener
import com.itransition.protectoria.psa_multitenant.restapi.GatewayRestServer
import com.itransition.protectoria.psa_multitenant.state.ApplicationState
import com.okaythis.fluttercommunicationchannel.fcc.FccApiImpl
import com.protectoria.psa.PsaManager
import com.protectoria.psa.api.PsaConstants
import com.protectoria.psa.api.converters.PsaIntentUtils
import com.protectoria.psa.api.entities.SpaAuthorizationData
import com.protectoria.psa.api.entities.SpaEnrollData
import com.protectoria.psa.dex.common.data.enums.PsaType
import com.protectoria.psa.dex.common.data.json.PsaGsonFactory
import com.protectoria.psa.dex.common.ui.PageTheme
import com.protectoria.psa.dex.design.DefaultPageTheme
import com.protectoria.psa.dex.ui.texts.TransactionResourceProvider
import com.protectoria.psa.scenarios.enroll.EnrollResultListener
import com.protectoria.psa.ui.activities.authorization.AuthorizationActivity
import com.reactnativeokaysdk.data.NativeResponse
import com.reactnativeokaysdk.logger.OkayRNExceptionLogger
import com.reactnativeokaysdk.resourceprovider.OkayResourceProvider
import com.reactnativeokaysdk.storage.SpaStorageImpl
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*


class OkaySdkModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  private var reactContext: ReactApplicationContext? = null
  private var psaManager: PsaManager? = null
  private var mPickerPromise: Promise? = null
  private lateinit var mSpaStorage: SpaStorage


  override fun getName(): String {
    return "RNOkaySdk"
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android

  private val mActivityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent) {
      if (requestCode == PsaConstants.ACTIVITY_REQUEST_CODE_PSA_ENROLL) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
          data.run {
            val resultData = PsaIntentUtils.enrollResultFromIntent(this)
            resultData.let {
              mSpaStorage.putEnrollmentId(it.enrollmentId)
              mSpaStorage.putExternalId(it.externalId)
              mPickerPromise?.resolve(Json.encodeToString(NativeResponse(mapOf(
                "enrolmentId" to it.enrollmentId,
                "externalId" to it.externalId,
                "enrolmentStatus" to true
              ))))
            }
          }
        } else {
          mPickerPromise?.reject(Json.encodeToString(NativeResponse(mapOf(
            "enrolmentStatus" to false, "enrolmentId" to "",
            "externalId" to ""
          ))))
        }
      }

      if (requestCode == PsaConstants.ACTIVITY_REQUEST_CODE_PSA_AUTHORIZATION) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
          mPickerPromise?.resolve(Json.encodeToString(NativeResponse(mapOf(
            "authSessionStatus" to true
          ))))
        } else {
          mPickerPromise?.reject(Json.encodeToString(NativeResponse(mapOf(
            "authSessionStatus" to false
          ))))
        }
      }

      /*      if (requestCode == PsaConstants.ACTIVITY_REQUEST_CODE_PSA_ENROLL) {
          if (resultCode == Activity.RESULT_OK) {
            mPickerPromise!!.resolve(parseEnrollmentData(data))
          } else {
            mPickerPromise!!.reject("" , resultCode.toString())
          }
        }
        if (requestCode == PsaConstants.ACTIVITY_REQUEST_CODE_PSA_AUTHORIZATION) {
          if (resultCode == Activity.RESULT_OK
            || resultCode == AuthorizationActivity.RESULT_OK_CONSUMED_PUSH) {
            mPickerPromise!!.resolve(resultCode)
          } else {
            mPickerPromise!!.reject("" , resultCode.toString())
          }
        }*/
    }
  }

  private val mLinkingScenarioListener: LinkingScenarioListener = object : LinkingScenarioListener {
    override fun onLinkingCompletedSuccessful(l: Long, s: String) {
      mPickerPromise!!.resolve(Json.encodeToString(NativeResponse(mapOf(
        "linkingSuccessStatus" to true
      ))))
    }

    override fun onLinkingFailed(applicationState: ApplicationState) {
      mPickerPromise!!.reject(Json.encodeToString(NativeResponse(mapOf(
        "linkingSuccessStatus" to false, "error" to applicationState.toString()
      ))))
    }
  }

  private val mUnlinkingScenarioListener: UnlinkingScenarioListener = object : UnlinkingScenarioListener {
    override fun onUnlinkingCompletedSuccessful() {
      mPickerPromise!!.resolve(Json.encodeToString(NativeResponse(mapOf(
        "unlinkingSuccessStatus" to true
      ))))
    }

    override fun onUnlinkingFailed(applicationState: ApplicationState) {
      mPickerPromise!!.resolve(Json.encodeToString(NativeResponse(mapOf(
        "unlinkingSuccessStatus" to false, "error" to applicationState.toString()
      ))))
    }
  }

  init {
    this.reactContext = reactContext
    mSpaStorage = SpaStorageImpl(reactContext)
    reactContext.addActivityEventListener(mActivityEventListener)
  }


  @ReactMethod
  fun initOkay(data: ReadableMap, promise: Promise) {

    val resourceProvider: OkayResourceProvider
    val initDataMap = data.getMap("initData")
    val okayUrlEndpoint = initDataMap?.getString("okayUrlEndpoint")
    val resourceProviderMap = initDataMap?.getMap("resourceProvider")

    if (resourceProviderMap != null) {
      resourceProvider = OkayResourceProvider(
        reactContext!!,
        getString(
          resourceProviderMap,
          OkayResourceProvider.BiometricPromptCancelButton
        ),
        getString(resourceProviderMap, OkayResourceProvider.EnrollmentDescription),
        getResourceId(
          getString(
            resourceProviderMap,
            OkayResourceProvider.ScreenshotsNotificationIconId
          )
        ),
        getString(resourceProviderMap, OkayResourceProvider.Fee),
        getString(resourceProviderMap, OkayResourceProvider.ScreenshotsChannelName),
        getString(resourceProviderMap, OkayResourceProvider.TransactionDetails),
        getString(resourceProviderMap, OkayResourceProvider.BiometricPromptSubTitle),
        getString(resourceProviderMap, OkayResourceProvider.Recipient),
        getString(resourceProviderMap, OkayResourceProvider.EnrollmentTitle),
        getString(resourceProviderMap, OkayResourceProvider.ConfirmButton),
        getString(
          resourceProviderMap,
          OkayResourceProvider.BiometricPromptDescription
        ),
        getString(
          resourceProviderMap,
          OkayResourceProvider.ScreenshotsNotificationText
        ),
        getString(resourceProviderMap, OkayResourceProvider.AuthScreenTitle),
        getString(resourceProviderMap, OkayResourceProvider.BiometricPromptTitle),
        getString(resourceProviderMap, OkayResourceProvider.ConfirmBiometricButton),
        getString(resourceProviderMap, OkayResourceProvider.PaymentDetailsButton),
        getString(
          resourceProviderMap,
          OkayResourceProvider.AuthorizationProgressView
        )
      )
      psaManager = PsaManager.init(reactContext, OkayRNExceptionLogger(), resourceProvider)
    } else {
      psaManager = PsaManager.init(reactContext, OkayRNExceptionLogger(), TransactionResourceProvider(reactContext))
    }
//    PsaManager.getInstance().setFccApi(FccApiImpl.INSTANCE)

    if (!okayUrlEndpoint.isNullOrEmpty()) {
      psaManager!!.setPssAddress(okayUrlEndpoint)
      initGatewayServer(okayUrlEndpoint)
      promise.resolve(Json.encodeToString(NativeResponse(mapOf(
        "initStatus" to true
      ))))
    } else {
      promise.reject("", Json.encodeToString(NativeResponse(mapOf(
        "initStatus" to false
      ))))
    }
  }

  private fun initGatewayServer(baseUrl: String) {
    GatewayRestServer.init(PsaGsonFactory().create(), "$baseUrl/gateway/")
  }


  @ReactMethod
  fun startEnrollment(data: ReadableMap, promise: Promise) {
    val activity: Activity? = reactContext!!.currentActivity
    mPickerPromise = promise

    val spaEnrollDataMap = data.getMap("SpaEnrollData")
    val appPns = spaEnrollDataMap!!.getString("appPns")
    val pubPss = spaEnrollDataMap.getString("pubPss")
    val installationId = spaEnrollDataMap.getString("installationId")
    val enrollInBackground = spaEnrollDataMap.getBoolean("enrollInBackground")
    val pageThemeMap = spaEnrollDataMap.getMap("pageTheme")
    val psaType = PsaType.OKAY

    if (enrollInBackground) {
      PsaManager.getInstance().startBackgroundEnroll(
        SpaEnrollData(
          appPns,
          pubPss,
          installationId,
          null,
          psaType
        ), object : EnrollResultListener {
        override fun onEnrollResult(p0: Int, data: Intent?) {
          data.apply {
            if (this == null) {
              promise.reject("", Json.encodeToString(NativeResponse(mapOf(
                "enrolmentStatus" to false, "enrolmentId" to "",
                "externalId" to ""
              ))))
              return@apply
            }


            val resultData = PsaIntentUtils.enrollResultFromIntent(this)
            resultData.let {
              if (it == null) {
                promise.reject("", Json.encodeToString(NativeResponse(mapOf(
                  "enrolmentStatus" to false, "enrolmentId" to "",
                  "externalId" to ""
                ))))
                return@apply
              }
              mSpaStorage.putEnrollmentId(it.enrollmentId)
              mSpaStorage.putExternalId(it.externalId)
              promise.resolve(Json.encodeToString(NativeResponse(mapOf(
                "enrolmentId" to it.enrollmentId,
                "externalId" to it.externalId,
                "enrolmentStatus" to true
              ))))
            }
          }
        }
      }, reactContext
      )
      return
    }

    val enrollData: SpaEnrollData = if (pageThemeMap != null) {
      val pageTheme = initPageTheme(pageThemeMap, promise)
      SpaEnrollData(appPns, pubPss, installationId, pageTheme, psaType)
    } else {
      SpaEnrollData(appPns, pubPss, installationId, null, psaType)
    }
    PsaManager.startEnrollmentActivity(activity, enrollData)

  }


  @ReactMethod
  fun startAuthorization(data: ReadableMap, promise: Promise) {
    val activity: Activity? = reactContext!!.currentActivity
    mPickerPromise = promise

    val spaEnrollDataMap = data.getMap("SpaAuthorizationData")
    val sessionId = spaEnrollDataMap!!.getInt("sessionId")
    val appPNS = spaEnrollDataMap.getString("appPns")
    val pageThemeMap = spaEnrollDataMap.getMap("pageTheme")
    val psaType = PsaType.OKAY
    val authorizationData: SpaAuthorizationData = if (pageThemeMap != null) {
      val pageTheme = initPageTheme(pageThemeMap, promise)
      SpaAuthorizationData(sessionId.toLong(), appPNS, pageTheme, psaType)
    } else {
      SpaAuthorizationData(sessionId.toLong(), appPNS, DefaultPageTheme.getDefaultPageTheme(reactContext), psaType)
    }
    PsaManager.startAuthorizationActivity(activity, authorizationData)
  }

  @ReactMethod
  fun linkTenant(linkingCode: String?, data: ReadableMap, promise: Promise) {
    try {
      mPickerPromise = promise
      val spaStorageMap = data.getMap("SpaStorage")
      val spaStorage: SpaStorage = SpaStorageImpl(reactContext)
      val externalID = if (spaStorageMap?.getString("externalId").isNullOrEmpty()) mSpaStorage.externalId else spaStorageMap?.getString("externalId")
      val enrollmentId = if (spaStorageMap?.getString("enrollmentId").isNullOrEmpty()) mSpaStorage.enrollmentId else spaStorageMap?.getString("enrollmentId")
      spaStorage.putExternalId(externalID)
      spaStorage.putEnrollmentId(enrollmentId)
      spaStorage.putAppPNS(spaStorageMap!!.getString("appPns"))
      spaStorage.putPubPssBase64(spaStorageMap.getString("pubPss"))
      spaStorage.putInstallationId(spaStorageMap.getString("installationId"))
      psaManager!!.linkTenant(linkingCode, spaStorage, mLinkingScenarioListener)
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun unlinkTenant(tenantId: Long, data: ReadableMap, promise: Promise) {
    try {
      mPickerPromise = promise
      val spaStorageMap = data.getMap("SpaStorage")
      val spaStorage: SpaStorage = SpaStorageImpl(reactContext)
      spaStorage.putAppPNS(spaStorageMap!!.getString("appPns"))
      spaStorage.putExternalId(spaStorageMap.getString("externalId"))
      spaStorage.putPubPssBase64(spaStorageMap.getString("pubPss"))
      spaStorage.putEnrollmentId(spaStorageMap.getString("enrollmentId"))
      spaStorage.putInstallationId(spaStorageMap.getString("installationId"))
      PsaManager.getInstance().unlinkTenant(tenantId, spaStorage, mUnlinkingScenarioListener)
    } catch (e: Exception) {
      promise.reject(e)
    }
  }


  @ReactMethod
  fun isEnrolled(promise: Promise) {
    promise.resolve(PsaManager.getInstance().isEnrolled)
  }


  @ReactMethod
  fun isReadyForAuthorization(promise: Promise) {
    promise.resolve(PsaManager.getInstance().isReadyForAuthorization)
  }


  @ReactMethod
  fun permissionRequest(promise: Promise) {
    val permissions = PsaManager.getRequiredPermissions()
    val writableArray: WritableArray = WritableNativeArray()
    for (permission in permissions) {
      writableArray.pushString(permission)
    }
    if (writableArray.size() == 0) {
      promise.reject("", "No permissions found.")
    }
    promise.resolve(writableArray)
  }


  private fun initPageTheme(pageThemeMap: ReadableMap?, promise: Promise): PageTheme {
    val mapper = ObjectMapper()
    var pageTheme = PageTheme()
    try {
      pageTheme = mapper.convertValue(toMap(pageThemeMap), PageTheme::class.java)
    } catch (e: Exception) {
      promise.reject("Invalid object property")
    }
    return pageTheme
  }

  private fun toMap(readableMap: ReadableMap?): Map<String, Any?>? {
    if (readableMap == null) {
      return null
    }
    val iterator = readableMap.keySetIterator()
    if (!iterator.hasNextKey()) {
      return null
    }
    val result: MutableMap<String, Any?> = HashMap()
    while (iterator.hasNextKey()) {
      val key = iterator.nextKey()
      if (key.contains("Color")) {
        result[key] = Color.parseColor(readableMap.getString(key))
      } else {
        result[key] = readableMap.getString(key)
      }
    }
    return result
  }

  private fun parseEnrollmentData(data: Intent): WritableNativeMap {
    val psaEnrollResultData = PsaIntentUtils.enrollResultFromIntent(data)
    val enrollmentData = WritableNativeMap()
    enrollmentData.putString("enrollmentId", psaEnrollResultData.enrollmentId)
    enrollmentData.putString("externalId", psaEnrollResultData.externalId)
    return enrollmentData
  }


  private fun getString(map: ReadableMap?, key: String): String {
    return if (map?.getString(key).isNullOrBlank()) "" else map?.getString(key)!!
  }

  private fun getResourceId(id: String): Int {
    val resources = reactContext!!.resources
    if (id.isBlank()) {
      return resources.getIdentifier(
        "ic_menu_camera",
        "drawable",
        reactContext!!.packageName
      )
    }
    return resources.getIdentifier(id, "drawable", reactContext!!.packageName)
  }
}

