package cn.cuptec.faros.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;

public class ThreadPoolExecutorFactory {
    private static final int CORE_POOL_SIZE = 40;
    private static final int MAXIMUM_POOL_SIZE = 40;
    private static final int KEEP_ALIVE_TIME = 60;
    private static ThreadPoolExecutor threadPoolExecutor = null;

    private ThreadPoolExecutorFactory() {
    }

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        if (null == threadPoolExecutor) {
            Class var1 = ThreadPoolExecutorFactory.class;
            synchronized(ThreadPoolExecutorFactory.class) {
                ThreadPoolExecutor t = threadPoolExecutor;
                if (null == t) {
                    Class var2 = ThreadPoolExecutorFactory.class;
                    synchronized(ThreadPoolExecutorFactory.class) {
                        t = new ThreadPoolExecutor(40, 40, 60L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), new DiscardOldestPolicy());
                    }

                    threadPoolExecutor = t;
                }
            }
        }

        return threadPoolExecutor;
    }
}