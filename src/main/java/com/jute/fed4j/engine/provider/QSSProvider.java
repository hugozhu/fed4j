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
 * Date: Nov 17, 2008
 * Time: 2:12:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class QSSProvider extends YSTProvider {
    public String fields = "suggestion,annotatedsuggestion,rwsuggestion,annotatedrwsuggestion,spelltoken";
    public String queryLanguage = "English";
    public String spellDatabase = "spellchecker";

    public QSSProvider () {
        super();
        host = "dest-yahoo3.idp.inktomisearch.com";
        path = "/SPELL";
    }

    public Map<String,String> getQueryMap() {
        Map<String,String> map = new HashMap();
        map.put("Query","ALLWORDS("+query+")");

        map.put("QueryLanguage",queryLanguage);
        map.put("QueryEncoding",queryEncoding);
        map.put("Fields",fields);
        map.put("Client",client);
        map.put("Spelldatabase",spellDatabase);
        map.put("spellexcludeenable","2");
        map.put("SpellState","enble");
        return map;
    }
}