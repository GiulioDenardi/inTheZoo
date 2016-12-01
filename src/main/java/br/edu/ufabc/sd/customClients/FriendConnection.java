package br.edu.ufabc.sd.customClients;

import java.util.Base64;
import java.util.Scanner;

import org.apache.zookeeper.KeeperException;

import br.edu.ufabc.sd.Client;
import br.edu.ufabc.sd.components.Barrier;
import br.edu.ufabc.sd.components.Queue;

public class FriendConnection extends Client {
	
	String ticket;
	String song;
	int size;
	static Scanner reader = new Scanner(System.in);

	public FriendConnection(String address, String ticket, String song, int size) {
		super(address);
		this.ticket = ticket;
		this.song = song;
		this.size = size;
	}
	
	public static void main(String[] args) {
		System.out.println("Você deseja:\n"
				+ "[1] Ajudar seu amigo a ganhar uma música nova?\n"
				+ "[2] Receber músicas de um amigo?\n"
				+ "[3] Lock\n"
				+ "[4] Leader Election");
		
		int option = reader.nextInt();
		
		switch (option) {
		case (1):
			helpFriend();
			break;
		case (2):
			getFriendMusics();
			break;
		case (3):
			break;
		case (4):
			break;
		default:
			System.out.println("Você não enviou um valor válido.");
		}
		
		System.out.println("Obrigado por utilizar nossos serviços!");
	}

	private static void getFriendMusics() {
		System.out.println("Digite o hash enviado pelo seu amigo!");
		String hash = reader.next();
		
		Queue q = new Queue(host, new String(Base64.getDecoder().decode(hash)), false);
		
		try {
			String text = "";
			
			do {
				text = q.consume();
				if (!text.equals("_F_")) {
					System.out.println(text);
				}
			} while (!text.equals("_F_"));
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void helpFriend() {
		System.out.println("Digite o nome do ticket enviado pelo seu amigo:");
		String ticket = reader.next();
		System.out.println("Digite o nome da música enviada pelo seu amigo:");
		String song = "/" + reader.next();
		System.out.println("Digite o nome da quantidade de amigos necessária enviada pelo seu amigo:");
		int size = reader.nextInt();
		System.out.println("Digite o servidor enviado pelo seu amigo:");
		String host = reader.next();
		
		FriendConnection connection = new FriendConnection(host, ticket, song, size);
		
		connection.connectToSong();
	}
	
	void connectToSong () {
		Barrier b = new Barrier(host, song, size);
		try {
			b.enter(ticket);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
