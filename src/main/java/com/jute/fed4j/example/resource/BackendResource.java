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

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 16, 2008
 * Time: 12:27:45 AM
 * To change this template use File | Settings | File Templates.
 */
/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 15, 2008
 * Time: 11:00:01 PM
 * To change this template use File | Settings | File Templates.
 */

@Path("/backend")
@Produces("text/xml")
public class BackendResource {

    @GET
    @Path("/yst")
    public String YST() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // Return some cliched textual content
        return "<xml><yst>result 1</yst></xml>\n";
    }

    @GET
    @Path("/qp")
    public String QP() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // Return some cliched textual content
        return "<xml><qp>result 2</qp></xml>\n";
    }


    @GET
    @Path("/ysm")
    public String YSM() {
        try {
            Thread.sleep(190);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // Return some cliched textual content
        return "<xml><ysm>result 3</ysm></xml>\n";
    }

}
