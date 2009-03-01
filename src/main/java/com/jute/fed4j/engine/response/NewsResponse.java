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

import com.jute.fed4j.engine.response.news.RESULTSET;
import com.jute.fed4j.engine.response.news.RESULT;
import com.jute.fed4j.engine.response.news.THUMBNAIL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Dec 13, 2008
 * Time: 3:28:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewsResponse  extends HttpResponse {
    public List<NewsResult> results;

    private static JAXBContext jaxbContext = null;

    static {
        try {
            jaxbContext = JAXBContext.newInstance("com.jute.fed4j.engine.response.news");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public NewsResponse (int code, String response) {
        super(code, response);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>NewsDD Results:</h2>");
        sb.append("<div id=\"newsdd\"");
        sb.append("<table width=100%><tr valign=top><td align=center width=\"65\">");
        for(NewsResult news:results) {
            if (news.images!=null) {
                sb.append(String.format("<div><a href='%s' title='%s'>%s</a></div>",news.link,news.title,news.images[0]));
            }
        }
        sb.append("</td><td>");
        for(NewsResult news:results) {
           sb.append(news);
        }
        sb.append("</td></tr></table>");
        sb.append("</div>");
        return sb.toString();
    }

    public void unmarshal() throws Exception {
        Unmarshaller unmarshaller=jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(null);
        RESULTSET resultset = (RESULTSET) unmarshaller.unmarshal(new InputSource(new StringReader(body)));
        if(resultset!=null && resultset.getRESULT()!=null) {
            results = new ArrayList(resultset.getRESULT().size());
            for(RESULT res:resultset.getRESULT()) {
                NewsResult result = new NewsResult();
                result.title = res.getTITLE();
                result.link  = res.getURL();
                result.abs   = res.getABSTRACT();
                result.source= res.getSOURCENAME();
                result.sourceLink = res.getSOURCEURL();
                result.language = res.getLANGUAGE();
                result.date = res.getPUBDATE().intValue();
                if (res.getMULTIMEDIA()!=null) {
                    if (res.getMULTIMEDIA().getTHUMBNAIL()!=null) {
                        result.images = new ImageResult[res.getMULTIMEDIA().getTHUMBNAIL().size()];
                        for (int i=0;i<result.images.length;i++) {
                            result.images[i] = new ImageResult(res.getMULTIMEDIA().getTHUMBNAIL().get(i));
                        }
                    }
                }
                results.add(result);
            }
        }
    }

    public class NewsResult {
        public String title;
        public String link;
        public String abs;
        public String source;
        public String sourceLink;
        public String language;
        public int date;
        public ImageResult[] images;

        public String getPubishDateString(int now) {
            int diff = now - date;
            if (diff <= 60) {
                return "just now";
            }

            if (diff < 3600) {
                return String.format("%d miniutes ago",diff/60);
            }

            if (diff < 3600*24) {
                return String.format("%d hours ago",diff/3600);
            }

            return String.format("%d days ago",diff/(3600*24) );
        }

        public String toString() {
            int now = (int) (System.currentTimeMillis()/1000l);
            StringBuilder sb = new StringBuilder();
            sb.append("<li>");
            sb.append(String.format("<a href=\"%s\">%s</a> - <small>%s</small><br/><small>from: <a href=\"%s\">%s</a></small>",
                        link,title,getPubishDateString(now),
                        sourceLink,source));
            sb.append("</li>");
            return sb.toString();
        }
    }

    public class ImageResult {
        public int width;
        public int height;
        public String url;
        public ImageResult(THUMBNAIL thumb) {
            width = thumb.getWIDTH().intValue();
            height = thumb.getHEIGHT().intValue();
            url  = thumb.getValue();
        }
        public String toString() {
            return String.format("<img border=\"0\" src=\"%s\" width=\"%d\" height=\"%d\"/>",url,width,height);
        }
    }
}