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

package com.jute.fed4j.example.response.yahoo;

import com.jute.fed4j.engine.response.HttpResponse;
import com.jute.fed4j.example.response.yahoo.websearch.ResultSet;
import com.jute.fed4j.example.response.yahoo.websearch.ResultType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.InputStream;
import java.util.*;
import java.math.BigInteger;

/**
 * Author: Hugo Zhu on  2009-3-2 10:32:32
 */
public class WebSearchResponse extends HttpResponse {
    private ResultSet result;
    private static JAXBContext jaxbContext = null;

    static {
        try {
            jaxbContext = JAXBContext.newInstance("com.jute.fed4j.example.response.yahoo.websearch");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public WebSearchResponse (int code, String response) {
        super(code, response);
    }

    /**
     * a simple way to render presentation, in actual production, we should return entity list instead
     * @return String
     */
    public String toHtml() {
        StringBuilder sb = new StringBuilder("<h2>Web Search Results:</h2>");
        if (result!=null) {
            int end = result.getFirstResultPosition().add(result.getTotalResultsReturned()).intValue()-1;
            sb.append(String.format("Display %s - %s of %s results",result.getFirstResultPosition(),end,result.getTotalResultsAvailable()));
            List<ResultType> resList = result.getResult();
            for (ResultType res:resList) {
                String title = res.getTitle();
                String url   = res.getUrl();
                String summary = res.getSummary();
                String displayUrl = res.getDisplayUrl();
                sb.append(String.format("<li><a href=\"%s\">%s</a><div>%s</div><em>%s</em></li>",url,title,summary,displayUrl));
            }
        }
        else {
            sb.append("No Results");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    public String toString() {
        return "WebSearch Response:"+result;
    }

    public void unmarshal(InputStream in) {
        if (jaxbContext == null) {
            return;
        }
        try {
            Unmarshaller unmarshaller=jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(null);
            result = (ResultSet) unmarshaller.unmarshal(new InputSource(new StringReader(body)));
        } catch (JAXBException e) {
            log.error("Failed to unmarshal WebSearch Response",e);
        }
    }    
}
