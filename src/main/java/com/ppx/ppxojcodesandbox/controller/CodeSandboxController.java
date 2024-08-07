package com.ppx.ppxojcodesandbox.controller;

import com.ppx.ppxojcodesandbox.CodeSandboxFactory;
import com.ppx.ppxojcodesandbox.CodeSandboxTemplate;
import com.ppx.ppxojcodesandbox.model.ExecuteCodeRequest;
import com.ppx.ppxojcodesandbox.model.ExecuteCodeResponse;
import com.ppx.ppxojcodesandbox.model.enums.QuestionSubmitLanguageEnum;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代码沙箱控制器，为了未来多种代码实现的扩展，这里使用了工厂模式
 */
@RestController
@RequestMapping("/codesandbox")
public class CodeSandboxController {

    @PostMapping("/execute")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        CodeSandboxTemplate sandboxTemplate = CodeSandboxFactory.getInstance(QuestionSubmitLanguageEnum.valueOf(executeCodeRequest.getLanguage()));
        return sandboxTemplate.executeCode(executeCodeRequest);
    }
}