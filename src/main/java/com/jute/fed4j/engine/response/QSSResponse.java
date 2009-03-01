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

import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 18, 2008
 * Time: 1:30:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class QSSResponse extends HttpResponse {
    public boolean hasSuggestion = false;
    public String suggestion = null;
    public boolean shouldRewrite = false;

    public QSSResponse (int code, String response) {
        super(code, response);
    }

    public String toString() {
        if (!this.hasSuggestion) {
            return "";
        }
        else {
            return String.format("<div><h1>Did you mean: <a href=\"%s\">%s</a>?</h1></div>","?p="+suggestion,suggestion);
        }
    }

    public void unmarshal() {
    }    
}