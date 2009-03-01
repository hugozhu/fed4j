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

import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.List;
import java.util.ArrayList;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 25, 2008
 * Time: 7:14:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class GossipResponse extends HttpResponse {
    public List<String> results;

    public GossipResponse(int code,String body) {
        super(code,body);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("");
        sb.append("<ul>Also Try: ");
        if (results!=null && results.size()>0) {
            try {
                for (String result : results) {
                    sb.append("<li>");
                    sb.append(String.format("<a href=\"?p=%s\">%s</a>", URLEncoder.encode(result,"UTF-8"),result));
                    sb.append("</li>");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else {
            return "";
        }
        sb.append("</ul>");
        return sb.toString();
    }

    public void unmarshal() {
        String xml = body;
        results = new ArrayList();
        try {
            InputSource inputSource = new InputSource(new StringReader(xml));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            inputSource.setEncoding("UTF-8");

            Document doc = db.parse(inputSource);
            NodeList resultNodes = doc.getElementsByTagName("m");
            for(int i=0;i<resultNodes.getLength();i++) {
                Element mNode = (Element) resultNodes.item(i);
                NodeList nodes = mNode.getElementsByTagName("s");
                if (nodes == null)
                    continue;
                for(int j=0;j<nodes.getLength();j++) {
                    results.add( ((Element) nodes.item(j)).getAttribute("k"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}