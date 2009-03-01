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
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 18, 2008
 * Time: 11:47:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class YSTResponse extends HttpResponse {
    public List<YSTResult> results;

    public YSTResponse(int code,String body) {
        super(code,body);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("<h2>Web Search Results:</h2>");
        sb.append("<ul>");
        if (results!=null) {
            for (YSTResult result : results) {
                sb.append("<li>");
                sb.append(String.format("<a href=\"%s\">%s</a>",result.link,result.title));
                sb.append("<div>");
                sb.append(result.abs+"<br/>");
                sb.append("</div>");
                sb.append("<em>");
                sb.append(result.displayUrl);
                sb.append("</em>");
                sb.append("</li>");
            }
        }
        else {
            sb.append("No Results");
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
            NodeList resultNodes = doc.getElementsByTagName("RESULT");
            for(int i=0;i<resultNodes.getLength();i++) {
                Element resultNode = (Element) resultNodes.item(i);
                if (resultNode.getElementsByTagName("XML.SUMMARY") == null)
                    continue;
                Element summary = (Element) resultNode.getElementsByTagName("XML.SUMMARY").item(0);                
                YSTResult yst = new YSTResult();
                yst.title = summary.getElementsByTagName("title").item(0).getTextContent();
                yst.displayUrl = summary.getElementsByTagName("dispurl").item(0).getTextContent();
                yst.abs = summary.getElementsByTagName("abstract").item(0).getTextContent();


                Element element = (Element) resultNode.getElementsByTagName("REDIRECTURL").item(0);
                yst.link = element.getTextContent();
                results.add(yst);
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public class YSTResult {
        public String title;
        public String link;
        public String abs;
        public String displayUrl;
    }
}