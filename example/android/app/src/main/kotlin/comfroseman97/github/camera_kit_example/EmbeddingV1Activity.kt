package comfroseman97.github.camera_kit_example

import android.os.Bundle
import comfroseman97.github.camera_kit.CameraKitPlugin
import io.flutter.app.FlutterActivity

class EmbeddingV1Activity : FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CameraKitPlugin.registerWith(registrarFor("camera_kit"))
    }
}