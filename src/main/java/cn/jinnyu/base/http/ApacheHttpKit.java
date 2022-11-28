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
package cn.jinnyu.base.http;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.slf4j.MDC;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author jinyu@jinnyu.cn
 * @date 2021-10-09
 */
@SuppressWarnings("unused")
@Slf4j
public enum ApacheHttpKit {

    /**
     * 用于配置参数
     */
    CONFIG(false),

    /**
     *
     */
    TRACE(false),
    /**
     *
     */
    HEAD(false),
    /**
     *
     */
    GET(false),
    /**
     *
     */
    GET_WITH_BODY(true),
    /**
     *
     */
    OPTIONS(false),
    /**
     *
     */
    POST(true),
    /**
     *
     */
    PUT(true),
    /**
     *
     */
    PATCH(true),
    /**
     *
     */
    DELETE(true);

    private final boolean body;

    ApacheHttpKit(boolean body) {
        this.body = body;
    }

    // --------------------------------------------------

    private static final SecureRandom                       RANDOM       = new SecureRandom();
    private static final AtomicBoolean                      initFlag     = new AtomicBoolean(false);
    public static        Config                             config       = Config.builder().build();
    @Setter
    @Getter
    private              PoolingHttpClientConnectionManager manager;
    @Setter
    @Getter
    private              RequestConfig                      requestConfig;
    @Setter
    @Getter
    private              HttpRequestRetryHandler            retryHandler;
    @Setter
    @Getter
    private              UserTokenHandler                   tokenHandler;
    @Getter
    @Setter
    private              List<HttpRequestInterceptor>       interceptors = new ArrayList<>();

    // --------------------------------------------------

    public static void init(Config config) {
        if (!initFlag.get()) {
            initFlag.set(true);
            ApacheHttpKit.config = config;
        }
    }

    // --------------------------------------------------

    public static class Config {

        private Config() {

        }

        /**
         * 连接池大小
         */
        private int            poolSize;
        /**
         * 空闲连接验证时长
         */
        private int            validIdleMs;
        /**
         * socket]超时时长
         */
        private int            socketConnectTimeout;
        /**
         * 连接超时时长
         */
        private int            connectionTimeout;
        /**
         * 请求超时时长
         */
        private int            connectionRequestTimeout;
        /**
         * 是否开启重试
         */
        private boolean        retry;
        /**
         * 重试次数
         */
        private int            retryCount;
        /**
         * 暂停模式
         */
        private RetryPauseMode retryPauseMode;
        /**
         * 暂停间隔(ms)
         * <br>
         * 随机模式下为最低暂停时长
         */
        private int            pauseMs;
        /**
         * 随机模式下最大等待时长
         * <br>
         * 总等待时长为 pauseMs + 随机时长 <= pauseMs + pauseRandomMaxMs
         */
        private int            pauseRandomMaxMs;

        public int getPoolSize() {
            return poolSize;
        }

