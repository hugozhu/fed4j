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

package com.jute.fed4j.config.test;

import junit.framework.TestCase;
import com.jute.fed4j.config.Configration;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Dec 8, 2008
 * Time: 2:40:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigrationTest extends TestCase {
    Configration config = null;

    protected void setUp() throws Exception {
        config = new Configration();
        //set dimensions
        config.setDimensions(new String[] {"bucket","cluster","entry","intl"});

        config.addLookupOrder(new String[]{"%{bucket}","%{cluster}","%{entry}","uk"},new String[]{
                                "%{bucket}_%{cluster}",
                                "%{bucket}_%{entry}",
                                "%{bucket}_%{intl}",
                                "%{bucket}",
                                "%{cluster}_%{intl}",
                                "%{cluster}",
                                "%{intl}_%{entry}",
                                "%{entry}",
                                "%{intl}",
                                "EUR", //uk inherits all EUR (group of EU intls) setting
                                "base"
                                }
                            );

        config.addLookupOrder(new String[]{"%{bucket}","%{cluster}","%{entry}","%{intl}"},new String[]{
                                "%{bucket}_%{cluster}",
                                "%{bucket}_%{entry}",
                                "%{bucket}_%{intl}",
                                "%{bucket}",
                                "%{cluster}_%{intl}",
                                "%{cluster}",
                                "%{intl}_%{entry}",
                                "%{entry}",
                                "%{intl}",
                                "base"
                                }
                            );

        //add configs

        //base setting
        config.put("base","intl","");
        config.put("base","key1","default value for key1");
        config.put("base","key2","default value for key2");
        //set up value substitution (only allow string substrituion)
        config.put("base","key3","value from key1: %{key1} and key2: %{key2} .");
        config.put("base","key4","value from key3: %{key3}");
        config.put("base","array_key",new String[]{"au","ca","nz","it"});

        config.put("base","backend_memcache",new LinkedHashMap(){{
            put("failover",true);
            put("expires", 3600);
            put("port",11211);
            put("timeout",10); //10 milliseconds
            put("servers",new LinkedList() {{
                add("127.0.0.1");
                add("127.0.0.2");
                add("127.0.0.3");
                add("127.0.0.4");
                add("127.0.0.5");
                add("127.0.0.6");
                add("127.0.0.9");
                add("127.0.0.7");
            }});
        }});

        //create List object then set to configs
        List mylist = new ArrayList();
        mylist.add("item 1");
        mylist.add("item 2: %{intl}");
        config.put("base","list_key",mylist);

        config.put("base","list2_key",new LinkedList(){{
                add("item1");
                add("item2");
            }});

        config.put("base","map_key",new HashMap(){{
            put("host","killdistance.corp.yahoo.com");
            put("port",4080);
            put("timeout",5000);
            put("path","/fed4j");
            put("country","country:%{intl}");            
        }});

        //intl:us setting
        config.put("us","intl","us");
        config.put("us","key1","value for us only");
        config.put("fed4j","key1","value for fed4j page no matter which intl");
        config.put("us_search","key1","value for us and fed4j page");

        //intl group: EUR setting
        config.put("EUR","key1","default value for EUR");
        config.put("EUR","key2","default value for EUR");

        //UK setting
        config.put("uk","intl","uk");
        config.put("uk","key2","uk value for key2");
        config.put("uk","array_key",new String[]{"%{intl}","de","fr"});
        
        //set up mergeble map value
        //try out a diffetent syntax for Map value
        config.put("uk","map_key",new HashMap<String,Object>(){{
            put("host","uk.corp.yahoo.com");
        }});
    }

    public void testInheritence() throws Exception {
        //set runtime dimension values
        Configration runtimeConfig = config.getInstance(new String[]{"","sk1","fed4j","us"});
        //retrieve value based on dimension values
        assertEquals(runtimeConfig.get("key1"),"value for us and fed4j page");

        runtimeConfig = config.getInstance(new String[]{"","sk1","","uk"});
        assertEquals(runtimeConfig.get("key1"),"default value for EUR");

        runtimeConfig = config.getInstance(new String[]{"UKC001","sk1","","uk"});
        assertEquals(runtimeConfig.get("key1"),"default value for EUR");
        assertEquals(runtimeConfig.get("key2"),"uk value for key2");

        runtimeConfig = config.getInstance(new String[]{"","sk1","","us"});
        assertEquals(runtimeConfig.get("key1"),"value for us only");
        assertEquals(runtimeConfig.get("key2"),"default value for key2");
    }

    public void testSubstitute() {
        Configration runtimeConfig = config.getInstance(new String[]{"UKC001","sk1","","uk"});
        assertEquals(runtimeConfig.get("key3"),"value from key1: default value for EUR and key2: uk value for key2 .");
        assertEquals(runtimeConfig.get("key4"),"value from key3: value from key1: default value for EUR and key2: uk value for key2 .");
    }


    public void testArray() {
        Configration runtimeConfig = config.getInstance(new String[]{"UKC001","sk1","","uk"});
        String[] array = (String[]) runtimeConfig.get("array_key");
        assertEquals(array[0],"uk");
    }

    public void testList() {
        Configration runtimeConfig = config.getInstance(new String[]{"","sk1","","us"});
        List list = (List) runtimeConfig.get("list_key");
        assertEquals(list.get(1),"item 2: us");
    }

    public void testList2() {
        Configration runtimeConfig = config.getInstance(new String[]{"","sk1","","uk"});
        List list = (List) runtimeConfig.get("list_key");
        assertEquals(list.get(1),"item 2: uk");
    }

    public void testMap() {
        Configration runtimeConfig = config.getInstance(new String[]{"","sk1","","uk"});
        Map map2 = (Map) runtimeConfig.get("map_key");
        assertEquals(map2.get("country"),"country:uk");
    }

    public void testMap2() {
        Configration runtimeConfig = config.getInstance(new String[]{"","sk1","","us"});
        Map map2 = (Map) runtimeConfig.get("map_key");
        assertEquals(map2.get("country"),"country:us");
    }

    public void testMerge() {
        Configration runtimeConfig = config.getInstance(new String[]{"","ac2","","uk"});
        Map map2 = (Map) runtimeConfig.get("map_key");
        assertEquals(map2.get("country"),"country:uk");
        assertEquals(map2.get("host"),"uk.corp.yahoo.com");
        assertEquals(map2.get("path"),"/fed4j");
        assertEquals(map2.get("timeout"),5000);
        assertEquals(map2.get("port"),4080);
    }

    public void testResolved() {
        Configration runtimeConfig = config.getInstance(new String[]{"","ac2","","uk"});
        Map map2 = (Map) runtimeConfig.get("map_key");
        assertEquals(map2.get("country"),"country:uk");
        assertEquals(map2.get("host"),"uk.corp.yahoo.com");
        assertEquals(map2.get("country"),"country:uk");
        assertEquals(map2.get("host"),"uk.corp.yahoo.com");
    }

    public void testRuntimeConfigration() {
        Configration runtimeConfig = config.getInstance(new String[]{"","ac2","","uk"});
        assertEquals( ((Map) runtimeConfig.get("map_key")).get("country"),"country:uk");
        
        runtimeConfig = config.getInstance(new String[]{"","ac2","","uk"});
        assertEquals( ((Map) runtimeConfig.get("map_key")).get("country"),"country:uk");

        runtimeConfig = config.getInstance(new String[]{"","ac2","","us"});
        assertEquals( ((Map) runtimeConfig.get("map_key")).get("country"),"country:us");
    }

    public void testMultiThreading() {
        new Thread(new Runnable() {
            public void run() {
                Configration runtimeConfig = config.getInstance(new String[]{"","ac2","","uk"});
                for (int i=0;i<10;i++) {
                    assertEquals( ((Map) runtimeConfig.get("map_key")).get("country"),"country:uk");
                    assertEquals( ((Map) runtimeConfig.get("map_key")).get("host"),"uk.corp.yahoo.com");
                    testRuntimeConfigration();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                Configration runtimeConfig = config.getInstance(new String[]{"","ac2","","uk"});
                for (int i=0;i<10;i++) {
                    assertEquals( ((Map) runtimeConfig.get("map_key")).get("country"),"country:uk");
                    assertEquals( ((Map) runtimeConfig.get("map_key")).get("host"),"uk.corp.yahoo.com");
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void testImmutable() {
        Configration runtimeConfig = config.getInstance(new String[]{"","ac2","","uk"});
        assertEquals( ((Map) runtimeConfig.get("map_key")).get("country"),"country:uk");
        boolean hasExpectedException = false;
        try {
            ((Map) runtimeConfig.get("map_key")).put("country","");
        } catch (UnsupportedOperationException e) {
            hasExpectedException = true;
        }
        assertTrue( hasExpectedException );
        assertEquals( ((Map) runtimeConfig.get("map_key")).get("country"),"country:uk");
    }

    public void testComplexConfig() {
        Configration runtimeConfig = config.getInstance(new String[]{"","ac2","","uk"});
        Map map = (Map) runtimeConfig.get("backend_memcache");
        String[] keys = (String[]) map.keySet().toArray(new String[]{});
        assertEquals(keys[0],"failover");
        assertEquals(keys[1],"expires");
        assertEquals(keys[2],"port");
        assertEquals(keys[3],"timeout");
        assertEquals(keys[4],"servers");
        List servers = (List) map.get("servers");
        
        assertEquals(servers.get(0),"127.0.0.1");
        boolean hasExpectedException = false;
        try {
            servers.clear();
        } catch (Exception e) {
            hasExpectedException = true;
        }
        assertTrue( hasExpectedException );
        assertEquals(servers.get(0),"127.0.0.1");
    }
}