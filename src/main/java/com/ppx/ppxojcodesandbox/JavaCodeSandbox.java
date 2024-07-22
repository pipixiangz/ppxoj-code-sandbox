package com.ppx.ppxojcodesandbox;

import com.ppx.ppxojcodesandbox.model.ExecuteCodeRequest;
import com.ppx.ppxojcodesandbox.model.ExecuteCodeResponse;

import java.io.File;

/**
 * Java 原生代码沙箱实现（可以直接复用模板方法）
 */
public class JavaCodeSandbox extends JavaCodeSandboxTemplate {

    // 覆盖父类的方法
    @Override
    public File saveCodeToFile(String code) {
        File file = super.saveCodeToFile(code);
        System.out.println("Save code to file: " + file.getAbsolutePath());
        return file;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
