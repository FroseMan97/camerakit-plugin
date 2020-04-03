import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
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
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: FlatButton(
          child: Text('Перейти к камере'),
          onPressed: () => Navigator.of(context).push(
            MaterialPageRoute(builder: (context) => CameraScreen()),
          ),
        ),
      ),
    );
  }
}

class CameraScreen extends StatefulWidget {
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

  @override
  Widget build(BuildContext context) {
    final bo = pathToFile != null && pathToFile.isNotEmpty;
    final widget = CameraKitPreview(
      (CameraKit con) {
        cameraKit = con;
      },
    );
    return Scaffold(
      
      floatingActionButton: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Visibility(
            visible: pathToFile == null || pathToFile.isEmpty,
            child: FloatingActionButton(
              heroTag: '1',
              child: Icon(Icons.camera),
              onPressed: () async {
                try {
                  final filePath = await takePicture();
                  setState(() {
                    pathToFile = filePath;
                  });
                } catch (e) {}
              },
            ),
            replacement: FloatingActionButton(
              heroTag: 'dds',
              backgroundColor: Colors.red,
              child: Icon(Icons.clear),
              onPressed: () {
                setState(() {
                  pathToFile = '';
                });
              },
            ),
          ),
          FloatingActionButton(
              heroTag: 'ddsdsad',
              backgroundColor: Colors.red,
              child: Icon(Icons.lens),
              onPressed: () async {
                await cameraKit.togle();
              },
            )
        ],
      ),
      appBar: AppBar(
        title: Text('Camera'),
        backgroundColor: Colors.transparent,
        elevation: 0,
        
      ),
      extendBodyBehindAppBar: true,
      body: Stack(
        alignment: Alignment.center,
        fit: StackFit.expand,
        children: <Widget>[
          widget,
          Visibility(
            visible: bo,
            child: Container(
              color: Colors.black,
              width: MediaQuery.of(context).size.width,
              height: MediaQuery.of(context).size.height,
              child: Image.file(File(pathToFile), fit: BoxFit.contain,),
              
            ),
          )
        ],
      ),
    );
  }
}
