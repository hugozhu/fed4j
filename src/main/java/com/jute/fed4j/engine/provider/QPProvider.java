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

package com.jute.fed4j.engine.provider;

import java.util.Map;
import java.util.HashMap;

/**
 * http://qp1.search.vip.sk1.yahoo.com:80/qp.php?src=web&site=yahoo&uq=qp&kg=US&intl=us&cy=us&ip=209.131.62.113&hloc=woeid%3A12797538&mq=qp&rr=rs%3A100
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 14, 2008
 * Time: 3:09:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class QPProvider extends HTTPProvider {

    public String source = "web";

    public String site = "yahoo";

    public String intl = "us";

    public String country = "US";

    public String location = "woeid:12797538";

    public String modifiedQuery = null;

    public QPProvider () {
        host = "qp1.fed4j.vip.sk1.yahoo.com";
        path = "/qp.php";
    }

    public Map<String,String> getQueryMap() {
        Map<String,String> map = new HashMap();
        map.put("src",source);
        map.put("site",site);
        map.put("kg",country);
        map.put("intl",intl);
        map.put("cy",intl);
        map.put("ip",ip);
        map.put("hloc",location);
        map.put("rr","rs:100");
        map.put("uq",query);

        if (modifiedQuery == null) {
            modifiedQuery = query;
        }

        map.put("mq",modifiedQuery);
        return map;
    }
}
