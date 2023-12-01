package com.hqx.nio.c1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @Description 文件夹及文件夹中的内容拷贝
 * @Create by hqx
 * @Date 2023/11/25 0:17
 */
public class TestFilesCopy {

    public static void main(String[] args) throws IOException {
        String source = "D:\\360\\test";
        String target = "D:\\360\\new_test";

        Files.walk(Paths.get(source)).forEach( path -> {
            try {
                // 拷贝目的路径
                String targetName = path.toString().replace(source, target);
                if (Files.isDirectory(path)) { // 是文件夹时
                    Files.createDirectory(Paths.get(targetName)); // 创建目拷贝目的路径文件夹
                } else if (Files.isRegularFile(path)) { // 是常规文件时
                    Files.copy(path, Paths.get(targetName));  // 拷贝文件到目的路径
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

}
