import 'dart:io';

import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import 'package:photo_view/photo_view_gallery.dart';

class DetailPage extends StatefulWidget {
  final List<String> photos;

  DetailPage(List<String> photos) : photos = photos.reversed.toList();

  @override
  _DetailPageState createState() => _DetailPageState();
}

class _DetailPageState extends State<DetailPage> {
  int currentIndex = 0;

  onImageChanged(int index) {
    setState(() {
      currentIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        title: Text('${currentIndex + 1} / ${widget.photos.length}'),
        backgroundColor: Colors.black,
      ),
      body: Dismissible(
        key: Key('page'),
        direction: DismissDirection.vertical,
        onDismissed: (direction) => Navigator.of(context).pop(),
        child: Container(
          color: Colors.black,
          height: MediaQuery.of(context).size.height,
          width: MediaQuery.of(context).size.width,
          child: SafeArea(
            child: PhotoViewGallery.builder(
              onPageChanged: onImageChanged,
              scrollPhysics: const BouncingScrollPhysics(),
              itemCount: widget.photos.length,
              loadingBuilder: (context, event) => Center(
                child: Container(
                  width: 20.0,
                  height: 20.0,
                  child: CircularProgressIndicator(
                    value: event == null
                        ? 0
                        : event.cumulativeBytesLoaded /
                            event.expectedTotalBytes,
                  ),
                ),
              ),
              builder: (context, index) {
                return PhotoViewGalleryPageOptions(
                  tightMode: true,
                  filterQuality: FilterQuality.high,
                  imageProvider: FileImage(
                    File(widget.photos[index]),
                  ),
                  minScale: PhotoViewComputedScale.contained,
                );
              },
            ),
          ),
        ),
      ),
    );
  }
}
