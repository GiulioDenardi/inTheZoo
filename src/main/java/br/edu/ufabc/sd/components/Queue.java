package br.edu.ufabc.sd.components;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import br.edu.ufabc.sd.Client;

/**
 * Producer-Consumer queue
 */
public class Queue extends Client {

    /**
     * Constructor of producer-consumer queue
     *
     * @param address
     * @param name
     * @param lyric 
     */
    public Queue (String address, String name, boolean shallLogin) {
        super(address);
        if (shallLogin) {
        	login();
        	this.root = loggedUser + name;
        } else {
        	this.root = name;
        }
    }

    /**
     * Add element to the queue.
     *
     * @param lyric
     * @return
     */

    public boolean produce(String song, String lyric) throws KeeperException, InterruptedException{
    	if (zk != null) {
    		if (zk.exists(root, false) == null) {
    			zk.create(root, lyric.getBytes(), Ids.OPEN_ACL_UNSAFE,
    					CreateMode.PERSISTENT);
    		}
    		
            zk.create(root + "/" + song, lyric.getBytes(), Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);

            return true;
    	} else {
    		return false;
    	}
    }


    /**
     * Remove first element from the queue.
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String consume() throws KeeperException, InterruptedException{
        Stat stat = null;

        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(root, true);
                if (list.size() == 0) {
            		System.out.println("Esperando por mais músicas...");
                    mutex.wait();
                } else {
                	String song = list.get(list.size()-1);
                	
                	if (song.equals("__F__") && list.size() > 1) {
                		song = list.get(list.size()-2);
                	}
                	
                	if (!song.equals("__F__")) {
                		System.out.println("Capturando música: " + song);
                		
                		byte[] b = zk.getData(root + "/" + song,
                                false, stat);
                		zk.delete(root + "/" + song, 0);
                		return new String(b);
                	} else {
                		System.out.println("Essas foram as músicas enviadas!");
                		byte[] b = zk.getData(root + "/" + song,
                                false, stat);
                		zk.delete(root + "/" + song, 0);
                		zk.delete(root, 0);
                        return new String(b);
                	}
                }
            }
        }
    }
}