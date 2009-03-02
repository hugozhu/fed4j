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

import com.jute.fed4j.engine.component.HttpComponent;
import com.jute.fed4j.engine.Provider;
import com.jute.fed4j.engine.Workflow;
import com.jute.fed4j.engine.response.HttpResponse;
import com.jute.fed4j.example.provider.google.TranslationProvider;

/**
 * Author: Hugo Zhu on  2009-3-2 17:55:13
 */
public class TranslationComponent extends HttpComponent {
    private Provider provider = null;
    private String translation = null;
    public TranslationComponent(String name) {
        super(name);
        this.connectTimeout = 1000;
        this.readTimeout = 5000;
        provider = new TranslationProvider();
    }

    @Override
    public boolean script(Workflow workflow) {
        this.provider.setQuery(workflow.getStringParameter("query"));
        this.uri = this.provider.getUri();
        return true;
    }

    public String getTranslation() {
        String translation = null;
        if (translation!=null) {
            return translation;
        }
        if (response!=null && response.getCode()==200) {
            String body = ((HttpResponse) response).getBody();
            String magic = "<div id=result_box dir=\"ltr\">";
            int pos = body.indexOf(magic);
            
            if (pos>-1) {
                pos+=magic.length();
                int pos2 = body.indexOf("</div>",pos);
                if (pos2>pos) {
                    translation = body.substring(pos,pos2);
                    if (provider.getQuery().equalsIgnoreCase(translation)) {
                        translation = null;
                    }
                }
            }
        }
        return translation;
    }

    @Override
    public String toString() {
        String result = this.getTranslation();
        if (result==null) {
            return "";
        }
        return String.format("查询结果也包含了：%s",result);
    }

    @Override
    public HttpResponse createResponse(int code, String body) {
        response =  new HttpResponse(code, body);
        return (HttpResponse) response;
    }
}
