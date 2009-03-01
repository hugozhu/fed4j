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

package com.jute.fed4j.config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;

/**
 * <pre>
 * Configration features:
 * 1) Multi-dimension
 * 2) Inheritence: you can set up a lookup order to resolve keys also be able to specify different order for different dimension values;
 * 3) String substitution: %{key} will be replaced with real value resolved with same dimension values;
 * 4) Mergeable map based configs: you can only override partial key sets for a map based config;
 * 5) Be able to do pretty print on all configs
 * <pre/>
 * <pre>
 * Questions: ?
 * 1) Should configration be immutable after built is done;
 * 2) Should each thread make a copy of after setDimensionValues() because of substrituion and mergable value feature?
 * 3) Object serilization based copy is slower than Clone in Java.
 * <pre/>
 * 
 */
public class Configration implements Cloneable,Serializable {
    public final static String SEPERATOR = "_";

    MultiDimensionHashMap hash = new MultiDimensionHashMap();
    private String[] dimensions = null;
    private Map<String[], String[]> lookupOrderSetting = new LinkedHashMap();

    private transient Map<Object, RuntimeConfigration> runtimeLookupCache = new ConcurrentHashMap();
   
    public String[] getDimensions() {
        return dimensions;
    }

    public void setDimensions(String[] dimensions) {
        this.dimensions = dimensions;
    }

    public void addLookupOrder(String[] lookupKey, String[] orders) {
        lookupOrderSetting.put(lookupKey, orders);
    }

    public void put(String section, String key, Object value) {
        //make sure the map built by anonymous inner class is serializable
        value = copyValue(value);
        hash.put(section, key, value);
    }
 
    public Object get(String key) {
        throw new UnsupportedOperationException();
    }

    public Map getMap(String key) {
        Map map = (Map) this.get(key);
        if (map == null) {
            map = Collections.emptyMap();
        }
        return map;
    }

    public List getList(String key) {
        List list = (List) this.get(key);
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }

    public int getInt(String key) {
        Object val = this.get(key);
        if (val instanceof Integer) {
            return (Integer) val;
        }
        return Integer.valueOf(String.valueOf(val));
    }

    private Object copyValue(Object value) {
        if(value instanceof Map) {
            if (value instanceof LinkedHashMap) {
                value = new LinkedHashMap(((Map) value));
            }
            else {
                value = new HashMap(((Map) value));
            }
            for(Map.Entry entry: ((Map<String,Object>) value).entrySet()) {
                entry.setValue(copyValue(entry.getValue()));
            }
        }
        else if(value instanceof List) {
            value = new LinkedList(((List) value));
            for(Object item: (List) value) {
                item = copyValue(item);
            }
        }
        return value;
    }



    
    /**
     * Make a runtime configration based on dimension values
     *
     * @param values
     */
    public Configration getInstance(String[] values) {
        assert values != null && values.length == dimensions.length;
        String key = genKey(values);
        RuntimeConfigration config = runtimeLookupCache.get(key);
        if (config != null) {
            return config;
        }
        synchronized (this) {
            config = new RuntimeConfigration();
            config.lookupOrder = new LinkedList();
            config.dimensionValueMap = new HashMap(dimensions.length);
            config.hash = this.hash; //keep a reference to original static config, but not copy
            for (int i = 0; i < dimensions.length; i++) {
                if (values[i] != null && values[i].length() > 0) {
                    config.dimensionValueMap.put(dimensions[i], values[i]);
                }
            }
            for (Map.Entry<String[], String[]> entry : lookupOrderSetting.entrySet()) {
                if (isMatched(values, entry.getKey())) {
                    String[] order = entry.getValue();
                    for (int i = 0; i < order.length; i++) {
                        String tmp = config.resolvePattern(order[i]);
                        if (tmp != null) {
                            config.lookupOrder.add(tmp);
                        }
                    }
                    try {
                        runtimeLookupCache.put(key, config);
                    } catch (Exception e) {
                        throw new RuntimeException("Can't make a RuntimeConfigration instance", e);
                    }
                    return config;
                }
            }
        }
        throw new NullPointerException("Failed to get an instance of configration by dimension values:"+key);
    }

