package com.github.zjiajun.timewheel;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        Timewheel timewheel = new Timewheel(10, 1);

        timewheel.addTask(2,() -> {
            System.out.println("延迟2秒执行_任务_1,执行时间:" + LocalDateTime.now());
        });

        timewheel.addTask(2,() -> {
            System.out.println("延迟2秒执行_任务_1,执行时间:" + LocalDateTime.now());
        });

        String taskId = timewheel.addTask(11,() -> {
            System.out.println("延迟11秒执行_任务_1,执行时间:"+ LocalDateTime.now());
        });


        timewheel.addTask(11,() -> {
            System.out.println("延迟11秒执行_任务_2,执行时间:" + LocalDateTime.now());
            String[] s = {"1"};
            s[1] = "2";
        }, (e, errorTaskId) -> {
                System.out.println("任务["+errorTaskId+"]执行异常, 异常信息:"+e.getMessage());
         });

        timewheel.start();

        TimeUnit.SECONDS.sleep(2);

        timewheel.cancelTask(taskId);

    }
}
