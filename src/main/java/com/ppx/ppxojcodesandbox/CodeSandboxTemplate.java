package com.ppx.ppxojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ppx.ppxojcodesandbox.model.*;
import com.ppx.ppxojcodesandbox.utils.ProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * 代码沙箱模板
 */
@Slf4j
public abstract class CodeSandboxTemplate implements CodeSandbox {

    String prefix;
    String globalCodeDirPath;
    String globalCodeFileName;

    /**
     * 超时时间，超过10秒则结束
     */
    public static final Long DEFAULT_TIME_OUT = 10000L;

    /**
     * 每个实现类必须实现编译以及运行的cmd
     *
     * @param userCodeParentPath 代码所在的父目录
     * @param userCodePath 代码所在目录
     * @return
     */
    abstract CodeSandboxCmd getCmd(String userCodeParentPath, String userCodePath);

    /**
     * 每个实现类可以选择性实现运行沙箱命令的方法
     *
     * @param cmd 编译和运行命令
     * @throws IOException
     * @throws InterruptedException
     */
    public void runSandboxCmd(CodeSandboxCmd cmd) throws IOException, InterruptedException {
        // 默认实现为空，子类可以选择性地覆盖这个方法
    }

    /**
     * 保存代码到文件中，不同编程语言要放到不同文件夹中
     * 保存到文件中的格式为: UUID/代码文件
     *
     * @param code 代码
     * @return
     */
    private File saveCodeToFile(String code) {
        String globalCodePath = System.getProperty("user.dir") + globalCodeDirPath;
        if (!FileUtil.exist(globalCodePath)) {
            FileUtil.mkdir(globalCodePath);
        }

        // 存放用户代码
        String userCodeParentPath = globalCodePath + prefix + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + globalCodeFileName;

        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    /**
     * 编译代码，返回编译的信息
     *
     * @param compileCmd 编译命令
     * @return
     * @throws IOException IOException
     */
    private ExecuteMessage compileCode(String compileCmd) throws IOException {
        Process compileProcess = Runtime.getRuntime().exec(compileCmd);
        return ProcessUtil.handleProcessMessage(compileProcess, "编译");
    }

    /**
     * 运行代码
     *
     * @param inputList 输入用例
     * @param runCmd 运行的cmd
     * @return
     * @throws RuntimeException RuntimeException
     */
    private List<ExecuteMessage> runCode(List<String> inputList, String runCmd) throws RuntimeException {
        List<ExecuteMessage> executeMessageList = new LinkedList<>();
        for (String input : inputList) {
            Process runProcess;
            try {
                runProcess = Runtime.getRuntime().exec(runCmd);
                new Thread(() -> {
                    try {
                        Thread.sleep(DEFAULT_TIME_OUT);
                        log.info("超时了，中断");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            ExecuteMessage executeMessage = ProcessUtil.handleProcessInteraction(runProcess, input, "运行");
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
            executeMessageList.add(executeMessage);
        }
        return executeMessageList;
    }

    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    @Override
    public final ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        // 保存代码
        File userCodeFile = saveCodeToFile(code);
        String userCodePath = userCodeFile.getAbsolutePath();
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        CodeSandboxCmd cmd = getCmd(userCodeParentPath, userCodePath);
        String compileCmd = cmd.getCompileCmd();
        String runCmd = cmd.getRunCmd();

        // 编译代码
        try {
            ExecuteMessage executeMessage = compileCode(compileCmd);
            if (executeMessage.getExitValue() != 0) {
                FileUtil.del(userCodeParentPath);
                return ExecuteCodeResponse.builder().status(2).message("编译错误").build();
            }
        } catch (IOException e) {
            FileUtil.del(userCodeParentPath);
            return errorResponse(e);
        }

        // 执行代码
        try {
            runSandboxCmd(cmd);
            List<ExecuteMessage> executeMessageList = runCode(inputList, runCmd);

            // 返回处理结果
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(1);
            JudgeInfo judgeInfo = new JudgeInfo();
            executeCodeResponse.setJudgeInfo(judgeInfo);
            List<String> outputList = new LinkedList<>();
            long maxTime = 0;
            for (ExecuteMessage executeMessage : executeMessageList) {
                if (ObjectUtil.equal(0, executeMessage.getExitValue())) {
                    outputList.add(executeMessage.getMessage());
                } else {
                    executeCodeResponse.setMessage(executeMessage.getErrorMessage());
                    executeCodeResponse.setStatus(3);
                    break;
                }
                maxTime = Math.max(maxTime, executeMessage.getTime());
            }
            judgeInfo.setTime(maxTime);
            executeCodeResponse.setOutputList(outputList);
            FileUtil.del(userCodeParentPath);
            return executeCodeResponse;
        } catch (RuntimeException | IOException | InterruptedException e) {
            FileUtil.del(userCodeParentPath);
            return errorResponse(e);
        }
    }

    /**
     * 错误返回
     * @param e
     * @return
     */
    private ExecuteCodeResponse errorResponse(Throwable e) {
        return ExecuteCodeResponse.builder().outputList(new ArrayList<>()).message(e.getMessage()).judgeInfo(new JudgeInfo()).status(2).build();
    }
}