    private String genKey(String[] arr) {
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(s);
            sb.append(":");
        }
        return sb.toString();
    }

    /**
     * check if the dimension values matche a config setting, so we can get the lookup order
     *
     * @param values
     * @param pattern
     * @return
     */
    private boolean isMatched(String[] values, String[] pattern) {
        assert values != null && pattern != null && values.length == pattern.length;
        for (int i = 0; i < values.length; i++) {
            if (!("%{" + dimensions[i] + "}").equals(pattern[i]) && !values[i].equals(pattern[i])) {
                return false;
            }
        }
        return true;
    }

    //merge map2 to map1, all existing keys' value in map1 are kept
    static void merge(Map<String, Object> map1, Map<String, Object> map2) {
        if (map2 == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : map2.entrySet()) {
            if (map1.get(entry.getKey()) == null) {
                map1.put(entry.getKey(), entry.getValue());
            }
        }
    }
}

class RuntimeConfigration extends Configration {
    List<String> lookupOrder = null;
    Map<String, String> dimensionValueMap = null;
    Map<String, ConfigData> cache = new HashMap();


    public void put(String section, String key, Object value) {
        throw new UnsupportedOperationException();
    }

    public Object get(String key) {
        ConfigData data = cache.get(key);
        if (data!=null) {
            return data.data;
        }
        
        Map<String, Object> mapValue = null;
        for (String section : lookupOrder) {
            Object value = hash.get(section, key);
            if (value != null) {                
                value = resolveValue(value);
                data = new ConfigData(value);
                if (value instanceof Map) {
                    if (mapValue == null) {
                        mapValue = (Map<String, Object>) value;
                    } else {
                        merge(mapValue, (Map<String, Object>) value);
                    }
                } else {
                    data.data = value;
                    break;
                }
            }
        }
        if (mapValue != null) {
            data.data = mapValue;
        }
        if (data!=null) {
            data.data = copyValue(data.data);         
            cache.put(key,data);
            return data.data;
        }
        return null;
    }

    /**
     * make data unmodifiable
     * @param value
     * @return
     */
    private Object copyValue(Object value) {
        if (value instanceof Map) {
            for(Map.Entry entry: ((Map<String,Object>) value).entrySet()) {
                entry.setValue(copyValue(entry.getValue()));
            }
            value = Collections.unmodifiableMap( (Map) value);
        }
        else if (value instanceof List) {
            for(Object item: (List) value) {
                item = copyValue(item);
            }
            value = Collections.unmodifiableList( (List) value);
        }
        return value;
    }

    /**
     * Resolove the pattern's value: "%{bucket}_%{cluster}
     *
     * @param pattern
     * @return
     */
    String resolvePattern(String pattern) {
        assert pattern != null;
        String[] parts = pattern.split(SEPERATOR);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].matches("%\\{(.*)\\}")) {
                String s = parts[i].substring(2, parts[i].length() - 1);
                if (dimensionValueMap.get(s) != null) {
                    sb.append(dimensionValueMap.get(s));
                } else {
                    return null;
                }
            } else {
                sb.append(parts[i]);
            }
            sb.append(SEPERATOR);
        }
        return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1);
    }


    static Pattern pattern = Pattern.compile("%\\{[^\\}]*\\}");
    
    /**
     * resolve the value, if the value is already resolved before, should not be resloved again
     *
     * @param value
     * @return
     */
    private Object resolveValue(Object value) {
        if (value instanceof String) {
            String s = (String) value;
            if (s.indexOf("%{") > -1) {
                Matcher m = pattern.matcher(s);
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    Object replace = get(m.group().substring(2, m.group().length() - 1));
                    if (replace == null || !(replace instanceof String)) {
                        replace = m.group();
                    }
                    m.appendReplacement(sb, (String) replace);
                }
                m.appendTail(sb);
                value = sb.toString();
            }
        } else if (value instanceof String[]) {
            String[] tmp = (String[]) value;
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = (String) resolveValue(tmp[i]);
            }
        } else if (value instanceof List) {
            List tmp = (List) value;
            for (int i = 0; i < tmp.size(); i++) {
                tmp.set(i, resolveValue(tmp.get(i)));
            }
        } else if (value instanceof Map) {
            Map<String, Object> tmp = (Map) value;
            for (Map.Entry<String, Object> entry : tmp.entrySet()) {
                entry.setValue(resolveValue(entry.getValue()));
            }
        }
        return value;
    }    
}

