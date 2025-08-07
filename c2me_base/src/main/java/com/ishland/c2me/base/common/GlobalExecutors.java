package com.ishland.c2me.base.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ishland.c2me.base.ModuleEntryPoint;
import com.ishland.c2me.base.common.util.C2MENormalWorkerThreadFactory;

import java.util.concurrent.*;

public class GlobalExecutors {

//    private static final C2MEForkJoinWorkerThreadFactory factory = new C2MEForkJoinWorkerThreadFactory("c2me", "C2ME worker #%d", Thread.NORM_PRIORITY - 1);
    private static final C2MENormalWorkerThreadFactory factory = new C2MENormalWorkerThreadFactory("c2me", "C2ME worker #%d", Thread.NORM_PRIORITY - 1);
    public static final int GLOBAL_EXECUTOR_PARALLELISM = (int) ModuleEntryPoint.globalExecutorParallelism;
//    public static final ForkJoinPool executor = new ForkJoinPool(
//            GLOBAL_EXECUTOR_PARALLELISM,
//            factory,
//            null,
//            true
//    );
    public static final ExecutorService executor = Executors.newFixedThreadPool(GLOBAL_EXECUTOR_PARALLELISM, factory);
    public static final Executor invokingExecutor = r -> {
        if (Thread.currentThread().getThreadGroup() == factory.getThreadGroup()) {
            r.run();
        } else {
            executor.execute(r);
        }
    };

    public static final ExecutorService asyncScheduler = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("c2me_sched").build());

//    public static final TaskExecutor<Runnable> asyncSchedulerTaskExecutor = TaskExecutor.create(asyncScheduler, "c2me-sched");

}
