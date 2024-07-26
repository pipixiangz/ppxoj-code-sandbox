# 皮皮码来判判 (Sandbox代码沙箱)
## ppx - online judge

基于Spring Boot + Spring Cloud 微服务+ Docker (+ Vue 3 + Arco Design)的编程题目在线评测系统。

Spring Boot + Spring Cloud Microservices + Docker (+ Vue 3 + Arco Design)

在系统前台，管理员可以创建、管理题目;用户可以自由搜索题目、阅读题目、编写并提交代码。

在系统后端，能够根据管理员设定的题目测试用例在自主实现的代码沙箱中对代码进行编译、运行、判断输出是否正确。其中，代码沙箱可以作为独立服务，提供给其他开发者使用。

Frontend: Administrators can create and manage problems, while users can freely search for problems, read problem statements, write and submit code.

项目前端Front-end：[项目前端](https://github.com/pipixiangz/ppxoj-code-sandbox)

Backend: The system can compile, run, and judge the correctness of the code based on test cases set by the administrators in a self-implemented code sandbox. This code sandbox can also be provided as an independent service for other developers to use.

后端微服务项目：[微服务项目](https://github.com/pipixiangz/ppxoj-backend-microservice)

![主页图片](https://github.com/pipixiangz/ppxoj-frontend/blob/main/imgs/mainPage.jpg)
![浏览题目](https://github.com/pipixiangz/ppxoj-frontend/blob/main/imgs/questionView.jpg)
![浏览提交题目](https://github.com/pipixiangz/ppxoj-frontend/blob/main/imgs/questionSubmitView.jpg)
![创建题目](https://github.com/pipixiangz/ppxoj-frontend/blob/main/imgs/createQuestionView.jpg)
![管理题目](https://github.com/pipixiangz/ppxoj-frontend/blob/main/imgs/manageQuestionView.jpg)
![做题页面](https://github.com/pipixiangz/ppxoj-frontend/blob/main/imgs/doQuestionView.png)
![关于](https://github.com/pipixiangz/ppxoj-frontend/blob/main/imgs/about.png)

自主设计判题机模块的架构，定义了代码沙箱的抽象调用接口和多种实现类（比如远程/本地代码沙箱），并通过静态工厂模式+Spring配置化的方式实现了对多种代码沙箱的灵活调用。

使用代理模式对代码沙箱接口进行能力增强，统一实现了对代码沙箱调用前后的日志记录，减少重复代码。

通过编写Java脚本自测代码沙箱，模拟了多种程序异常情况并针对性解决，如使用守护线程+Thread.sleep等待机制实现了对进程的超时中断、使用JVM -Xmx 参数限制用户程序占用的最大堆内存、使用黑白名单＋字典树的方式实现了对敏感操作的限制。

使用Java安全管理器和自定义的Security Manager对用户提交的代码进行权限控制，比如关闭写文件、执行文件权限，进一步提升了代码沙箱的安全性。

为保证沙箱宿主机的稳定性，选用Docker隔离用户代码，使用Docker Java库创建容器隔离执行代码，并通过tty和Docker进行传参交互，从而实现了更安全的代码沙箱。

使用虚拟机搭建Ubuntu Linux+Docker环境，并通过JetBrains Client连接虚拟机进行实时SSH远程开发，提高了开发效率。

由于Java原生和Docker代码沙箱的实现流程完全一致（编译、执行、获取输出、清理），选用模板方法模式定义了一套标准的流程并允许子类自行扩展部分流程，提高代码一致性并大幅简化冗余代码。

为防止用户恶意请求代码沙箱服务，（采用API签名认证的方式，）给调用方分配签名密钥，并通过校验请求头中的密钥保证了API调用安全。


### Judging Machine Module Design
A custom architecture for the judging machine module was designed, defining abstract interfaces for the code sandbox and various implementation classes (e.g., remote/local code sandboxes). Flexible invocation of different code sandboxes was achieved through a combination of the static factory pattern and Spring configuration.

### Enhanced Code Sandbox Interface
The proxy pattern was employed to enhance the capabilities of the code sandbox interface, standardizing pre- and post-invocation logging to reduce redundant code.

### Java Program Compilation and Execution
The exec method of the Java Runtime object was used to compile and execute Java programs. Execution results were obtained through the Process class’s input stream, realizing a native Java code sandbox.

### Code Sandbox Testing and Optimization
Custom Java scripts were written to test the code sandbox, simulating various program exceptions and addressing issues accordingly. For instance, a daemon thread and Thread.sleep mechanism were used for process timeout interruption, JVM -Xmx parameter was set to limit the maximum heap memory usage, and sensitive operations were restricted using a whitelist/blacklist and trie structure.

### Security Enhancements
A Java Security Manager and a custom Security Manager were used to enforce permission control on user-submitted code, such as disabling file write and execution permissions, enhancing the security of the code sandbox.

### Docker-Based Isolation
To ensure the stability of the sandbox host, Docker was used to isolate user code. The Docker Java library was utilized to create containers for isolated code execution, with parameters passed through tty and Docker, resulting in a more secure code sandbox.

### Development Environment
An Ubuntu Linux + Docker environment was set up on a virtual machine, with JetBrains Client used for real-time SSH remote development, improving development efficiency.

### Template Method Pattern
Given the identical process flow for both native Java and Docker code sandboxes (compilation, execution, output retrieval, cleanup), the template method pattern was adopted to define a standard process, allowing subclasses to extend specific parts of the process. This improved code consistency and significantly reduced redundant code.

### API Security
To prevent malicious requests to the code sandbox service, signature-based API authentication was implemented. A signature key was assigned to each caller, and requests were validated through the request header key to ensure secure API calls.
