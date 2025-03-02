package com.byneapp.flutter_config

import android.content.Context
import android.content.res.Resources
import androidx.annotation.NonNull
import io.flutter.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.lang.reflect.Field

class FlutterConfigPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {

  private var applicationContext: Context? = null
  private lateinit var channel: MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    applicationContext = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_config")
    channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    applicationContext = null
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
    if (call.method == "loadEnvVariables") {
      result.success(loadEnvVariables())
    } else {
      result.notImplemented()
    }
  }

  private fun loadEnvVariables(): Map<String, Any?> {
    val variables = mutableMapOf<String, Any?>()
    try {
      val packageName = applicationContext!!.packageName
      val resId = applicationContext!!.resources.getIdentifier("build_config_package", "string", packageName)
      val className = try {
        applicationContext!!.getString(resId)
      } catch (e: Resources.NotFoundException) {
        packageName
      }
      val clazz = Class.forName("$className.BuildConfig")
      for (field in clazz.declaredFields) {
        field.isAccessible = true
        variables[field.name] = field.get(null)
      }
    } catch (e: Exception) {
      Log.d("FlutterConfig", "Could not access BuildConfig", e)
    }
    return variables
  }

  // ActivityAware implementations (optional, needed if activity context is required)
  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    // Handle activity attachment if needed
  }

  override fun onDetachedFromActivityForConfigChanges() {
    // Handle detachment for config changes if needed
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    // Handle reattachment after config changes if needed
  }

  override fun onDetachedFromActivity() {
    // Handle activity detachment if needed
  }
}
