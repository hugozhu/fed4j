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

package com.jute.fed4j.engine.response;

import com.jute.fed4j.engine.Response;

import java.util.Map;

public class HttpResponse implements Response {
    public int code;
    public String body;
    public Map<String,String> headers;    
    public boolean ok = true;

    public HttpResponse(int code,String body) {
        this.body = body;
        this.code = code;
    }

    public String getBody() {
        return body;
    }

    public void unmarshal() throws Exception {

    }

    public String toString() {
        return body;
    }

    public int getCode() {
        return code;
    }
}
