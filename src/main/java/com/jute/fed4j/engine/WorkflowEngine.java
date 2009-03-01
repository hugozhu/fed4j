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
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;

public class WorkflowEngine implements Executor {
    public static Logger logger = Logger.getLogger(Component.class.getName());
    
    public Workflow workflow;
    public static boolean enableThreadPool = true;
    private static MyThreadPoolExecutor threadPoolExecutor =
                        new MyThreadPoolExecutor(20, Integer.MAX_VALUE, 3600L, TimeUnit.SECONDS,new SynchronousQueue());
    private static Executor directExecutor     = new MyDirectExecutor();

    /**
     * max execution time in milliseconds
     */
    private long maxExecutionTime = 5000;

    private boolean blocking = true;

    public WorkflowEngine(Workflow workflow) {
        this.workflow = workflow;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public void setMaxExecutionTime(long maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     * run the workflow
     */
    public void run() {        
        workflow.run(this);
    }

    /**
     * run the workflow
     */
    public void awaitForFinishing() {
        workflow.awaitForFinishing(this);        
    }    

    /**
     * run task in a thread
     * @param task
     */
    public void execute(Runnable task) {
        try {
            Executor executor = enableThreadPool? threadPoolExecutor : directExecutor;
            executor.execute(task);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        } finally {

        }
    }

    protected int getActiveCount() {
        return threadPoolExecutor.getActiveCount();
    }

    /**
     * Execute two tasks serially
     * @param task
     * @param task2
     */
    public void executeTwo(Runnable task, Runnable task2) {
        if (task==null) {
            throw new NullPointerException();
        }
        MySerialExecutor executor = new MySerialExecutor(this);
        executor.execute(task);
        if (task2!=null) {
            executor.execute(task2);
        }
    }



    
    /**
     * for debug
     * @return
     */
    public String getTimetable() {
        StringBuilder sb = new StringBuilder("<table border=1 cellspadding=2 cellspace=3>");
        sb.append("<tr>" +
                "<th>time(ms)</th>" +
                "<th>event</th>" +
                "<th>duration</th>" +
                "<th>thread name</th>" +
                "<th>thread group</th>" +
                "</tr>");
        Map<String,Long> timeCache = new HashMap();
        Map<String,String> colorCache = new HashMap();
        Map<String,Integer> threadCache = new HashMap();
        int dispatchStartColor = 0xFFFF33;
        for(Workflow.TimetableEntry entry: workflow.timeTable) {
            String color = "#FFFFFF";
            String event = entry.event;
            int duration = 0;
            switch (entry.type) {
                case DISPATCH_START:
                    //pick a safe web color, see: http://oreilly.com/catalog/wdnut/excerpt/web_palette.html
                    color = "#"+Integer.toHexString(dispatchStartColor-0x6600*(colorCache.size()));
                    event+=" dispatch";
                    timeCache.put(entry.event,entry.time);
                    colorCache.put(entry.event,color);
                    break;
                case DISPATCH_FINISH:
                    color = colorCache.get(entry.event);
                    event+=" finish";
                    duration = (int)(entry.time - timeCache.get(entry.event));
                    break;
                default:
            }
            if (threadCache.get(entry.thread)==null) {
                threadCache.put(entry.thread,1);
            }
            else {
                threadCache.put(entry.thread,threadCache.get(entry.thread)+1);
            }
            sb.append("<tr bgcolor=\""+color+"\"><td>"
                    +entry.time+"</td><td>"
                    +event+"</td><td>"
                    +duration+"</td><td>"
                    +entry.thread+"</td><td>"
                    +entry.group
                    +"</td></tr>"
            );
        }
        sb.append(String.format("<tr><td colspan=\"5\">Total used threads: %s</td></tr>",threadCache.size()));
        sb.append("</table>");
        return sb.toString();
    }

    public String getDot(String name) {
        if (name == null) {
            name = "yfed_j workflow";
        }
        StringBuilder sb = new StringBuilder("digraph \""+name+"\" {\n");        
        HashMap map = new HashMap();
        for(Workflow.TimetableEntry entry: workflow.timeTable) {
            if (entry.type == Workflow.EventType.ARCSTATE_CHANGE) {
                String[] nodes = entry.event.split("->");
                String from = nodes[0].trim();
                String to   = nodes[1].trim();
                Workflow.WorkflowNode node = workflow.getNode(from);
                if (map.get(from)==null) {
                    sb.append(String.format("\t%s",from));
                    if (node!=null) {
                        String shape = "ellipse";
                        if (node.component.type==ComponentType.FORK) {
                            shape = "triangle";
                        }
                        else if (node.component.type==ComponentType.JOIN) {
                            shape = "invtriangle";
                        }
                        else if (node.component.type==ComponentType.START) {
                            shape = "polygon sides=6,peripheries=3,color=grey,style=filled"; 
                        }
                        sb.append(String.format(" [shape=%s",shape));
                        if (node.component.error) {
                            sb.append(String.format(" color=%s style=filled","red"));
                        }
                        sb.append("]");
                    }
                    sb.append(";\n");                    
                    map.put(from,Boolean.TRUE);
                }
                sb.append(String.format("\t%s",entry.event));
                Component component = node.component;
                if (component!=null && component.getDuration()>0) {
                    sb.append(String.format(" [label=\"%sms\"",component.getDuration()));
                    sb.append(String.format(" color=%s",component.error?"red":"blue"));
                    sb.append("]");
                }
                sb.append(";\n");
            }
        }
        sb.append("\tfinish [shape=polygon sides=6,peripheries=3,color=grey,style=filled];\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * for debug
     * @return
     */
    public String getWaterfallImage() {
        int n = 1;
        List<Component> components = new ArrayList();
        for(Component component: workflow.getComponents().values()) {
            if (component instanceof HttpComponent) {
                n++;
                components.add(component);
            }
        }
        Collections.sort(components,new Comparator() {
            public int compare(Object o1, Object o2) {
                if ( ((Component)o1).startTime < ((Component)o2).startTime) {
                    return -1;
                }
                return 1;
            }
        });
        int width  = 600;
        int height = 50 * n + 50;
        BufferedImage image = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        Font font = new Font("Dialog", Font.BOLD, 12);
        g.setFont(font);
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(Color.BLUE);
        g.drawRect(50,50,width-100,height-100);

        //add padding
        width-=100;
        height-=100;

        g.setColor(Color.GRAY);

        Stroke stroke = new BasicStroke(1,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
            new float[] { 12, 12 }, 0);
        g.setStroke(stroke);

        for(int i=2; i<=width/50; i++) {
            g.drawLine(i*50,50,i*50,height+50);
        }

        //draw x axis
        g.setColor(Color.BLACK);
        float xScale = (float) ( (workflow.getDuration()/100 + 1)*100 ) / width;
        for(int i=0; i<=width/50; i++) {
            int t = (int) (i * 50 * xScale);
            int offset = String.valueOf(t).length()*3;
            g.drawString(t+"",(i+1)*50 - offset,height+75);
        }

        //draw y axis
        n = 1;
        for(Component component:components) {
            if (component instanceof HttpComponent) {
                n++;
                g.setColor(Color.BLACK);
                g.drawString(component.name,0,n*50 - 25);

                if (component.error) {
                    g.setColor(Color.RED);
                }
                else {
                    g.setColor(Color.GREEN);
                }

                //total time;
                int x = (int) component.startTime;
                int w = (int) component.latency;
                g.fillRect(50 + (int) (x / xScale), n*50 - 35, (int)(w/xScale) , 25);
                x = x + w;
                g.setColor(Color.DARK_GRAY);
                g.drawString(component.latency+"ms",50 + (int) (x / xScale),n*50-25);                

                if (!component.error) {
                    //connect time;
                    g.setColor(Color.PINK);
                    x = (int) component.startTime;
                    w =  (int) ((HttpComponent)component).connectTime;
                    g.fillRect(50 + (int) (x / xScale), n*50 - 35, (int)(w/xScale) , 25);

                    //read  time;
                    g.setColor(Color.GRAY);
                    x += w;
                    w =  (int) ((HttpComponent)component).readTime;
                    g.fillRect(50 + (int) (x / xScale), n*50 - 35, (int)(w/xScale) , 25);

                    //transfer
                    g.setColor(Color.GREEN);
                    x += w;
                    w =  (int) ((HttpComponent)component).transferTime;
                    g.fillRect(50 + (int) (x / xScale), n*50 - 35, (int)(w/xScale) , 25);

                    //unmarshal
                    g.setColor(Color.cyan);
                    x += w;
                    w =  (int) ((HttpComponent)component).unmarshalTime;
                    g.fillRect(50 + (int) (x / xScale), n*50 - 35, (int)(w/xScale) , 25);
                }
            }
        }

        g.setColor(Color.BLACK);
        String label = "YFED - Query: "+workflow.getStringParameter("query")+" Duration: "+workflow.getDuration()+"ms";
        g.drawString(label, Math.max(0,width/2 - label.length() * 3), 25);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(image,"png", output);
            return Base64.encode(output.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}

class MyThreadPoolExecutor extends ThreadPoolExecutor {
    public MyThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
    }
}

class MyDirectExecutor implements Executor {
    public void execute(Runnable r) {
        new Thread(r).start();
    }
}

class MySerialExecutor implements Executor {
    final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
    final Executor executor;
    Runnable active;

    MySerialExecutor(Executor executor) {
        this.executor = executor;
    }

    public synchronized void execute(final Runnable r) {
        tasks.offer(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (active == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            executor.execute(active);
        }
    }
}