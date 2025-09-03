package com.reactnativepasskey

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.UiThreadUtil

import androidx.credentials.CredentialManager
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.*
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import androidx.credentials.exceptions.publickeycredential.GetPublicKeyCredentialDomException

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.fragment.app.FragmentActivity

class PasskeyModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val mainScope = CoroutineScope(Dispatchers.Main)

  override fun getName(): String {
    return "Passkey"
  }

  @ReactMethod
  fun create(requestJson: String, forcePlatformKey: Boolean, forceSecurityKey: Boolean, promise: Promise) {
    val credentialManager = CredentialManager.create(reactApplicationContext.applicationContext)
    val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(requestJson)

    UiThreadUtil.runOnUiThread {
      mainScope.launch {
        try {
          val activity = reactApplicationContext.currentActivity
          if (activity == null) {
            promise.reject("Passkey", "Activity is null")
            return@launch
          }

          val result = credentialManager.createCredential(
            activity as FragmentActivity,
            createPublicKeyCredentialRequest
          )

          val response = result.data.getString("androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON")
          promise.resolve(response)
        } catch (e: CreateCredentialException) {
          promise.reject("Passkey", handleRegistrationException(e))
        } catch (e: Exception) {
          promise.reject("Passkey", "Unexpected error: ${e.message}")
        }
      }
    }
  }

  private fun handleRegistrationException(e: CreateCredentialException): String {
    e.printStackTrace()
    return when (e) {
      is CreatePublicKeyCredentialDomException -> {
        e.errorMessage?.toString() ?: "CreatePublicKeyCredentialDomException"
      }
      is CreateCredentialCancellationException -> {
        "UserCancelled"
      }
      is CreateCredentialInterruptedException -> {
        "Interrupted"
      }
      is CreateCredentialProviderConfigurationException -> {
        "NotConfigured"
      }
      is CreateCredentialUnknownException -> {
        "UnknownError"
      }
      is CreateCredentialUnsupportedException -> {
        "NotSupported"
      }
      else -> {
        e.errorMessage?.toString() ?: "Unknown error"
      }
    }
  }

  @ReactMethod
  fun get(requestJson: String, forcePlatformKey: Boolean, forceSecurityKey: Boolean, promise: Promise) {
    val credentialManager = CredentialManager.create(reactApplicationContext.applicationContext)
    val getCredentialRequest = GetCredentialRequest(listOf(GetPublicKeyCredentialOption(requestJson)))

    UiThreadUtil.runOnUiThread {
      mainScope.launch {
        try {
          val activity = reactApplicationContext.currentActivity
          if (activity == null) {
            promise.reject("Passkey", "Activity is null")
            return@launch
          }

          val result = credentialManager.getCredential(
            activity as FragmentActivity,
            getCredentialRequest
          )

          val response = result.credential.data.getString("androidx.credentials.BUNDLE_KEY_AUTHENTICATION_RESPONSE_JSON")
          promise.resolve(response)
        } catch (e: GetCredentialException) {
          promise.reject("Passkey", handleAuthenticationException(e))
        } catch (e: Exception) {
          promise.reject("Passkey", "Unexpected error: ${e.message}")
        }
      }
    }
  }

  private fun handleAuthenticationException(e: GetCredentialException): String {
    e.printStackTrace()
    return when (e) {
      is GetPublicKeyCredentialDomException -> {
        e.errorMessage?.toString() ?: "GetPublicKeyCredentialDomException"
      }
      is GetCredentialCancellationException -> {
        "UserCancelled"
      }
      is GetCredentialInterruptedException -> {
        "Interrupted"
      }
      is GetCredentialProviderConfigurationException -> {
        "NotConfigured"
      }
      is GetCredentialUnknownException -> {
        "UnknownError"
      }
      is GetCredentialUnsupportedException -> {
        "NotSupported"
      }
      is NoCredentialException -> {
        "NoCredentials"
      }
      else -> {
        e.errorMessage?.toString() ?: "Unknown error"
      }
    }
  }
}