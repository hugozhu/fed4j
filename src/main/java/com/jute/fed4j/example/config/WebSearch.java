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

package com.jute.fed4j.example.config;

import com.jute.fed4j.config.Configration;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Jan 12, 2009
 * Time: 3:54:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebSearch {
    Configration config = null;

    WebSearch() {
        config = new Configration();
        //set dimensions
        config.setDimensions(new String[] {"bucket","cluster","entry","intl"});

        config.addLookupOrder(new String[]{"%{bucket}","%{cluster}","%{entry}","uk"},new String[]{
                                "%{bucket}_%{cluster}",
                                "%{bucket}_%{entry}",
                                "%{bucket}_%{intl}",
                                "%{bucket}",
                                "%{cluster}_%{intl}",
                                "%{cluster}",
                                "%{intl}_%{entry}",
                                "%{entry}",
                                "%{intl}",
                                "EUR", //uk inherits all EUR (group of EU intls) setting
                                "base"
                                }
                            );

        config.addLookupOrder(new String[]{"%{bucket}","%{cluster}","%{entry}","%{intl}"},new String[]{
                                "%{bucket}_%{cluster}",
                                "%{bucket}_%{entry}",
                                "%{bucket}_%{intl}",
                                "%{bucket}",
                                "%{cluster}_%{intl}",
                                "%{cluster}",
                                "%{intl}_%{entry}",
                                "%{entry}",
                                "%{intl}",
                                "base"
                                }
                            );        
    }

    protected void config() {
        config.put("base","intl","");

        config.put("base","backend_memcache",new LinkedHashMap(){{
            put("failover",true);
            put("expires", 3600);
            put("port",11211);
            put("timeout",10); //10 milliseconds
            put("servers",new LinkedList() {{
                add("127.0.0.1");
                add("127.0.0.2");
                add("127.0.0.3");
                add("127.0.0.4");
                add("127.0.0.5");
                add("127.0.0.6");
                add("127.0.0.9");
                add("127.0.0.7");
            }});
        }});

        config.put("base","backend_yst",new LinkedHashMap(){{
            put("host","eagle-west-proxy.idp.inktomisearch.com");
            put("port",55556);
            put("path","/search");

            put("timeout",5000);
            put("connectTimeout",200);
        }});

        config.put("base","backend_qp",new LinkedHashMap(){{
            put("host","qp1.search.vip.sk1.yahoo.com");
            put("port",80);
            put("path","/qp.php");

            put("timeout",300);
            put("connectTimeout",100);
        }});

        config.put("base","backend_news",new LinkedHashMap(){{
            put("host","news.partner.yahoo.com");
            put("port",8075);
            put("path","/xml");
            put("hits",6);
            put("offset",0);

            put("timeout",300);
            put("connectTimeout",100);
        }});
    }
    
    public static Configration getConfigration() {    
        return WebSearchHolder.web.config;
    }
}

class WebSearchHolder {
    static WebSearch web = new WebSearch();
    static {
        web.config();
    }
}
