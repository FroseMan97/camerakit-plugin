package comfroseman97.github.camera_kit

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle

import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import java.util.concurrent.atomic.AtomicInteger

class CameraPluginFactory internal constructor(
        private val binaryMessenger: BinaryMessenger?,
        private val application: Application?,
        private val lifecycle: Lifecycle?,
        private val registrar: PluginRegistry.Registrar? // V1 embedding only.
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        return CameraKitController(id, context, binaryMessenger, application, lifecycle, registrar)
    }
}