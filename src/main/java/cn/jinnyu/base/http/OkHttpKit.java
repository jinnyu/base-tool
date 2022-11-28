package cn.jinnyu.base.http;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author jinyu@jinnyu.cn
 * @date 2021-10-09
 */
@SuppressWarnings("unused")
@Slf4j
public enum OkHttpKit {

    CONFIG(false, "CONFIG"),
    /**
     *
     */
    GET(false, "GET"),
    /**
     *
     */
    GET_WITH_BODY(true, "GET"),
    /**
     *
     */
    HEAD(false, "HEAD"),
    /**
     *
     */
    POST(true, "POST"),
    /**
     *
     */
    PUT(true, "PUT"),
    /**
     *
     */
    PATCH(true, "PATCH"),
    /**
     *
     */
    DELETE(true, "DELETE");

    private final boolean body;
    private final String  method;

    OkHttpKit(boolean body, String method) {
        this.body = body;
        this.method = method;
    }

    @Override
    public String toString() {
        return "[" + this.method + "," + this.defaultClient.interceptors();
    }

    // --------------------------------------------------

    /**
     * 默认TraceId策略
     * <p>
     * 请求时在header中添加从线程中获取到的traceId(如有)
     * </p>
     */
    public static class TraceIdInterceptor implements Interceptor {

        private static final List<String> list = new LinkedList<>();

        static {
            list.add("traceId");
            list.add("traceid");
            list.add("X-USP-TraceId");
            list.add("X-USP-Trace-Id");
            list.add("x-usp-traceid");
            list.add("x-usp-trace-id");
        }

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            String  traceId = getTraceIdFromMdc();
            Request request = chain.request();
            if (null != traceId && !"".equals(traceId)) {
                request = chain.request().newBuilder().addHeader("X-USP-Trace-Id", traceId).build();
            }
            return chain.proceed(request);
        }

        @Override
        public String toString() {
            return "TraceId Filter";
        }

