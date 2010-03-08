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

package com.jute.fed4j.example.provider.google;

import com.jute.fed4j.engine.provider.HttpProvider;

import java.util.Map;
import java.util.HashMap;

/**
 * Author: Hugo Zhu on  2009-3-2 16:43:05
 * http://translate.google.com/translate_t?text=Java&sl=auto&tl=zh-CN#
 */
public class TranslationProvider extends HttpProvider {
    private String sourceLanguage = "auto";
    private String targetLanguage = "zh-CN";

    public TranslationProvider() {
        this.host = "translate.google.com";
        path = "/translate_t";
        hash = "";
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public Map<String, String> getQueryMap() {
        Map map = new HashMap();
        map.put("text",query);
        map.put("sl",sourceLanguage);
        map.put("tl",targetLanguage);
        return map;
    }
}
