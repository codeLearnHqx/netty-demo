package com.hqx.nio.c1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 遍历目录文件
 * @Create by hqx
 * @Date 2023/11/24 23:23
 */
public class TestFileWalkFileTree {


    public static void main(String[] args) throws IOException {

        Files.walkFileTree(Paths.get("D:\\360\\test"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("====> 进入目录前：" + dir);
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("====> 遍历目录中：" + file);
                Files.delete(file); // 删除文件夹中的文件
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                System.out.println("====> 退出目录后：" + dir);
                Files.delete(dir); // 删除文件夹
                return super.postVisitDirectory(dir, exc);
            }
        });

    }

    private static void m2() throws IOException {
        AtomicInteger jarCount = new AtomicInteger();
        Files.walkFileTree(Paths.get("C:\\Program Files\\Java\\jdk1.8.0_152"), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".jar")) {
                    System.out.println(file);
                    jarCount.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }
        });

        System.out.println("jar count: " + jarCount);
    }

    private static void m1() throws IOException {
        // 用 int count = 0; 来做计数器不能在内部类中使用，
        // 因为在内部类中使用时相当于 final int count = 0; 局部变量会被自动加上 final
        // 引用类型的局部变量在 被加上 final 之后再内部类中也能正常使用
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(Paths.get("D:\\浏览器"), new SimpleFileVisitor<Path>() {
            /*
             * 遍历文件目录前
             */
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("=====>" + dir);
                dirCount.incrementAndGet(); // 加1
                return super.preVisitDirectory(dir, attrs);
            }
            /*
             * 遍历文件目录时
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("=====>" + file);
                fileCount.incrementAndGet(); // 加1
                return super.visitFile(file, attrs);
            }
        });

        System.out.println("dir count: " + dirCount);
        System.out.println("file count: " + fileCount);
    }

}
