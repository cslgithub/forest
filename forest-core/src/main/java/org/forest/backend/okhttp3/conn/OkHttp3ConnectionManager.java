package org.forest.backend.okhttp3.conn;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.forest.backend.ForestConnectionManager;
import org.forest.exceptions.ForestRuntimeException;
import org.forest.http.ForestRequest;
import org.forest.ssl.*;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.util.concurrent.TimeUnit;

/**
 * @author gongjun[jun.gong@thebeastshop.com]
 * @since 2018-02-27 17:10
 */
public class OkHttp3ConnectionManager implements ForestConnectionManager {

    /**
     * connection pool
     */
    private ConnectionPool pool;

    public OkHttp3ConnectionManager() {
        init();
    }

    public void init() {
        pool = new ConnectionPool();
    }

    public X509TrustManager getX509TrustManager(ForestRequest request) {
        try {
            SSLKeyStore sslKeyStore = request.getKeyStore();
            if (sslKeyStore == null) {
                return new TrustAllManager();
            }
            return new ForestX509TrustManager(request.getKeyStore());
        } catch (Exception e) {
            throw new ForestRuntimeException(e);
        }
    }

    public OkHttpClient getClient(ForestRequest request) {
        Integer timeout = request.getTimeout();
        if (timeout == null) {
            timeout = request.getConfiguration().getTimeout();
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(pool)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS);

        if ("https".equals(request.getProtocol())) {
            SSLSocketFactory sslSocketFactory = SSLUtils.getSSLSocketFactory(request);
            builder
                    .sslSocketFactory(sslSocketFactory, getX509TrustManager(request))
                    .hostnameVerifier(TrustAllHostnameVerifier.DEFAULT);
        }

        return builder.build();
    }

}