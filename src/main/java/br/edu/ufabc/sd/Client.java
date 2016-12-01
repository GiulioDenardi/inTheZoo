package br.edu.ufabc.sd;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import br.edu.ufabc.sd.components.Barrier;
import br.edu.ufabc.sd.components.Lock;
import br.edu.ufabc.sd.components.Queue;

public class Client implements Watcher {

    protected static ZooKeeper zk = null;
    protected static Integer mutex;
    protected static Scanner reader = new Scanner(System.in);
    protected static final String host = "localhost:2181";
    protected static String loggedUser;
	private static boolean showMessage = true;

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
            mutex.notify();
        }
    }

    public static void main(String args[]) {
    	System.out.println("Bem-Vindo ao sistema de envio de arquivos doidão!\n"
    			+ "Escolha uma opção para iniciar o aplicativo:\n\n"
    			+ ""
    			+ "[B] Gerar uma música gratuita.\n"
    			+ "[Q] Enviar uma música para algum colega estilo Snapchat\n"
    			+ "[L] Pegar a música do dia! (Pode haver filas devido a permitirmos apenas 1 download por vez.)\n"
    			+ "[E] Leader Election");
    	
    	char option = reader.next().charAt(0);
    	
    	switch (option) {
    	case ('B'):
    		startBarrier();
    		break;
    	case ('Q'):
    		startQueue();
    		break;
    	case ('L'):
    		startLock();
    		break;
    	case ('E'):
//    		startLeaderElection();
    		break;
    	default:
    		System.out.println("Você não digitou um valor válido!");
    	}
    	
    	if (showMessage) {
    		System.out.println("Obrigado por utilizar nosso serviço!");
        	
        	reader.close();
    	}
    }

	private static void startLock() {
		System.out.println("Você gostaria de ficar na fila para receber a música do dia?\n"
				+ "Pode demorar algum tempo dependendo da quantidade de pessoas que estão a pedindo! [true/false]");
		
		boolean check = reader.nextBoolean();
		
		if (check) {
			Lock lock = new Lock(host);
			
			try {
				showMessage = lock.lock();
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Tudo bem! Pode passar mais tarde! Ficamos no aguardo de seu retorno :)");
		}
	}

	protected void login() {
		System.out.println("Favor entrar com seu nome de usuário:");
		String usuario = "/" + reader.next();
		
		try {
			Stat exists = zk.exists(usuario, false);
    		if (exists == null) {
				zk.create(usuario, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    		}
    		
    		exists = zk.exists(usuario + "/songs", false);
    		if (exists == null) {
    			zk.create(usuario + "/songs", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    		}
    		loggedUser = usuario;
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    private static void startQueue() {
    	String hash = new BigInteger(130, new SecureRandom()).toString(32);
    	Queue q = new Queue(host, "/songs/" + hash, true);
    	
    	System.out.println("Agora basta enviar este código para seu amigo para que ele possa ouvir as músicas:\n"
				+ Base64.getEncoder().encodeToString((loggedUser + "/songs/" + hash).getBytes()));
    	
		Boolean sendSong = true;
		
		while (sendSong == true) {
			System.out.println("Digite o nome da música:");
			reader.nextLine();
			String song = reader.nextLine();
			
			System.out.println("\nDigite a letra da música (Escreva \"esc\" (sem aspas) para terminar de escrever):");
			String line;
			String lyric = "";
			while (!(line = reader.nextLine()).equals("esc")) {
				lyric += line + "\n";
			}
			
			try {
				q.produce(song, lyric);
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.println("Gostaria de enviar mais músicas? [true/false]");
			sendSong = reader.nextBoolean();
		}
		
		try {
			q.produce("__F__", "_F_");
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

}