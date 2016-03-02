package com.newer.listview.com.newer.file;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


/**
 * 实现文件以及文件夹的操作
 * Created by mine on 15-11-15.
 */
public class FileOperator {

    private static int counter=0;   //文件夹复制计数器

    /**
     * 文件以及文件夹的删除
     *
     * @param f
     */
    public static void remove(File f) {
        if (f.isFile()) {
            f.delete();
            return;
        } else {
            File[] files = f.listFiles();
            if (null == files || files.length == 0) {
                f.delete();
            } else {
                for (File file : files) {
                    remove(file);
                }
            }
        }
        f.delete();
    }

    /**
     * 复制一个文件到指定文件夹，若文件已存在，则创建副本
     *
     * @param sourceFile   源文件
     * @param targetFolder 目标文件夹
     */
    public static void copyFile(File sourceFile, File targetFolder) throws Exception {
        //出错处理
        if (sourceFile.isDirectory() || targetFolder.isFile()) {
            throw new Exception("参数错误");
        }
        if (!sourceFile.exists() || !targetFolder.exists()) {
            throw new Exception("文件或文件夹不存在");
        }

        //新建文件输入流并对它进行缓冲
        FileInputStream in = new FileInputStream(sourceFile);
        BufferedInputStream inBuff = new BufferedInputStream(in);

        //创建文件（存在则创建副本）
        String fileName = sourceFile.getName();
        String fileFirstName = fileName.substring(0, fileName.lastIndexOf("."));
        String fileLastName = fileName.substring(fileName.lastIndexOf(".") + 1);
        File newFile;
        boolean isExit = false;
        for (File file : targetFolder.listFiles()) {
            if (file.getName().equals(fileName)) {
                isExit = true;
            }
        }
        if (isExit) {
            newFile = creatNewFile(targetFolder, fileFirstName, fileLastName, 0);
        } else {
            newFile = new File(targetFolder.getPath(), fileName);
        }

        //新建文件输出流并对它进行缓冲
        FileOutputStream out = new FileOutputStream(newFile);
        BufferedOutputStream outBuff = new BufferedOutputStream(out);

        //缓冲数组
        byte[] b = new byte[1024 * 5];
        int len;
        while ((len = inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }
        //刷新此缓冲的输出流
        outBuff.flush();

        //关闭流
        inBuff.close();
        outBuff.close();
        out.close();
        in.close();
    }

    public static void copyFolder(File sourceFolder, File targetFolder) throws Exception {
        //出错处理
        if (!sourceFolder.isDirectory() && !targetFolder.isDirectory()) {
            throw new Exception("参数错误");
        }
        if (!sourceFolder.exists() && !targetFolder.exists()) {
            throw new Exception("文件夹不存在");
        }

        //创建同名文件夹
        File newFolder;
        boolean isExist=false;
        for(File f:targetFolder.listFiles()){
            if(f.getName().equals(sourceFolder.getName())){
                isExist=true;
            }
        }
        if(isExist){
            newFolder=createNewFolder(sourceFolder,targetFolder,0);
        }else{
            newFolder=new File(targetFolder,sourceFolder.getName());
            newFolder.mkdir();
        }

        Log.d("正在复制文件夹" + (++counter) + ":", newFolder.getName());

        //复制源文件夹下的文件列表
        for(File f:sourceFolder.listFiles()){
            if(f.isFile()){
                Log.d("正在复制文件" + (++counter) + ":", f.getName());

                copyFile(f,newFolder);
            }else{
                copyFolder(f,newFolder);
            }
        }
    }

    /**
     * 智能复制文件副本
     *
     * @param folder    父文件对象
     * @param firstName 文件名（不含类型）
     * @param lastName  文件类型
     * @param i         副本参数
     * @return 返回创建的文件对象
     */
    private static File creatNewFile(File folder, String firstName, String lastName, int i) {
        File newFile = null;
        boolean isExit = false;
        for (File file : folder.listFiles()) {
            if (i == 0) {
                if (file.getName().equals(firstName + "_(副本)." + lastName)) {
                    isExit = true;
                }
            } else {
                if (file.getName().equals(firstName + "_(副本" + i + ")." + lastName)) {
                    isExit = true;
                }
            }
        }
        if (isExit) {
            return creatNewFile(folder, firstName, lastName, ++i);
        } else {
            if (i == 0) {
                newFile = new File(folder.getPath(), firstName + "_(副本)." + lastName);
                return newFile;
            } else {
                newFile = new File(folder.getPath(), firstName + "_(副本" + i + ")." + lastName);
                return newFile;
            }
        }
    }

    /**
     * 智能创建一个文件夹副本，
     *
     * @param sourceFolder 源文件夹
     * @param targetFolder 目标文件夹
     * @param i=0          副本参数
     * @return 返回一个新创建的文件夹对象
     */
    private static File createNewFolder(File sourceFolder, File targetFolder, int i) {
        String fileName = sourceFolder.getName();
        File newFolder = null;
        boolean isExist = false;
        for (File f : targetFolder.listFiles()) {
            if (i == 0) {
                if (f.getName().equals(fileName + "_(副本)")) {
                    isExist = true;
                }
            } else {
                if (f.getName().equals(fileName + "_(副本" + i + ")")) {
                    isExist = true;
                }
            }
        }
        if (isExist) {
            return createNewFolder(sourceFolder, targetFolder, ++i);
        } else {
            if (i == 0) {
                newFolder = new File(targetFolder, fileName + "_(副本)");
                newFolder.mkdir();
                return newFolder;
            } else {
                newFolder = new File(targetFolder, fileName + "_(副本" + i + ")");
                newFolder.mkdir();
                return newFolder;
            }
        }
    }

   /* *//**
     *  获得文件创建时间
     * @param filePath
     * @return
     *//*
    public static String getCreateTime(String filePath) {
        String strTime = null;
        try {
            Process p = Runtime.getRuntime().exec("cmd /C dir " + filePath + "/tc");
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.endsWith(".txt")) {
                    strTime = line.substring(0, 17);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strTime;
    }*/

}
