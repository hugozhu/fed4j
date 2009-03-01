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

import com.jute.fed4j.engine.component.HttpComponent;

import java.util.concurrent.*;
import java.util.logging.Logger;

public class Dispatcher implements Runnable, Callable {
    public static Logger logger = Logger.getLogger(Component.class.getName());
    
    protected WorkflowEngine engine;
    protected Workflow workflow;
    protected Component component;
    protected FutureTask futureTask;

    public Dispatcher(WorkflowEngine engine, Component component) {
        this.engine = engine;
        this.workflow = engine.workflow;
        this.component = component;
        assert this.engine!=null;
        assert this.workflow!=null;
        assert this.component!=null;
        futureTask = new FutureTask(new Callable() {
            public Object call() throws Exception {
                return innerRun();
            }
        });
    }

    public Object call() throws Exception {
        return innerRun();
    }

    public void run() {
        futureTask.run();
    }

    protected Object innerRun() {
        assert component.type == ComponentType.DATA;
        
        try {
            long now = workflow.addEvent(component.name, Workflow.EventType.DISPATCH_START);
            this.component.startTime = now - workflow.startTimestamp;
            component.dispatch(workflow);
            return component.response;
        }
        finally {
            if (component instanceof HttpComponent) {
                if (!component.error && ((HttpComponent) component).preRequestTimestamp!=-1 && ((HttpComponent) component).postRequestTimestamp!=-1) {
                    ((HttpComponent) component).connectTime = ((HttpComponent) component).preRequestTimestamp - workflow.startTimestamp - component.startTime;
                    ((HttpComponent) component).readTime = ((HttpComponent) component).postRequestTimestamp - ((HttpComponent) component).preRequestTimestamp;
                    ((HttpComponent) component).transferTime = ((HttpComponent) component).preUnmarshalTimestamp - ((HttpComponent) component).postRequestTimestamp;
                    ((HttpComponent) component).unmarshalTime = ((HttpComponent) component).postUnmarshalTimestamp - ((HttpComponent) component).preUnmarshalTimestamp;

                }
            }
            long now = workflow.addEvent(component.name, Workflow.EventType.DISPATCH_FINISH);
            component.finishTime = now - workflow.startTimestamp;
            component.latency = component.finishTime - component.startTime;
            component.exit(workflow);
        }
    }

    public Object get()
        throws InterruptedException, ExecutionException {
        return futureTask.get();
    }

    public Object get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return futureTask.get(timeout, unit);
    }

    public boolean isCancelled() {
        return futureTask.isCancelled();
    }

    public boolean isDone() {
        return futureTask.isDone();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return futureTask.cancel(mayInterruptIfRunning);
    }
}
