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
import com.jute.fed4j.engine.Workflow;
import com.jute.fed4j.engine.Component;
import com.jute.fed4j.engine.response.QSSResponse;
import com.jute.fed4j.engine.response.HttpResponse;
import com.jute.fed4j.engine.response.YSMResponse;
import com.jute.fed4j.engine.provider.YSMProvider;

import java.net.InetAddress;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 16, 2008
 * Time: 12:50:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class YSMComponent extends HttpComponent {
    YSMProvider ysm = new YSMProvider();

    public YSMComponent(String name) {
        super(name);
        connectTimeout = 500;
        readTimeout = 2000;
        timeout = 2000;

        try {
            this.enableProxy = InetAddress.getLocalHost().getHostName().equals("qa.gsp2.fed4j.corp.sk1.yahoo.com");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        this.proxyHost   = "build2.fed4j.corp.yahoo.com";
        this.proxyPort   = 80;
    }

    public boolean script(Workflow workflow) {
        ysm.query = workflow.getStringParameter("query");
        Component component = workflow.getComponent("QSS");
        if (component!=null) {
            if (!component.error) {
                try {
                    //blocking call
                    QSSResponse result = (QSSResponse) component.getResponse();
                    ysm.ip = workflow.getStringParameter("client_ip");
                    if (result!=null && result.hasSuggestion) {
                        ysm.query = result.suggestion;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        uri = ysm.getUri();
        return true;
    }

    public HttpResponse createResponse(int code, String body) {
        return new YSMResponse(code, body);
    }
}
