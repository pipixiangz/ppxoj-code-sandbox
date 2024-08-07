package com.ppx.ppxojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ppx.ppxojcodesandbox.model.*;
import com.ppx.ppxojcodesandbox.model.enums.JudgeInfoMessageEnum;
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
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class CodeSandboxTemplate implements CodeSandbox {

    protected String prefix;
    protected String globalCodeDirPath;
    protected String globalCodeFileName;

    // 使用常量替代魔法数字
    private static final long DEFAULT_TIMEOUT_MS = 10000L;
    private static final int TIMEOUT_EXIT_CODE = -10001;

    /**
     * 获取编译和运行的命令
     * @param userCodeParentPath 代码所在的父目录
     * @param userCodePath 代码所在目录
     * @return 编译和运行的命令
     */
    protected abstract CodeSandboxCmd getCmd(String userCodeParentPath, String userCodePath);

    /**
     * 保存代码到文件
     * @param code 代码内容
     * @return 保存代码的文件
     */
    private File saveCodeToFile(String code) {
        String globalCodePath = System.getProperty("user.dir") + globalCodeDirPath;
        FileUtil.mkdir(globalCodePath);

        String userCodeParentPath = globalCodePath + prefix + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + globalCodeFileName;
        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    /**
     * 编译代码
     * @param compileCmd 编译命令
     * @return 编译结果
     * @throws IOException 编译过程中的IO异常
     */
    private ExecuteMessage compileCode(String compileCmd) throws IOException {
        Process compileProcess = Runtime.getRuntime().exec(compileCmd);
        return ProcessUtil.handleProcessMessage(compileProcess, "编译");
    }

    /**
     * 运行代码
     * @param inputList 输入用例列表
     * @param runCmd 运行命令
     * @return 运行结果列表
     */
    private List<ExecuteMessage> runCode(List<String> inputList, String runCmd) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                ExecuteMessage executeMessage = runSingleCase(runProcess, input);
                executeMessageList.add(executeMessage);

                if (executeMessage.getExitValue() != 0) {
                    break;
                }
            } catch (IOException | InterruptedException e) {
                log.error("运行代码出错", e);
                executeMessageList.add(createErrorMessage(e));
                break;
            }
        }
        return executeMessageList;
    }

    /**
     * 运行单个测试用例
     * @param runProcess 运行进程
     * @param input 输入
     * @return 运行结果
     */
    private ExecuteMessage runSingleCase(Process runProcess, String input) throws InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ExecuteMessage executeMessage = ProcessUtil.handleProcessInteraction(runProcess, input, "运行");

        boolean finished = runProcess.waitFor(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        stopWatch.stop();

        if (!finished) {
            runProcess.destroy();
            return ExecuteMessage.builder()
                    .exitValue(TIMEOUT_EXIT_CODE)
                    .errorMessage("超时")
                    .time(DEFAULT_TIMEOUT_MS)
                    .build();
        }

        executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        return executeMessage;
    }

    @Override
    public final ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        File userCodeFile = saveCodeToFile(executeCodeRequest.getCode());
        String userCodePath = userCodeFile.getAbsolutePath();
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        try {
            CodeSandboxCmd cmd = getCmd(userCodeParentPath, userCodePath);
            ExecuteMessage compileMessage = compileCode(cmd.getCompileCmd());

            if (compileMessage.getExitValue() != 0) {
                return createErrorResponse(2, "编译错误", JudgeInfoMessageEnum.COMPILE_ERROR);
            }

            List<ExecuteMessage> runMessages = runCode(executeCodeRequest.getInputList(), cmd.getRunCmd());
            return processRunResults(runMessages);
        } catch (Exception e) {
            log.error("执行代码出错", e);
            return createErrorResponse(2, e.getMessage(), JudgeInfoMessageEnum.SYSTEM_ERROR);
        } finally {
            FileUtil.del(userCodeParentPath);
        }
    }

    /**
     * 处理运行结果
     * @param executeMessageList 运行结果列表
     * @return 执行代码的响应
     */
    private ExecuteCodeResponse processRunResults(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setStatus(1);


        JudgeInfo judgeInfo = new JudgeInfo();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0;

        for (ExecuteMessage executeMessage : executeMessageList) {
            if (executeMessage.getExitValue() == 0) {
                outputList.add(executeMessage.getMessage());
                maxTime = Math.max(maxTime, executeMessage.getTime());
            } else {
                return createErrorResponse(3, executeMessage.getErrorMessage(),
                        executeMessage.getExitValue() == TIMEOUT_EXIT_CODE ?
                                JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED :
                                JudgeInfoMessageEnum.RUNTIME_ERROR);
            }
        }

        judgeInfo.setTime(maxTime);
        response.setJudgeInfo(judgeInfo);
        response.setOutputList(outputList);
        return response;
    }

    /**
     * 创建错误响应
     * @param status 状态码
     * @param message 错误消息
     * @param resultType 结果类型
     * @return 错误响应
     */
    private ExecuteCodeResponse createErrorResponse(int status, String message, JudgeInfoMessageEnum resultType) {
        return ExecuteCodeResponse.builder()
                .status(status)
                .message(message)
                .outputList(new ArrayList<>())
                .judgeInfo(new JudgeInfo())
                .build();
    }

    /**
     * 创建错误消息
     * @param e 异常
     * @return 错误消息
     */
    private ExecuteMessage createErrorMessage(Exception e) {
        return ExecuteMessage.builder()
                .exitValue(-1)
                .errorMessage(e.getMessage())
                .build();
    }
}
