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

package com.jute.fed4j.example;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 16, 2008
 * Time: 12:22:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    private static int getPort(int defaultPort) {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(getPort(9998)).build();
    }

    public static final URI BASE_URI = getBaseURI();

    protected static SelectorThread startServer() throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages", "com.jute.fed4j.example.resource");

        System.out.println("Starting grizzly...");
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(BASE_URI, initParams);
        return threadSelector;
    }

    public static void main(String[] args) throws IOException {
        SelectorThread threadSelector = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nYou can try: %s \nHit enter 'q' to stop it...",
                BASE_URI, "http://localhost:9998/test?p=1%2B1"));
        char c = (char) System.in.read();
        while(c!='q') {
            c = (char) System.in.read();
        }
        threadSelector.stopEndpoint();
        System.exit(0);
    }
}
