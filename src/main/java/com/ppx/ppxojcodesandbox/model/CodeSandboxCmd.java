package com.ppx.ppxojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
/**
 * 代码沙箱命令
 */
public class CodeSandboxCmd {
    // 编译命令
    private String compileCmd;
    // 运行命令
    private String runCmd;
}