package cn.jinnyu.base.id;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-06-29
 */
@Slf4j
public final class SnowFlake {

    /**
     * 数据中心占用的位数
     */
    private final static long DATA_CENTER_BIT  = 5;
    /**
     * 机器标识占用的位数
     */
    private final static long WORKER_BIT       = 5;
    /**
     * 序列号占用的位数
     */
    private final static long SEQUENCE_BIT     = 12;
    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATA_CENTER  = ~(-1L << DATA_CENTER_BIT);
    private final static long MAX_WORKER       = ~(-1L << WORKER_BIT);
    private final static long MAX_SEQUENCE     = ~(-1L << SEQUENCE_BIT);
    /**
     * 每一部分向左的位移
     */
    private final static long DATA_CENTER_LEFT = SEQUENCE_BIT + WORKER_BIT;
    private final static long WORKER_LEFT      = SEQUENCE_BIT;
    private final static long TIMESTAMP_LEFT   = DATA_CENTER_LEFT + DATA_CENTER_BIT;
    /**
     * 起始的时间戳 (可设置当前时间之前的邻近时间)
     */
    @Getter
    private final long startTimestamp;
    /**
     * 数据中心ID(0~31)
     */
    @Getter
    private final long dataCenterId;
    /**
     * 工作机器ID(0~31)
     */
    @Getter
    private final long workerId;
    /**
     * 毫秒内序列(0~4095)
     */
    private              long sequence         = 0L;
    /**
     * 上次生成ID的时间截
     */
    private              long lastTimestamp    = -1L;

    public SnowFlake() {
        // 1970-01-01 00:00:00
        this(0L, 0, 0);
    }

    public SnowFlake(long dataCenterId, long workerId) {
        // 1970-01-01 00:00:00
        this(0L, dataCenterId, workerId);
    }

    public SnowFlake(long startTimestamp, long dataCenterId, long workerId) {
        if (startTimestamp < 0) {
            throw new IllegalArgumentException("StartTimestamp must be greater than 0!");
        }
        if (dataCenterId > MAX_DATA_CENTER || dataCenterId < 0) {
            throw new IllegalArgumentException("DataCenterId can't be greater than MAX_DATA_CENTER_(" + MAX_DATA_CENTER + ") or less than 0");
        }
        if (workerId > MAX_WORKER || workerId < 0) {
            throw new IllegalArgumentException("WorkerId can't be greater than MAX_WORKER_(" + MAX_WORKER + ") or less than 0");
        }
        this.startTimestamp = startTimestamp;
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
        log.info("Start timestamp: [{}], DatacenterId: [{}], WorkerId: [{}]", this.startTimestamp, this.dataCenterId, this.workerId);
    }

    /**
     * 产生下一个ID
     */
    public synchronized long nextId() {
        long current = getCurrentTimestamp();
        if (current < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Generated ID may be duplicate!");
        }
        if (current == lastTimestamp) {
            // 相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                // 阻塞到下一个毫秒,获得新的时间戳
                current = getNextMill();
            }
        } else {
            // 不同毫秒内，序列号置为0
            sequence = 0L;
        }
        lastTimestamp = current;
        // @formatter:off
        // 移位并通过或运算拼到一起组成64位的ID
        return (current - startTimestamp) << TIMESTAMP_LEFT    // 时间戳部分
                | dataCenterId             << DATA_CENTER_LEFT // 数据中心部分
                | workerId                 << WORKER_LEFT      // 机器标识部分
                | sequence;                                    // 序列号部分
        // @formatter:on
    }

    private long getNextMill() {
        long mill = getCurrentTimestamp();
        while (mill <= lastTimestamp) {
            mill = getCurrentTimestamp();
        }
        return mill;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

}
