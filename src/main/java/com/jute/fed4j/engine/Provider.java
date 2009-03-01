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

package com.jute.fed4j.engine;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.net.URI;
import java.util.Map;

public abstract class Provider {
    protected Log log = LogFactory.getLog(this.getClass());

    protected String host = "";

    protected int port = 80;

    protected String path = "/";

    protected String query=null;

    protected String ip="127.0.0.1";
    
    protected int timeout = 100;

    protected int offset  = 0;

    protected int hits    = 10;

    protected String inputEncoding = "UTF-8";

    protected String outputEncoding = "UTF-8";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public String getInputEncoding() {
        return inputEncoding;
    }

    public void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
    }

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public abstract URI getUri();

    public void config(Map map) {
        if (map.containsKey("host")) {
            host = (String) map.get("host");
        }
        if (map.containsKey("path")) {
            path = (String) map.get("path");
        }
        if (map.containsKey("port")) {
            port = (Integer) map.get("port");
        }
        if (map.containsKey("timeout")) {
            timeout = (Integer) map.get("timeout");
        }
    }
}
