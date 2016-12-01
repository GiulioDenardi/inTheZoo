package br.edu.ufabc.sd.components;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;

import br.edu.ufabc.sd.Client;

public class Lock extends Client {

	final long wait = 10000; //ms
	final String pathName = "/todaySong";
	String node;

	public Lock(String address) {
		super(address);
		this.root = pathName;
	}

	public boolean lock() throws KeeperException, InterruptedException {
		node = zk.create(root + "/lock-", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		
		return testMin();
	}

	boolean testMin() throws KeeperException, InterruptedException {
		while (true) {
			Thread.sleep(5000);
			Integer suffix = new Integer(node.substring(pathName.length() + 6));
			
			List<String> list = zk.getChildren(root, false);
			
			Integer min = new Integer(list.get(list.size()-1).substring(5));
			
			
			String minString = list.get(list.size()-1);
			
			for (String s : list) {
				Integer tempValue = new Integer(s.substring(5));
				
				if (tempValue < min) {
					min = tempValue;
					minString = s;
				}
			}
			
			if (suffix.equals(min)) {
				//Precisaria remover o watch do znode aqui. Porém, ficou na gambiarra do "boolean success".
				compute();
				return true;
			}
			
			// Step 4
			// Wait for the removal of the next lowest sequence number
			Integer max = min;
			String maxString = minString;
			for (String s : list) {
				Integer tempValue = new Integer(s.substring(5));
				// System.out.println("Temp value: " + tempValue);
				if (tempValue > max && tempValue < suffix) {
					max = tempValue;
					maxString = s;
				}
			}
			
//			// Exists with watch
//			Stat s = zk.exists(root + "/" + maxString, this);
//			
//			// Step 5
//			if (s != null) {
//				continue;
//			}
		}
	}

//	synchronized public void process(WatchedEvent event) {
//		synchronized (mutex) {
//			if (event.getType() == Event.EventType.NodeDeleted) {
//				try {
//					testMin();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

	void compute() {
		System.out.println("Seu tempo de fila acabou! Você já pode pegar sua música!!");
		
		try {
			int i = 0;
			while (i < wait) {
				System.out.println("Tempo para receber a música: " + (wait-i)/1000);
				Thread.sleep(1000);
				i += 1000;
			}
			
			byte[] data = zk.getData(pathName, false, null);
			
			zk.delete(node, 0);
			
			System.out.println(new String(data));
		} catch (KeeperException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
}
