package com.reactnativeokaysdk.resourceprovider

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.View
import android.view.Window
import com.protectoria.psa.dex.common.data.dto.gui_data.TransactionInfo
import com.protectoria.psa.dex.common.ui.ResourceProvider

class OkayResourceProvider(
    private val context: Context,
    private val provideTextForBiometricPromtCancelButton: String,
    private val provideTextForEnrollmentDescription: String,
    private val provideScreenshotsNotificationIconId: Int,
    private val provideTextForFee: String,
    private val provideScreenshotsChannelName: String,
    private val provideTextForTransactionDetails: String,
    private val provideTextForBiometricPromtSubTitle: String,
    private val provideTextForRecipient: String,
    private val provideTextForEnrollmentTitle: String,
    private val provideTextForConfirmButton: String,
    private val provideTextForBiometricPromtDescription: String,
    private val provideScreenshotsNotificationText: String,
    private val provideTextForAuthScreenTitle: String,
    private val provideTextForBiometricPromtTitle: String,
    private val provideTextForConfirmBiometricButton: String,
    private val provideTextForPaymentDetailsButton: String,
    private val provideTextForAuthorizationProgressView: String
) : ResourceProvider {

    override fun provideTextForBiometricPromtCancelButton(): String {
        return provideTextForBiometricPromtCancelButton
    }

    override fun provideTextForEnrollmentDescription(): String {
        return provideTextForEnrollmentDescription
    }

    override fun provideScreenshotsNotificationIconId(): Int {
        return provideScreenshotsNotificationIconId
      val resources = context!!.applicationContext.resources
      return resources.getIdentifier(
        "ic_menu_camera",
        "drawable",
        context!!.applicationContext!!.packageName
      )
    }

    override fun provideTextForFee(): String {
        return provideTextForFee
    }

    override fun provideScreenshotsChannelName(): String {
        return provideScreenshotsChannelName
    }

    override fun provideTextForTransactionDetails(p0: TransactionInfo?): SpannableStringBuilder {
        return SpannableStringBuilder(provideTextForTransactionDetails)
    }

    override fun provideTextForBiometricPromtSubTitle(): String {
        return provideTextForBiometricPromtSubTitle
    }

    override fun provideTextForRecipient(): String {
        return provideTextForRecipient
    }

    override fun provideTextForEnrollmentTitle(): String {
        return provideTextForEnrollmentTitle
    }

    override fun provideTextForConfirmButton(): String {
        return provideTextForConfirmButton
    }

    override fun provideTextForBiometricPromtDescription(): String {
        return provideTextForBiometricPromtDescription
    }

    override fun provideScreenshotsNotificationText(): String {
        return provideScreenshotsNotificationText
    }

    override fun provideTextForAuthScreenTitle(p0: TransactionInfo?): String? {
        return provideTextForAuthScreenTitle
    }

    override fun provideTextForBiometricPromtTitle(): String {
        return provideTextForBiometricPromtTitle
    }

    override fun provideTextForConfirmBiometricButton(): String {
        return provideTextForConfirmBiometricButton
    }

    override fun provideTextForPaymentDetailsButton(): String {
        return provideTextForPaymentDetailsButton
    }

    override fun provideTextForAuthorizationProgressView(): String {
        return provideTextForAuthorizationProgressView
    }

  override fun provideTextForInvalidPinRetryErrorText(): String {
    return "You entered an incorrect PIN"
  }

  override fun provideSplashView(p0: Context?): View? {
        return View(context)
    }

    override fun provideLoaderColor(): String {
        return "#000000"
    }

  override fun customizeWindow(p0: Window?) {}

  companion object {
        const val BiometricPromptCancelButton =
            "cancelButtonText";
        const val EnrollmentDescription =
            "enrollmentDescriptionText";
        const val ScreenshotsNotificationIconId =
            "androidScreenhotsNotificationIconId";
        const val Fee = "feeLabelText";
        const val ScreenshotsChannelName = "androidScreenshotsChannelName";
        const val TransactionDetails = "androidTransactionDetails";
        const val BiometricPromptSubTitle =
            "androidBiometricPromptSubTitle";
        const val Recipient = "recipientLabelText";
        const val EnrollmentTitle = "enrollmentTitleText";
        const val ConfirmButton = "confirmButtonText";
        const val BiometricPromptDescription =
            "androidBiometricPromptDescription";
        const val ScreenshotsNotificationText =
            "androidScreenshotsNotificationText";
        const val AuthScreenTitle = "androidAuthScreenTitle";
        const val BiometricPromptTitle =
            "androidBiometricPromptTitle";
        const val ConfirmBiometricButton =
            "androidConfirmBiometricButtonText";
        const val PaymentDetailsButton =
            "massPaymentDetailsButtonText";
        const val AuthorizationProgressView =
            "androidAuthorizationProgressViewText";
    }
}
