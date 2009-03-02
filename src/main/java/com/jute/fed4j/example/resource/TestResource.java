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

package com.jute.fed4j.example.resource;

import com.jute.fed4j.engine.Workflow;
import com.jute.fed4j.engine.Component;
import com.jute.fed4j.engine.WorkflowEngine;
import com.jute.fed4j.engine.component.HttpComponent;
import com.jute.fed4j.engine.component.JoinComponent;

import com.jute.fed4j.example.workflow.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 16, 2008
 * Time: 12:28:58 AM
 * To change this template use File | Settings | File Templates.
 */
@Path("/")
@Produces("text/html;charset=UTF-8")
public class TestResource {
    @Context
    Request request;
    static String VERSION="$Id$";

    public TestResource() {

    }

    @GET
    @Path("/test")
    public String test(@Context UriInfo ui) {
        String ip = "";

        //hack to get request ip
        Field[] fields = request.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals("request")) {
                fields[i].setAccessible(true);
                try {
                    ip = ((HttpServletRequest) fields[i].get(request)).getRemoteAddr();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        //get query parameter from http request
        String query = ui.getQueryParameters().getFirst("p");
        if (query == null) {
            query = "Java";
        }


        //detect switches
        if (ui.getQueryParameters().get("disableThreadPool")!=null) {
            WorkflowEngine.enableThreadPool = false;
        }

        if (ui.getQueryParameters().get("enablePersistentConnection")!=null) {
            System.setProperty("http.enablePersistentConnection","true");
        }
        else {
            System.setProperty("http.enablePersistentConnection","false");
        }

        //building workflow
        Workflow workflow = new Workflow();

        //data binding
        workflow.setParameter("query", query);
        workflow.setParameter("client_ip", ip);
        workflow.setParameter("ui",ui);

        if (ui.getQueryParameters().get("http_client")!=null) {
            workflow.setParameter("engine.http.client",ui.getQueryParameters().getFirst("http_client"));
        }

        //start building static workflow, but F0Component can still add components dynamically
        workflow.init("start");
        workflow.addComponent("start", new F0Component("f0"));

        WebSearchComponent webSearch = new WebSearchComponent("WebSearch");
        TranslationComponent translation = new TranslationComponent("Translation");
        WebSearchComponent webSearch2 = new TranslatedWebSearchComponent("TranslatedWebSearch");

        workflow.addComponent("f0",webSearch);
        workflow.addComponent("f0",translation);
        workflow.addComponent("Translation",webSearch2);

//        Component j9 = new J9Component("j9");
//        workflow.addComponent("WebSearch",j9);
//        workflow.addComponent("TranslatedWebSearch",j9);
        //finish static workflow building

        //dispatch
        WorkflowEngine engine = new WorkflowEngine(workflow);
        engine.setMaxExecutionTime(5000); //max execution time in milliseconds
        engine.setBlocking(false);        //run will not be blocked
        engine.run();                     //start executing the workflow

        /**
         * possible to add some business logic here while engine is running
         */
        
        engine.awaitForFinishing();      //block until workflow is finished   

        //get response
 
        //output
        StringBuilder result = new StringBuilder("<html><head>");

        buildStyles(result);

        result.append("</head>");
        result.append("<body>");

        buildHead(result);
        buildSearchBox(result,workflow);

        result.append("<hr/>");
        result.append("<div id=\"as\">");
        result.append("</div>");

        result.append("<div id=\"res\">");

        result.append("<table><tr><td width=\"50%\" valign=\"top\">");

        result.append("\n"+webSearch.toString());

        result.append("</td><td width=\"50%\" valign=\"top\">");
        result.append("<div>"+translation.toString()+"</div>");        
        result.append("\n"+webSearch2.toString());        
        result.append("</td></tr></table>");

        //debug
        if (ui.getQueryParameters().getFirst("debug") != null) {
            if (ui.getQueryParameters().getFirst("debug").contains("waterfall") || ui.getQueryParameters().get("debug").contains("waterfall")) {
                result.append(String.format("<hr><img src=\"data:image/png;base64,%s\"", engine.getWaterfallImage()));
            }

            if (ui.getQueryParameters().getFirst("debug").contains("timetable") || ui.getQueryParameters().get("debug").contains("timetable")) {
                result.append(engine.getTimetable());
            }

            if (ui.getQueryParameters().getFirst("debug").contains("dot") || ui.getQueryParameters().get("debug").contains("dot")) {
                result.append("<hr>");
                result.append("<p><b>DOT Graph:</b>");
                result.append("<form action=\"http://graph.gafol.net/create\" method=\"post\" target=\"_blank\">");
                result.append("<textarea name=\"graph\" rows=\"5\" cols=\"80\">");
                result.append(engine.getDot(String.format("Query: %s - %sms",query, workflow.getDuration())));
                result.append("</textarea><br/>");
                result.append("<input type=\"hidden\" name=\"layout\" value=\"dot\"/>");
                result.append("<input type=\"hidden\" name=\"private\" value=\"1\"/>");
                result.append("<input type=\"submit\" name=\"preview\" value=\"View Graph\"/>");
                result.append("</form><br/>");
            }

            if (ui.getQueryParameters().getFirst("debug").contains("req") || ui.getQueryParameters().get("debug").contains("req")) {
                result.append("<hr>");
                for (Component component : workflow.getComponents().values()) {
                    if (component instanceof HttpComponent) {
                        result.append(((HttpComponent) component).dumpRequest());
                        result.append("\n");
                    }
                }
            }

            if (ui.getQueryParameters().getFirst("debug").contains("res") || ui.getQueryParameters().get("debug").contains("res")) {
                result.append("<hr>");
                for (Component component : workflow.getComponents().values()) {
                    if (component instanceof HttpComponent) {
                        result.append(((HttpComponent) component).dumpResponse());
                        result.append("\n");
                    }
                }
            }
        }
        result.append("</div>"); //id=res
        result.append("</body></html>");
        return result.toString();
    }

    protected void buildStyles(StringBuilder result) {
        result.append("<style>");
        result.append("li{margin-bottom:10px}");
        result.append("body{font:small/1.2em arial,helvetica,clean,sans-serif;font:x-small;}");
        result.append("div#res h2{font:0.8em arial}");
        result.append("div#as ul {margin:0;padding:10px 5px;font-weight:bold}");
        result.append("div#as li {margin:0;padding:0 5px;list-style-type:none;display:inline}");
        result.append("div#newsdd div {margin-bottom:5px}");
        result.append("div#res div#newsdd li {font-size:0.76em;padding:0;margin:0 0 2px}");
        result.append("</style>");
    }

    protected void buildHead(StringBuilder result) {
        result.append("<h1>fed4j demo</h1><div align=\"right\"><em>Version: "+VERSION+"</em></div>");
    }

    protected void buildSearchBox(StringBuilder sb,Workflow workflow) {
        String query = workflow.getStringParameter("query");
        UriInfo uriInfo = (UriInfo) workflow.getParameter("ui");
        if (query == null) {
            query = "2*3*9";
        }
        else {
            query = HttpComponent.escapeHTML(query);
        }
        sb.append("<form action=\"\" method=\"get\">");
        sb.append("<input type=\"text\" name=\"p\" value=\""+query+"\" size=\"80\"/>");
        sb.append("<input type=\"submit\" name=\"s\" value=\"Search\"/>");
        String[] names = {"waterfall","timetable","dot","req","res"};
        sb.append("<div> Debug Options:");
        for (String name: names) {
            String checked = "";
            if (uriInfo.getQueryParameters().get("debug") != null) {
                if (uriInfo.getQueryParameters().get("debug").contains(name) || uriInfo.getQueryParameters().getFirst("debug").contains(name)) {
                    checked = "checked";
                }
            }
            sb.append("<input type=\"checkbox\" name=\"debug\" "+checked+" value=\""+name+"\"/>"+name+" ");
        }
        sb.append("</div>");

        String[] features = {"enablePersistentConnection", "disableThreadPool"};
        sb.append("<div> Feature Switches:");
        for (String name: features) {
            String checked = "";
            if (uriInfo.getQueryParameters().get(name) != null) {
                checked = "checked";
            }
            sb.append("<input type=\"checkbox\" name=\""+name+"\" "+checked+" value=\"1\"/>"+name+" ");
        }

        String[] clients = {"jakarta", "jesery","jetty"};
        sb.append("<div> HttpClient impl:");
        for (String name: clients) {
            String checked = "";
            if (name.equals(uriInfo.getQueryParameters().getFirst("http_client")!=null?uriInfo.getQueryParameters().getFirst("http_client"):"jakarta")) {
                checked = "checked";
            }
            sb.append("<input type=\"radio\" value=\""+name+"\" "+checked+" name=\"http_client\"/>"+name+" ");
        }
        sb.append("</div>");        
        sb.append("</form>");
    }
}