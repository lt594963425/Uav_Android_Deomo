package com.dji.ux.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * $activityName
 *
 * @author ${LiuTao}
 * @date 2018/4/15
 * 写文件的工具类
 * 1、内部存储
 * 常见就是我们的/data/data目录下的数据
 */
public class FileUtils {

    public static final String TAG = "FileUtils";
    /**
     * APP根目录 文件路径
     */
    public static final String ROOT_DIR = "mpay/data";

    /**
     * 判断SD卡是否挂载
     */
    public static boolean isSDCardAvailable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 文件目录
     * 获取应用目录，当SD卡存在时，获取SD卡上的目录，当SD卡不存在时，获取应用的cache目录
     * 这得到的路径为 /base_spalsh/b/c/point/
     * http:\/\/pwj-web-slb-c9336dcf9adf6fce.elb.cn-northwest-1.amazonaws.com.cn\/\/static\/upload\/images\/20190531\/e59f0e7e6e47e5401fbefabe89918074.jpg
     */
    public static String getDir(String projectname) {
        StringBuilder sb = new StringBuilder();
        if (isSDCardAvailable()) {
            sb.append(getExternalStoragePath());
        } else {
            sb.append(getCachePath());
        }
        sb.append(projectname + File.separator);
        String path = sb.toString();
        if (createDirs(path)) {
            return path;
        } else {
            return "";
        }
    }

    /**
     * 文件目录
     * 获取应用目录，当SD卡存在时，获取SD卡上的目录，当SD卡不存在时，获取应用的cache目录
     * 这得到的路径为 /base_spalsh/b/c/point
     */
    public static String getRootDir() {
        StringBuilder sb = new StringBuilder();
        if (isSDCardAvailable()) {
            sb.append(getExternalStoragePath());

        } else {
            sb.append(getCachePath());
        }

        String path = sb.toString();
        if (createDirs(path)) {
            return path;
        } else {
            return "";
        }
    }

    /**
     * 获取SD下的应用目录
     */
    public static String getExternalStoragePath() {
        StringBuilder sb = new StringBuilder();
        // /storage/emulated/0
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append(ROOT_DIR);
        sb.append(File.separator);
        return sb.toString();
    }

    /**
     * /data/user/0/com.paiwujie.robredpacket/files
     *
     * @return
     */
    public static String getAppFilesDirPath() {
        String path = UIUtils.getContext().getFilesDir().getPath();
        if (createDirs(path)) {
            return path;
        } else {
            return "";
        }
    }

    /**
     * /data/user/0/com.paiwujie.robredpacket/cache
     *
     * @return
     */
    public static String getAppCacheDirPath() {
        String path = UIUtils.getContext().getCacheDir().getPath();
        if (createDirs(path)) {
            return path;
        } else {
            return "";
        }
    }

