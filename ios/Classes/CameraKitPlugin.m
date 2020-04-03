#import "CameraKitPlugin.h"
#if __has_include(<camera_kit/camera_kit-Swift.h>)
#import <camera_kit/camera_kit-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "camera_kit-Swift.h"
#endif

@implementation CameraKitPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftCameraKitPlugin registerWithRegistrar:registrar];
}
@end
