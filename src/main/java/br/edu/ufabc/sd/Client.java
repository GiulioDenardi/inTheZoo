package br.edu.ufabc.sd;

import java.io.IOException;
import java.util.Scanner;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import br.edu.ufabc.sd.components.Barrier;
import br.edu.ufabc.sd.components.Queue;

public class Client implements Watcher {

    protected static ZooKeeper zk = null;
    protected static Integer mutex;
    protected static Scanner reader = new Scanner(System.in);
    protected static final String host = "localhost:2181";

    protected String root;

    public Client (String address) {
        if(zk == null){
            try {
                System.out.println("Starting ZK:");
                zk = new ZooKeeper(address, 2181, this);
                mutex = new Integer(-1);
                System.out.println("Finished starting ZK: " + zk);
            } catch (IOException e) {
                System.out.println(e.toString());
                zk = null;
            }
        }
        //else mutex = new Integer(-1);
    }

    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            System.out.println("Process: " + event.getType());
            mutex.notify();
        }
    }

    public static void main(String args[]) {
    	System.out.println("Bem-Vindo ao sistema de envio de arquivos doidão!\n"
    			+ "Escolha uma opção para iniciar o aplicativo:\n\n"
    			+ ""
    			+ "[B] Gerar uma música gratuita.\n"
    			+ "[Q] Enviar uma música para algum colega estilo Snapchat\n"
    			+ "[L] Lock\n"
    			+ "[E] Leader Election");
    	
    	char option = reader.next().charAt(0);
    	
    	switch (option) {
    	case ('B'):
    		startBarrier();
    		break;
    	case ('Q'):
//    		startQueue();
    		break;
    	case ('L'):
//    		startLock();
    		break;
    	case ('E'):
//    		startLeaderElection();
    		break;
    	default:
    		System.out.println("Você não digitou um valor válido!");
    	}
    	
    	
    	System.out.println("Obrigado por utilizar nosso serviço!");
    	
    	reader.close();
//    	switch 
//        if (args[0].equals("qTest"))
//            queueTest(args);
//        else
//            barrierTest(args);
    }

    private static void startBarrier() {
    	int size = 3;
		System.out.println("Para gerar uma música gratuita você precisa convidar "
				+ size 
				+ " amigos para baixarem ela ao mesmo tempo que você.\n"
				+ "Escolha uma música:\n\n"
				+ "[las] - Like a Stone;\n"
				+ "[sib] - Somewhere I Belong;");
		
		String song = "/" + reader.next();
		
		System.out.println("Digite um nome único para seus amigos encontrarem a sua opção.");
		String ticket = reader.next();
		
		Barrier b = new Barrier(host, song, size);
		
		try{
			boolean flag = b.enter(ticket);
            
            if(!flag) System.out.println("Não foi possível encontrar esta música.");
        } catch (KeeperException e){
        	System.err.println(e);
        } catch (InterruptedException e){
        	System.err.println(e);
        }
		
		try {
			System.out.println("Sucesso!\nAqui está sua música:");
			System.out.println(new String(zk.getData(song, false, null)));
		} catch (KeeperException e) {
			System.err.println("Há um problema com esta música. Não foi possível enviá-la");
		} catch (InterruptedException e) {
			System.err.println("Há um problema com esta música. Não foi possível enviá-la");
		}
		
		try{
            b.leave(ticket);
        } catch (KeeperException e){
        	System.err.println(e);
        } catch (InterruptedException e){
        	System.err.println(e);
        }
	}

	public static void queueTest(String args[]) {
        Queue q = new Queue(args[1], "/app1");

        System.out.println("Input: " + args[1]);
        int i;
        Integer max = new Integer(args[2]);

        if (args[3].equals("p")) {
            System.out.println("Producer");
            for (i = 0; i < max; i++)
                try{
                    q.produce(10 + i);
                } catch (KeeperException e){

                } catch (InterruptedException e){

                }
        } else {
            System.out.println("Consumer");

            for (i = 0; i < max; i++) {
                try{
                    int r = q.consume();
                    System.out.println("Item: " + r);
                } catch (KeeperException e){
                    i--;
                } catch (InterruptedException e){

                }
            }
        }
    }

//    public static void barrierTest(String args[]) {
//        Barrier b = new Barrier(args[1], args[3], new Integer(args[2]));
//        try{
//            boolean flag = b.enter();
//            System.out.println("Entered barrier: " + args[2]);
//            if(!flag) System.out.println("Error when entering the barrier");
//        } catch (KeeperException e){
//        	System.err.println(e);
//        } catch (InterruptedException e){
//        	System.err.println(e);
//        }
//
//        //Aqui fica o código realizado no barrier.
//        try {
//			System.out.println("Enviando parte " + b.getFileData() + " do arquivo...");
//		} catch (KeeperException e) {
//			System.err.println(e);
//		} catch (InterruptedException e) {
//			System.err.println(e);
//		}
//        
//        try{
//            b.leave();
//        } catch (KeeperException e){
//        	System.err.println(e);
//        } catch (InterruptedException e){
//        	System.err.println(e);
//        }
//        System.out.println("Left barrier");
//    }
}