/*
 * Copyright (C) <2009>  Hugo Zhu <contact@hugozhu.info>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jute.fed4j.engine.component.http;

import com.jute.fed4j.engine.component.HttpComponent;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.*;
import org.apache.http.util.EntityUtils;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Dec 12, 2008
 * Time: 5:56:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class HttpDispatcherImpl_Jakarta extends HttpDispatcher {
    private static ClientConnectionManager sharedConnectionManager = null;
    private static SchemeRegistry schemeRegistry = new SchemeRegistry();


    static {
        schemeRegistry.register(
                new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(
                new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));     
    }


    private final static Object locker = new Object();
    
    /**
     * get a connection manager
     * @param params
     * @return
     */
    private ClientConnectionManager getConnectionManager(HttpParams params, boolean enablePersistentConnection) {
        ClientConnectionManager connectionManager;
        if (enablePersistentConnection) {
            //be careful, somehow the ThreadSafeClientConnManager dosn't perform well with high traffic
            if (sharedConnectionManager==null) {
                synchronized (locker) {
                    if (sharedConnectionManager==null) {
                        ConnManagerParams.setMaxTotalConnections(params, 100);
                        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                        ConnManagerParams.setTimeout(params, 10);
                        sharedConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
                    }
                }
            }
            connectionManager = sharedConnectionManager;
        }
        else {
            connectionManager = new SingleClientConnManager(params,schemeRegistry);
        }
        return connectionManager;
    }

    private HttpComponent commponent;

    public void run(HttpComponent component) {
        this.commponent = component;
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, component.connectTimeout);
        HttpConnectionParams.setSoTimeout(params, component.readTimeout);

        try {
            this.init(component);
            HttpClient httpclient = new YfedHttpClient(getConnectionManager(params,component.enablePersistentConnection), params);
            if (component.enableProxy && "http".equals(component.proxyType)) {
                HttpHost proxy = new HttpHost(component.proxyHost, component.proxyPort, component.proxyType);
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }
            HttpUriRequest request = new HttpRequest(component.method,component.uri);
            YfedHttpResponseHandler responseHandler = new YfedHttpResponseHandler(component.responseCharset);
            String body = httpclient.execute(request, responseHandler);
            this.onResponse(component,responseHandler.code,body);
        } catch (SocketTimeoutException e) {
            onException(component,-2, " socket timeout error occurs: "+e.getMessage());
        } catch (ClientProtocolException e) {
            onException(component,-3, " error resposed from server: "+e.getMessage());
        }  catch (IOException e) {
            onException(component,-4, " error occurs during dispatch: "+e.getMessage());
        }
        catch (Exception e) {
            onException(component,-5, "error occurs during parsing xml:"+e.getMessage());
        }
    }

    static class YfedClientConnManager extends ThreadSafeClientConnManager {
        public YfedClientConnManager(HttpParams params, SchemeRegistry schreg) {
            super(params,schreg);
        }
    }

    class YfedHttpClient extends DefaultHttpClient {
        public YfedHttpClient(
                final ClientConnectionManager conman,
                final HttpParams params) {
            super(conman, params);
        }

        @Override
        protected HttpRequestExecutor createRequestExecutor() {
            return new YfedHttpRequestExecutor();
        }

        /**
         * @todo
         * @return
         */
        @Override
        protected HttpRoutePlanner createHttpRoutePlanner() {
            return new DefaultHttpRoutePlanner
                (getConnectionManager().getSchemeRegistry());
        }
    }

    class YfedHttpRequestExecutor extends HttpRequestExecutor {
        
        @Override
        public void preProcess(
                final org.apache.http.HttpRequest request,
                final HttpProcessor processor,
                final HttpContext context)
                    throws HttpException, IOException {
            onConnect(commponent);
            super.preProcess(request,processor,context);
        }

        @Override
        public void postProcess(HttpResponse response, HttpProcessor processor, HttpContext context) throws HttpException, IOException {
            onRead(commponent);
            super.postProcess(response, processor, context);
        }
    }

    class YfedHttpResponseHandler implements ResponseHandler<String> {
        int code;
        Header[] headers;
        private String responseCharset = null;

        YfedHttpResponseHandler(String responseCharset) {
            this.responseCharset = responseCharset;
        }

        /**
        * Returns the response body as a String if the response was successful (a
        * 2xx status code). If no response body exists, this returns null. If the
        * response was unsuccessful (>= 300 status code), throws an
        * {@link org.apache.http.client.HttpResponseException}.
        */
       public String handleResponse(final HttpResponse response)
               throws HttpResponseException, IOException {
           StatusLine statusLine = response.getStatusLine();
           code = statusLine.getStatusCode();
           headers = response.getAllHeaders();

           if (statusLine.getStatusCode() >= 300) {
               throw new HttpResponseException(statusLine.getStatusCode(),
                       statusLine.getReasonPhrase());
           }

           HttpEntity entity = response.getEntity();
           return entity == null ? null : EntityUtils.toString(entity, responseCharset);
       }
    }    
}

class HttpRequest extends HttpRequestBase {
    String method;

    public HttpRequest() {
        super();
    }

    public HttpRequest(String method, final URI uri) {
        super();
        this.method = method;
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return method;
    }
}