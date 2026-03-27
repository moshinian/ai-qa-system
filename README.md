# AI QA System

基于 Java + Spring Boot 的 AI 智能问答系统。

## 当前进度
- Spring Boot 项目初始化
- `/ping` 健康检查接口
- `/api/chat/ask` 问答接口
- 本地知识库检索 + Prompt 拼接
- DeepSeek Chat API 接入雏形
- 已补充基础异常日志和全局异常处理雏形

## 技术栈
- Java 17
- Spring Boot 3
- Maven

## 目录结构

`src/main/java/com/example/aiqa` 当前建议按职责拆分：

- `controller`：对外提供 HTTP 接口，只负责接收请求和返回结果
- `service`：组织业务主流程，例如问答链路编排
- `client`：封装第三方能力调用，例如 DeepSeek API
- `config`：配置类和配置绑定，例如 `LlmProperties`
- `handler`：全局异常处理，不和 `controller` 混放
- `dto`：接口请求体、响应体、统一返回结构
- `rag/loader`：知识库加载
- `rag/splitter`：文本切分
- `rag/retriever`：检索逻辑
- `rag/model`：RAG 过程中的模型对象

当前分层调用关系建议保持为：

`controller -> service -> client / rag`

这样后续继续加参数校验、异常建模、检索策略时，结构不会乱。

## 配置说明
- 公共默认配置放在 `src/main/resources/application.yml`
- 本地私有配置放在 `src/main/resources/application-local.yml`
- `application-local.yml` 已加入 `.gitignore`，不要提交真实密钥

本地开发时，可在 `application-local.yml` 中填写：

```yaml
llm:
  base-url: https://api.deepseek.com
  chat-path: /chat/completions
  api-key: your-deepseek-api-key
  model: deepseek-chat
  system-prompt: 你是一个严谨的知识库问答助手。
```

## 启动方式

默认启动：

```bash
mvn spring-boot:run
```

使用本地配置文件启动：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 调试与排错

推荐在 VS Code 中调试以下位置：
- `ChatController.ask(...)`
- `RagChatService.ask(...)`
- `LlmClient.chat(...)`

当前 `LlmClient` 已增加这些日志：
- 请求 URL
- 模型名
- 失败时的 HTTP 状态码
- 失败时的响应体

如果接口调用失败，可优先在控制台查看日志关键字：
- `调用大模型，请求地址`
- `调用大模型失败`

项目中还加入了最小版全局异常处理：
- 未捕获异常会统一记录日志
- 接口会返回 `Result.fail(500, "服务器内部异常，请稍后重试")`
