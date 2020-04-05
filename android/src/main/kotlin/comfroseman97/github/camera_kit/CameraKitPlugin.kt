package comfroseman97.github.camera_kit

import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter
import io.flutter.plugin.common.PluginRegistry
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

class CameraKitPlugin(): FlutterPlugin, ActivityAware {
    override fun onDetachedFromActivity() {
        Log.v(CameraKitController.tag, "CAMERA onDetachedFromActivity")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.v(CameraKitController.tag, "CAMERA onReattachedToActivityForConfigChanges")
        lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding);
        pluginBinding
            ?.getPlatformViewRegistry()
            ?.registerViewFactory(
                 "camera_kit", 
                    CameraPluginFactory(
                        pluginBinding?.getBinaryMessenger(),
                        binding.activity,
                        lifecycle,
                        null
                    ))
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        Log.v(CameraKitController.tag, "CAMERA onAttachedToActivity")
        lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding)
        pluginBinding
                ?.getPlatformViewRegistry()
                ?.registerViewFactory(
                        "camera_kit",
                        CameraPluginFactory(
                                pluginBinding?.getBinaryMessenger(),
                                binding.activity,
                                lifecycle,
                                null
                        ))
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.v(CameraKitController.tag, "CAMERA onDetachedFromActivityForConfigChanges")
    }

    private var lifecycle: Lifecycle? = null
    private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Log.v(CameraKitController.tag, "CAMERA onAttachedToEngine")
        pluginBinding = flutterPluginBinding
    }
    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        Log.v(CameraKitController.tag, "CAMERA onDetachedFromEngine")
        pluginBinding = null
    }

    /** Plugin registration embedding v1 */
    companion object {
        @JvmStatic
        fun registerWith(registrar: PluginRegistry.Registrar) {
            if (registrar.activity() == null) {
                // When a background flutter view tries to register the plugin, the registrar has no activity.
                // We stop the registration process as this plugin is foreground only.
                return;
            }
            registrar
                    .platformViewRegistry()
                    .registerViewFactory(
                            "camera_kit",
                            CameraPluginFactory(registrar.messenger(),null, null, registrar)
                    )
        }
    }
}