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
import com.jute.fed4j.engine.provider.QSSProvider;
import com.jute.fed4j.engine.Workflow;
import com.jute.fed4j.engine.response.HttpResponse;
import com.jute.fed4j.engine.response.QSSResponse;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 17, 2008
 * Time: 2:11:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class QSSComponent extends HttpComponent {
    QSSProvider qss = new QSSProvider();
    
    public QSSComponent(String name) {
        super(name);
        connectTimeout = 100;
        readTimeout = 300;
    }

    public boolean script(Workflow workflow) {
        qss.query = workflow.getStringParameter("query");
        uri = qss.getUri();
        return true;
    }

    public HttpResponse createResponse(int code, String body) {
        return new QSSResponse(code, body);
    }
}
