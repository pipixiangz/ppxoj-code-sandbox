package com.ppx.ppxojcodesandbox;

import com.ppx.ppxojcodesandbox.model.ExecuteCodeRequest;
import com.ppx.ppxojcodesandbox.model.ExecuteCodeResponse;

public abstract class JavaCodeSandboxTemplate implements CodeSandbox{
    
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest){
        return null;
    }
}