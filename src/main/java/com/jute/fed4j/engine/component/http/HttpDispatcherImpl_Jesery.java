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

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.jute.fed4j.engine.component.HttpComponent;

public class HttpDispatcherImpl_Jesery extends HttpDispatcher {
    private final static ClientConfig cc = new DefaultClientConfig();
    private final static Client client = Client.create(cc);

    WebResource resource = null; 

    public void init(HttpComponent component) {
        resource = client.resource(component.uri);
        resource.addFilter(new TimeoutFilter(component.connectTimeout,component.readTimeout));
        
        if (component.enablePersistentConnection) {
            //enableing persistente http connection
            //see: http://java.sun.com/j2se/1.4.2/docs/guide/net/properties.html
            System.setProperty("http.keepAlive","true");
            System.setProperty("http.maxConnections","500");
            System.setProperty("sun.net.http.errorstream.enableBuffering","true");
            System.setProperty("sun.net.http.errorstream.timeout","10");  //milliseconds
            System.setProperty("sun.net.http.errorstream.bufferSize","4096");
        }
        else {
            System.setProperty("http.keepAlive","false");
        }

        if (component.enableProxy) {
            if (component.proxyType.equals("http")) {
                System.setProperty("http.proxyHost",component.proxyHost);
                System.setProperty("http.proxyPort",component.proxyPort+"");
            }
            else {
                System.setProperty("socksProxyHost",component.proxyHost);
                System.setProperty("socksProxyPort",component.proxyPort+"");
            }
        }
    }

    public void run(HttpComponent component) {
        try {
            this.init(component);
            this.onConnect(component);
            this.onRead(component);
            this.onResponse(component, resource.head().getStatus(),resource.get(String.class));
        } catch (ClientHandlerException e) {
            onException(component,-2,"Error occurs during dispatch:"+e.getMessage());
        } catch (Exception e) {
            onException(component,-3,"Error occurs during parsing xml:"+e.getMessage());
        }
    }

}


class TimeoutFilter extends ClientFilter {
    int connectTimeout;
    int readTimeout;
    public TimeoutFilter(int x, int y) {
        connectTimeout = x;
        readTimeout = y;
    }

    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        cr.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectTimeout);
        cr.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, readTimeout);
        return getNext().handle(cr);
    }
}