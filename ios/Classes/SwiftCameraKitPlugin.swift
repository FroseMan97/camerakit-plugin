import Flutter
import UIKit
import CameraKit_iOS

public class SwiftCameraKitPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let cameraKitViewFactory = CameraKitViewFactory(with: registrar)
    registrar.register(cameraKitViewFactory, withId: "camera_kit")
  }
}

public class CameraKitView: NSObject, FlutterPlatformView {
    let previewView: CKFPreviewView
    let captureSession: CKFPhotoSession!
    let _channel: FlutterMethodChannel

    public func view() -> UIView {
        return self.previewView
    }

    public required init(_ frame: CGRect, viewId: Int64, args: Any?, with registrar: FlutterPluginRegistrar) {
        self.captureSession = CKFPhotoSession()
        self.captureSession.resolution = CGSize(width: 3024, height: 4032)
        self.captureSession.flashMode = .on
        self._channel = FlutterMethodChannel(name: "camera_kit_\(viewId)", binaryMessenger: registrar.messenger())
        self.previewView = CKFPreviewView(frame: frame)
        self.previewView.session = captureSession
        
        self.previewView.previewLayer?.videoGravity = .resizeAspectFill
    
        super.init()

        _channel.setMethodCallHandler { call, result in
            if call.method == "capture" {
                self.captureSession.capture( { (image, settings) in
                let params = call.arguments as! [String: Any]
                let path = params["path"] as! String
                let endpath = URL(fileURLWithPath: path)
                    if let jpegData = image.jpegData(compressionQuality: 1) {
                        try! jpegData.write(to: endpath, options: [.atomic])
                }
                
                result(nil)
                }) { (error) in
                    result(FlutterError(code: "CaptureError", message: "Error when capture image", details: nil))
                }
            }
            if call.method == "dispose" {
                result(nil)
            }
            if call.method == "start" {
                self.previewView.session?.start()
                result(nil)
            }
        }
    }
    deinit {
        print("deinit ------======")
    }

}

public class CameraKitViewFactory : NSObject, FlutterPlatformViewFactory {
    let _registrar: FlutterPluginRegistrar

    init(with registrar: FlutterPluginRegistrar) {
        _registrar = registrar
    }

    public func create(withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?) -> FlutterPlatformView {
        //return NativeView(frame, viewId: viewId, args: args)
        return CameraKitView(frame, viewId: viewId, args: args, with: _registrar)
    }

    public func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
        return FlutterStandardMessageCodec.sharedInstance()
    }
}

public class NativeView : NSObject, FlutterPlatformView {
    
    let frame : CGRect
    let viewId : Int64
    
    init(_ frame:CGRect, viewId:Int64, args: Any?){
        self.frame = frame
        self.viewId = viewId
    }
    
    public func view() -> UIView {
        let view : UIView = UIView(frame: self.frame)
        view.backgroundColor = UIColor.lightGray
        return view
    }
    
}
