package com.github.zjiajun.timewheel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 时间轮
 * @since 1.0.0
 * @author zhujiajun
 */
public class Timewheel {

    // 时间轮的槽数量
    private final int slotCount;
    // 时间轮
    private final List<List<TimerTask>> wheel;
    // 时间轮的槽间隔,单位秒
    private final int slotInterval;
    // 当前槽位置
    private int currentSlot;
    //一轮时间
    private final int roundTime;
    //当前轮数
    private int round;
    
    private final ScheduledExecutorService executor;

    /**
     * 
     * @param slotCount 时间轮的槽数量
     * @param slotInterval 槽间隔,单位秒
     */
    public Timewheel(int slotCount, int slotInterval) {
        this.slotCount = slotCount;
        this.slotInterval = slotInterval;
        this.wheel = new ArrayList<>(slotCount);
        this.currentSlot = 0;
        this.roundTime = slotCount * slotInterval;
        this.round = 0;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        //初始化时间轮
        for (int i = 0; i < slotCount; i++) {
            wheel.add(new ArrayList<>());
        }
    }

    /**
     * Add Task without taskErrorHandler
     * @param delay 延迟执行的时间，单位秒
     * @param task 具体执行任务 
     * @return taskId
     */
    public String addTask(int delay, Runnable task) {
        return addTask(delay, task, null);
    }

    /**
     * Add Task with taskErrorHandler
     * @param delay 延迟执行的时间，单位秒
     * @param task  具体执行任务
     * @param taskErrorHandler 任务执行异常处理
     * @return taskId
     */
    public String addTask(int delay, Runnable task, TaskErrorHandler taskErrorHandler) {
        if (delay <= 0 || task == null) {
            return null;
        }
        //延迟执行时间大于时间轮的一轮时间,计算正确的轮数
        int execRound = delay / roundTime;
        int pos = (currentSlot + delay / slotInterval) % slotCount;
        TimerTask timerTask = new TimerTask(execRound, task, taskErrorHandler);
        wheel.get(pos).add(timerTask);
        System.out.printf("当前时间轮的槽位置%d,添加任务[%s]到第%d轮---第%d槽, 延迟%d秒执行, 现在是%s\n",
                currentSlot, timerTask.getTaskId(), execRound, pos, delay, LocalDateTime.now());
        return timerTask.getTaskId();
    }


    /**
     * 取消任务
     * @param taskId 任务Id
     * @return 是否取消成功
     */
    public boolean cancelTask(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            return false;
        }
        for (List<TimerTask> timerTasks : wheel) {
            for (TimerTask task : timerTasks) {
                if (task.getTaskId().equals(taskId)) {
                    System.out.println("取消任务:" + taskId);
                    task.cancel();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 启动时间轮
     */
    public void start() {
        executor.scheduleWithFixedDelay(() -> {
                System.out.printf("当前执行第%d轮---第%d槽\n", round, currentSlot);
                List<TimerTask> timerTasks = wheel.get(currentSlot);
                Iterator<TimerTask> iterator = timerTasks.iterator();
                while (iterator.hasNext()) {
                    TimerTask timerTask = iterator.next();
                    if (timerTask.isCanceled()) {
                        iterator.remove();
                        continue;
                    }
                    if (round == timerTask.execRound) {
                        try {
                            timerTask.task.run();
                        } catch(Exception e) {
                            if (timerTask.getTaskErrorHandler() != null) {
                                timerTask.getTaskErrorHandler().handle(e, timerTask.getTaskId());
                            } else {
                                e.printStackTrace();
                            }
                        }
                        iterator.remove();
                    }
                }
                currentSlot = (currentSlot + 1 ) % slotCount;
                //说明是新的一轮开始, 轮数自增
                if (currentSlot == 0 ) {
                    round++;
                }
        }, 0, slotInterval, TimeUnit.SECONDS);
    }

    /**
     * 停止时间轮
     */
    public void stop() {
        executor.shutdown();
    }

    /**
     * 任务执行异常处理
     */
   public interface TaskErrorHandler {

        /**
         * 处理任务执行异常 
         * @param e 异常
         * @param taskId 任务Id
         */
        void handle(Exception e, String taskId);

   }

    private static class TimerTask {

        private final String taskId;
        //记录任务需要在第几轮执行
        private final int execRound;
        private final Runnable task;
        private volatile boolean isCanceled;
        private final TaskErrorHandler taskErrorHandler;

        public TimerTask(int execRound, Runnable task, TaskErrorHandler taskErrorHandler) {
            this.taskId = UUID.randomUUID().toString();
            this.execRound = execRound;
            this.task = task;
            this.isCanceled = false;
            this.taskErrorHandler = taskErrorHandler;
        }
        
        public String getTaskId() {
            return taskId;
        }

        public boolean isCanceled() {
            return isCanceled;
        }

        public void cancel() {
            this.isCanceled = true;
        }

        public TaskErrorHandler getTaskErrorHandler() {
            return taskErrorHandler;
        }

    }
    
}