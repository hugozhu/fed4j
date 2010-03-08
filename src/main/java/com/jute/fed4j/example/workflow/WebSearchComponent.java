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

package com.jute.fed4j.example.workflow;

import com.jute.fed4j.engine.component.HttpComponent;
import com.jute.fed4j.engine.Provider;
import com.jute.fed4j.engine.Workflow;
import com.jute.fed4j.engine.Response;
import com.jute.fed4j.engine.response.HttpResponse;
import com.jute.fed4j.example.provider.yahoo.WebSearchProvider;
import com.jute.fed4j.example.response.yahoo.WebSearchResponse;

/**
 * Author: Hugo Zhu on  2009-3-1 21:26:21
 */
public class WebSearchComponent extends HttpComponent {
    protected Provider provider = null;
    
    public WebSearchComponent(String name) {
        super(name);
        this.connectTimeout = 1000; 
        this.readTimeout = 5000;
        provider = new WebSearchProvider();
    }

    @Override
    public boolean script(Workflow workflow) {
        this.provider.setQuery(workflow.getStringParameter("query"));
        this.uri = this.provider.getUri();
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (response!=null && response.getCode()==200) {
            return ((WebSearchResponse)response).toHtml();
        }
        else {
            sb.append("");
        }
        return sb.toString(); 
    }

    @Override
    public HttpResponse createResponse(int code, String body) {
        return new WebSearchResponse(code, body);
    }
}
