#import "FlutterZipArchivePlugin.h"
#import "SSZipArchive.h"

@implementation FlutterZipArchivePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_zip_archive"
            binaryMessenger:[registrar messenger]];
  FlutterZipArchivePlugin* instance = [[FlutterZipArchivePlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"zip" isEqualToString:call.method]) {
      [self zip:call result:result];
  }else if ([@"unzip" isEqualToString:call.method]) {
      [self unzip:call result:result];
  } else {
    result(FlutterMethodNotImplemented);
  }
}
- (void)zip:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSString *src =call.arguments[@"src"];
    NSString *dest =call.arguments[@"dest"];
    NSString *password =call.arguments[@"password"];
    NSDictionary *m1 =nil;
    BOOL success = [SSZipArchive createZipFileAtPath:dest
                             withContentsOfDirectory:src
                                 keepParentDirectory:NO
                                    compressionLevel:-1
                                            password:password.length > 0 ? password : nil
                                                 AES:NO
                                     progressHandler:nil];
    if(success){
        m1= @{
              @"result":  @"success",
              @"path": dest,
              };
    }else{
        m1= @{
              @"result":  @"fail"
              };
    }
    result(m1);
    
}

- (void)unzip:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSString *src =call.arguments[@"zip"];
    NSString *dest =call.arguments[@"dest"];
    NSString *password =call.arguments[@"password"];
    NSDictionary *m1 =nil;
    BOOL success = [SSZipArchive unzipFileAtPath:src
                                   toDestination:dest
                              preserveAttributes:YES
                                       overwrite:YES
                                  nestedZipLevel:0
                                        password:password.length > 0 ? password : nil
                                           error:nil
                                        delegate:nil
                                 progressHandler:nil
                               completionHandler:nil];
    if(success){
        m1= @{
              @"result":  @"success",
              @"path": dest,
              };
    }else{
        m1= @{
              @"result":  @"fail"
              };
    }
    result(m1);
}


@end
