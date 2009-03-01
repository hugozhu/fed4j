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
 * http://us-iy1.search.vip.sk1.yahoo.com:80/us/iy.web.php?ei=UTF-8&eo=UTF-8&p=1+1&o=xml&intl=us&ult=2&q=1%2B1&sck=1%2B1&sct=calculator&testid=H181&src=web&fefr=inq-x-ff
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 18, 2008
 * Time: 11:21:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class IYProvider extends HTTPProvider {

    public String source = "web";

    public String intl = "us";

    public String output = "xml";

    public String shortcut = "";

    public String modifiedQuery = null;

    public IYProvider () {
        host = "us-iy1.fed4j.vip.sk1.yahoo.com";
        path = "/us/iy.web.php";
    }

    public Map<String,String> getQueryMap() {
        Map<String,String> map = new HashMap();
        map.put("o",output);
        map.put("intl",intl);
        map.put("ei","UTF-8");
        map.put("eo","UTF-8");
        map.put("o",output);
        map.put("q",query);
        if (modifiedQuery==null) {
            modifiedQuery = query;
        }
        map.put("p",modifiedQuery);
        map.put("sct",shortcut);
        map.put("sck",query);
        map.put("source",source);
        map.put("ult","2");
        return map;
    }

}
