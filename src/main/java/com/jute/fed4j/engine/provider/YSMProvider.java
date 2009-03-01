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
 * Time: 3:09:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class YSMProvider extends HTTPProvider {
    public String partner = "yahoo_mozilla_moz2_trans_us";

    public String intl    = "us";

    public String affilData = "ua=Mozilla%2F5.0+%28Macintosh%3B+U%3B+Intel+Mac+OS+X+10.5%3B+en-US%3B+rv%3A1.9.0.4%29+Gecko%2F2008102920+Firefox%2F3.0.4&uref=http%3A%2F%2Fsearch.yahoo.com%2Fsearch%3Fp%3Dqp%26amp%3Bei%3DUTF-8%26amp%3Bfr%3Dmoz2%26amp%3Bdebug%3Dreq&uid=2lp7ru54fhks0%26b%3D4%26d%3DzA7d54hpYEI0plg.rGgcMgXEPm9v5FHh6RcaOg--%26s%3Due&uts=1224266752";

    public String inputEncoding = "utf8";

    public String outputEncoding = "utf8";


    public YSMProvider () {
        host = "yahoo-west.overture.com";
        path = "/d/fed4j/p/yahoo/xml/us/production/v8/";
    }

    public Map<String,String> getQueryMap() {
        Map<String,String> map = new HashMap<String,String>();
        map.put("Keywords",query);
        map.put("Partner",partner);
        map.put("mkt",intl);
        map.put("affilData","ip="+ip+"&"+affilData);
        map.put("outputCharEnc",outputEncoding);
        map.put("keywordCharEnc",inputEncoding);
        map.put("QSSUsed","0");
        map.put("comm","true");
        map.put("commMax","8");
        map.put("commMin","6");
        map.put("start",String.valueOf(offset+1));
        map.put("bolding","true");
        map.put("kkClient","yahoouskingkong");
        map.put("hloc","woeid:12797538");
        map.put("serveUrl","http://fed4j.yahoo.com/fed4j");
        map.put("reginfo","01;M;30-34;us;95054");
        return map;
    }
}
