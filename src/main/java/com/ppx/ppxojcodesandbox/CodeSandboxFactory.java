package com.ppx.ppxojcodesandbox;

import com.ppx.ppxojcodesandbox.model.enums.QuestionSubmitLanguageEnum;

public class CodeSandboxFactory {

    /**
     * 根据编程语言枚举值获取对应的代码沙箱实例
     *
     * @param language 编程语言枚举值
     * @return 对应的 CodeSandboxTemplate 实例
     */
    public static CodeSandboxTemplate getInstance(QuestionSubmitLanguageEnum language) {
        switch (language) {
            case JAVA:
                // return new JavaNativeCodeSandbox();
                return new CppNativeCodeSandbox();
            case CPP:
                return new CppNativeCodeSandbox();
            case PYTHON:
                // return new PythonNativeCodeSandbox();
                return new CppNativeCodeSandbox();
            default:
                throw new RuntimeException("暂不支持的编程语言: " + language);
        }
    }
}
