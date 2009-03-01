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

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 18, 2008
 * Time: 11:33:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoinComponent extends Component {
    public JoinComponent(String name) {
        super(name, ComponentType.JOIN);
    }
}