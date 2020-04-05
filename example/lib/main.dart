import 'dart:io';
import 'dart:math';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:camera_kit/camera_kit.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: FirstScreen(),
    );
  }
}

class FirstScreen extends StatelessWidget {
  CameraKit cameraKit;
  @override
  Widget build(BuildContext context) {
    final textStyle = TextStyle(fontSize: 18);
    return Scaffold(
      extendBodyBehindAppBar: true,
      body: CameraScreen(),
    );
  }
}

class CameraScreen extends StatefulWidget {
  final double size;

  CameraScreen({this.size});

  @override
  _CameraScreenState createState() => _CameraScreenState();
}

class _CameraScreenState extends State<CameraScreen> {
  CameraKit cameraKit;
  String pathToFile = '';

  Future<String> takePicture() async {
    final Directory extDir = await getApplicationDocumentsDirectory();
    final String dirPath = '${extDir.path}/Pictures/cme';
    await Directory(dirPath).create(recursive: true);
    final String filePath =
        '$dirPath/${DateTime.now().millisecondsSinceEpoch}.jpg';
    final message = await cameraKit.captureImage(filePath);
    print('CAMERA ------- $message');
    return filePath;
  }

  var key = GlobalKey();
  @override
  Widget build(BuildContext context) {
    final bo = pathToFile != null && pathToFile.isNotEmpty;
    final camera = CameraKitPreview(
      (CameraKit con) {
        cameraKit = con;
      },
    );

    return Container(
      color: Colors.black,
      child: SafeArea(
        child: Scaffold(
          extendBodyBehindAppBar: true,
          appBar: AppBar(
            title: Text('Camera'),
            centerTitle: true,
            backgroundColor: Colors.black,
            elevation: 0,
          ),
          body: SafeArea(
            top: false,
            child: Stack(
              children: [
                Column(
                  children: [
                    Expanded(
                      child: Container(
                        child: camera,
                      ),
                    ),
                    Container(
                      width: MediaQuery.of(context).size.width,
                      color: Colors.black,
                      child: GestureDetector(
                        onTap: () async {
                          HapticFeedback.mediumImpact();
                          final path = await takePicture();
                          HapticFeedback.mediumImpact();
                          HapticFeedback.vibrate();

                          setState(() {
                            pathToFile = path;
                          });
                        },
                        child: Padding(
                          padding: const EdgeInsets.symmetric(vertical: 16.0, horizontal: 32),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: <Widget>[
                              
                              SizedBox(
                                height: 56,
                                width: 56,
                                child: GestureDetector(
                                  onTap: () async {
                                    cameraKit.togle();
                                  },
                                  child: Icon(Icons.switch_camera, size: 36,color: Colors.white,),),
                              ),
                              CircleAvatar(
                                foregroundColor: Colors.transparent,
                                backgroundColor: Colors.white,
                                radius: 32,
                                child: CircleAvatar(
                                  foregroundColor: Colors.transparent,
                                  radius: 30,
                                  backgroundColor: Colors.black,
                                  child: CircleAvatar(
                                    foregroundColor: Colors.transparent,
                                    backgroundColor: Colors.white,
                                    radius: 28,
                                  ),
                                ),
                              ),
                              SizedBox(
                                height: 56,
                                width: 56,
                                child: Visibility(
                                  visible: bo,
                                  child: InkWell(
                                    onTap: () => Navigator.of(context)
                                        .push(MaterialPageRoute(
                                            builder: (context) => Scaffold(
                                                  extendBodyBehindAppBar: true,
                                                  appBar: AppBar(
                                                    backgroundColor:
                                                        Colors.transparent,
                                                  ),
                                                  backgroundColor: Colors.black,
                                                  body: Center(
                                                      child: Hero(
                                                    tag: 'picture',
                                                    child: Image.file(
                                                      File(pathToFile),
                                                      fit: BoxFit.cover,
                                                    ),
                                                  )),
                                                ))),
                                    child: Hero(
                                      transitionOnUserGestures: true,
                                      tag: 'picture',
                                      child: Image.file(
                                        File(pathToFile),
                                        fit: BoxFit.cover,
                                      ),
                                    ),
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    )
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