    /**
     * /storage/emulated/0/Android/data/com.paiwujie.robredpacket/cache
     *
     * @return
     */
    public static String getAppExternalCacheDir() {
        try {
            String path = UIUtils.getContext().getExternalCacheDir().getPath();
            if (createDirs(path)) {
                return path;
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * /storage/emulated/0/Android/data/com.paiwujie.robredpacket/files/test
     *
     * @return
     */
    public static String getAppExternalFilesDir() {
        String path = UIUtils.getContext().getExternalFilesDir("test").getPath();
        if (createDirs(path)) {
            return path;
        } else {
            return "";
        }
    }

    /**
     * 获取应用的cache目录
     * /data/user/0/com.paiwujie.robredpacket/cache/
     */
    public static String getCachePath() {
        File f = UIUtils.getContext().getCacheDir();
        if (createDirs(f.getPath())) {
            return f.getPath() + "/";
        } else {
            return "";
        }
    }

    /**
     * 创建文件夹
     */
    public static boolean createDirs(String dirPath) {
        File file = new File(dirPath);
        Log.e("TAG", "filepath:" + file);
        if (!file.exists() || !file.isDirectory()) {
            return file.mkdirs();
        }
        return true;
    }

    /**
     * 复制文件，可以选择是否删除源文件
     */
    public static boolean copyFile(String srcPath, String destPath,
                                   boolean deleteSrc) {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);
        return copyFile(srcFile, destFile, deleteSrc);
    }


    /***
     *  复制文件，可以选择是否删除源文件
     * @param srcFile 原文件
     * @param destFile  目标文件
     * @param deleteSrc 是否删除
     * @return
     */
    public static boolean copyFile(File srcFile, File destFile,
                                   boolean deleteSrc) {
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int i = -1;
            while ((i = in.read(buffer)) > 0) {
                out.write(buffer, 0, i);
                out.flush();
            }
            if (deleteSrc) {
                srcFile.delete();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            return false;
        } finally {
            IOUtils.close(out);
            IOUtils.close(in);
        }
        return true;
    }

    /**
     * 判断文件是否可写
     */
    public static boolean isWriteable(String path) {
        try {
            if (StringUtils.isEmpty(path)) {
                return false;
            }
            File f = new File(path);
            return f.exists() && f.canWrite();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            return false;
        }
    }

    /**
     * 修改文件的权限,例如"777"等
     */
    public static void chmod(String path, String mode) {
        try {
            String command = "chmod " + mode + " " + path;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }

    /**
     * 把数据写入文件
     *
     * @param is       数据流
     * @param path     文件路径
     * @param recreate 如果文件存在，是否需要删除重建
     * @return 是否写入成功
     */
    public static boolean writeFile(InputStream is, String path,
                                    boolean recreate) {
        boolean res = false;
        File f = new File(path);
        FileOutputStream fos = null;

        try {
            if (recreate && f.exists()) {
                f.delete();
            }
            if (!f.exists() && null != is) {
                File parentFile = new File(f.getParent());
                parentFile.mkdirs();
                int count = -1;
                byte[] buffer = new byte[1024];
                fos = new FileOutputStream(f);
                while ((count = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }
                res = true;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        } finally {
            IOUtils.close(fos);
            IOUtils.close(is);
        }
        return res;
    }

    /**
     * 把字符串数据写入文件
     *
     * @param content 需要写入的字符串
     * @param path    文件路径名称
     * @param append  是否以添加的模式写入
     * @return 是否写入成功
     */
    public static boolean writeFile(byte[] content, String path, boolean append) {
        boolean res = false;
        File f = new File(path);
        LogUtil.e(TAG, f.getPath());
        RandomAccessFile raf = null;
        try {
            if (f.exists()) {
                if (!append) {
                    f.delete();
                    f.createNewFile();
                }
            } else {
                f.createNewFile();
            }
            if (f.canWrite()) {
                raf = new RandomAccessFile(f, "rw");
                raf.seek(raf.length());
                raf.write(content);
                res = true;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            res = false;
        } finally {
            IOUtils.close(raf);
        }
        return res;
    }

    //创建或者重命名
    public static File createOrRenameFile(File from) {
        String[] fileInfo = getFileInfo(from);
        String toPrefix = fileInfo[0];
        String toSuffix = fileInfo[1];
        File file;
        file = FileUtils.createOrRenameFile(from, toPrefix, toSuffix);
        return file;

    }

    /**
     * sdcard/pic/tanyang.jpg
     * <p>
     * toPrefix: tanyang
     * toSuffix: tanyang.jpg
     *
     * @param from
     * @param toPrefix
     * @param toSuffix
     * @return
     */

    public static File createOrRenameFile(File from, String toPrefix, String toSuffix) {
        File directory = from.getParentFile();
        if (!directory.exists()) {
            if (directory.mkdir()) {
            }
        }
        File newFile = new File(directory, toPrefix + toSuffix);
        for (int i = 1; newFile.exists() && i < Integer.MAX_VALUE; i++) {
            newFile = new File(directory, toPrefix + '(' + i + ')' + toSuffix);
        }
        if (!from.renameTo(newFile)) {
            return from;
        }
        return newFile;
    }


    /**
     * 获取文件的 toPrefix，toSuffix；
     * 1）假如我们文件的全路径是 sdcard/pic/tanyang.jpg ，那么 toPrefix 是 tanyang，toSuffix 是 。jpg
     * <p>
     * 2）假如我们文件的全路径是 sdcard/pic/tanyang ，那么 toPrefix 是 tanyang，toSuffix 是 空字符串。相当于 String toSuffix=”“;
     *
     * @param from fileInfo[0]: toPrefix;
     *             fileInfo[1]:toSuffix;
     * @return
     */
    public static String[] getFileInfo(File from) {
        String fileName = from.getName();
        int index = fileName.lastIndexOf(".");
        String toPrefix = "";
        String toSuffix = "";
        if (index == -1) {
            toPrefix = fileName;
        } else {
            toPrefix = fileName.substring(0, index);
            toSuffix = fileName.substring(index, fileName.length());
        }
        return new String[]{toPrefix, toSuffix};
    }

    public static String generateSuffix() {
        // 获得当前时间
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        // 转换为字符串
        String formatDate = format.format(new Date());
        // 随机生成文件编号
        int random = new Random().nextInt(10000);
        return new StringBuffer().append(formatDate).append(
                random).toString();
    }

    /**
     * 文件 重命名
     *
     * @param from
     * @return
     */
    public static File createFileWithCurDate(File from) {
        String[] fileInfo = getFileInfo(from);
        String toPrefix = "pwj" + generateSuffix();
        String toSuffix = fileInfo[1];
        File to = new File(from.getParent(), toPrefix + toSuffix);
        if (copy(from.getPath(), to.getPath(), false)) {
            return to;
        } else {
            return from;
        }


    }

    /**
     * 将文件转为byte[]
     *
     * @param file 文件
     * @return
     */
    public static byte[] getBytes(File file) {
        ByteArrayOutputStream out = null;
        try {
            FileInputStream in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            byte[] b = new byte[(int) file.length()];
            int i = 0;
            while ((i = in.read(b)) != -1) {

                out.write(b, 0, b.length);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] s = out.toByteArray();
        return s;

    }

    /**
     * 把字符串数据写入文件
     *
     * @param content 需要写入的字符串
     * @param path    文件路径名称
     * @param append  是否以添加的模式写入
     * @return 是否写入成功
     */
    public static boolean writeFile(String content, String path, boolean append) {
        byte[] bytes = new byte[0];
        try {
            bytes = content.getBytes("UTF-8");
            return writeFile(bytes, path, append);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;


    }

    /**
     * 把键值对写入文件
     *
     * @param filePath 文件路径
     * @param key      键
     * @param value    值
     * @param comment  该键值对的注释
     */
    public static void writeProperties(String filePath, String key,
                                       String value, String comment) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(filePath)) {
            return;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File f = new File(filePath);
        try {
            if (!f.exists() || !f.isFile()) {
                f.createNewFile();
            }
            fis = new FileInputStream(f);
            Properties p = new Properties();
            p.load(fis);// 先读取文件，再把键值对追加到后面
            p.setProperty(key, value);
            fos = new FileOutputStream(f);
            p.store(fos, comment);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        } finally {
            IOUtils.close(fis);
            IOUtils.close(fos);
        }
    }

    /**
     * 根据键读取值
     */
    public static String readProperties(String filePath, String key,
                                        String defaultValue) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(filePath)) {
            return null;
        }
        String value = null;
        FileInputStream fis = null;
        File f = new File(filePath);
        try {
            if (!f.exists() || !f.isFile()) {
                f.createNewFile();
            }
            fis = new FileInputStream(f);
            Properties p = new Properties();
            p.load(fis);
            value = p.getProperty(key, defaultValue);

        } catch (IOException e) {
            LogUtil.e(TAG, e.getMessage());
        } finally {
            IOUtils.close(fis);
        }
        return value;
    }

    /**
     * 把字符串键值对的map写入文件
     */
    public static void writeMap(String filePath, Map<String, String> map,
                                boolean append, String comment) {
        if (map == null || map.size() == 0 || StringUtils.isEmpty(filePath)) {
            return;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File f = new File(filePath);
        try {
            if (!f.exists() || !f.isFile()) {
                f.createNewFile();
            }
            Properties p = new Properties();
            if (append) {
                fis = new FileInputStream(f);
                p.load(fis);// 先读取文件，再把键值对追加到后面
            }
            p.putAll(map);
            fos = new FileOutputStream(f);
            p.store(fos, comment);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        } finally {
            IOUtils.close(fis);
            IOUtils.close(fos);
        }
    }

    /**
     * 把字符串键值对的文件读入map
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map<String, String> readMap(String filePath,
                                              String defaultValue) {
        if (StringUtils.isEmpty(filePath)) {
            return null;
        }
        Map<String, String> map = null;
        FileInputStream fis = null;
        File f = new File(filePath);
        try {
            if (!f.exists() || !f.isFile()) {
                f.createNewFile();
            }
            fis = new FileInputStream(f);
            Properties p = new Properties();
            p.load(fis);
            map = new HashMap<String, String>((Map) p);// 因为properties继承了map，所以直接通过p来构造一个map
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        } finally {
            IOUtils.close(fis);
        }
        return map;
    }

    /**
     * 改名
     */
    /**
     * @param src
     * @param des
     * @param delete
     * @return
     */
    public static boolean copy(String src, String des, boolean delete) {
        LogUtil.e("文件上传 源文件,", src);
        LogUtil.e("文件上传 复制文件,", des);
        File file = new File(src);
        if (!file.exists()) {
            return false;
        }
        //复制的文件
        File desFile = new File(des);
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = new FileOutputStream(desFile);
            byte[] buffer = new byte[1024];
            int count = -1;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            return false;
        } finally {
            IOUtils.close(in);
            IOUtils.close(out);
        }
        if (delete) {
            file.delete();
        }
        return true;
    }

    /***删除文件
     *
     * @param filepath
     * @param name
     */
    public static void deleteFile(String filepath, String name) {
        File file = new File(filepath + name);
        if (filepath == null && name == null) {
            return;
        }
        if (file.exists()) {
            file.delete();
        }

    }

    /***删除目标文件
     *
     * @param filepath
     */
    public static void deleteTargetFile(String filepath) {
        File file = new File(filepath);
        if (file.exists()) {
            LogUtil.e("头像", "---------------删除--------------");
            file.delete();
        }

    }

    /***删除文件夹和文件夹里面的文件
     *
     * @param pPath
     */
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteDirWihtFile(file); // 递规的方式删除文件夹
            }
        }
        dir.delete();// 删除目录本身
    }

    /**
     * 读取文件内容
     *
     * @param pathfile
     * @param name
     * @return
     */
    public static String readtext(String pathfile, String name) {
        File f = new File(pathfile + name);
        if (!f.exists()) {
            return null;
        }
        FileInputStream is;
        String result = null;
        try {
            is = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] array = new byte[1024];
            int len = -1;
            while ((len = is.read(array)) > 0 - 1) {
                bos.write(array, 0, len);
            }
            byte[] data = bos.toByteArray(); // 取内存中保存的数据
            result = new String(data, "UTF-8");
            bos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 读取文件内容
     *
     * @return
     */
    public static String readtext(File f) {
        if (!f.exists()) {
            return null;
        }
        FileInputStream is;
        String result = null;
        try {
            is = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] array = new byte[1024];
            int len = -1;
            while ((len = is.read(array)) > 0 - 1) {
                bos.write(array, 0, len);
            }
            byte[] data = bos.toByteArray(); // 取内存中保存的数据
            result = new String(data);
            bos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean deleteMissionFile(String pathfile, String name) {
        File f = new File(pathfile + name);
        if (f.exists()) {
            return f.delete();
        } else {
            return true;
        }
    }

    /***
     * 获取指定路径下的文件目录
     */
    private static void getFilePath() {

    }

    /***
     * 读取指定目录下的文件
     * @param strPath
     * @return
     */
    public static ArrayList<String> readFileListNames(String strPath) {
        ArrayList<String> fileLists = new ArrayList<>();
        String filename;//文件名
        String suf;//文件后缀
        File dir = new File(strPath);//文件夹dir
        Log.e("fileUtils", "readFileListNames:" + strPath);
        File[] files = dir.listFiles();//文件夹下的所有文件或文件夹

        if (files == null) {
            return fileLists;
        }

        for (int i = 0; i < files.length; i++) {

            if (files[i].isDirectory()) {
//                System.out.println("---" + files[i].getAbsolutePath());
//                readFileListNames(files[i].getAbsolutePath());//递归文件夹！！！
            } else {
                filename = files[i].getName();
                fileLists.add(filename);//对于文件才把它的路径加到filelist中
            }

        }
        return fileLists;
    }

    /***
     * 获取指定工程目录下所有目录
     * @param strPath
     * @return
     */
    public static ArrayList<String> readFileLists(String strPath) {
        ArrayList<String> fileLists = new ArrayList<>();
        try {
            String filename;//文件名
            Log.e("readFileListNames:", strPath);
            File dir = new File(strPath);//文件夹dir
            if (!dir.exists() || !dir.isDirectory()) {
                // dir.mkdirs();
                return fileLists;
            }
            Log.e("文件夹名字:", dir.getName());
            File[] files = dir.listFiles();//文件夹下的所有文件或文件夹
            if (files == null) {
                return fileLists;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) { //是一个目录
                    fileLists.add(files[i].getName());
                }
            }
        } catch (Exception e) {
            LogUtil.e("readFileLists", e.toString());
            e.printStackTrace();
        }
        return fileLists;
    }

    /**
     * 文件的写入
     * 传入一个文件的名称和一个Bitmap对象
     * 最后的结果是保存一个图片
     */
    public static void comparesBitmap(String path, String key, Bitmap bitmap) {

        try {
            File file = new File(path + key);
            if (!file.exists()) {
                file.createNewFile();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
            int options = 100;
            while (baos.toByteArray().length / 1024 > 1024) { // 循环判断如果压缩后图片是否大于500kb,大于继续压缩
                baos.reset();// 重置baos即清空baos
                options -= 10;// 每次都减少10
                if (options < 0) {
                    options = 5;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                    break;
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中

            }
            FileOutputStream out = new FileOutputStream(file);
            out.write(baos.toByteArray());
            out.flush();
            out.close();
            Uri uri = Uri.fromFile(file);
            UIUtils.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recycleBitmap(bitmap);
    }

    /**
     * 文件的写入
     * 传入一个文件的名称和一个Bitmap对象
     * 最后的结果是保存一个图片
     */
    public static void saveToSDCard(String path, Bitmap bitmap) {

        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            FileOutputStream out = new FileOutputStream(file);
            out.write(baos.toByteArray());
            out.flush();
            out.close();
            Uri uri = Uri.fromFile(file);
            UIUtils.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        recycleBitmap(bitmap);
    }

    public static void recycleBitmap(Bitmap... bitmaps) {
        if (bitmaps == null) {
            return;
        }
        for (Bitmap bm : bitmaps) {
            if (null != bm && !bm.isRecycled()) {
                bm.recycle();
            }
        }
    }

    /***
     * 保存到SD卡
     * @param path
     * @param key
     * @param bitmap
     */
    public static void savebitMapToSDCard(String path, String key, Bitmap bitmap) {
        FileOutputStream fos;
        try {
            File files = new File(path);
            if (!files.exists()) {
                files.mkdir();
            }
            File file = new File(path + key);
            fos = new FileOutputStream(file);
            //保存图片的设置，压缩图片
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();//关闭流
            bitmap.recycle();
            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            UIUtils.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件的读取，
     * 根据文件的名字，读取出一个Bitmap的对象，
     * 如果之前保存过就有值，否则是null
     */
    public static Bitmap readFromSDCard(String path, String key) {

        return BitmapFactory.decodeFile(path + key);
    }

    public static byte[] createBitmapThumbnail(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 32) { // 循环判断如果压缩后图片是否大于500kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            if (options < 0) {
                options = 5;
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                break;
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中

        }
        bitmap.recycle();
        return baos.toByteArray();
    }


}
