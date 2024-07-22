package com.ppx.ppxojcodesandbox.unsafe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 向服务器写文件（植入危险程序）
 */
public class WriteFileError {

    public static void main(String[] args) throws InterruptedException, IOException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/危险程序.sh";
        // 将java -version的输出（包括任何错误消息）都显示在标准输出中。
        String errorProgram = "java -version 2>&1";
        // 将errorProgram写入filePath文件中
        Files.write(Paths.get(filePath), Arrays.asList(errorProgram));
        System.out.println("危险程序植入成功，嘿嘿嘿");
    }
}
