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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.ArrayList;
import java.io.StringReader;

import com.jute.fed4j.engine.response.ysm.ResultsType;
import com.jute.fed4j.engine.response.ysm.ResultSetType;
import com.jute.fed4j.engine.response.ysm.ListingType;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 18, 2008
 * Time: 3:38:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class YSMResponse extends HttpResponse {
    public List<YSMResult> results;
    private static JAXBContext jaxbContext = null;
    static {
        try {
            jaxbContext = JAXBContext.newInstance("com.jute.fed4j.engine.response.ysm");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
    
    public YSMResponse(int code,String body) {
        super(code,body);
    }

    public String toString(String position) {
        StringBuilder sb = new StringBuilder("<h2>Sponsor Results on "+position+":</h2>");
        sb.append("<ul>");
        boolean hasResults = false;
        if (results!=null) {
            for (YSMResult result : results) {
                String link = result.getLink(position);
                if (link != null) {
                    hasResults = true;
                    sb.append(result.toString(link));
                }
            }
        }
        if (!hasResults) {
            return "";
        }
        sb.append("</ul>");
        return sb.toString();
    }

    public String toString() {
        return toString("North");
    }

    public void unmarshal() {
        results = new ArrayList();
        try {
            Unmarshaller unmarshaller=jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(null);
            JAXBElement element = (JAXBElement) unmarshaller.unmarshal(new InputSource(new StringReader(body)));
            ResultsType root = (ResultsType) element.getValue();
            List<ResultSetType> resultSet = root.getResultSet();
            for (ResultSetType resultSetEntry: resultSet) {
                List<ListingType> lists = resultSetEntry.getListing();
                for (ListingType list: lists) {
                    YSMResult result = new YSMResult();
                    result.title = list.getTitle();
                    result.abs   = list.getDescription();
                    result.displayUrl = list.getSiteHost();
                    List<ListingType.ClickUrl> urls = list.getClickUrl();
                    for (ListingType.ClickUrl url: urls) {
                        if (url.getType().contains("South")) {
                            result.southLink = url.getValue();
                        } else if (url.getType().contains("East")) {
                            result.eastLink = url.getValue();
                        } else if (url.getType().contains("North")) {
                            result.northLink = url.getValue();
                        }
                    }
                    results.add(result);
                }
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public class YSMResult {
        public String title;
        public String northLink;
        public String southLink;
        public String eastLink;
        public String abs;
        public String displayUrl;


        public String getLink(String position) {
            if ("North".equals(position)) {
                return this.northLink;
            }
            if ("South".equals(position)) {
                return this.southLink;
            }
            if ("East".equals(position)) {
                return this.eastLink;
            }
            return null;
        }

        public String toString(String link) {
            StringBuilder sb = new StringBuilder();
            sb.append("<li>");
            sb.append(String.format("<a href=\"%s\">%s</a>",link,title));
            sb.append("<div>"+abs+"</div><em>");
            sb.append(displayUrl);
            sb.append("</em>");
            sb.append("</li>");
            return sb.toString();
        }
    }
}
