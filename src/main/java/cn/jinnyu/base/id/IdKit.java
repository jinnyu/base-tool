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
