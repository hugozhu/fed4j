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

package com.jute.fed4j.engine;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * Created by IntelliJ IDEA.
 * User: hzhu
 * Date: Nov 14, 2008
 * Time: 3:44:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Workflow {
    public static Logger logger = Logger.getLogger(Component.class.getName());
    
    public long startTimestamp;
    public long finishTimestamp;
    private Map<String, Object> parameters;    
    private Map<String,Component> components;
    private Map<String, WorkflowNode> nodes;
    private WorkflowNode root;

    private boolean active = false;

    ReentrantLock lock = new ReentrantLock();
    Condition finished = lock.newCondition();
    AtomicInteger activeTaskCount = new AtomicInteger(0);

    protected Queue<TimetableEntry> timeTable = new ConcurrentLinkedQueue();

    public enum EventType { INFO, ARCSTATE_CHANGE, DISPATCH_START, DISPATCH_FINISH };

    public Workflow () {
       parameters = new HashMap();
    }

    public WorkflowNode start() {
        return root;
    }

    protected void run(WorkflowEngine engine) {
        startTimestamp = System.currentTimeMillis();
        active = true;
        start().run(engine,null);
        if (engine.isBlocking()) {
            awaitForFinishing(engine);
        }
    }

    protected void awaitForFinishing(WorkflowEngine engine) {
        if (active) {
            addEvent("-- main thread awaiting to finish --");
            lock.lock();
            try {
                if (activeTaskCount.intValue()!=0) {
                    finished.await(engine.getMaxExecutionTime(), TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
               logger.log(Level.WARNING,"interrupted while awaiting for workflow to finish:"+e.getMessage());
            } finally {
                lock.unlock();
            }
            addEvent("workflow finished!");
            active = false;
            finishTimestamp = System.currentTimeMillis();
        }
    }

    public WorkflowNode getNode(String name) {
        return nodes.get(name);
    }

    public void awaitComponentCompletion(String name, int timeout, TimeUnit unit) {
        
    }

    public Component getComponent(String name) {
        return getComponents().get(name);
    }

    public boolean isComponentOK(String name) {
        Component component = getComponents().get(name);
        if (component!=null && !component.error) {
            return true;
        }
        return false;
    }

    public String getStringParameter(String key) {
        return (String) parameters.get(key);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public void setParameter(String key,Object value) {
        parameters.put(key,value);
    }

    public void init(String startName) {
        components = new HashMap(); //changed from ConcurrentHashMap
        nodes = new HashMap();
        root = new WorkflowNode(new Component(startName,ComponentType.START));
        nodes.put(startName,root);
    }

    public synchronized void addComponent(String parent, Component component) {
        WorkflowNode parentNode = nodes.get(parent);
        if (parentNode == null) {
            throw new IllegalArgumentException(parent+" doesn't exist");
        }
        if (parentNode.component.type == ComponentType.DATA && parentNode.next!=null) {
            throw new IllegalArgumentException(parent+" data component already have a next component, you shall add a fork component in between");
        }
        

//        if (active && component.type == ComponentType.JOIN) {
//            throw new IllegalArgumentException("Can't dynamically add join component:"+component.name +" to "+parent);
//        }

        WorkflowNode node = nodes.get(component.name);

        if (component.type == ComponentType.DATA && node!= null) {
           throw new IllegalArgumentException(component.name+" data component already exists"); 
        }

        if (node == null) {
            node = new WorkflowNode(component);
        }

        if (parentNode.component.type == ComponentType.FORK) {
            parentNode.addChild(node);
        }
        
        if (component.type == ComponentType.JOIN) {
            node.addParent(parentNode);
        }
        
        parentNode.setNext(node);
        nodes.put(component.name,node);
        getComponents().put(component.name,component);
    }

    public int getDuration() {
        return (int) (finishTimestamp - startTimestamp);
    }

    public Map<String, Component> getComponents() {
        return components;
    }

    public long addEvent(String event, EventType type) {
        long now = System.currentTimeMillis();
        timeTable.add(new TimetableEntry(now,event,type));
        return now;
    }

    public long addEvent(String event) {
        return addEvent(event,EventType.INFO);
    }

    private void addInput(String from, String to) {
        int n = activeTaskCount.incrementAndGet();
        addEvent("add input:"+from+"->"+to+" active tasks: "+n);
    }

    private void addOutput(String from, String to) {
        int n = activeTaskCount.decrementAndGet();
        addEvent("add output:"+from+"->"+to+" active tasks: "+n);
        if(n==0) {
            lock.lock();
            finished.signal();
            lock.unlock();
        }
    }

    class WorkflowNode {
        String name;
        Component component;

        WorkflowNode next;

        List<WorkflowNode> parents;  //join node has multiple input
        List<WorkflowNode> children; //fork node has multiple output

        AtomicInteger count = null;


        WorkflowNode(Component component) {
            this.name = component.name;
            this.component = component;
            if (this.component.type == ComponentType.JOIN) {
                count = new AtomicInteger(0);
                parents = new LinkedList();
            }
            else if (this.component.type == ComponentType.FORK) {
                children = new LinkedList();
            }
        }

        public WorkflowNode addChild(WorkflowNode node) {
            if (this.component.type == ComponentType.FORK) {
                //join node always goes to the last
                if (node.component.type == ComponentType.JOIN) {
                    this.children.add(node);
                }
                else {
                    this.children.add(0,node);
                }
            }
            return node;
        }

        public WorkflowNode addParent(WorkflowNode node) {
            if (this.component.type == ComponentType.JOIN) {
                this.parents.add(node);
            }
            return node;
        }

        public WorkflowNode setNext(WorkflowNode node) {
            if (this.component.type != ComponentType.FORK) {
                this.next = node;
            }
            return node;
        }

        /**
         * run as quick as it can
         * @param engine
         */
        final private void run(final WorkflowEngine engine, final WorkflowNode parent) {
            addEvent((parent==null?"":parent.name+"->")+name,(parent==null)?EventType.INFO:EventType.ARCSTATE_CHANGE);           
            switch (component.type) {
                case FORK:
                    if(component.script(engine.workflow) && children!=null) {
                        for(WorkflowNode childNode: children) {
                            childNode.run(engine,this);
                            addInput(name,childNode.name);
                        }
                    }
                    else {
                        addEvent(name+"->finish",EventType.ARCSTATE_CHANGE);
                    }
                    if (next != null) {
                       logger.log(Level.WARNING, "Fork component should not have a next single-node");
                       next = null;
                    }
                    break;
                case JOIN:
                    //the last arrived node will trigger the join node to continue;
                    if (count.incrementAndGet() == this.parents.size()) {
                        if(component.script(engine.workflow) && next!=null) {
                            next.run(engine,this);
                        }
                        if (next == null) {
                            addEvent(name+"->finish",EventType.ARCSTATE_CHANGE);
                        }
                    }
                    addOutput(parent.name,name);
                    break;
                case DATA:
                    //check if we need dispatch this data component
                    if (component.script(engine.workflow)) {
                        Runnable task = component.run(engine);
                        if (next!=null) {
                            final WorkflowNode thisNode = this;
                            Runnable nextTask = new Runnable() {
                                public void run() {
                                    next.run(engine,thisNode);
                                }
                            };
                            //asyc call
                            engine.executeTwo(task,nextTask);
                        }
                        else {
                            Runnable nextTask = new Runnable() {
                                public void run() {
                                    addOutput(name,"finish");
                                    addEvent(name+"->finish",EventType.ARCSTATE_CHANGE);
                                }
                            };
                            //asyc call
                            engine.executeTwo(task,nextTask);
                        }
                    }
                    else {
                        component.cancel(engine.workflow,component);
                        if (next!=null) {
                            next.run(engine,this);
                        }
                        else {
                            addOutput(name,"finish");
                            addEvent(name+"->finish",EventType.ARCSTATE_CHANGE);
                        }
                    }
                    break;
                default:
                    if (next!=null) {
                        next.run(engine,this);
                    }
            }
        }
    }

    class TimetableEntry {
        long time;
        String event;
        String thread;
        String group;
        EventType type;

        TimetableEntry(long now, String event, EventType type) {
            this.time = now - startTimestamp;
            this.event = event;
            this.thread = Thread.currentThread().getName();
            this.group  = Thread.currentThread().getThreadGroup().getName();
            this.type = type;
        }
    }
}