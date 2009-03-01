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

import java.net.URI;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 14, 2008
 * Time: 3:09:02 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Provider {
    public String host = "";
    public int port = 80;
    public String path = "/";

    public String query=null;

    public String ip="127.0.0.1";
    
    public int timeout = 100;

    public int offset  = 0;

    public int hits    = 10;

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
