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

import com.jute.fed4j.engine.response.ErrorResponse;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Component {
    public static Logger logger = Logger.getLogger(Component.class.getName());

    public String name;
    public ComponentType type;

    public long startTime = -1;
    public long finishTime = -1;
    public long latency = -1;
    public boolean error = false;

    final private Lock lock = new ReentrantLock();
    final private Condition responseAvailiable = lock.newCondition();
//    final private BlockingQueue responseQueue = new ArrayBlockingQueue(1);

    /**
     * dispatcher for this component
     */
    protected Dispatcher dispatcher;

    /**
     * response result object of this component
     */
    protected Response response;

    /**
     * overall timeout for this component
     */
    public int timeout = 1000;

    public Component(String name, ComponentType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * gating script for component configration
     *
     * @param workflow
     */
    public synchronized boolean script(Workflow workflow) {
        return true;
    }

    /**
     * executing I/O task
     * fetching data from backend: http, mysql, memcache, binary protocol ... etc
     * blocking call
     * @param workflow
     */
    public synchronized void dispatch(Workflow workflow) {
        
    }


    /**
     * post dispatch clean up
     * @param workflow
     */
    public void exit(Workflow workflow) {
        
    }

    public void cancel(Workflow workflow, Component caller) {
        error = true;
        response = new ErrorResponse(ErrorResponse.CANCLED,name+" is cancled by "+caller.name);
        if (dispatcher!=null && !dispatcher.isDone() && !dispatcher.isCancelled()) {
            dispatcher.cancel(true);
        }
    }

    /**
     * none-blocking call
     * @param engine
     */
    public Runnable run (WorkflowEngine engine) {
        dispatcher = new Dispatcher(engine, this);
        return dispatcher;
    }

    public void setResponse(Response response) {
        if (response==null) {
            response = new ErrorResponse(ErrorResponse.NULL,"null response");
        }
        this.response = response;
        lock.lock();
        responseAvailiable.signal();
        lock.unlock();
//        if (responseQueue.isEmpty())
//            responseQueue.offer(response);
    }

    public synchronized Response getResponse() {
//        if (response!=null && (dispatcher==null || dispatcher.isDone())) {
         if (response!=null) {
            return response;
        }

        if (dispatcher!=null) {
            try {
                response = (Response) dispatcher.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                error = true;
                logger.log(Level.WARNING, "[{0}] Thread is interrupted:"+e.getMessage(),new Object[]{name});
            } catch (ExecutionException e) {
                error = true;
                logger.log(Level.WARNING, "[{0}] Thread execution error: "+e.getMessage(),new Object[]{name});
            } catch (TimeoutException e) {
                error = true;
                logger.log(Level.WARNING, "[{0}][{1}] Thread timed out after "+timeout+" milliseconds. " +
                        "start time: {2}",new Object[]{name,Thread.currentThread().toString(),startTime});
            } catch (Exception e) {
                error = true;
                logger.log(Level.WARNING, "[{0}] Unknown error",new Object[]{name});
            }
            finally {

            }
        }
        else if(type==ComponentType.DATA) {
            lock.lock();
            try {
                logger.log(Level.INFO,"[{0}] Trying to get response before dispatching",new Object[]{name});
                responseAvailiable.await();
//                response = (Response) responseQueue.poll(5000,TimeUnit.MILLISECONDS);// replace responseQueue.take(); don't wait for more than 5s for dispatching
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "[{0}] Interrupted while waiting for dispatching",new Object[]{name});
            } finally {
                lock.unlock();
            }
//            if (response!=null) {
//                responseQueue.offer(response);
//            }
        }
        return response;
    }

    public int getDuration() {
        return (int) latency;
    }

    public boolean isDone() {
        return dispatcher.isDone();
    }

    public boolean isCancelled() {
        return dispatcher.isCancelled();
    }
}
