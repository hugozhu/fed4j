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

package com.jute.fed4j.example.provider.yahoo;

import com.jute.fed4j.engine.provider.HttpProvider;

import java.util.Map;
import java.util.HashMap;

/**
 * API: http://developer.yahoo.com/search/web/V1/webSearch.html
 * eg: http://search.yahooapis.com/WebSearchService/V1/webSearch?appid=5ShQ8P3V34GqsC9ekWuQSSRSrS3pghYGLA9PWF.T_.8Mb.Prvh5ZWLFbn3aeGVqL5Q--&query=madonna&results=2
 */
public class WebSearch extends HttpProvider {
    String appid = "5ShQ8P3V34GqsC9ekWuQSSRSrS3pghYGLA9PWF.T_.8Mb.Prvh5ZWLFbn3aeGVqL5Q--";

    public WebSearch() {
        host = "search.yahooapis.com";
        port = 80;
        path = "/WebSearchService/V1/webSearch";
    }

    public Map<String, String> getQueryMap() {
        Map<String,String> map = new HashMap<String,String>();
        map.put("query",query);
        map.put("appid",appid);
        map.put("results",String.valueOf(hits));
        return map;
    }
}
