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

package com.jute.fed4j.engine.component.http;

import com.jute.fed4j.engine.component.HttpComponent;
import com.jute.fed4j.engine.Response;
import com.jute.fed4j.engine.Workflow;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Dec 12, 2008
 * Time: 5:16:28 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class HttpDispatcher {

    public static HttpDispatcher getInstance(Workflow workflow) {
        if ("jesery".equals(workflow.getParameter("engine.http.client"))) {
            return new HttpDispatcherImpl_Jesery();

        }
        else { //default to use Jakarta
            return new HttpDispatcherImpl_Jakarta();
        }
    }

    public abstract void run(HttpComponent component);

    public void init(HttpComponent component) {
        
    }

    //connected
    public void onConnect(HttpComponent component) {
        component.preRequestTimestamp = System.currentTimeMillis();
    }

    //get the first byte
    public void onRead(HttpComponent component) {
        component.postRequestTimestamp = System.currentTimeMillis(); 
    }


    //response completed
    public void onResponse(HttpComponent component, int code, String body) throws Exception {
        if (body!=null) {
            Response response = component.createResponse(code,body);
            component.preUnmarshalTimestamp = System.currentTimeMillis();
            response.unmarshal(null);
            component.postUnmarshalTimestamp = System.currentTimeMillis();
            component.setResponse(response);
        }
        else {
            component.createResponse(-1,"Empty Content");
        }
    }

    public void onException(HttpComponent component, int code, String message) {
        component.error = true;
        component.setResponse(component.createResponse(code,message));
        component.logger.warning("["+component.name+"] "+message+" "+component.uri);
    }
}
