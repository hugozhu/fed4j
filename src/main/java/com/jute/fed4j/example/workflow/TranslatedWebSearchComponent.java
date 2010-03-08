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

/**
 * Author: Hugo Zhu on  2009-3-2 18:30:08
 */
public class TranslatedWebSearchComponent extends WebSearchComponent {
    public TranslatedWebSearchComponent(String name) {
        super(name);
    }

    @Override
    public boolean script(Workflow workflow) {
        TranslationComponent translation = (TranslationComponent) workflow.getComponent("Translation");    
        if (translation==null || translation.getTranslation()==null) {
            return false;
        }
        workflow.setParameter("translated_query",translation.getTranslation());
        provider.setQuery(workflow.getStringParameter("translated_query"));
        this.uri = provider.getUri();        
        return true;
    }
}
