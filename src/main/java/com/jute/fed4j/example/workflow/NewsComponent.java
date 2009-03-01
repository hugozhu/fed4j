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
import com.jute.fed4j.engine.provider.NewsProvider;
import com.jute.fed4j.engine.Workflow;
import com.jute.fed4j.engine.response.HttpResponse;
import com.jute.fed4j.engine.response.NewsResponse;
import com.jute.fed4j.config.Configration;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Dec 13, 2008
 * Time: 3:19:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewsComponent extends HttpComponent {
    NewsProvider news = new NewsProvider();

    public NewsComponent(String name) {
        super(name);
        connectTimeout = 100;
        readTimeout = 500;
        timeout = 500;
        
        enableProxy = false;
        proxyType   = "socks";
        proxyHost   = "socks.yahoo.com";
        proxyPort   = 1080;
    }

    public boolean script(Workflow workflow) {
        Configration config = (Configration) workflow.getParameter("config");
        news.config(config.getMap("backend_news"));
        this.connectTimeout = (Integer) config.getMap("backend_news").get("connectTimeout");
        
        this.timeout = this.readTimeout = news.timeout;

        news.query = workflow.getStringParameter("query");
        uri = news.getUri();
        return true;
    }

    public HttpResponse createResponse(int code, String body) {
        return new NewsResponse(code, body);
    }
}
