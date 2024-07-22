package com.ppx.ppxojcodesandbox;


import com.ppx.ppxojcodesandbox.model.ExecuteCodeRequest;
import com.ppx.ppxojcodesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {
    /**
     * 代码沙箱接口定义
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
