package com.ppx.ppxojcodesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 */
@Data
public class ExecuteMessage {

    // 程序退出码
    // 0：表示程序成功执行，没有遇到任何错误。
    // 非零值：表示程序遇到错误或异常，具体的非零值可以用来区分不同类型的错误。
    private Integer exitValue;

    // 正常输出
    private String message;

    // 错误输出
    private String errorMessage;

    // 运行时间
    private Long time;

    // 内存
    private Long memory;
}

