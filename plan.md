# 这是一个进度记忆体

这份文件的用途：
- 给新会话快速续接项目上下文
- 记录这个项目已经做到哪里
- 记录下一步应该做什么
- 避免每次重新解释项目背景

如果你在新会话里看到这份文件，应当把它当成当前项目的“编程进度记忆体”来使用。

---

## 一、项目本质

这个项目的本质不是单纯把功能做出来，而是：

**我想学习如何在 WSL + VS Code 环境下，从 0 搭一个 Java + Spring Boot 的 AI 问答系统。**

所以后续协作时要遵循这些原则：
- 优先按“适合学习”的方式推进，而不是一味追求最短代码
- 每一步尽量保持结构清晰，方便理解
- 新增功能时，最好能看出分层、职责和演进路径
- 如果要改动，尽量基于当前项目继续迭代，不要轻易推倒重来

---

## 二、当前开发环境

- 开发环境：WSL + VS Code Remote
- 项目目录：`/root/workspace/ai-qa-system`
- 代码仓库：`https://github.com/moshinian/ai-qa-system.git`
- Java：17
- 框架：Spring Boot 3.3.5
- 构建工具：Maven

说明：
- 当前项目根目录里没有 `mvnw`，所以本地构建依赖系统里的 `mvn`
- 当前会话里尝试过执行 `mvn -q -DskipTests package`
- 这次构建没有完成，不是因为 Java 代码先报错，而是因为 Maven 插件下载/本地仓库写入受限，无法据此完全确认当前代码编译通过

---

## 三、当前目标阶段判断

这个项目已经不只是“RAG 雏形”了。

更准确地说，当前处于：

**阶段 3 早期：本地知识库检索 + Prompt 组装 + OpenAI 兼容接口调用雏形**

也就是说，主链路已经开始从：

用户提问 -> 模拟回答

演进为：

用户提问 -> 本地知识库检索 -> 拼接 Prompt -> 调用 LLM（若配置了 API Key）-> 返回结果

---

## 四、当前代码实际已完成内容

以下内容是根据当前代码仓库实际文件确认的。

### 1. Spring Boot 基础项目已经搭起来

已存在启动类：
- `src/main/java/com/example/aiqa/AiQaApplication.java`

已启用：
- `@SpringBootApplication`
- `@EnableConfigurationProperties(LlmProperties.class)`

### 2. 基础接口已经存在

已实现：
- `GET /ping`
- `POST /api/chat/ask`

对应文件：
- `src/main/java/com/example/aiqa/controller/PingController.java`
- `src/main/java/com/example/aiqa/controller/ChatController.java`

### 3. 统一返回结构和基础 DTO 已完成

已存在：
- `ChatRequest`
- `ChatResponse`
- `Result<T>`

对应文件：
- `src/main/java/com/example/aiqa/dto/ChatRequest.java`
- `src/main/java/com/example/aiqa/dto/ChatResponse.java`
- `src/main/java/com/example/aiqa/dto/Result.java`

当前接口调用大致是：
- 前端或 Postman 发 `question`
- Controller 调用 `RagChatService`
- 返回 `Result.success(new ChatResponse(answer))`

### 4. 本地知识库文件已经存在

当前知识库目录：
- `src/main/resources/knowledge/`

已有知识文件：
- `springboot.txt`
- `redis.txt`
- `rag.txt`

说明：
- 现在是最基础的本地 txt 知识库
- 适合学习 RAG 最小闭环
- 还没有引入向量数据库、Embedding 或更复杂的检索机制

### 5. 知识加载与切分已经完成

已存在：
- `KnowledgeLoader`
- `TextSplitter`
- `KnowledgeChunk`

对应职责：
- `KnowledgeLoader`：启动时加载 `knowledge/*.txt`
- `TextSplitter`：按固定长度切分文本
- `KnowledgeChunk`：表示知识分块

当前实现特点：
- 在 `@PostConstruct` 阶段加载知识库
- 使用固定长度切块，当前切块长度写的是 `120`
- 切分策略比较朴素，是按字符长度切，不是按语义切

### 6. 关键词检索版 Retriever 已完成

已存在：
- `SimpleKeywordRetriever`

当前检索方式：
- 从问题里提取关键词
- 对每个 chunk 做包含匹配
- 按命中数量排序
- 取 Top K（当前 `RagChatService` 里传的是 `3`）

这说明：
- RAG 的“检索”部分已经有最小可运行版本
- 但它还是非常基础的关键词召回，不是向量检索

### 7. RAG 服务主流程已经串起来

已存在：
- `src/main/java/com/example/aiqa/service/RagChatService.java`

当前逻辑：
1. 接收问题
2. 调用 `SimpleKeywordRetriever.retrieveTopK(question, 3)`
3. 如果没有命中，返回“知识库中未找到相关信息。”
4. 如果命中，拼接知识上下文
5. 构造 Prompt
6. 调用 `LlmClient.chat(prompt)`
7. 返回结果

