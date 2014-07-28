/**
 * @(#)$CurrentFile
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 *<br> Copyright:  Copyright (c) 2013
 *<br> Company:厦门畅享信息技术有限公司
 *<br> @author criss
 *<br> 13-8-15 上午10:51
 *<br> @version 1.0
 *————————————————————————————————
 *修改记录
 *    修改者：
 *    修改时间：
 *    修改原因：
 *————————————————————————————————
 */
package com.sunsharing.sfs.common.btree;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <pre></pre>
 * <br>----------------------------------------------------------------------
 * <br> <b>功能描述:</b>
 * <br>
 * <br> 注意事项:
 * <br>
 * <br>
 * <br>----------------------------------------------------------------------
 * <br>
 */
public class BplusTree implements B {
    Logger logger = Logger.getLogger(BplusTree.class);
    // 是否可以进入多个reader - 可以

    // 是否可以进入多个writer - 不可以

    // 当有reader进入后, writer是否可以进入 - 不可以

    // 当有writer进入后, reader是否可以进入 - 不可以
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    /**
     * 主题
     */
    String topic;

    long latestKey = -1;

    /** 根节点 */
    protected Node root;

    /** 阶数，M值 */
    protected int order;

    /** 叶子节点的链表头*/
    protected Node head;

    public Node getHead() {
        return head;
    }