        private String getTraceIdFromMdc() {
            for (String s : list) {
                String id = MDC.get(s);
                if (null != id && !"".equals(id)) {
                    return id;
                }
            }
            return null;
        }

    }

    /**
     * 默认重试策略
     * <p>
     * 重试3次 每次间隔100ms 超过3次后抛出异常
     * </p>
     */
    public static class RetryInterceptor implements Interceptor {
        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            int       defaultRetry = 5;
            int       pauseMs      = 100;
            Request   request      = chain.request();
            Response  response;
            Exception exception    = null;
            for (int i = 1; i <= defaultRetry; i++) {
                try {
                    return chain.proceed(request);
                } catch (Exception e) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(pauseMs);
                    } catch (InterruptedException ex) {
                        exception = e;
                    }
                    log.info("Http请求失败, 当前第{}次重试. {}", i, e.getMessage());
                }
            }
            throw new RuntimeException("请求失败", exception);
        }

        @Override
        public String toString() {
            return "Retry Filter";
        }
    }

    // --------------------------------------------------

    // @formatter:off
    @Setter
    public OkHttpClient defaultClient = createClient(
            new Config.Builder().
            addInterceptor(new TraceIdInterceptor()).
            addInterceptor(new RetryInterceptor()).
            build()
    );
    // @formatter:on

    // --------------------------------------------------

    public static final Interceptor TRACE_ID_INTERCEPTOR = new TraceIdInterceptor();
    public static final Interceptor RETRY_INTERCEPTOR    = new RetryInterceptor();
    public static final MediaType   MEDIA_TYPE_JSON      = MediaType.parse("application/json");
    public static final MediaType   MEDIA_TYPE_STREAM    = MediaType.parse("application/octet-stream");

    public static OkHttpClient createClient(Config config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // 拦截器
        config.getInterceptors().forEach(builder::addInterceptor);
        // @formatter:off
        // 连接池
        builder.connectionPool(new ConnectionPool(config.getPoolSize(), config.getValidIdleTimeout(), TimeUnit.SECONDS)).
        // 连接超时
        connectTimeout(Duration.of(config.getConnectionTimeout(), ChronoUnit.SECONDS)).
        // 请求超时
        callTimeout(Duration.of(config.getConnectionRequestTimeout(), ChronoUnit.SECONDS)).
        // 写入超时
        writeTimeout(Duration.of(config.getConnectionRequestTimeout(), ChronoUnit.SECONDS)).
        // 读取超时
         readTimeout(Duration.of(config.getConnectionRequestTimeout(), ChronoUnit.SECONDS));
        return builder.build();
        // @formatter:on
    }

    public static class Config {

        private Config() {

        }

        /**
         * 连接池大小
         */
        private int                     poolSize;
        /**
         * 空闲连接验证时长 s
         */
        private int                     validIdleTimeout;
        /**
         * socket超时时长 s
         */
        private int                     socketConnectTimeout;
        /**
         * 连接超时时长 s
         */
        private int                     connectionTimeout;
        /**
         * 请求超时时长 s
         */
        private int                     connectionRequestTimeout;
        /**
         * 拦截器列表
         */
        private LinkedList<Interceptor> interceptors;

        public int getPoolSize() {
            return poolSize;
        }

        public int getValidIdleTimeout() {
            return validIdleTimeout;
        }

        public int getSocketConnectTimeout() {
            return socketConnectTimeout;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public int getConnectionRequestTimeout() {
            return connectionRequestTimeout;
        }

        public LinkedList<Interceptor> getInterceptors() {
            return interceptors;
        }

        public void setInterceptors(LinkedList<Interceptor> interceptors) {
            this.interceptors = interceptors;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            public Builder() {

            }

            /**
             * 连接池大小
             */
            private int                     poolSize                 = 10;
            /**
             * 空闲连接验证时长
             */
            private int                     validIdleTimeout         = 60;
            /**
             * socket超时时长
             */
            private int                     socketConnectTimeout     = 15;
            /**
             * 连接超时时长
             */
            private int                     connectionTimeout        = 15;
            /**
             * 请求超时时长
             */
            private int                     connectionRequestTimeout = 30;
            /**
             * 拦截器列表
             */
            private LinkedList<Interceptor> interceptors;

            public Builder poolSize(int poolSize) {
                this.poolSize = poolSize;
                return this;
            }

            public Builder validIdleMs(int validIdleMs) {
                this.validIdleTimeout = validIdleMs;
                return this;
            }

            public Builder socketConnectTimeout(int socketConnectTimeout) {
                this.socketConnectTimeout = socketConnectTimeout;
                return this;
            }

            public Builder connectionTimeout(int connectionTimeout) {
                this.connectionTimeout = connectionTimeout;
                return this;
            }

            public Builder connectionRequestTimeout(int connectionRequestTimeout) {
                this.connectionRequestTimeout = connectionRequestTimeout;
                return this;
            }

            public Builder interceptors(LinkedList<Interceptor> interceptors) {
                this.interceptors = interceptors;
                return this;
            }

            public Builder addInterceptor(Interceptor interceptor) {
                if (null == this.interceptors) {
                    this.interceptors = new LinkedList<>();
                }
                this.interceptors.add(interceptor);
                return this;
            }

            public Config build() {
                //noinspection DuplicatedCode
                Config config = new Config();
                config.poolSize = this.poolSize;
                config.validIdleTimeout = this.validIdleTimeout;
                config.socketConnectTimeout = this.socketConnectTimeout;
                config.connectionTimeout = this.connectionTimeout;
                config.connectionRequestTimeout = this.connectionRequestTimeout;
                config.interceptors = this.interceptors;
                return config;
            }

        }

    }

    // ----- raw

    public String raw(String url) throws IOException {
        return raw(defaultClient, url, null, null, null);
    }

    public String raw(OkHttpClient client, String url) throws IOException {
        return raw(client, url, null, null, null);
    }

    public String raw(String url, Map<String, String> header, Map<String, String> cookie) throws IOException {
        return raw(defaultClient, url, header, cookie, null);
    }

    public String raw(OkHttpClient client, String url, Map<String, String> header, Map<String, String> cookie) throws IOException {
        return raw(client, url, header, cookie, null);
    }

    public String raw(String url, Map<String, String> header, Map<String, String> cookie, RequestBody data) throws IOException {
        return raw(defaultClient, url, header, cookie, data);
    }

    public String raw(OkHttpClient client, String url, Map<String, String> header, Map<String, String> cookie, RequestBody data) throws IOException {
        try (Response response = execute(client, url, header, cookie, data)) {
            ResponseBody body = response.body();
            return Objects.isNull(body) ? null : body.string();
        }
    }

    // ----- Form

    public static RequestBody formBody(Map<String, String> data) {
        FormBody.Builder builder = new FormBody.Builder();
        if (null != data && data.size() > 0) {
            data.forEach(builder::add);
        }
        return builder.build();
    }

    public static RequestBody multipartFormBody(Map<String, List<Object>> data) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        data.forEach((k, v) -> {
            if (null != v && !v.isEmpty()) {
                builder.setType(MultipartBody.FORM);
                v.forEach(value -> {
                    if (null != value) {
                        if (value.getClass().equals(File.class)) {
                            builder.addFormDataPart(k, ((File) value).getName(), RequestBody.Companion.create((File) value, MEDIA_TYPE_STREAM));
                        } else {
                            builder.addFormDataPart(k, value.toString());
                        }
                    } else {
                        throw new IllegalArgumentException("form data can not be null!");
                    }
                });
            } else {
                throw new IllegalArgumentException("form data can not be null!");
            }
        });
        return builder.build();
    }

    public String multipartForm(String url, Map<String, List<Object>> data) throws IOException {
        return multipartForm(defaultClient, url, null, null, data);
    }

    public String multipartForm(OkHttpClient client, String url, Map<String, List<Object>> data) throws IOException {
        return multipartForm(client, url, null, null, data);
    }

    public String multipartForm(String url, Map<String, String> header, Map<String, String> cookie, Map<String, List<Object>> data) throws IOException {
        return multipartForm(defaultClient, url, header, cookie, data);
    }

    public String multipartForm(OkHttpClient client, String url, Map<String, String> header, Map<String, String> cookie, Map<String, List<Object>> data) throws IOException {
        try (Response response = execute(client, url, header, cookie, multipartFormBody(data))) {
            ResponseBody body = response.body();
            return Objects.isNull(body) ? null : body.string();
        }
    }

    public String form(String url, Map<String, String> data) throws IOException {
        return form(defaultClient, url, null, null, data);
    }

    public String form(OkHttpClient client, String url, Map<String, String> data) throws IOException {
        return form(client, url, null, null, data);
    }

    public String form(String url, Map<String, String> header, Map<String, String> cookie, Map<String, String> data) throws IOException {
        return form(defaultClient, url, header, cookie, data);
    }

    public String form(OkHttpClient client, String url, Map<String, String> header, Map<String, String> cookie, Map<String, String> data) throws IOException {
        try (Response response = execute(client, url, header, cookie, formBody(data))) {
            ResponseBody body = response.body();
            return Objects.isNull(body) ? null : body.string();
        }
    }

    // ----- Json

    public static RequestBody jsonBody(String json) {
        return RequestBody.Companion.create(json, MEDIA_TYPE_JSON);
    }

    public String json(String url, String json) throws IOException {
        return json(defaultClient, url, null, null, json);
    }

    public String json(OkHttpClient client, String url, String json) throws IOException {
        return json(client, url, null, null, json);
    }

    public String json(String url, Map<String, String> header, Map<String, String> cookie, String json) throws IOException {
        return json(defaultClient, url, header, cookie, json);
    }

    public String json(OkHttpClient client, String url, Map<String, String> header, Map<String, String> cookie, String json) throws IOException {
        try (Response response = execute(client, url, header, cookie, jsonBody(null == json ? "" : json))) {
            ResponseBody body = response.body();
            return Objects.isNull(body) ? null : body.string();
        }
    }

    // ----- 最终执行

    public Response execute(OkHttpClient client, String url, Map<String, String> header, Map<String, String> cookie, RequestBody data) throws IOException {
        LocalDateTime start = null, end;
        if (log.isDebugEnabled()) {
            start = LocalDateTime.now();
        }
        Request.Builder builder = new Request.Builder().url(url);
        switch (this) {
            case CONFIG:
                throw new RuntimeException("CONFIG实例不支持执行实际请求, 仅用于配置Http请求配置.");
            case GET:
                builder.get();
                break;
            case HEAD:
                builder.head();
                break;
            default:
                break;
        }
        setHeader(builder, header);
        setCookie(builder, cookie);
        setData(builder, data);
        if (log.isDebugEnabled()) {
            log.debug("请求方法 -> [{}] url -> [{}]", this.method, url);
            log.debug("请求头   -> [{}] cookie -> [{}]", header, cookie);
        }
        Response response = client.newCall(builder.build()).execute();
        if (log.isDebugEnabled()) {
            end = LocalDateTime.now();
            log.debug("请求耗时 -> {}ms", Duration.between(start, end).toMillis());
        }
        return response;
    }

    private void setHeader(Request.Builder request, Map<String, String> header) {
        if (null != header && header.size() > 0) {
            header.forEach(request::header);
        }
    }

    private void setCookie(Request.Builder request, Map<String, String> cookie) {
        if (null != cookie && cookie.size() > 0) {
            StringBuilder builder = new StringBuilder();
            boolean       first   = true;
            for (Map.Entry<String, String> entry : cookie.entrySet()) {
                String key   = entry.getKey();
                String value = entry.getValue();
                if (!first) {
                    builder.append("; ");
                } else {
                    first = false;
                }
                builder.append(key).append('=').append(value);
            }
            request.header("Cookie", builder.toString());
        }
    }

    private void setData(Request.Builder request, RequestBody data) {
        if (this.body && null != data) {
            request.method(this.method, data);
        }
    }

}
