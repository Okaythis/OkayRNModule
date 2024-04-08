package com.reactnativeokaysdk

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.facebook.react.bridge.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.itransition.protectoria.psa_multitenant.data.SpaStorage
import com.itransition.protectoria.psa_multitenant.protocol.scenarios.linking.LinkingScenarioListener
import com.itransition.protectoria.psa_multitenant.protocol.scenarios.unlinking.UnlinkingScenarioListener
import com.itransition.protectoria.psa_multitenant.restapi.GatewayRestServer
import com.itransition.protectoria.psa_multitenant.state.ApplicationState
import com.okaythis.fccabstractcore.data.dto.fonts.LocalStorageCustomFontConfig
import com.okaythis.fccabstractcore.data.dto.fonts.config.LocalFontConfig
import com.okaythis.fccabstractcore.interfaces.data.AbstractFccData
import com.okaythis.fccabstractcore.interfaces.data.AbstractFlutterEngineDependency
import com.okaythis.fccabstractcore.interfaces.fcc.FccApi
import com.okaythis.fccabstractcore.interfaces.parcel.Parcel
import com.okaythis.fluttercommunicationchannel.fcc.FccApiImpl
import com.protectoria.psa.LoginListener
import com.protectoria.psa.PsaManager
import com.protectoria.psa.api.PsaConstants
import com.protectoria.psa.api.converters.PsaIntentUtils
import com.protectoria.psa.api.entities.PsaErrorData
import com.protectoria.psa.api.entities.SpaAuthorizationData
import com.protectoria.psa.api.entities.SpaEnrollData
import com.protectoria.psa.api.entities.TenantInfoData
import com.protectoria.psa.dex.common.data.dto.AttestationRequest
import com.protectoria.psa.dex.common.data.enums.PsaType
import com.protectoria.psa.dex.common.data.json.PsaGsonFactory
import com.protectoria.psa.dex.common.ui.PageTheme
import com.protectoria.psa.dex.design.DefaultPageTheme
import com.protectoria.psa.dex.ui.texts.TransactionResourceProvider
import com.protectoria.psa.scenarios.enroll.EnrollResultListener
import com.protectoria.psa.scenarios.transaction.TransactionResultListener
import com.protectoria.pss.dto.notification.DeviceUIType
import com.reactnativeokaysdk.logger.OkayRNExceptionLogger
import com.reactnativeokaysdk.resourceprovider.OkayResourceProvider
import com.reactnativeokaysdk.storage.SpaStorageImpl
import com.reactnativeokaysdk.utils.getBooleanOrNull
import com.reactnativeokaysdk.utils.getIntOrNull
import io.flutter.plugin.common.MethodCall
import org.json.JSONObject
import timber.log.Timber


class OkaySdkModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var reactContext: ReactApplicationContext? = null
  private var psaManager: PsaManager? = null
  private var mPickerPromise: Promise? = null
  private lateinit var mSpaStorage: SpaStorage

  override fun getName(): String {
    return "OkaySdk"
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android

  private val mActivityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(
      activity: Activity?,
      requestCode: Int,
      resultCode: Int,
      data: Intent?
    ) {
      if (requestCode == PsaConstants.ACTIVITY_REQUEST_CODE_PSA_ENROLL) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
          data?.run {
            val resultData = PsaIntentUtils.enrollResultFromIntent(this)
            resultData.let {
              mSpaStorage.putEnrollmentId(it.enrollmentId)
              mSpaStorage.putExternalId(it.externalId)
              mPickerPromise?.resolve(
                Arguments.fromBundle(
                  bundleOf(
                    "enrollmentId" to it.enrollmentId,
                    "externalId" to it.externalId,
                    "enrollmentStatus" to true
                  )
                )
              )
            }
          }
        } else {
          mPickerPromise?.reject(
            "", JSONObject(
              mapOf(
                "enrollmentStatus" to false, "enrollmentId" to "",
                "externalId" to ""
              )
            ).toString()
          )
        }
      }

      if (requestCode == PsaConstants.ACTIVITY_REQUEST_CODE_PSA_AUTHORIZATION) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
          mPickerPromise?.resolve(
            Arguments.fromBundle(
              bundleOf(
                "authSessionStatus" to true
              )
            )
          )
        } else {
          mPickerPromise?.reject(
            "", JSONObject(
              mapOf(
                "authSessionStatus" to false
              )
            ).toString()
          )
        }
      }
    }
  }

  private val mLinkingScenarioListener: LinkingScenarioListener = object : LinkingScenarioListener {
    override fun onLinkingCompletedSuccessful(l: Long, s: String) {
      mPickerPromise!!.resolve(
        Arguments.fromBundle(
          bundleOf(
            "linkingSuccessStatus" to true
          )
        )
      )
    }

    override fun onLinkingFailed(applicationState: ApplicationState) {
      mPickerPromise!!.reject(
        "", JSONObject(
          mapOf(
            "linkingSuccessStatus" to false, "error" to applicationState.toString()
          )
        ).toString()
      )
    }
  }

  private val mUnlinkingScenarioListener: UnlinkingScenarioListener =
    object : UnlinkingScenarioListener {
      override fun onUnlinkingCompletedSuccessful() {
        mPickerPromise!!.resolve(
          Arguments.fromBundle(
            bundleOf(
              "unlinkingSuccessStatus" to true
            )
          )
        )
      }

      override fun onUnlinkingFailed(applicationState: ApplicationState) {
        mPickerPromise!!.resolve(
          Arguments.fromBundle(
            bundleOf(
              "unlinkingSuccessStatus" to false, "error" to applicationState.toString()
            )
          )
        )
      }
    }

  init {
    this.reactContext = reactContext
    mSpaStorage = SpaStorageImpl(reactContext)
    Timber.plant(Timber.DebugTree())
    reactContext.addActivityEventListener(mActivityEventListener)
  }


  @ReactMethod
  fun initOkay(data: ReadableMap, promise: Promise) {

    val resourceProvider: OkayResourceProvider
    val okayUrlEndpoint = data.getString("okayUrlEndpoint")
    val resourceProviderMap = data.getMap("resourceProvider")
    val fontConfigArray = data.getArray("fontConfig")
    val fontConfig = arrayListOf<LocalFontConfig>()

    if (fontConfigArray != null) {
      for (i in 0 until fontConfigArray.size()) {
        val config = fontConfigArray.getMap(i)
        val fontVariant = config?.getString("fontVariant")
        val fontAssetPath = config?.getString("fontAssetPath")
        if (fontVariant != null && fontAssetPath != null) {
          Timber.d("Font variants: $fontVariant - $fontAssetPath")
          reactContext?.assets?.let {
            fontConfig.add(LocalFontConfig(fontVariant, it.open(fontAssetPath)))
          }
        }
      }
    }

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
      psaManager = PsaManager.init(
        reactContext,
        OkayRNExceptionLogger(),
        TransactionResourceProvider(reactContext)
      )
    }

    if (!okayUrlEndpoint.isNullOrEmpty()) {
      psaManager!!.setPssAddress(okayUrlEndpoint)
      initGatewayServer(okayUrlEndpoint, fontConfig)
      promise.resolve(
        Arguments.fromBundle(
          bundleOf(
            "initStatus" to true
          )
        )
      )
    } else {
      promise.reject(
        "", JSONObject(
          mapOf(
            "initStatus" to false
          )
        ).toString()
      )
    }
  }

  private fun initGatewayServer(baseUrl: String, fontConfig: ArrayList<LocalFontConfig>?) {
    GatewayRestServer.init(PsaGsonFactory().create(), "$baseUrl/gateway/")
    val h = Handler(Looper.getMainLooper());
    h.post {
      if (fontConfig != null) {
        PsaManager.getInstance()
          .setFccApi(
            FccApiImpl.INSTANCE as FccApi<Parcel, AbstractFccData, AbstractFlutterEngineDependency>,
            LocalStorageCustomFontConfig(fontConfig)
          )
        return@post
      }
      PsaManager.getInstance()
        .setFccApi(FccApiImpl.INSTANCE as FccApi<Parcel, AbstractFccData, AbstractFlutterEngineDependency>)
    }
  }


  @ReactMethod
  fun startEnrollment(data: ReadableMap, promise: Promise) {
    val activity: Activity? = reactContext!!.currentActivity
    mPickerPromise = promise

    val appPns = data.getString("appPns")
    val pubPss = data.getString("pubPss")
    val installationId = data.getString("installationId")
    val enrollInBackground = data.getBooleanOrNull("enrollInBackground")
    val pageThemeMap = data.getMap("pageTheme")
    val psaType = PsaType.OKAY

    if (enrollInBackground == true) {
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
                promise.reject(
                  "", JSONObject(
                    mapOf(
                      "enrollmentStatus" to false, "enrollmentId" to "",
                      "externalId" to ""
                    )
                  ).toString()
                )
                return@apply
              }


              val resultData = PsaIntentUtils.enrollResultFromIntent(this)
              resultData.let {
                if (it == null) {
                  promise.reject(
                    "", JSONObject(
                      mapOf(
                        "enrollmentStatus" to false, "enrollmentId" to "",
                        "externalId" to ""
                      )
                    ).toString()
                  )
                  return@apply
                }
                mSpaStorage.putEnrollmentId(it.enrollmentId)
                mSpaStorage.putExternalId(it.externalId)
                promise.resolve(
                  Arguments.fromBundle(
                    bundleOf(
                      "enrollmentId" to it.enrollmentId,
                      "externalId" to it.externalId,
                      "enrollmentStatus" to true
                    )
                  )
                )
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

    val sessionId = data.getIntOrNull("sessionId")
    val appPNS = data.getString("appPns")
    val clientServerUrl = data.getString("clientServerUrl") ?: ""
    val extSessionId = data.getString("extSessionId") ?: ""
    val userExternalId = data.getString("userExternalId") ?: ""
    val isDisableMultipleRetry = data.getBooleanOrNull("isDisableMultipleRetry") ?: true
    val deviceUiType = data.getString("deviceUiType") ?: "FLUTTER"
    val pageThemeMap = data.getMap("pageTheme")
    val psaType = PsaType.OKAY

    val resultListener =
      TransactionResultListener { resultCode, data ->
        data?.let {
          if (resultCode == AppCompatActivity.RESULT_OK) {
            mPickerPromise?.resolve(
              Arguments.fromBundle(
                bundleOf(
                  "authSessionStatus" to true
                )
              )
            )
          } else {
            mPickerPromise?.reject(
              "", JSONObject(
                mapOf(
                  "authSessionStatus" to false
                )
              ).toString()
            )
          }
        }
      }


    takeIf { sessionId !== null && appPNS !== null }
      ?.run {
        val uiType = if(deviceUiType == "FLUTTER") DeviceUIType.FLUTTER else DeviceUIType.NATIVE
        val authorizationData: SpaAuthorizationData = if (pageThemeMap != null) {
          val pageTheme = initPageTheme(pageThemeMap, promise)
          SpaAuthorizationData(clientServerUrl, extSessionId, userExternalId, sessionId!!.toLong(), isDisableMultipleRetry, appPNS, pageTheme, psaType, uiType)
        } else {
          SpaAuthorizationData(clientServerUrl, extSessionId, userExternalId, sessionId!!.toLong(), isDisableMultipleRetry, appPNS, null, psaType, uiType)
        }

        if (deviceUiType == "FLUTTER") {
          PsaManager.getInstance().startAuthorizationWithAbstractUI(activity, authorizationData, resultListener)
          return
        }
        PsaManager.startAuthorizationActivity(activity, authorizationData)
      }
      ?: promise.reject("", "sessionId or appPns were not provided")
  }

  @ReactMethod
  fun linkTenant(linkingCode: String?, data: ReadableMap, promise: Promise) {
    try {
      mPickerPromise = promise
      val spaStorage: SpaStorage = SpaStorageImpl(reactContext)
      val externalID = if (data.getString("externalId")
          .isNullOrEmpty()
      ) mSpaStorage.externalId else data.getString("externalId")
      val enrollmentId = if (data.getString("enrollmentId")
          .isNullOrEmpty()
      ) mSpaStorage.enrollmentId else data.getString("enrollmentId")
      spaStorage.putExternalId(externalID)
      spaStorage.putEnrollmentId(enrollmentId)
      spaStorage.putAppPNS(data.getString("appPns"))
      spaStorage.putPubPssBase64(data.getString("pubPss"))
      spaStorage.putInstallationId(data.getString("installationId"))
      psaManager!!.linkTenant(linkingCode, spaStorage, mLinkingScenarioListener)
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun unlinkTenant(tenantId: Int, data: ReadableMap, promise: Promise) {
    try {
      mPickerPromise = promise
      val spaStorage: SpaStorage = SpaStorageImpl(reactContext)
      spaStorage.putAppPNS(data.getString("appPns"))
      spaStorage.putExternalId(data.getString("externalId"))
      spaStorage.putPubPssBase64(data.getString("pubPss"))
      spaStorage.putEnrollmentId(data.getString("enrollmentId"))
      spaStorage.putInstallationId(data.getString("installationId"))
      PsaManager.getInstance()
        .unlinkTenant(tenantId.toLong(), spaStorage, mUnlinkingScenarioListener)
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

  @ReactMethod
  fun updateDeviceToken(token: String, promise: Promise) {
    promise.resolve(false)
  }


  private fun initPageTheme(pageThemeMap: ReadableMap?, promise: Promise): PageTheme {
    val pageTheme: PageTheme = PageTheme();
//    val mapper = ObjectMapper()
//    var pageTheme: PageTheme = DefaultPageTheme.getDefaultPageTheme(reactContext);
//    try {
//      pageTheme = mapper.convertValue(toMap(pageThemeMap), PageTheme::class.java)
//    } catch (e: Exception) {
//      Timber.d(e)
//      promise.reject("", "Invalid object property")
//    }

//    val map = toMap(pageThemeMap) as Map<String, Any?>
//    val actionBarBackgroundColor: String? = map["actionBarBackgroundColor"]
//    val actionBarTextColor: String? = map["actionBarTextColor"]
//    val buttonTextColor: String? = map["buttonTextColor"]
//    val paymentDetailsButtonTextColor: String? = map["paymentDetailsButtonTextColor"]
//    val buttonBackgroundColor: String? = map["buttonBackgroundColor"]
    val screenBackgroundColor: String? = pageThemeMap?.getString("screenBackgroundColor") ?: "#ffffff"
//    val progressColor: String? = map["progressColor"]
//    val progressMessageTextColor: String? = map["progressMessageTextColor"]
//    val nameTextColor: String? = map["nameTextColor"]
//    val titleTextColor: String? = map["titleTextColor"]
//    val messageTextColor: String? = map["messageTextColor"]
//    val inputTextColor: String? = map["inputTextColor"]
//    val inputSelectionColor: String? = map["inputSelectionColor"]
//    val inputErrorColor: String? = map["inputErrorColor"]
//    val inputDefaultColor: String? = map["inputDefaultColor"]
//    val questionMarkColor: String? = map["questionMarkColor"]
//    val transactionTypeTextColor: String? = map["transactionTypeTextColor"]
//    val infoSectionTitleColor: String? = map["infoSectionTitleColor"]
//    val infoSectionValueColor: String? = map["infoSectionValueColor"]
//    val infoSectionValueSecondaryColor: String? = map["infoSectionValueSecondaryColor"]
//    val fromTextColor: String? = map["fromTextColor"]
//    val authInfoBackgroundColor: String? = map["authInfoBackgroundColor"]
//    val showDetailsTextColor: String? = map["showDetailsTextColor"]
//    val confirmButtonBackgroundColor: String? = map["confirmButtonBackgroundColor"]
//    val confirmButtonTextColor: String? = map["confirmButtonTextColor"]
//    val cancelButtonBackgroundColor: String? = map["cancelButtonBackgroundColor"]
//    val cancelButtonTextColor: String? = map["cancelButtonTextColor"]
//    val authConfirmationBackgroundColor: String? = map["authConfirmationBackgroundColor"]
//    val authConfirmationTitleColor: String? = map["authConfirmationTitleColor"]
//    val authConfirmationMessageColor: String? = map["authConfirmationMessageColor"]
//    val authConfirmationThumbColor: String? = map["authConfirmationThumbColor"]
//    val authConfirmationApostropheColor: String? = map["authConfirmationApostropheColor"]
//    val authConfirmationButtonBackgroundColor: String? = map["authConfirmationButtonBackgroundColor"]
//    val authConfirmationButtonTextColor: String? = map["authConfirmationButtonTextColor"]
//    val authCancellationBackgroundColor: String? = map["authCancellationBackgroundColor"]
//    val authCancellationTitleColor: String? = map["authCancellationTitleColor"]
//    val authCancellationMessageColor: String? = map["authCancellationMessageColor"]
//    val authCancellationThumbColor: String? = map["authCancellationThumbColor"]
//    val authCancellationApostropheColor: String? = map["authCancellationApostropheColor"]
//    val authCancellationButtonBackgroundColor: String? = map["authCancellationButtonBackgroundColor"]
//    val authCancellationButtonTextColor: String? = map["authCancellationButtonTextColor"]
//    val pinTitleTextColor: String? = map["pinTitleTextColor"]
//    val pinFilledValueTextColor: String? = map["pinFilledValueTextColor"]
//    val pinValueTextColor: String? = map["pinValueTextColor"]
//    val pinNumberButtonTextColor: String? = map["pinNumberButtonTextColor"]
//    val pinNumberButtonBackgroundColor: String? = map["pinNumberButtonBackgroundColor"]
//    val pinRemoveButtonTextColor: String? = map["pinRemoveButtonTextColor"]
//    val pinRemoveButtonBackgroundColor: String? = map["pinRemoveButtonBackgroundColor"]
//    val sessionTimerProgressColor: String? = map["sessionTimerProgressColor"]
//    val sessionTimerTextColor: String? = map["sessionTimerTextColor"]

    pageTheme.tenantLogoPath = ""
    pageTheme.tenantName = ""


    pageTheme.setColor(PageTheme.SCREEN_BACKGROUND_COLOR, Color.parseColor(screenBackgroundColor))
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
  @ReactMethod
  fun startBiometricLogin(promise: Promise){
    val loginListener = object: LoginListener {
      override fun onSuccess(p0: AttestationRequest?) {
        promise.resolve(
          Arguments.fromBundle(
            bundleOf(
              "biometricLoginStatus" to true,
              "payload" to p0?.payload.toString(),
              "header" to p0?.header.toString(),
              "signature" to p0?.signature.toString(),
              "protectedAlgo" to p0?.protectedAlgo.toString()
            )
          )
        )
      }

      override fun onError(p0: PsaErrorData?) {
        promise.resolve(
          Arguments.fromBundle(
            bundleOf(
              "biometricLoginStatus" to false,
              "status" to p0?.status?.code,
              "message" to p0?.message.toString(),
              "sessionRemainingSeconds" to p0?.sessionRemainingSeconds,
            )
          )
        )
      }
    }
    PsaManager.getInstance().startBiometricLogin(reactContext!!.currentActivity, loginListener)
  }
  @ReactMethod
  fun startPINLogin(data: ReadableMap, promise: Promise){
    val publicKeyInBase64: String? = data.getString("publicKeyInBase64")
    val clientVerificationServerURL: String? = data.getString("clientVerificationServerURL")
    val wrongPinRetries: Int = data.getInt("wrongPinRetries") ?: 3
    val userExternalId: String? = data.getString("userExternalId")

    val loginListener = object: LoginListener {
      override fun onSuccess(p0: AttestationRequest?) {
        promise.resolve(
          Arguments.fromBundle(
            bundleOf(
              "pinLoginStatus" to true,
              "payload" to p0?.payload,
              "header" to p0?.header,
              "signature" to p0?.signature,
              "protectedAlgo" to p0?.protectedAlgo
            )
          )
        )
      }

      override fun onError(p0: PsaErrorData?) {
        promise.resolve(
          Arguments.fromBundle(
            bundleOf(
              "pinLoginStatus" to false,
              "statusCode" to p0?.status?.code,
              "message" to p0?.message,
            )
          )
        )
      }
    }
    PsaManager.getInstance().startPinLogin(reactContext!!.currentActivity, publicKeyInBase64, clientVerificationServerURL, wrongPinRetries, userExternalId, loginListener)
  }


  private fun parseEnrollmentData(data: Intent): WritableNativeMap {
    val psaEnrollResultData = PsaIntentUtils.enrollResultFromIntent(data)
    val enrollmentData = WritableNativeMap()
    enrollmentData.putString("enrollmentId", psaEnrollResultData.enrollmentId)
    enrollmentData.putString("externalId", psaEnrollResultData.externalId)
    return enrollmentData
  }


  private fun getString(map: ReadableMap?, key: String): String {
    return if (map?.getString(key).isNullOrBlank()) key else map?.getString(key)!!
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
