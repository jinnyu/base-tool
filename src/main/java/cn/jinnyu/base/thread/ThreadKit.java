/*
 * Copyright (c) 2022, Jinnyu (jinyu@jinnyu.cn).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.jinnyu.base.thread;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jinyu@jinnyu.cn
 * @date 2021-12-23
 */
public enum ThreadKit {

    ;

    // @formatter:off
    public static final int                      DEFAULT_CORE_SIZE       = 4;
    public static final int                      DEFAULT_POOL_SIZE       = 40;
    public static final long                     DEFAULT_POOL_KEEP_ALIVE = 10L;
    public static final TimeUnit                 DEFAULT_POOL_TIME_UNIT  = TimeUnit.MINUTES;
    public static final ThreadFactory            DEFAULT_THREAD_FACTORY  = new ThreadFactory() {
        private final AtomicInteger index = new AtomicInteger(1);
        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "common-pool-" + index.getAndIncrement());
        }
    };
    public static final RejectedExecutionHandler DEFAULT_REJECT_HANDLER  = new ThreadPoolExecutor.AbortPolicy();
    // @formatter:on

    /**
     * 创建默认线程池
     * <pre>
     *     4, 40, 10min, queue, common-pool-n, reject
     * </pre>
     *
     * @param queue 任务队列
     * @return 线程池
     */
    public static ThreadPoolExecutor pool(ArrayBlockingQueue<Runnable> queue) {
        return pool(DEFAULT_CORE_SIZE, DEFAULT_POOL_SIZE, DEFAULT_POOL_KEEP_ALIVE, DEFAULT_POOL_TIME_UNIT, queue, DEFAULT_THREAD_FACTORY, DEFAULT_REJECT_HANDLER);
    }

    public static ThreadPoolExecutor pool(ArrayBlockingQueue<Runnable> queue, String poolNamePrefix) {
        return pool(DEFAULT_CORE_SIZE, DEFAULT_POOL_SIZE, DEFAULT_POOL_KEEP_ALIVE, DEFAULT_POOL_TIME_UNIT, queue, createFactory(poolNamePrefix), DEFAULT_REJECT_HANDLER);
    }

    public static ThreadPoolExecutor pool(int core, int poolSize, ArrayBlockingQueue<Runnable> queue) {
        return pool(core, poolSize, DEFAULT_POOL_KEEP_ALIVE, DEFAULT_POOL_TIME_UNIT, queue, DEFAULT_THREAD_FACTORY, DEFAULT_REJECT_HANDLER);
    }

    public static ThreadPoolExecutor pool(int core, int poolSize, ArrayBlockingQueue<Runnable> queue, String poolNamePrefix) {
        return pool(core, poolSize, DEFAULT_POOL_KEEP_ALIVE, DEFAULT_POOL_TIME_UNIT, queue, createFactory(poolNamePrefix), DEFAULT_REJECT_HANDLER);
    }

    public static ThreadPoolExecutor pool(int core, int poolSize, ArrayBlockingQueue<Runnable> queue, RejectedExecutionHandler handler) {
        return pool(core, poolSize, DEFAULT_POOL_KEEP_ALIVE, DEFAULT_POOL_TIME_UNIT, queue, DEFAULT_THREAD_FACTORY, handler);
    }

    public static ThreadPoolExecutor pool(int core, int poolSize, ArrayBlockingQueue<Runnable> queue, String poolNamePrefix, RejectedExecutionHandler handler) {
        return pool(core, poolSize, DEFAULT_POOL_KEEP_ALIVE, DEFAULT_POOL_TIME_UNIT, queue, createFactory(poolNamePrefix), handler);
    }

    public static ThreadPoolExecutor pool(int core, int poolSize, ArrayBlockingQueue<Runnable> queue, ThreadFactory factory, RejectedExecutionHandler handler) {
        return pool(core, poolSize, DEFAULT_POOL_KEEP_ALIVE, DEFAULT_POOL_TIME_UNIT, queue, factory, handler);
    }

    public static ThreadPoolExecutor pool(int core, int poolSize, long keepAlive, TimeUnit unit, ArrayBlockingQueue<Runnable> queue, ThreadFactory factory, RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(core, poolSize, keepAlive, unit, queue, factory, handler);
    }

    public static ThreadFactory createFactory(String poolNamePrefix) {
        // @formatter:off
        return new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(1);
            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, poolNamePrefix + index.getAndIncrement());
            }
        };
        // @formatter:on
    }

}
