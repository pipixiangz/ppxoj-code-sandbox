package com.ppx.ppxojcodesandbox;

import com.ppx.ppxojcodesandbox.model.CodeSandboxCmd;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * cpp本机代码沙箱
 */
@Slf4j
public class CppNativeCodeSandbox extends CodeSandboxTemplate {

    // 定义 C++ 代码文件存放的前缀路径
    private static final String PREFIX = File.separator + "cpp";
    // 定义代码存放的全局目录路径
    private static final String GLOBAL_CODE_DIR_PATH = File.separator + "tempCode";
    // 定义 C++ 代码文件的名称
    private static final String GLOBAL_CPP_NAME = File.separator + "main.cpp";

    // 初始化父类中的路径信息
    public CppNativeCodeSandbox() {
        super.prefix = PREFIX;
        super.globalCodeDirPath = GLOBAL_CODE_DIR_PATH;
        super.globalCodeFileName = GLOBAL_CPP_NAME;
    }

    @Override
    public CodeSandboxCmd getCmd(String userCodeParentPath, String userCodePath) {
        return CodeSandboxCmd.builder()
                .compileCmd(String.format("g++ %s -o %s", userCodePath, userCodePath.substring(0, userCodePath.length() - 4)))
                .runCmd(userCodeParentPath + File.separator + "main")
                .build();
    }

    @Override
    public void runSandboxCmd(CodeSandboxCmd cmd) throws IOException, InterruptedException {
        // 编译代码
        Process compileProcess = Runtime.getRuntime().exec(cmd.getCompileCmd());
        compileProcess.waitFor();

        // 设置环境变量并运行代码
        ProcessBuilder processBuilder = new ProcessBuilder(cmd.getRunCmd());
        processBuilder.environment().put("LC_ALL", "en_US.UTF-8");
        processBuilder.environment().put("LANG", "en_US.UTF-8");
        Process runProcess = processBuilder.start();
        runProcess.waitFor();
    }
}
