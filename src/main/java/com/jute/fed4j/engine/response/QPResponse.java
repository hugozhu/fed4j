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

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.jute.fed4j.engine.response.qp.QueryPlan;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 18, 2008
 * Time: 11:14:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class QPResponse extends HttpResponse {
    public QueryPlan queryPlan;
    private static JAXBContext jaxbContext = null;
    
    static {
        try {
            jaxbContext = JAXBContext.newInstance("com.jute.fed4j.engine.response.qp");
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public QPResponse (int code, String response) {
        super(code, response);
    }

    public String toString() {
        return "QP Response:"+queryPlan;
    }

    public void unmarshal() {
        if (jaxbContext == null) {
            return;
        }
        try {
            Unmarshaller unmarshaller=jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(null);
            queryPlan = (QueryPlan) unmarshaller.unmarshal(new InputSource(new StringReader(body)));
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}