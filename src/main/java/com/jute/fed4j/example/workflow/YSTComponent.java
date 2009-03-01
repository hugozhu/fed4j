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

import com.jute.fed4j.config.Configration;
import com.jute.fed4j.engine.component.HttpComponent;
import com.jute.fed4j.engine.provider.YSTProvider;
import com.jute.fed4j.engine.Workflow;
import com.jute.fed4j.engine.response.HttpResponse;
import com.jute.fed4j.engine.response.YSTResponse;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 16, 2008
 * Time: 1:01:33 AM
 * To change this template use File | Settings | File Templates.
 */

public class YSTComponent extends HttpComponent {
    YSTProvider yst = new YSTProvider();

    public YSTComponent(String name) {
        super(name);
    }

    public boolean script(Workflow workflow) {
        Configration config = (Configration) workflow.getParameter("config");
        Map map = config.getMap("backend_yst");
        yst.config(map);
        yst.query = workflow.getStringParameter("query");

        this.connectTimeout = (Integer) map.get("connectTimeout");        
        this.timeout = this.readTimeout = yst.timeout;
        this.uri = yst.getUri();
        return true;
    }

    public HttpResponse createResponse(int code, String body) {
        return new YSTResponse(code, body);
    }
}