        public int getValidIdleMs() {
            return validIdleMs;
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

        public boolean isRetry() {
            return retry;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public RetryPauseMode getRetryPauseMode() {
            return retryPauseMode;
        }

        public int getPauseMs() {
            return pauseMs;
        }

        public int getPauseRandomMaxMs() {
            return pauseRandomMaxMs;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private Builder() {

            }

            /**
             * 连接池大小
             */
            private int            poolSize                 = 10;
            /**
             * 空闲连接验证时长
             */
            private int            validIdleMs              = 60000;
            /**
             * socket]超时时长
             */
            private int            socketConnectTimeout     = 10000;
            /**
             * 连接超时时长
             */
            private int            connectionTimeout        = 10000;
            /**
             * 请求超时时长
             */
            private int            connectionRequestTimeout = 30000;
            /**
             * 是否开启重试
             */
            private boolean        retry                    = true;
            /**
             * 重试次数
             */
            private int            retryCount               = 5;
            /**
             * 暂停模式
             */
            private RetryPauseMode retryPauseMode           = RetryPauseMode.FIXED;
            /**
             * 暂停间隔(ms)
             * <br>
             * 随机模式下为最低暂停时长
             */
            private int            pauseMs                  = 0;
            /**
             * 随机模式下最大等待时长
             * <br>
             * 总等待时长为 pauseMs + 随机时长 <= pauseMs + pauseRandomMaxMs
             */
            private int            pauseRandomMaxMs         = 100;

            public Builder poolSize(int poolSize) {
                this.poolSize = poolSize;
                return this;
            }

            public Builder validIdleMs(int validIdleMs) {
                this.validIdleMs = validIdleMs;
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

            public Builder retry(boolean retry) {
                this.retry = retry;
                return this;
            }

            public Builder retryCount(int retryCount) {
                this.retryCount = retryCount;
                return this;
            }

            public Builder retryPauseMode(RetryPauseMode retryPauseMode) {
                this.retryPauseMode = retryPauseMode;
                return this;
            }

            public Builder pauseMs(int pauseMs) {
                this.pauseMs = pauseMs;
                return this;
            }

            public Builder pauseRandomMaxMs(int pauseRandomMaxMs) {
                this.pauseRandomMaxMs = pauseRandomMaxMs;
                return this;
            }

            public Config build() {
                Config config = new Config();
                config.poolSize = this.poolSize;
                config.validIdleMs = this.validIdleMs;
                config.socketConnectTimeout = this.socketConnectTimeout;
                config.connectionTimeout = this.connectionTimeout;
                config.connectionRequestTimeout = this.connectionRequestTimeout;
                config.retry = this.retry;
                config.retryCount = this.retryCount;
                config.retryPauseMode = this.retryPauseMode;
                config.pauseMs = this.pauseMs;
                config.pauseRandomMaxMs = this.pauseRandomMaxMs;
                return config;
            }
        }

    }

    public enum RetryPauseMode {
        FIXED, RANDOM
    }

    // --------------------------------------------------

    private static PoolingHttpClientConnectionManager toConnectionManager(Config config) {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(config.getPoolSize());
        manager.setValidateAfterInactivity(config.getValidIdleMs());
        return manager;
    }

    private static RequestConfig toRequestConfig(Config config) {
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setSocketTimeout(config.getSocketConnectTimeout());
        builder.setConnectTimeout(config.getConnectionTimeout());
        builder.setConnectionRequestTimeout(config.getConnectionRequestTimeout());
        return builder.build();
    }

    private static HttpRequestInterceptor toTraceIdInterceptor() {
        return new InnerTraceIdInterceptor();
    }

    private static class InnerTraceIdInterceptor implements HttpRequestInterceptor {
        private static final List<String> list = new LinkedList<>();

        static {
            list.add("traceId");
            list.add("traceid");
            list.add("X-USP-TraceId");
            list.add("X-USP-Trace-Id");
            list.add("x-usp-traceid");
            list.add("x-usp-trace-id");
        }

        @Override
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            String clientTraceId = getTraceIdFromMdc();
            if (null != clientTraceId && !"".equals(clientTraceId)) {
                request.addHeader("X-USP-Trace-Id", clientTraceId);
            }
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

    // --------------------------------------------------

    public static UrlEncodedFormEntity toFormEntity(Map<String, String> form) {
        List<BasicNameValuePair> collect = form.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        try {
            return new UrlEncodedFormEntity(collect, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
            return null;
        }
    }

    public static StringEntity toJsonEntity(String json) {
        return new StringEntity(json, ContentType.APPLICATION_JSON);
    }

    // --------------------------------------------------

    public String raw(String url) throws IOException {
        return executeToString(config, url, null, null, null);
    }

    public String raw(String url, Map<String, String> headers, Map<String, String> cookies) throws IOException {
        return executeToString(config, url, headers, cookies, null);
    }

    public String form(String url, Map<String, String> form) throws IOException {
        return form(url, null, null, form);
    }

    public String form(String url, Map<String, String> headers, Map<String, String> cookies, Map<String, String> form) throws IOException {
        return executeToString(config, url, headers, cookies, toFormEntity(form));
    }

    public String json(String url, String json) throws IOException {
        return json(url, null, null, json);
    }

    public String json(String url, Map<String, String> headers, Map<String, String> cookies, String json) throws IOException {
        StringEntity entity = null;
        if (this.body && null != json && !"".equals(json)) {
            entity = toJsonEntity(json);
        }
        return executeToString(config, url, headers, cookies, entity);
    }

    public String executeToString(Config config, String url, Map<String, String> headers, Map<String, String> cookies, HttpEntity data) throws IOException {
        if (!initFlag.get()) {
            manager = toConnectionManager(config);
            requestConfig = toRequestConfig(config);
            List<Class<? extends IOException>> classes = Arrays.asList(InterruptedIOException.class, UnknownHostException.class, ConnectException.class, SSLException.class);
            retryHandler = new RetryHandler(config.isRetry(), config.getRetryCount(), config.getRetryPauseMode(), config.getPauseMs(), config.getPauseRandomMaxMs(), classes);
            tokenHandler = new DefaultUserTokenHandler();
            interceptors.add(toTraceIdInterceptor());
            initFlag.set(true);
        }
        HttpClientBuilder builder = HttpClients.custom();
        if (null != manager) {
            builder.setConnectionManager(manager);
        }
        if (null != retryHandler) {
            builder.setRetryHandler(retryHandler);
        }
        if (null != tokenHandler) {
            builder.setUserTokenHandler(tokenHandler);
        }
        if (interceptors.size() > 0) {
            interceptors.forEach(builder::addInterceptorLast);
        }
        CloseableHttpClient client = builder.build();
        return executeToString(client, requestConfig, url, headers, cookies, data);
    }

    public String executeToString(CloseableHttpClient client, RequestConfig requestConfig, String url, Map<String, String> headers, Map<String, String> cookies, HttpEntity data) throws IOException {
        CloseableHttpResponse response = execute(client, requestConfig, url, headers, cookies, data);
        try {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } finally {
            int code = response.getStatusLine().getStatusCode();
            if (200 != code) {
                response.close();
            }
        }
    }

    public CloseableHttpResponse execute(CloseableHttpClient client, RequestConfig requestConfig, String url, Map<String, String> headers, Map<String, String> cookies, HttpEntity data) throws IOException {
        LocalDateTime start = null, end;
        if (log.isDebugEnabled()) {
            start = LocalDateTime.now();
        }
        HttpRequest request = getRequest(url, requestConfig);
        setHeader(request, headers);
        setCookie(request, cookies);
        setData(request, data);
        if (log.isDebugEnabled()) {
            log.debug("请求方法 -> [{}] url -> [{}]", this.name(), url);
            log.debug("请求头   -> [{}] cookie -> [{}]", headers, cookies);
        }
        CloseableHttpResponse response;
        try {
            response = client.execute((HttpUriRequest) request);
        } finally {
            try {
                EntityUtils.consume(data);
            } catch (IOException e) {
                log.error("Error when consume entity", e);
            }
        }
        if (log.isDebugEnabled()) {
            end = LocalDateTime.now();
            log.debug("请求耗时 -> {}ms", Duration.between(start, end).toMillis());
        }
        return response;
    }
    // --------------------------------------------------

    private HttpRequest getRequest(String url, RequestConfig C) {
        switch (this) {
            case TRACE:
                HttpTrace trace = new HttpTrace(url);
                trace.setConfig(C);
                return trace;
            case HEAD:
                HttpHead head = new HttpHead(url);
                head.setConfig(C);
                return head;
            case GET:
                HttpGet get = new HttpGet(url);
                get.setConfig(C);
                return get;
            case GET_WITH_BODY:
                HttpGetWithBody getWithBody = new HttpGetWithBody(url);
                getWithBody.setConfig(C);
                return getWithBody;
            case OPTIONS:
                HttpOptions options = new HttpOptions(url);
                options.setConfig(C);
                return options;
            case POST:
                HttpPost post = new HttpPost(url);
                post.setConfig(C);
                return post;
            case PUT:
                HttpPut put = new HttpPut(url);
                put.setConfig(C);
                return put;
            case PATCH:
                HttpPatch patch = new HttpPatch(url);
                patch.setConfig(C);
                return patch;
            case DELETE:
                HttpDeleteWithBody delete = new HttpDeleteWithBody(url);
                delete.setConfig(C);
                return delete;
            default:
                throw new RuntimeException("method not support");
        }
    }

    private void setHeader(HttpRequest request, Map<String, String> headers) {
        if (null != headers && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key   = entry.getKey();
                String value = entry.getValue();
                request.setHeader(new BasicHeader(key, value));
            }
        }
    }

    private void setCookie(HttpRequest request, Map<String, String> cookies) {
        if (null != cookies && cookies.size() > 0) {
            StringBuilder builder = new StringBuilder();
            boolean       first   = true;
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                String key   = entry.getKey();
                String value = entry.getValue();
                if (first) {
                    first = false;
                } else {
                    builder.append("; ");
                }
                builder.append(key).append('=').append(value);
            }
            request.setHeader(new BasicHeader("Cookie", builder.toString()));
        }
    }

    private void setData(HttpRequest request, HttpEntity entity) {
        if (this.body && null != entity) {
            switch (this) {
                case GET_WITH_BODY:
                    HttpGetWithBody get = (HttpGetWithBody) request;
                    get.setEntity(entity);
                    break;
                case POST:
                    HttpPost post = (HttpPost) request;
                    post.setEntity(entity);
                    break;
                case PUT:
                    HttpPut put = (HttpPut) request;
                    put.setEntity(entity);
                    break;
                case PATCH:
                    HttpPatch patch = (HttpPatch) request;
                    patch.setEntity(entity);
                    break;
                case DELETE:
                    HttpDeleteWithBody delete = (HttpDeleteWithBody) request;
                    delete.setEntity(entity);
                    break;
                default:
                    break;
            }
        }
    }

    private static class HttpGetWithBody extends HttpEntityEnclosingRequestBase {

        public final static String METHOD_NAME = "GET";

        public HttpGetWithBody() {
            super();
        }

        public HttpGetWithBody(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpGetWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }

    }

    private static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

        public final static String METHOD_NAME = "DELETE";

        public HttpDeleteWithBody() {
            super();
        }

        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }

    }

