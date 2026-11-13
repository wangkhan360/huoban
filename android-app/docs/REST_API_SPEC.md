# 成长萌宠 V3.0｜Android 学生端 REST API 接口定义

- **Base URL**: `https://api.growthpet.example.com/v3/`
- **鉴权方式**: 设备绑定后由家长核验接口下发短期 Student JWT（Bearer Token），不包含任何大模型密钥。
- **Mock 隔离**: 客户端当前通过 Hilt 绑定 `MockAiConversationRepository`，在界面明确显示模拟演示模式标识；接入真实后端时仅需替换 Hilt 绑定为 `RemoteAiConversationRepository`。

## 接口清单

| 方法 | 路径 | 用途 | 关键校验 |
|---|---|---|---|
| POST | `student/binding/verify` | 家长核验码绑定与孩子身份拉取 | 核验码一次性失效，绑定设备指纹 |
| POST | `student/ai/chat` | 多轮对话与意图/动态卡片生成 | 服务端注入学段 Prompt 与画像上下文 |
| GET | `student/{childId}/points` | 查询服务端权威积分余额与明细 | 客户端只读展示，禁止客户端篡改 |
| POST | `student/{childId}/wishes/{wishId}/submit` | 提交愿望与家庭协商单 | 状态流转为 `PENDING_PARENT` |
| POST | `student/{childId}/rewards/{rewardId}/redeem` | 发起奖励兑换申请 | 服务端校验积分充足并通知家长审批 |
| POST | `student/{childId}/recordings/upload` | 上传英语朗读录音文件（Multipart） | 校验孩子主动授权标志与音频时长 |
