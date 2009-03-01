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

import com.jute.fed4j.engine.component.ForkComponent;
import com.jute.fed4j.engine.Workflow;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 18, 2008
 * Time: 11:34:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class F0Component extends ForkComponent {
    public F0Component(String name) {
        super(name);
    }

    public boolean script(Workflow workflow) {
        return true;
    }
}