    public static class RetryHandler extends DefaultHttpRequestRetryHandler {

        private final boolean        enable;
        private final int            count;
        private final RetryPauseMode mode;
        private final int            sleep;
        private final int            pause;

        public RetryHandler(boolean enable, int count, RetryPauseMode mode, int sleep, int pause, Collection<Class<? extends IOException>> clazz) {
            super(count, enable, clazz);
            this.enable = enable;
            this.count = count;
            this.mode = mode;
            this.sleep = sleep;
            this.pause = pause;
        }

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (enable) {
                Args.notNull(exception, "Exception parameter");
                Args.notNull(context, "HTTP context");
                if (executionCount > this.count) {
                    return false;
                }
                final HttpClientContext clientContext = HttpClientContext.adapt(context);
                if (requestIsAborted(clientContext.getRequest())) {
                    return false;
                }
                // 添加自定义休眠时长
                doSleep(this.mode, this.sleep, this.pause);
                return !clientContext.isRequestSent();
            } else {
                return false;
            }
        }

        private void doSleep(RetryPauseMode mode, int basicTime, int pause) {
            try {
                if (RetryPauseMode.RANDOM.equals(mode)) {
                    TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(pause) + basicTime);
                } else {
                    TimeUnit.MILLISECONDS.sleep(basicTime);
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }

    }

}
