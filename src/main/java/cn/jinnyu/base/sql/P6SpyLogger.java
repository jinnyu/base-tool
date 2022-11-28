package cn.jinnyu.base.sql;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

/**
 * @author jinyu@jinnyu.cn
 * @date 2021-05-26
 */
public class P6SpyLogger implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        return "[Time: " + elapsed + " ms] SQL: " + sql.replaceAll("\\s+", " ");
    }

}
