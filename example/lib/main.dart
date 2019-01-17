import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_zip_archive/flutter_zip_archive.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';
void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  TextEditingController _controller;

  @override
  void initState() {
    super.initState();
    _controller=TextEditingController(text: "1234");
  }



  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
                Row(
                  children: <Widget>[
                    Text("密码："),
                    Expanded(child: TextField(
                      controller: _controller,
                    ))

                  ],
                ),
              MaterialButton(
                color: Theme.of(context).primaryColor,
                onPressed: _selectFile,
              child: Text("压缩"),),
                MaterialButton(
                  color: Theme.of(context).primaryColor,
                  onPressed:_unzip,
                  child: Text("解压"),)
            ],
          ),
        ),
      ),
    );
  }

  Future _selectFile() async {
    var file = await ImagePicker.pickImage(source: ImageSource.gallery);
    Directory _cacheDir = await getTemporaryDirectory();
    if (file == null) {
      return;
    }
   var _map=await FlutterZipArchive.zip(_cacheDir.path+"/test",_cacheDir.path+"/123.zip","1234");
    print("_map:"+_map.toString());
  }
  Future _unzip() async {
    Directory _cacheDir = await getTemporaryDirectory();
    var _map=await FlutterZipArchive.unzip( _cacheDir.path+"/123.zip",_cacheDir.path,"1234");
    print("_map:"+_map.toString());
  }
}
