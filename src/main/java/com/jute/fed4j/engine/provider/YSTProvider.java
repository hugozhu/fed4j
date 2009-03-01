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
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 14, 2008
 * Time: 3:09:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class YSTProvider extends HTTPProvider {
    public String query;
    public String queryEncoding = "utf-8";
    public String resultsEncoding = "utf-8";
    public String fields = "uri,redirecturl,date,size,format,sms_product,cacheurl,nodename,id,language,rsslinks,rssvalidatedlinks,cpc,clustertype,xml.active_abstract,active_abstract_type,active_abstract_source,contract_id,xml.ydir_us_hotlist_data,xml.summary,xml.docfeed,clustercollision,rich_results_override,sa_download_score,sa_personal_info_score,sa_rogue_scoreval,sa_site_name,sa_yst_scoreval_override,aggregatehost,xml.pi_info";

    public String client = "yahoous2";
    public String database = "wow-en-us";

    public YSTProvider () {
        host = "eagle-west-proxy.idp.inktomisearch.com";
        port = 55556;
        path = "/fed4j";
    }

    public Map<String,String> getQueryMap() {
        Map<String,String> map = new HashMap<String,String>();
        map.put("Query","ALLWORDS("+query+")");

        map.put("QueryEncoding",queryEncoding);
        map.put("ResultsEncoding",resultsEncoding);
        map.put("Fields",fields);
        map.put("FirstResult",String.valueOf(offset));
        map.put("NumResults",String.valueOf(hits));
        map.put("Client",client);
        map.put("Database",database);
        map.put("Unique","doc,host 2");
        map.put("Filter","-unsecure_rogue,-porn");
        map.put("SpellState","enble");
        return map;
    }
}