/**
 *
 * A simple implementation of multi-dimension data store which allows same key saved in different section,
 * and can be retrieved with specified order later.
 * 
 */
class MultiDimensionHashMap implements Cloneable, Serializable {
    public Map<String, Map<String, Object>> data = new HashMap();

    public MultiDimensionHashMap() {
        addSection("base");
    }

    //current section
    private String section = null;

    public synchronized void addSection(String section) {
        if (data.get(section) == null) {
            data.put(section, new HashMap());
        }
        this.section = section;
    }

    public Object put(String section, String key, Object value) {
        addSection(section);
        return put(key, value);
    }

    protected Object put(String key, Object value) {
        Map store = data.get(section);
        return store.put(key, value);
    }

    public Object get(String section, String key) {
        Map<String, Object> map = data.get(section);
        Object data = null;
        if (map!=null) {
            data = map.get(key);
        }
        return data == null ? null : SerialClone.clone(data);
    }

    public MultiDimensionHashMap copy() {
        MultiDimensionHashMap copy = SerialClone.clone(this);
        return copy;
    }
}

class ConfigData implements Cloneable, Serializable {
    long lastAccess = -1;
    long count = -1;
    boolean resolved = false;
    boolean mergeable = false;
    Object data;

    public ConfigData(Object obj) {
        data = obj;
    }

    public Object clone() {
        Object o;
        try {
            o = super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return o;
    }

    public int asInt() {
        return (Integer) data;
    }

    public String asString() {
        return (String) data;
    }

    public Map asMap() {
        return (Map) data;
    }

    public List asList() {
        return (List) data;
    }
}

class SerialClone {
    public static <T> T clone(T x) {
        try {
            return cloneX(x);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static <T> T cloneX(T x) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        CloneOutput cout = new CloneOutput(bout);
        cout.writeObject(x);
        byte[] bytes = bout.toByteArray();

        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        CloneInput cin = new CloneInput(bin, cout);

        @SuppressWarnings("unchecked")
                T clone = (T) cin.readObject();
        return clone;
    }

    private static class CloneOutput extends ObjectOutputStream {
        Queue<Class<?>> classQueue = new LinkedList<Class<?>>();

        CloneOutput(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void annotateClass(Class<?> c) {
            classQueue.add(c);
        }

        @Override
        protected void annotateProxyClass(Class<?> c) {
            classQueue.add(c);
        }
    }

    private static class CloneInput extends ObjectInputStream {
        private final CloneOutput output;

        CloneInput(InputStream in, CloneOutput output) throws IOException {
            super(in);
            this.output = output;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass osc)
                throws IOException, ClassNotFoundException {
            Class<?> c = output.classQueue.poll();
            String expected = osc.getName();
            String found = (c == null) ? null : c.getName();
            if (!expected.equals(found)) {
                throw new InvalidClassException("Classes desynchronized: " +
                        "found " + found + " when expecting " + expected);
            }
            return c;
        }

        @Override
        protected Class<?> resolveProxyClass(String[] interfaceNames)
                throws IOException, ClassNotFoundException {
            return output.classQueue.poll();
        }
    }
}