package comfroseman97.github.camera_kit

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.common.MethodChannel
import androidx.lifecycle.Lifecycle
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import com.camerakit.CameraKitView
import io.flutter.embedding.engine.plugins.activity.ActivityAware


class CameraKitController(
        private val id:Int,
        private val context: Context,
        private val binaryMessenger: BinaryMessenger?,
        private val application: Application?,
        private val lifecycle: Lifecycle?,
        private val registrar: PluginRegistry.Registrar?
) :
        DefaultLifecycleObserver,
        MethodChannel.MethodCallHandler,
        PlatformView
{

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when(call.method) {
            "capture" -> {
                cameraView.captureImage(CameraKitView.ImageCallback { cameraKitView, capturedImage ->
                    val savedPhoto = File(call.argument<String>("path"))
                    try {
                        val outputStream = FileOutputStream(savedPhoto.getPath())
                        outputStream.write(capturedImage)
                        outputStream.close()
                        result.success(null)
                    } catch (e: java.io.IOException) {
                        e.printStackTrace()
                        result.error("", "error when take photo", "")
                    }
                })
            }
            "togle" -> {
            try {
                cameraView.toggleFacing()
                result.success(null)
            }catch (e: java.io.IOException) {
                result.error("","","")
            }
        }
        }
    }

    companion object{
        val tag = "CAMERA_KEK"
    }

    lateinit var cameraView: CameraKitView
    private val methodChannel: MethodChannel
    private var disposed: Boolean = false

    init {
        methodChannel = MethodChannel(binaryMessenger, "camera_kit_$id")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // If the sdk is less than 21 (min sdk for Camera2) we don't register the plugin.
            methodChannel.setMethodCallHandler(this)
            if (lifecycle != null) {
                lifecycle.addObserver(this);
            }
        }
    }

    override fun getView(): View {
        return cameraView!!
    }

    override fun onCreate(owner: LifecycleOwner) {
        Log.v(tag, "CAMERA onCreate")
        Log.v(tag, "CAMERA init")
        cameraView = CameraKitView(context)

    }

    override fun onStart(owner: LifecycleOwner) {
        Log.v(tag,"CAMERA onStart")
        if (disposed) {
            return
        }
        cameraView?.onStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.v(tag,"CAMERA onResume")
        if (disposed) {
            return
        }
        cameraView?.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.v(tag,"CAMERA onPause")
        if (disposed) {
            return
        }
        cameraView?.onPause()

    }

    override fun onStop(owner: LifecycleOwner) {
        Log.v(tag,"CAMERA onStop")
        if (disposed) {
            return
        }
        cameraView?.onStop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.v(tag,"CAMERA onDestroy")
        if (disposed) {
            return
        }
        cameraView?.onStop()

    }

    override fun dispose() {
        Log.v(tag,"CAMERA dispose")
        disposed = true
        cameraView.onStop()


        methodChannel.setMethodCallHandler(null)

    }

    private fun getApplication(): Application? {
        return if (registrar != null && registrar.activity() != null) {
            registrar.activity().application
        } else {
            application
        }
    }

}