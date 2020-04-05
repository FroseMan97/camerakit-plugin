package comfroseman97.github.camera_kit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.impl.PreviewConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraView
import androidx.camera.view.PreviewView
import androidx.camera.view.TextureViewMeteringPointFactory
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.common.MethodChannel
import androidx.lifecycle.Lifecycle
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry
import androidx.lifecycle.LifecycleOwner
import com.camerakit.CameraKitView
import com.camerakit.preview.CameraSurfaceView
import java.io.File
import java.io.FileOutputStream
import com.google.common.util.concurrent.ListenableFuture
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import org.w3c.dom.Text
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class CameraKitController(
        private val id:Int,
        private val context: Context,
        private val binaryMessenger: BinaryMessenger?,
        private val activity: Activity?,
        private val lifecycle: Lifecycle?,
        private val registrar: PluginRegistry.Registrar?
) :
        DefaultLifecycleObserver,
        MethodChannel.MethodCallHandler,
        PlatformView,
        PluginRegistry.RequestPermissionsResultListener

{


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?): Boolean {

        if (true) {
            Log.v("perm_res", "true")
            //view_finder.post { bindCameraUseCases() }
            return true
        } else {
            Log.v("perm_res", "false")
            return false
        }
    }

    private lateinit var imageCapture: ImageCapture
    private val executor = Executors.newSingleThreadExecutor()
    lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private val REQUEST_CODE_PERSMISSIONS = 101
    private val  REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE")
    private lateinit var cameraExecutor: ExecutorService
    private var view: View? = null
     private var view_finder: PreviewView? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    lateinit var lifecycleOwner: LifecycleOwner
    lateinit var preview:Preview

    private fun takePicture(file:File, result: MethodChannel.Result) {
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file)

                .build()
        imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val msg = "Photo capture succeeded: ${file.absolutePath}"
                Log.v("capta", msg)
                Handler(Looper.getMainLooper()).post { result.success("") }
            }
//
            override fun onError(exception: ImageCaptureException) {
                val msg = "Photo capture failed: ${exception.message}"
                Log.v("capta", msg)
                Handler(Looper.getMainLooper()).post { result.error("", msg, "") }
            }
        })
    }

    override fun onFlutterViewAttached(flutterView: View) {
        super.onFlutterViewAttached(flutterView)
        view = flutterView

    }

    override fun onFlutterViewDetached() {
        super.onFlutterViewDetached()
        view = null
    }


    //// 2
    //val rotationDegrees = when (view_finder!!.display.rotation) {
    //    Surface.ROTATION_0 -> 0
    //    Surface.ROTATION_90 -> 90
    //    Surface.ROTATION_180 -> 180
    //    Surface.ROTATION_270 -> 270
    //    else -> return
    //}
    //matris.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
