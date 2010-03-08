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

import com.jute.fed4j.engine.Provider;

import java.util.Map;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;

public abstract class HttpProvider extends Provider {
    protected String schema="http";
    protected String hash = null;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public abstract Map<String,String> getQueryMap();

    public URI getUri() {
        Map<String,String> queries = getQueryMap();
        StringBuffer query = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : queries.entrySet()) {
                if (entry.getValue()==null) {
                    continue;
                }
                query.append("&");
                query.append(entry.getKey()+"="+ URLEncoder.encode(entry.getValue(), inputEncoding));
            }
            if (query.length()>0) {
                query.replace(0,1,"?");
            }
            if (hash!=null) {
                query.append("#"+hash);
            }
            URL url = new URL(schema,host,port,path + query.toString());
            return url.toURI();
        } catch (Exception e) {
            log.fatal("Failed to generate Uri for provider",e);
        }
        return null;
    }
}
