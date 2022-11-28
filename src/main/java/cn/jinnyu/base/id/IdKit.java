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
package cn.jinnyu.base.id;

import cn.jinnyu.base.lang.LangKit;
import cn.jinnyu.base.random.RandomKit;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-07-05
 */
@Slf4j
public enum IdKit {

    ;

    public enum Type {
        OBJECT_ID, SNOW_FLAKE, RANDOM
    }

    private static final AtomicReference<SnowFlake> cache = new AtomicReference<>();
    private static final ReentrantLock              sync  = new ReentrantLock(true);

    public static void setSnowFlake(SnowFlake snowFlake) {
        cache.set(snowFlake);
        log.info("Set new snowFlake instance. Start timestamp: [{}], DatacenterId: [{}], WorkerId: [{}]", snowFlake.getStartTimestamp(), snowFlake.getDataCenterId(), snowFlake.getWorkerId());
    }

    public static String getId(Type type) {
        switch (type) {
            case OBJECT_ID:
                return ObjectId.get().toHexString();
            case SNOW_FLAKE:
                if (LangKit.isNull(cache.get())) {
                    try {
                        sync.lock();
                        if (LangKit.isNull(cache.get())) {
                            cache.set(new SnowFlake());
                        }
                    } finally {
                        if (sync.isLocked()) {
                            sync.unlock();
                        }
                    }
                }
                return String.valueOf(cache.get().nextId());
            default:
                return RandomKit.getString(16);
        }
    }

}