//
    //// 3
    //    if(view_finder.getChildAt(0) != null) {
    //        (view_finder.getChildAt(0) as TextureView).setTransform(matris)
    //    }
    //}
    @SuppressLint("ClickableViewAccessibility")
    private fun setUpPinchToZoom(cameraControl: CameraControl, cameraInfo: CameraInfo) {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio: Float = cameraInfo.zoomState.value?.zoomRatio ?: 0F
                val delta = detector.scaleFactor
                cameraControl.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(context, listener)

        view_finder?.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }



    @SuppressLint("RestrictedApi")
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when(call.method) {
            "capture" -> {
                takePicture(File(call.argument<String>("path")), result)
            }
            "flashOn" -> {
                //cameraView.flash = ImageCapture.FLASH_MODE_ON
                result.success("")
            }
            "flashOff" -> {
                //cameraView.flash = ImageCapture.FLASH_MODE_OFF
                result.success("")
            }
            "togle" -> {

                //
                // bindCameraUseCases()
           try {
               lensFacing = if(CameraSelector.LENS_FACING_BACK == lensFacing) {
                   CameraSelector.LENS_FACING_FRONT
               } else {
                   CameraSelector.LENS_FACING_BACK
               }
               try {
                   // Only bind use cases if we can query a camera with this orientation
                   CameraX.getCameraWithLensFacing(lensFacing)
                   bindCamera()
               } catch (exc: Exception) {
                   // Do nothing
               }
               result.success(null)
           }catch (e: java.io.IOException) {
               result.error("","","")
           }
        }
        }
    }

    companion object{
        val tag = "CAMERA_KEK"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private val methodChannel: MethodChannel
    private var disposed: Boolean = false
    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
        methodChannel = MethodChannel(binaryMessenger, "camera_kit_$id")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // If the sdk is less than 21 (min sdk for Camera2) we don't register the plugin.
            methodChannel.setMethodCallHandler(this)
            lifecycle?.addObserver(this)
        }
    }

    override fun getView(): View {
        Log.v(CameraKitController.tag, "getView")
        if(view_finder == null) {
            view_finder = PreviewView(context)
        }
        Log.v("getview", "${view_finder?.getChildAt(0)?.width} ${view_finder?.getChildAt(0)?.height}")
        return view_finder!!
    }
    @SuppressLint("RestrictedApi")
    override fun onCreate(owner: LifecycleOwner) {
        lifecycleOwner = owner
        view_finder = PreviewView(context)

        view_finder!!.post {
            bindCamera()
        }
        //view_finder = PreviewView(context)
        //view_finder.setBackgroundColor(1)
        ////view_finder.fitsSystemWindows = true
        //view_finder.isSoundEffectsEnabled = true
        //Log.v("childres", "${view_finder.getChildAt(0)}")
        //view_finder.preferredImplementationMode = PreviewView.ImplementationMode.TEXTURE_VIEW
        //view_finder.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_AUTO
        //view_finder.scaleType = PreviewView.ScaleType.FIT_START
//
        //view_finder.preferredImplementationMode = PreviewView.ImplementationMode.TEXTURE_VIEW
        Log.v(tag, "CAMERA onCreate")
        Log.v(tag, "CAMERA init")


        //view_finder.addOnLayoutChangeListener { view, i, i2, i3, i4, i5, i6, i7, i8 ->  updateTransform()}

    }
    @SuppressLint("RestrictedApi")
    fun bindCamera() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(Runnable {
            CameraX.unbindAll()
            val cameraProvider = cameraProviderFuture.get()
            var cameraSelector : CameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
            imageCapture = ImageCapture.Builder()
                    .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetAspectRatioCustom(Rational(view_finder!!.width, view_finder!!.height))
                    .setCameraSelector(cameraSelector)
                    .setMaxResolution(Size(1080, 1920))
                    .build()
            val orientationEventListener = object : OrientationEventListener(context) {
                override fun onOrientationChanged(orientation : Int) {
                    // Monitors orientation values to determine the target rotation value
                    val rotation : Int = when (orientation) {
                        in 45..134 -> Surface.ROTATION_270
                        in 135..224 -> Surface.ROTATION_180
                        in 225..314 -> Surface.ROTATION_90
                        else -> Surface.ROTATION_0
                    }

                    imageCapture.targetRotation = rotation
                }
            }
            orientationEventListener.enable()

            var preview : Preview = Preview.Builder()
                    //.setTargetResolution(screenSize)
                    .setCameraSelector(cameraSelector)
                    .setTargetAspectRatioCustom(Rational(view_finder!!.width, view_finder!!.height))
                    .build()
            var camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
            preview.setSurfaceProvider(view_finder?.createSurfaceProvider(camera.cameraInfo))
            setUpPinchToZoom(camera.cameraControl, camera.cameraInfo)
        }, ContextCompat.getMainExecutor(activity?.applicationContext))
}

    override fun onStart(owner: LifecycleOwner) {
        lifecycleOwner = owner
        Log.v(tag,"CAMERA onStart")
        if (disposed) {
            return
        }

    }

    override fun onResume(owner: LifecycleOwner) {

        lifecycleOwner = owner
        Log.v(tag,"CAMERA onResume")
        if (disposed) {
            return
        }
        //view_finder.post{
        //    bindCameraUseCases()
        //}

    }

    override fun onPause(owner: LifecycleOwner) {
        lifecycleOwner = owner
        Log.v("childres", "${view_finder?.getChildAt(0)}")

        Log.v(tag,"CAMERA onPause")
        if (disposed) {
            return
        }



    }

    override fun onStop(owner: LifecycleOwner) {
        lifecycleOwner = owner
        Log.v(tag,"CAMERA onStop")
        if (disposed) {
            return
        }
    }
    @SuppressLint("RestrictedApi")
    override fun onDestroy(owner: LifecycleOwner) {
        lifecycleOwner = owner
        Log.v(tag,"CAMERA onDestroy")
        if (disposed) {
            return
        }
        CameraX.unbindAll()
        view_finder = null


    }
    @SuppressLint("RestrictedApi")
    override fun dispose() {
        Log.v(tag,"CAMERA dispose")
        disposed = true
        methodChannel.setMethodCallHandler(null)
        CameraX.unbindAll()
        view_finder = null
    }


    private fun getApplication(): Application? {
        if(activity != null) {
            return activity.application
        }
        if(registrar != null) {
            registrar.activity().application
        }
        return null
    }

}


//private fun Preview.setSurfaceProvider(previewView: PreviewView?) {
//    previewView.
//}

private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
    private val frameRateWindow = 8
    private val frameTimestamps = ArrayDeque<Long>(5)
    private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
    private var lastAnalyzedTimestamp = 0L
    var framesPerSecond: Double = -1.0
        private set

    /**
     * Used to add listeners that will be called with each luma computed
     */
    fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

    /**
     * Helper extension function used to extract a byte array from an image plane buffer
     */
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    /**
     * Analyzes an image to produce a result.
     *
     * <p>The caller is responsible for ensuring this analysis method can be executed quickly
     * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
     * images will not be acquired and analyzed.
     *
     * <p>The image passed to this method becomes invalid after this method returns. The caller
     * should not store external references to this image, as these references will become
     * invalid.
     *
     * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
     * call image.close() on received images when finished using them. Otherwise, new images
     * may not be received or the camera may stall, depending on back pressure setting.
     *
     */
    override fun analyze(image: ImageProxy) {
        // If there are no listeners attached, we don't need to perform analysis
        if (listeners.isEmpty()) {
            image.close()
            return
        }

        // Keep track of frames analyzed
        val currentTime = System.currentTimeMillis()
        frameTimestamps.push(currentTime)

        // Compute the FPS using a moving average
        while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
        val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
        val timestampLast = frameTimestamps.peekLast() ?: currentTime
        framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

        // Analysis could take an arbitrarily long amount of time
        // Since we are running in a different thread, it won't stall other use cases

        lastAnalyzedTimestamp = frameTimestamps.first

        // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
        val buffer = image.planes[0].buffer

        // Extract image data from callback object
        val data = buffer.toByteArray()

        // Convert the data into an array of pixel values ranging 0-255
        val pixels = data.map { it.toInt() and 0xFF }

        // Compute average luminance for the image
        val luma = pixels.average()

        // Call all listeners with new value
        listeners.forEach { it(luma) }

        image.close()
    }
}

/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Double) -> Unit



