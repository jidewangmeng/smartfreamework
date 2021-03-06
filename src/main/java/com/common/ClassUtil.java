package com.common;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by wm on 2017/8/3.
 */
public final class ClassUtil {


    /**
     * 获取类加载器
     *
     * @return
     */
    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 加载类
     *
     * @param className
     * @param isInitialized
     * @return
     */
    public static Class<?> loadClass(String className, boolean isInitialized) {

        Class<?> cls = null;

        try {

            cls = Class.forName(className, isInitialized, getClassLoader());
        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        }

        return cls;
    }

    /**
     * 加载指定包下的所有类
     *
     * @param packageName
     * @return
     */
    public static Set<Class<?>> getClassSet(String packageName) {

        Set<Class<?>> classSet = new HashSet<Class<?>>();

        try {
            Enumeration<URL> urls = getClassLoader().getResources(packageName.replace(".", "/"));
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();

                if (url != null) {

                    String protool = url.getProtocol();
                    if (protool.equals("file")) {

                        //TODO   ????
                        String packagePath = url.getPath().replaceAll("%20", "");

                        packagePath = packagePath.substring(0, packagePath.length() - 1);
                        addClass(classSet, packagePath, packageName);

                    } else if (protool.equals("jar")) {

                        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                        if (jarURLConnection != null) {

                            JarFile jarFile = jarURLConnection.getJarFile();
                            if (jarFile != null) {
                                Enumeration<JarEntry> jarEntries = jarFile.entries();

                                while (jarEntries.hasMoreElements()) {
                                    JarEntry jarEntry = jarEntries.nextElement();

                                    String jarEntryName = jarEntry.getName();

                                    if (jarEntryName.endsWith(".class")) {

                                        String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                                        doAddClass(classSet, className);
                                    }

                                }

                            }

                        }

                    }

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return classSet;

    }

    private static void addClass(Set<Class<?>> classSet, String packagePath, String packageName) {


        File[] files = new File(packagePath).listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(".class") || file.isDirectory());
            }
        });

        for (File file : files) {

            String fileName = file.getName();
            //文件
            if (file.isFile()) {
                String className = fileName.substring(0, fileName.lastIndexOf("."));
                if (packageName != null && !packageName.equals("")) {

                    className = packageName + "." + className;
                }
                doAddClass(classSet, className);
            } else {
                //目录
                String subPackagePath = fileName;
                if (packagePath != null && !packagePath.equals("")) {

                    subPackagePath = packagePath + "/" + subPackagePath;

                }
                String subPackageName = fileName;
                if (packageName != null && !packageName.equals("")) {

                    subPackageName = packageName + "." + subPackageName;
                }

                addClass(classSet, subPackagePath, subPackageName);

            }
        }


    }

    private static void doAddClass(Set<Class<?>> classSet, String className) {
        System.out.println("classSet:"+className);
        Class<?> cls = loadClass(className, false);
        classSet.add(cls);

    }


}
