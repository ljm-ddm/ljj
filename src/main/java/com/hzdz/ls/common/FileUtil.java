package com.hzdz.ls.common;

import java.io.*;
import java.util.Calendar;
import java.util.List;

/**
 * 文件上传工具类
 * 作者：江南一叶竹筏
 * 时间：2017/9/28
 */
public class FileUtil {

    //上传文件的最大长度
    public static long maxFileSize = 1024 * 1024 * 1024 * 2L;//2G


    /**
     * 通过输入流来上传文件
     * @param is
     * @param path
     * @return
     * @throws IOException
     */
    public static String upload4Stream(InputStream is, String path, String name) throws IOException {

        UploadState state = UploadState.UPLOAD_FAILURE;
        FileOutputStream fos = null;
        path = getPath(path);
        //截取后缀名
        name = getFilename() + name.substring(name.lastIndexOf("."), name.length());
        String fileName = path + name;
        mkFile(path, name);
        try {
            fos = new FileOutputStream(fileName);
            byte[] buffer = new byte[1024 * 1024];
            int len = 0;
            while((len = is.read(buffer)) > 0){
                fos.write(buffer, 0, len);
            }
            state = UploadState.UPLOAD_SUCCESS;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (is != null){
                is.close();
            }
            if (fos != null){
                fos.flush();
                fos.close();
            }
        }

        return name;

    }

    /**
     * 文件上传
     * @param file
     * @param path
     * @return
     * @throws IOException
     */
    public static String upload4Stream(File file, String path, String name) throws IOException {
        UploadState state = UploadState.UPLOAD_FAILURE;
        FileInputStream fis = null;
        String fileName = null;
        try {
            long size = file.length();
            if (size <= 0) {
                state = UploadState.UPLOAD_ZEROSIZE;
            } else {
                if (size <= maxFileSize) {
                    fis = new FileInputStream(file);
                    fileName  = upload4Stream(fis, name, getFilename()+path);
                } else {
                    state = UploadState.UPLOAD_OVERSIZE;
                }
            }
        } catch (FileNotFoundException e) {
            state = UploadState.UPLOAD_NOTFOUND;
            e.printStackTrace();
        } catch (IOException e) {
            state = UploadState.UPLOAD_FAILURE;
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return fileName;
    }

    private static String valueOfString(String str, int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len - str.length(); i++) {
            sb.append("0");
        }
        return (sb.length() == 0) ? (str) : (sb.toString() + str);
    }

    private static String getFilename(){
        StringBuffer fileName = new StringBuffer();
        Calendar calendar = Calendar.getInstance();
        fileName.append(String.valueOf(calendar.get(Calendar.YEAR)))
                .append(valueOfString(String.valueOf(calendar.get(Calendar.MONTH) + 1),2))
                .append(valueOfString(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)),2))
                .append(valueOfString(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)),2))
                .append(valueOfString(String.valueOf(calendar.get(Calendar.MINUTE)),2))
                .append(valueOfString(String.valueOf(calendar.get(Calendar.SECOND)),2))
                .append(valueOfString(String.valueOf(calendar.get(Calendar.MILLISECOND)),3));
        return fileName.toString() + NumberUtil.createNum(8);//时间加上8位随机数保证文件名的唯一

    }

    /**
     * 处理文件路径，得到系统的保存路径
     * @param path
     * @return
     */
    private static String  getPath(String path){
        path = path.replace("\\", "/");
        String lastChar = path.substring(path.length() - 1);
        if (!"/".equals(lastChar)){
            path += "/";
        }
        return path;
    }

    /**
     * 创建目录
     * @param path
     * @return
     */
    private static boolean mkFile(String path, String name) throws IOException {
        File file = null;
        try {
            file = new File(path,name);
            System.out.println(file.getAbsolutePath());
            if(!file.exists()){
                boolean f = file.getParentFile().mkdirs();
                file.createNewFile();
                System.out.println(file.getAbsolutePath());
                //return file.mkdirs();
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }finally {
            //file = null;
        }
        return true;
    }

    /**
     * 删除文件
     * @param path
     * @return
     */
    public static boolean delete(String path){
        boolean result = false;
        try {
            File file = new File(path);
            file.delete();
            result = true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 批量删除文件
     * @param pathList
     * @return
     */
    public static boolean batchDelete(List<String> pathList){
        boolean result = false;
        String path = null;
        try {
            for (int i = 0; i < pathList.size(); i++) {
                path = pathList.get(i);
                File file = new File(path);
                file.delete();
            }
            result = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static void main(){

    }

}
