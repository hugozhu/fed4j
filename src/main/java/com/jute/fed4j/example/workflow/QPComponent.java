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
import com.jute.fed4j.engine.provider.QPProvider;
import com.jute.fed4j.engine.Workflow;
import com.jute.fed4j.engine.response.HttpResponse;
import com.jute.fed4j.engine.response.QPResponse;
import com.jute.fed4j.config.Configration;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 16, 2008
 * Time: 12:50:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class QPComponent extends HttpComponent {
    QPProvider qp = new QPProvider();

    public QPComponent(String name) {
        super(name);
        connectTimeout = 100;
        readTimeout = 300;
        timeout = 500;
    }

    public boolean script(Workflow workflow) {
        Configration config = (Configration) workflow.getParameter("config");
        Map map = config.getMap("backend_qp");
        qp.config(map);

        this.connectTimeout = (Integer) map.get("connectTimeout");
        this.timeout = this.readTimeout = qp.timeout;        

        qp.query = workflow.getStringParameter("query");
        this.uri = qp.getUri();
        return true;
    }

    public HttpResponse createResponse(int code, String body) {
        return new QPResponse(code, body);
    }

    public void dispatch (Workflow workflow) {
// simulate slow dispatch for QP        
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
        super.dispatch(workflow);
    }
}
