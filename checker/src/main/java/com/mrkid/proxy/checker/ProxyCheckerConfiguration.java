package com.mrkid.proxy.checker;

import com.mrkid.proxy.checker.utils.AddressUtils;
import com.mrkid.proxy.checker.writer.PlainFormatWriter;
import com.mrkid.proxy.checker.writer.SquidFormatWriter;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * User: xudong
 * Date: 12/12/2016
 * Time: 12:15 PM
 */
@Configuration
public class ProxyCheckerConfiguration {
    @Bean
    public CloseableHttpAsyncClient httpAsyncClient(int maxConcurrency) {

        final int TIMEOUT = 30 * 1000;
        // reactor config
        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setSoTimeout(TIMEOUT).build();

        HttpAsyncClientBuilder asyncClientBuilder = HttpAsyncClientBuilder.create();
        asyncClientBuilder.setDefaultIOReactorConfig(reactorConfig);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT).build();
        asyncClientBuilder.setDefaultRequestConfig(config);

        // make sure it is less than  num in /proc/sys/fs/file-max
        // because we use several websites to determine if proxy is available,
        // the MaxConnPerRoute should be maxConcurrency * website_numbers(10 is enough)
        asyncClientBuilder.setMaxConnPerRoute(10 * maxConcurrency).setMaxConnTotal(10 * maxConcurrency);

        asyncClientBuilder.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, " +
                "like Gecko) Chrome/54.0.2840.98 Safari/537.36");

        final CloseableHttpAsyncClient httpAsyncClient = asyncClientBuilder.build();
        httpAsyncClient.start();
        return httpAsyncClient;
    }


    @Bean
    public File dataDirectory() {
        File dataDirectory = new File("data");
        if (!dataDirectory.exists()) {
            dataDirectory.mkdir();
        }

        return dataDirectory;
    }

    @Bean
    public ProxyCheckResponseWriter squidFormatWriter(File dataDirectory) throws IOException {
        return new SquidFormatWriter(dataDirectory);
    }

    @Bean
    public ProxyCheckResponseWriter plainFormatWriter(File dataDirectory) throws IOException {
        return new PlainFormatWriter(dataDirectory);
    }

    @Bean
    public List<ProxyCheckResponseWriter> proxyCheckResponseWriters(ProxyCheckResponseWriter... writers) {
        return Arrays.asList(writers);
    }


    @Bean
    public String ip() throws IOException {
        return AddressUtils.getMyPublicIp();
    }

    @Bean
    public int maxConcurrency() {
        return 1000;
    }

    @Bean
    public int overallTimeout() {
        return 60 * 1000;
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(10,
                new BasicThreadFactory.Builder().namingPattern("Timeout Scheduler-%d").build());
    }


}
