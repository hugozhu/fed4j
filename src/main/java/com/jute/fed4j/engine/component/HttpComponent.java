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

package com.jute.fed4j.engine.component;

import com.jute.fed4j.engine.Component;
import com.jute.fed4j.engine.ComponentType;
import com.jute.fed4j.engine.Workflow;
import com.jute.fed4j.engine.Response;
import com.jute.fed4j.engine.component.http.HttpDispatcher;
import com.jute.fed4j.engine.response.HttpResponse;

import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;

public abstract class HttpComponent extends Component {
    public String method = "GET";
    public String responseCharset = "UTF-8";

    public URI uri = null;
    public int connectTimeout = 10;
    public int readTimeout = 100;

    public long connectTime = -1;
    public long readTime = -1;
    public long transferTime = -1;
    public long unmarshalTime = -1;

    public long preRequestTimestamp = -1;
    public long postRequestTimestamp = -1;
    public long preUnmarshalTimestamp = -1;
    public long postUnmarshalTimestamp = -1;

    public boolean enableProxy = false;
    public String proxyType = "http"; //default proxy type
    public String proxyHost = null;
    public int proxyPort = 1080;       //default proxy port
   
    public boolean enablePersistentConnection = false;
    

    public HttpComponent(String name) {
        super(name, ComponentType.DATA);
    }

    public HttpResponse createResponse(int code,String body) {
        return new com.jute.fed4j.engine.response.HttpResponse(code, body);
    }

    public void dispatch (Workflow workflow) {
        if ("true".equals(System.getProperty("http.enablePersistentConnection"))) {
            enablePersistentConnection = true;
        }

        if (this.uri == null) {
            setResponse(createResponse(-1,"empty uri"));
            throw new IllegalArgumentException("empty uri");
        }
        else {
            HttpDispatcher dispatcher = HttpDispatcher.getInstance(workflow);
            dispatcher.run(this);
        }
    }

    public String dumpRequest() {
        StringBuffer sb = new StringBuffer("\n\n<b>Component: "+name+"</b>\n");
        if (uri==null) {
           sb.append("<empty request>");
           return sb.toString().replace("\n","<br/>");
        }
        sb.append(String.format("<b>Request:</b>\n<a href=\"%s\">%s</a>\n",uri,uri));
        sb.append("<b>Queries:</b>\n");
        try {
            String query = uri.toURL().getQuery();
            if (query!=null) {
                String[] parts = query.split("&");
                for(String part:parts) {
                    int pos = part.indexOf("=");
                    if (pos > -1) {
                        sb.append(part.substring(0,pos));
                        sb.append(": ");
                        sb.append(URLDecoder.decode(part.substring(pos+1),"UTF-8"));
                    }
                    else {
                        sb.append(part);
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString().replace("\n","<br/>");
    }

    public String dumpResponse() {
        Response response = getResponse();
        StringBuffer sb = new StringBuffer("\n\n<b>Component: "+name+"</b>\n");
        if (response==null) {
           sb.append("<empty response>");
           return sb.toString().replace("\n","<br/>");
        }
        sb.append("<b>Response:</b>\n");
        sb.append("<b>Latency:</b> "+ latency+"ms\n");
        sb.append("<b>Connect Time:</b> "+ connectTime+"ms\n");
        sb.append("<b>Read Time:</b> "+ readTime+"ms\n");
        sb.append("<b>Transfer Time:</b> "+ transferTime+"ms\n");
        sb.append("<b>Unmarshal Time:</b> "+ unmarshalTime+"ms\n");
        sb.append("<b>Code:</b> "+ response.getCode()+"\n");
        if( ((HttpResponse) response).headers !=null ) {
            sb.append("<b>Headers:</b>\n");
            for (Map.Entry entry: ((HttpResponse) response).headers.entrySet()) {
                sb.append(entry.getKey()+": "+escapeHTML(entry.getValue().toString())+"\n");
            }
        }
        sb.append("<b>Body:</b>\n"+ escapeHTML(((HttpResponse) response).body));
        return sb.toString().replace("\n","<br/>");
    }

    public static String escapeHTML(String string) {
        if (string==null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(string.length());
        // true if last char was blank
        boolean lastWasBlankChar = false;
        int len = string.length();
        char c;

        for (int i = 0; i < len; i++)
            {
            c = string.charAt(i);
            if (c == ' ') {
                // blank gets extra work,
                // this solves the problem you get if you replace all
                // blanks with &nbsp;, if you do that you loss
                // word breaking
                if (lastWasBlankChar) {
                    lastWasBlankChar = false;
                    sb.append("&nbsp;");
                    }
                else {
                    lastWasBlankChar = true;
                    sb.append(' ');
                    }
                }
            else {
                lastWasBlankChar = false;
                //
                // HTML Special Chars
                if (c == '"')
                    sb.append("&quot;");
                else if (c == '&')
                    sb.append("&amp;");
                else if (c == '<')
                    sb.append("&lt;");
                else if (c == '>')
                    sb.append("&gt;");
                else {
                    int ci = 0xffff & c;
                    if (ci < 160 )
                        // nothing special only 7 Bit
                        sb.append(c);
                    else {
                        // Not 7 Bit use the unicode system
                        sb.append("&#");
                        sb.append(new Integer(ci).toString());
                        sb.append(';');
                        }
                    }
                }
            }
        return sb.toString();
    }
}