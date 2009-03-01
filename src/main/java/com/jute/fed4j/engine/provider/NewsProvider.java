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
 * http://news.partner.yahoo.com:8075/xml?custid=yahoo%2Fus%2Fsc%2Fslotted&limlanguage=en&age=7d&search=shortcuts&dups=hide&query=java+&hits=6&offset=0&ocr=no&ranking=usrank&collapse=on&defidx=title
query:
  custid: yahoo/us/sc/slotted
  limlanguage: en
  age: 7d
  search: shortcuts
  dups: hide
  query: java
  hits: 6
  offset: 0
  ocr: no
  ranking: usrank
  collapse: on
  defidx: title
 */
public class NewsProvider extends HTTPProvider {
    public String customId = "yahoo/us/sc/slotted";
    public String language = "en";
    public String age = "7d";
    public String searchFor = "shortcuts";

    public boolean hideDulicate = true;
    public boolean collapse = true;
    public boolean ocr = false;

    public String ranking = "usrank";
    public String defaultIndex = "title";

    public NewsProvider () {
        host = "news.partner.yahoo.com";
        port = 8075;
        path = "/xml";
        hits = 6;
        offset = 0;
    }

   public Map<String,String> getQueryMap() {
       Map<String,String> map = new HashMap();
       map.put("custid",customId);
       map.put("limlanguage",language);
       map.put("age",age);
       map.put("fed4j",searchFor);
       map.put("query",query);
       map.put("hits",String.valueOf(hits));
       map.put("offset",String.valueOf(offset));
       map.put("ocr",ocr?"no":"yes");
       map.put("ranking",ranking);
       map.put("collapse",collapse?"on":"off");
       map.put("defidex",defaultIndex);
       return map;
    }

    public void config(Map map) {
        if (map.containsKey("hits")) {
            hits = (Integer) map.get("hits");
        }
        if (map.containsKey("offset")) {
            offset = (Integer) map.get("offset");
        }
    }
}
