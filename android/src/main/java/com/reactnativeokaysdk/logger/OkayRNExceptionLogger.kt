package com.reactnativeokaysdk.logger

import com.protectoria.psa.dex.common.utils.logger.ExceptionLogger
import android.text.TextUtils
import android.util.Log
import timber.log.Timber
import java.lang.Exception


class OkayRNExceptionLogger : ExceptionLogger {
  override fun exception(message: String?, exception: Exception?) {
    if (!TextUtils.isEmpty(message)) {
      Timber.i("identificator", message)
    }
    Timber.i("identificator", exception)

    Log.d("DTLogger", exception.toString())
  }

  override fun setUserIdentificator(identificator: String?) {
    identificator?.let { Timber.i(it) }
  }
}
