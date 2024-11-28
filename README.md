# TimeWheel 时间轮

一个简单的基于时间轮算法的延迟任务执行框架。

## 功能特性

- 支持添加延迟任务
- 支持取消已添加的任务
- 支持任务执行异常处理
- 基于时间槽和轮次的调度机制
- 单线程执行,避免并发问题

### API 说明

#### 1. 创建时间轮

```java
// 创建具有指定槽数和槽间隔的时间轮
Timewheel timewheel = new Timewheel(int slotCount, int slotInterval);
```

#### 2. 任务管理

```java
// 添加延迟任务，返回任务ID
String addTask(int delay, Runnable task);
// 添加带异常处理的延迟任务，返回任务ID
String addTask(int delay, Runnable task, TaskErrorHandler errorHandler);
// 取消指定ID的任务
boolean cancelTask(String taskId);
```

#### 3. 时间轮控制

```java
// 启动时间轮
void start();
// 停止时间轮
void stop();

```

#### 4. 异常处理接口

```java
// 任务异常处理器接口
interface TaskErrorHandler {
    void handle(Exception e, String taskId);
}
```

## TODO 待完善功能

1. 任务执行增强

   - 支持并行执行任务(可配置线程池)
   - 支持任务优先级设置
   - 支持任务执行超时控制
   - 支持任务执行失败重试

2. 监控与统计

   - 统计任务执行成功/失败数量
   - 记录任务执行耗时
   - 支持查询正在执行的任务列表
   - 支持查询等待执行的任务列表

3. 任务管理
   - 支持批量添加/取消任务
   - 支持动态修改任务延迟时间
   - 支持暂停/恢复任务执行
