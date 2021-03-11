package com.jlcool.flutterziparchive;

import android.os.AsyncTask;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterZipArchivePlugin
 */
public class FlutterZipArchivePlugin implements MethodCallHandler {
    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_zip_archive");
        channel.setMethodCallHandler(new FlutterZipArchivePlugin());
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "zip":
                new ZipTask(call, result).execute();
                break;
            case "unzip":
                new UnZipTask(call, result).execute();
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private class ZipTask extends AsyncTask<Void, Void, Map<String, Object>> {
        private MethodCall call;
        private Result result;

        public ZipTask(MethodCall call, Result result) {
            this.call = call;
            this.result = result;
        }

        @Override
        protected Map<String, Object> doInBackground(Void... params) {
            return zip(call, result);
        }

        @Override
        protected void onPostExecute(Map<String, Object> data) {
            super.onPostExecute(data);
            this.result.success(data);
        }
    }

    public Map<String, Object> zip(MethodCall call, Result result) {
        String src = call.argument("src");
        String dest = call.argument("dest");
        String passwd = call.argument("password");
        Map<String, Object> m1 = new HashMap();
        String path = zip(src, dest, passwd);
        if (path == null) {
            m1.put("result", "fail");
        } else {
            m1.put("result", "success");
            m1.put("path", path);
        }
        return m1;
    }

    private class UnZipTask extends AsyncTask<Void, Void, Map<String, Object>> {
        private MethodCall call;
        private Result result;

        public UnZipTask(MethodCall call, Result result) {
            this.call = call;
            this.result = result;
        }

        @Override
        protected Map<String, Object> doInBackground(Void... params) {
            return unzip(call, result);
        }

        @Override
        protected void onPostExecute(Map<String, Object> data) {
            super.onPostExecute(data);
            this.result.success(data);
        }
    }

    public Map<String, Object> unzip(MethodCall call, Result result) {
        String zip = call.argument("zip");
        String dest = call.argument("dest");
        String passwd = call.argument("password");
        Map<String, Object> m1 = new HashMap();
        try {
            File[] _files = unzip(zip, dest, passwd);
            String[] _paths = new String[_files.length];
            for (int i = 0; i < _files.length; i++) {
                _paths[i] = _files[i].getName();
            }
            m1.put("result", "success");
            m1.put("files", StringUtils.join(_paths, ","));
        } catch (Exception ex) {
            m1.put("result", "fail");
        }
        return m1;
    }

    /**
     * 使用给定密码解压指定的ZIP压缩文件到指定目录
     * <p>
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     *
     * @param zip    指定的ZIP压缩文件
     * @param dest   解压目录
     * @param passwd ZIP文件的密码
     * @return 解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public File[] unzip(String zip, String dest, String passwd) throws ZipException {
        File zipFile = new File(zip);
        return unzip(zipFile, dest, passwd);
    }

    /**
     * 使用给定密码解压指定的ZIP压缩文件到当前目录
     *
     * @param zip    指定的ZIP压缩文件
     * @param passwd ZIP文件的密码
     * @return 解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */


    public File[] unzip(String zip, String passwd) throws ZipException {
        File zipFile = new File(zip);

        File parentDir = zipFile.getParentFile();
        return unzip(zipFile, parentDir.getAbsolutePath(), passwd);
    }

    /**
     * 使用给定密码解压指定的ZIP压缩文件到指定目录
     * <p>
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     *
     * @param zipFile 指定的ZIP压缩文件
     * @param dest    解压目录
     * @param passwd  ZIP文件的密码
     * @return 解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public File[] unzip(File zipFile, String dest, String passwd) throws ZipException {
        ZipFile zFile = new ZipFile(zipFile);
        zFile.setFileNameCharset("GBK");
        if (!zFile.isValidZipFile()) {
            throw new ZipException("压缩文件不合法,可能被损坏.");
        }
        File destDir = new File(dest);
        if (destDir.isDirectory() && !destDir.exists()) {
            destDir.mkdir();
        }
        if (zFile.isEncrypted()) {
            zFile.setPassword(passwd.toCharArray());
        }
        zFile.extractAll(dest);

        List<FileHeader> headerList = zFile.getFileHeaders();
        List<File> extractedFileList = new ArrayList<File>();
        for (FileHeader fileHeader : headerList) {
            if (!fileHeader.isDirectory()) {
                extractedFileList.add(new File(destDir, fileHeader.getFileName()));
            }
        }
        File[] extractedFiles = new File[extractedFileList.size()];
        extractedFileList.toArray(extractedFiles);
        return extractedFiles;
    }

    /**
     * 压缩指定文件到当前文件夹
     *
     * @param src 要压缩的指定文件
     * @return 最终的压缩文件存放的绝对路径, 如果为null则说明压缩失败.
     */
    public String zip(String src) {
        return zip(src, null);
    }

    /**
     * 使用给定密码压缩指定文件或文件夹到当前目录
     *
     * @param src    要压缩的文件
     * @param passwd 压缩使用的密码
     * @return 最终的压缩文件存放的绝对路径, 如果为null则说明压缩失败.
     */
    public String zip(String src, String passwd) {
        return zip(src, null, passwd);
    }


    public String zip(String src, String dest, String passwd) {


        File srcFile = new File(src);
        dest = buildDestinationZipFilePath(srcFile, dest);
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);           // 压缩方式
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);    // 压缩级别
        if (passwd != null && passwd != "") {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD); // 加密方式
            parameters.setPassword(passwd.toCharArray());
        }
        try {
            ZipFile zipFile = new ZipFile(dest);
            if (srcFile.isDirectory()) {
                // 如果不创建目录的话,将直接把给定目录下的文件压缩到压缩文件,即没有目录结构

                File[] subFiles = srcFile.listFiles();
                ArrayList<File> temp = new ArrayList<File>();
                Collections.addAll(temp, subFiles);
                zipFile.addFiles(temp, parameters);
                return dest;


            } else {
                zipFile.addFile(srcFile, parameters);
            }
            return dest;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 构建压缩文件存放路径,如果不存在将会创建
     * 传入的可能是文件名或者目录,也可能不传,此方法用以转换最终压缩文件的存放路径
     *
     * @param srcFile   源文件
     * @param destParam 压缩目标路径
     * @return 正确的压缩文件存放路径
     */
    private String buildDestinationZipFilePath(File srcFile, String destParam) {
        if (destParam == null || destParam == "") {
            if (srcFile.isDirectory()) {
                destParam = srcFile.getParent() + File.separator + srcFile.getName() + ".zip";
            } else {
                String fileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));
                destParam = srcFile.getParent() + File.separator + fileName + ".zip";
            }
        } else {
            createDestDirectoryIfNecessary(destParam);  // 在指定路径不存在的情况下将其创建出来
            if (destParam.endsWith(File.separator)) {
                String fileName = "";
                if (srcFile.isDirectory()) {
                    fileName = srcFile.getName();
                } else {
                    fileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));
                }
                destParam += fileName + ".zip";
            }
        }
        return destParam;
    }

    /**
     * 在必要的情况下创建压缩文件存放目录,比如指定的存放路径并没有被创建
     *
     * @param destParam 指定的存放路径,有可能该路径并没有被创建
     */
    private void createDestDirectoryIfNecessary(String destParam) {
        File destDir = null;
        if (destParam.endsWith(File.separator)) {
            destDir = new File(destParam);
        } else {
            destDir = new File(destParam.substring(0, destParam.lastIndexOf(File.separator)));
        }
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }
}
