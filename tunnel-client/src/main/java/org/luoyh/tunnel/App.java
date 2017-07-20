package org.luoyh.tunnel;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

/**
 * 
 * @author luoyh(Roy)-Jul 20, 2017
 *
 */
public class App {
	
	private static String serverHost = "localhost";
	private static int serverPort = 22222;
	
	private static String localHost = "localhost";
	private static int localPort = 8080;
	
	private static String signalHost = "localhost";
	private static  int signalPort = 33333;
	
	private static String LINE = System.lineSeparator();
	
	public static String EMPTY = "HTTP/1.1 200" + LINE + 
								"Vary: Origin" + LINE +
								"Content-Length: 0" + LINE + 
								"Date: Tue, 18 Jul 2017 02:05:29 GMT" + LINE + LINE;
	
	private static final Vertx vertx = Vertx.factory.vertx();
	//private static ExecutorService pool = Executors.newCachedThreadPool();
	private static Map<String, NetSocket> nets = new ConcurrentHashMap<>();
	private static LinkedList<NetSocket> clients = new LinkedList<>();
	
	private static void serverConn() {
		vertx.createNetClient().connect(serverPort, serverHost, r -> {
			if (r.succeeded()) {
				NetSocket client = r.result();
				client.handler(buf -> {
					
					if (clients.isEmpty()) {
						localConn();
					}
					NetSocket c = null;
					synchronized (clients) {
						while (clients.isEmpty() || (c = clients.removeFirst()) == null) {
							localConn();
							try {
								clients.wait(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					
					nets.put(c.writeHandlerID(), client);
					c.write(buf);
				});
//				client.closeHandler(v -> {
//					System.err.println("remote closed!!");
//					serverConn();
//				});
			} else {
				
			}
		});
	}
	
	private static void localConn() {
		System.out.println("localConn");
		NetClient nc = Vertx.factory.vertx().createNetClient();
		nc.connect(localPort, localHost, (r) -> {
			if (r.succeeded()) {
				NetSocket c = r.result();
				c.handler(buf -> {
					String id = c.writeHandlerID();
					NetSocket net = nets.get(id);
					if (null == net) {
						return;
					}
					net.write(buf);
				});
				synchronized (clients) {
					clients.addLast(c);
					clients.notify();
				}
				c.closeHandler(v -> {
					nets.remove(c.writeHandlerID());
				});
			} else {
			}
		});
	}
	
	private static void signalConn() {
		vertx.createNetClient().connect(signalPort, signalHost, (r) -> {
			if (r.succeeded()) {
				NetSocket c = r.result();
				
				c.handler(buf -> { // signal server connect
					serverConn();
				});
				c.closeHandler(v -> {
					System.err.println("signal closed!");
					signalConn();
				});
			} else {
				signalConn();
			}
		});
	}
	
	public static void main(String[] args) {
		serverHost = args[0];
		serverPort = Integer.parseInt(args[1]);
		localHost = args[2];
		localPort =  Integer.parseInt(args[3]);
		signalHost = args[0];
		
		new Thread(() -> signalConn()).start();
	}

}
