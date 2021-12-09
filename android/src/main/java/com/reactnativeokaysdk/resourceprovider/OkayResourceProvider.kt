package com.reactnativeokaysdk.resourceprovider

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.View
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

    override fun provideSplashView(): View {
        return View(context)
    }

    override fun provideLoaderColor(): String {
        return "#000000"
    }

    companion object {
        const val BiometricPromptCancelButton =
            "provideTextForBiometricPromptCancelButton";
        const val EnrollmentDescription =
            "provideTextForEnrollmentDescription";
        const val ScreenshotsNotificationIconId =
            "provideScreenshotsNotificationIconId";
        const val Fee = "provideTextForFee";
        const val ScreenshotsChannelName = "provideScreenshotsChannelName";
        const val TransactionDetails = "provideTextForTransactionDetails";
        const val BiometricPromptSubTitle =
            "provideTextForBiometricPromptSubTitle";
        const val Recipient = "provideTextForRecipient";
        const val EnrollmentTitle = "provideTextForEnrollmentTitle";
        const val ConfirmButton = "provideTextForConfirmButton";
        const val BiometricPromptDescription =
            "provideTextForBiometricPromptDescription";
        const val ScreenshotsNotificationText =
            "provideScreenshotsNotificationText";
        const val AuthScreenTitle = "provideTextForAuthScreenTitle";
        const val BiometricPromptTitle =
            "provideTextForBiometricPromptTitle";
        const val ConfirmBiometricButton =
            "provideTextForConfirmBiometricButton";
        const val PaymentDetailsButton =
            "provideTextForPaymentDetailsButton";
        const val AuthorizationProgressView =
            "provideTextForAuthorizationProgressView";
    }
}