    public void setHead(Node head) {
        this.head = head;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void reverseEach(long startKey,long endKey,NodeHandle handle)
    {
        ReentrantReadWriteLock.ReadLock readLock = null;
        try
        {
            readLock = lock.readLock();
            readLock.lock();
            Node n = getNodeNoLock(endKey);
            if(n==null)
            {
                n  = getNodeNoLock(latestKey);
            }
            boolean bb = true;
            List list = n.getEntries();
            for(int i=list.size()-1;i>=0;i--)
            {
                Map.Entry key = (Map.Entry)list.get(i);
                //System.out.println(key.getKey());
                if(((Long)key.getKey())>=(startKey) && ((Long)key.getKey())<=(endKey))
                {
                    bb = handle.handle((Long)key.getKey(),(byte[])key.getValue());
                    if(!bb)
                    {
                        return;
                    }
                }
            }
            if(bb)
            {
                Node node = null;
                while((node = n.getPrevious())!=null)
                {
                    list = node.getEntries();
                    for(int i=list.size()-1;i>=0;i--)
                    {
                        Map.Entry key = (Map.Entry)list.get(i);
                        if(((Long)key.getKey())<=(startKey))
                        {
                            return;
                        }
                        //System.out.println(key.getKey());
                        boolean b = handle.handle((Long)key.getKey(),(byte[])key.getValue());
                        if(!b)
                        {
                            return;
                        }
                    }
                    n = node;
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            readLock.unlock();
        }
    }

    public void eachByKey(long startKey,NodeHandle handle)
    {
        ReentrantReadWriteLock.ReadLock readLock = null;
        try
        {
            readLock = lock.readLock();
            readLock.lock();
            Node n = getNodeNoLock(startKey);
            if(n==null)
            {
                n  = head;
            }
            boolean bb = true;
            for(Iterator iter=n.getEntries().iterator();iter.hasNext();)
            {
                Map.Entry key = (Map.Entry)iter.next();

                logger.debug(key.getKey());
                if(((Long)key.getKey())>(startKey))
                {
                    bb = handle.handle((Long)key.getKey(),(byte[])key.getValue());
                    if(!bb)
                    {
                        return;
                    }
                }
            }
            if(bb)
            {
                Node node = null;
                while((node = n.getNext())!=null)
                {

                    for(Iterator iter=node.getEntries().iterator();iter.hasNext();)
                    {
                        Map.Entry key = (Map.Entry)iter.next();
                        logger.debug(key.getKey());
                        boolean b = handle.handle((Long)key.getKey(),(byte[])key.getValue());
                        if(!b)
                        {
                            return;
                        }
                    }
                    n = node;
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public Object get(Comparable key) {
        ReentrantReadWriteLock.ReadLock readLock = null;
        try
        {
            readLock = lock.readLock();
            readLock.lock();
            Object o = root.get(key);
            return o;
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }finally {
            readLock.unlock();
        }
    }

    private Node getNodeNoLock(Comparable key)
    {
        Node o = root.getNode(key);
        return o;
    }

//    public Node getNode(Comparable key){
////        ReentrantReadWriteLock.ReadLock readLock = null;
//        try
//        {
////            readLock = lock.readLock();
////            readLock.lock();
//            Node o = root.getNode(key);
//            return o;
//        }catch (Exception e)
//        {
//            e.printStackTrace();
//            return null;
//        }finally {
////            readLock.unlock();
//        }
//    }


    @Override
    public synchronized void remove(Comparable key) {
        ReentrantReadWriteLock.WriteLock writeLock = null;
        try
        {
            writeLock = lock.writeLock();
            writeLock.lock();
            root.remove(key, this);
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            writeLock.unlock();
        }

    }

    public AtomicLong getNum()
    {
        return num;
    }

    private AtomicLong num = new AtomicLong();
    @Override
    public synchronized void insertOrUpdate(Comparable key, Object obj) {
        ReentrantReadWriteLock.WriteLock writeLock = null;
        try
        {
            writeLock = lock.writeLock();
            writeLock.lock();
            num.incrementAndGet();
            if(latestKey<(Long)key)
            {
                latestKey = (Long)key;
            }
            logger.debug("insertOrUpdate:"+key);
            root.insertOrUpdate(key, obj, this);
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            writeLock.unlock();
        }

    }

    public long getLatestKey() {
        return latestKey;
    }

    public void setLatestKey(long latestKey) {
        this.latestKey = latestKey;
    }

    public BplusTree(int order){
        if (order < 3) {
            System.out.print("order must be greater than 2");
            System.exit(0);
        }
        this.order = order;
        root = new Node(true, true);
        head = root;
    }

    //测试
    public static void main(String[] args) {
        final BplusTree tree = new BplusTree(10);
        Random random = new Random();
        long current = System.currentTimeMillis();
//        for (int j = 0; j < 10; j++) {
//            if(j!=5)
//                tree.insertOrUpdate(new Long(j), "123".getBytes());
//        }
        tree.insertOrUpdate(new Long(1), "123".getBytes());
        tree.insertOrUpdate(new Long(2), "123".getBytes());
        tree.insertOrUpdate(new Long(3), "123".getBytes());
        tree.insertOrUpdate(new Long(4), "123".getBytes());
        tree.insertOrUpdate(new Long(5), "123".getBytes());
        tree.insertOrUpdate(new Long(6), "123".getBytes());
        //tree.remove(new Long(1));
        long duration = System.currentTimeMillis() - current;


        new Thread(){
            public void run()
            {
                for(int i=0;i<10000;i++)
                {
                    tree.insertOrUpdate(new Long(i),"123".getBytes());
                    try
                    {
                        Thread.sleep(10);
                    }catch (Exception e)
                    {

                    }
                    System.out.println("s------------------"+i+"--------------------");
                }
                System.out.println("结束");
            }
        }.start();

        new Thread(){
            public void run()
            {
                for(int i=0;i<100;i++)
                {

                    tree.eachByKey(new Long(3),new NodeHandle(){
                        @Override
                        public boolean handle(long key, byte[] value) {
                            System.out.println(key);
                            return true;
                        }
                    });
                    System.out.println("r------------------"+i+"--------------------");
                    try
                    {
                        Thread.sleep(100);
                    }catch (Exception e)
                    {

                    }
                }
                System.out.println("结束");
            }
        }.start();

//        Node node = tree.getHead();
//        System.out.println(node.hasNext());
//        Long k = 0L;
//        do
//        {
//            k++;
//            Node tmp = node.previous;
//            if(tmp==null)
//            {
//                tmp = tree.getHead();
//            }
//            boolean isdelete = false;
//            //if(k>=100 && k<=800)
//            {
////                if(node.contains(k))
////                {
////                    tree.remove(k);
////                    System.out.println("delete:"+isdelete);
////                    isdelete = true;
////                }
//            }
//
//
//            if(isdelete)
//            {
//                node = tmp;
//            } else
//            {
//                node = node.getNext();
//            }
//
//        }while(node.hasNext());
//
//        System.out.println("hexin:"+k);
//        //tree.insertOrUpdate(9.5, 9.5);
////        tree.remove(10);
////        tree.insertOrUpdate(80, 80);
////        System.out.println("time elpsed for duration: " + duration);
////        Node n = tree.getNode(80).getNext();
////        for(Iterator iter=n.getEntries().iterator();iter.hasNext();)
////        {
////            Map.Entry key = (Map.Entry)iter.next();
////            System.out.println(n.isLeaf+"80:"+key.getKey()+":"+key.getValue());
////        }
//
////        tree.eachByKey(5L,new NodeHandle(){
////
////            @Override
////            public void handle(long key, byte[] value) {
////                if(key==7L)
////                {
////                    tree.remove(new Long(7));
////                }
////                if(key==9L)
////                {
////                    tree.remove(new Long(9));
////                }
////            }
////        });

//        tree.eachByKey(new Long(0),new NodeHandle(){
//
//            @Override
//            public boolean handle(long key, byte[] value) {
//                //System.out.println(key);
//                return true;
//            }
//        });
//        tree.reverseEach(new Long(3),new Long(10),new NodeHandle(){
//            @Override
//            public boolean handle(long key, byte[] value) {
//                System.out.println(key);
//                return true;
//            }
//        });

        //int search = 80;
        //System.out.print(tree.get(search));
//        Node head = tree.getHead();
//        System.out.println(head);
//        while(head.getNext()!=null)
//        {
//            head = head.getNext();
//            System.out.println(head);
//        }
    }

}



