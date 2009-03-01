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

package com.jute.fed4j.example.workflow;

import com.jute.fed4j.engine.Workflow;
import com.jute.fed4j.engine.response.QPResponse;
import com.jute.fed4j.engine.response.qp.Iy;
import com.jute.fed4j.engine.response.qp.NewsDd;
import com.jute.fed4j.engine.component.ForkComponent;

/**
 * Shortcuts forl
 * User: hzhu
 * Date: Nov 19, 2008
 * Time: 12:22:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class F1Component extends ForkComponent {
    public F1Component(String name) {
        super(name);
    }

    public boolean script(Workflow workflow) {
        QPComponent qp = (QPComponent) workflow.getComponent("QP");
        if (qp!=null) {
            QPResponse result = (QPResponse) qp.getResponse();
            
            if (result==null || result.queryPlan==null) {
                return true; //continue with f1->j1
            }

            if (result.queryPlan.getDispatchPlan()==null) {
                return true; //continue with f1->j1
            }

            Iy iy = result.queryPlan.getDispatchPlan().getIy();
            if (iy!=null) {
                IYComponent iyComponent = new IYComponent(iy.getCat());
                iyComponent.shortcut = iy.getCat();
                workflow.addComponent(name, iyComponent);
//                workflow.addComponent(iyComponent.name,workflow.getComponent("j9"));
            }

            NewsDd newsDD = result.queryPlan.getDispatchPlan().getNewsDd();
            if (newsDD!=null) {
                workflow.addComponent(name,new NewsComponent("NewsDD"));
//                workflow.addComponent("NewsDD",workflow.getComponent("j9"));
            }
        }
        return true;
    }
}
