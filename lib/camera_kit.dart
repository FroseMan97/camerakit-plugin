import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/material.dart';

class CameraKit {
  final MethodChannel _channel;

  CameraKit._(MethodChannel channel) : _channel = channel;

  Future captureImage(String path) async {
    print('${_channel.name}');
    try{
      final result = await _channel.invokeMethod('capture', {
      "path": path
    }).catchError(
      (e) => print(e));
    }
    catch(error) {
print(error);
    }

  }

  Future dispose() async {
    await _channel.invokeMethod('dispose');
  }

  Future togle() async {
    await _channel.invokeMethod('togle');
  }

  Future start() async {
    await _channel.invokeMethod('start');
  }

  static CameraKit init(int id) {
    final MethodChannel methodChannel = MethodChannel('camera_kit_$id');

    return CameraKit._(methodChannel);
  }
}

class CameraKitPreview extends StatefulWidget {
  final Function onCreated;

  CameraKitPreview(this.onCreated);

  @override
  _CameraKitPreviewState createState() => _CameraKitPreviewState();
}

class _CameraKitPreviewState extends State<CameraKitPreview>{
  final GlobalKey key = GlobalKey();
  @override
  Widget build(BuildContext context) {
    if(defaultTargetPlatform == TargetPlatform.android){
      return AndroidView(
        key: key,
        onPlatformViewCreated: (id) {
          if(widget.onCreated != null) {
            widget.onCreated(CameraKit.init(id));
          }
        },
        viewType: 'camera_kit',
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else if(defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        key: key,
        onPlatformViewCreated: (id) {
          if (widget.onCreated != null) {
            widget.onCreated(CameraKit.init(id));
          }
        },
        viewType: 'camera_kit',
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return Text('CameraKit not support this platform');
    }
  }
}