这意味着：
- 目前已经不是单纯 mock 接口
- 已经形成了最小版“检索增强问答”主链路

### 8. LLM 配置类已经存在

已存在：
- `src/main/java/com/example/aiqa/config/LlmProperties.java`

当前字段包括：
- `baseUrl`
- `apiKey`
- `model`
- `chatPath`
- `temperature`
- `connectTimeout`
- `readTimeout`

说明：
- 已经开始把大模型调用配置化
- 配置方式是典型的 OpenAI 兼容接口写法

### 9. LLM 客户端雏形已经存在

已存在：
- `src/main/java/com/example/aiqa/client/LlmClient.java`

当前行为：
- 如果没有配置 `LLM_API_KEY`，返回调试文本和 Prompt
- 如果配置了 `LLM_API_KEY`，调用 `base-url + chat-path`
- 使用 `RestTemplate` 发起 POST 请求
- 按 OpenAI Chat Completions 风格解析响应
- 从 `choices[0].message.content` 里取答案

这说明：
- “接真实模型”的第一版代码已经写出来了
- 当前设计目标是先跑通，再逐步增强健壮性

### 10. application.yml 已加入 llm 配置

文件：
- `src/main/resources/application.yml`

当前已有配置：

```yaml
server:
  port: 8080

llm:
  base-url: ${LLM_BASE_URL:https://api.openai.com}
  api-key: ${LLM_API_KEY:}
  model: ${LLM_MODEL:gpt-4o-mini}
  chat-path: /v1/chat/completions
  temperature: 0.2
  connect-timeout: 5000
  read-timeout: 30000
```

说明：
- 配置未写死，支持环境变量覆盖
- 适合作为学习 OpenAI 兼容接口接入的第一版

---

## 五、当前项目“已经做了”与“还没做完”的边界

### 已经做了

- Spring Boot 项目初始化
- 基础 Controller 和 DTO
- `/ping` 健康检查接口
- `/api/chat/ask` 问答接口
- 本地 txt 知识库
- 知识加载
- 文本切块
- 关键词检索
- Prompt 拼接
- LLM 配置类
- LLM 客户端第一版
- 当未配置 API Key 时返回调试 Prompt 的机制

### 还没做完

- 没有确认当前项目在本机环境下一定能完整构建成功
- 没有测试类或自动化测试
- 没有全局异常处理
- 没有请求参数校验，比如空问题、超长问题
- 没有区分“检索失败”“模型失败”“配置缺失”等错误类型
- `LlmClient` 目前直接返回字符串，缺少更清晰的错误建模
- 没有日志体系，仍然有 `System.out.println`
- 检索逻辑仍然非常基础，只是关键词匹配
- 文本切分逻辑仍然非常基础，按字符长度硬切
- 没有会话记忆
- 没有前端页面
- 没有数据库
- 没有向量库
- 没有 Embedding
- 没有对 Prompt 结果做结构化管理
- 没有对配置做更严格的校验和说明

---

## 六、当前代码状态的准确判断

如果在新会话中需要一句话概括当前进度，可以直接使用下面这句：

**这个项目已经完成了 Java + Spring Boot AI 问答系统的最小后端骨架，并且已经具备“本地知识库检索 + Prompt 组装 + OpenAI 兼容接口调用雏形”，但还缺少测试、异常处理、健壮性和更真实的 RAG 能力。**

---

## 七、下一步建议优先级

如果继续往下做，建议按这个顺序推进：

### 优先级 1：先把当前主链路验证稳

建议先做：
- 确认项目可以正常启动和请求
- 用未配置 `LLM_API_KEY` 的模式手动验证接口输出
- 如果需要，再配置真实模型 Key 验证完整链路

目标：
- 确认“检索 + Prompt + LLM 调用”主流程真正跑通

### 优先级 2：补基础工程能力

建议新增：
- 参数校验
- 全局异常处理
- 更明确的错误返回
- 日志替代 `System.out.println`

目标：
- 让这个项目从“能跑”变成“结构更像正式后端项目”

### 优先级 3：把 RAG 做得更像 RAG

后续可做：
- 优化切块策略
- 优化关键词提取
- 增加更合理的召回/排序
- 再往后再考虑 Embedding 和向量检索

目标：
- 保持学习节奏，逐步理解 RAG 演进过程

---

## 八、后续协作提醒

后续如果我在新会话中说“先看 `context.md`”，应理解为：

- 先读取这份进度记忆体
- 再检查代码仓库实际状态
- 优先根据“学习项目”的目标来给建议或写代码
- 明确区分哪些是已经完成，哪些是计划中但还未真正落地

如果发现代码状态和这份记忆体不一致，应当：
- 以当前仓库实际代码为准
- 顺手更新这份 `context.md`

---

## 九、当前最值得记住的一句话

**这是一个以学习为核心的项目：目标是在 WSL + VS Code 中，从 0 走通 Java + Spring Boot AI 问答系统，并逐步理解 Spring Boot、接口设计、RAG、LLM 接入和工程化演进。**
