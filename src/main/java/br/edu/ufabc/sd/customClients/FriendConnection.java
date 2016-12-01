package br.edu.ufabc.sd.customClients;

import java.util.Scanner;

import org.apache.zookeeper.KeeperException;

import br.edu.ufabc.sd.Client;
import br.edu.ufabc.sd.components.Barrier;

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
	
	public static void main(String[] args) {
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

}
