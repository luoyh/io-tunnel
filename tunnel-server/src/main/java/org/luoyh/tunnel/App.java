package org.luoyh.tunnel;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

/**
 * 
 * @author luoyh(Roy)-Jul 20, 2017
 *
 */
public class App {
	private static final String LINE = System.lineSeparator();
	
	private static String EMPTY = "HTTP/1.1 200" + LINE + 
								"Vary: Origin" + LINE +
								"Content-Length: 0" + LINE + 
								"Date: Tue, 18 Jul 2017 02:05:29 GMT" + LINE + LINE;
	
	public static String end = "0" + LINE + LINE;
	
	private static Vertx vertx = Vertx.factory.vertx();
	private static NetSocket signal = null;
	private static ExecutorService pool = Executors.newCachedThreadPool();
	private static Map<String, NetSocket> nets = new ConcurrentHashMap<>();
	private static LinkedList<NetSocket> clients = new LinkedList<>();
	
	private static void obtainClient() {
		signal.write("new");
	}
	
	public static void main(String[] args) {
		final int netPort = Integer.parseInt(args[0]);
		final int remotePort = Integer.parseInt(args[1]);
		final int signalPort = 33333;
		
		// signal thread.
		new Thread(() -> {
			NetServer ns = vertx.createNetServer();
			ns.connectHandler(s -> {
				if (null != signal) {
					signal.close();
					signal = null;
				}
				s.closeHandler(__ -> signal = null);
				signal = s;
			});
			ns.listen(signalPort);
		}).start();
		
		new Thread(() -> {
			NetServer ns = vertx.createNetServer();
			
			ns.connectHandler(s -> {
				pool.submit(() -> {
					s.handler(buf -> {
						if (clients.isEmpty() && null == signal) {
							s.write(EMPTY);
							return;
						}
						if (null == signal) {
							return;
						}
						NetSocket client  = null;
						synchronized (clients) {
							while (clients.isEmpty() || (client = clients.removeFirst()) == null) {
								obtainClient();
								try {
									clients.wait(2000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						nets.put(client.writeHandlerID(), s);
						// dict.put(client.writeHandlerID(), s);
						
						client.write(buf);
					});
				});
			});
			
			ns.listen(netPort, "0.0.0.0", r -> {
				if (r.succeeded()) {
					System.out.println("11111 server is listening!");
				} else {
					System.out.println("failed to bind!");
				}
			});
		}).start();
		
		new Thread(() -> {
			NetServer ns = vertx.createNetServer();
			
			ns.connectHandler(s -> {
				s.handler(buf -> {
					try {
						NetSocket net = nets.get(s.writeHandlerID());
						if (null != net) {
							net.write(buf);
						} else {
							System.err.println("net is null");
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});
				s.closeHandler(__ -> {
					nets.remove(s.writeHandlerID());
				});
				synchronized (clients) {
					clients.addLast(s);
					clients.notify();
				}
			});
			
			ns.listen(remotePort, "0.0.0.0", r -> {
				if (r.succeeded()) {
					System.out.println("22222 server is listening!");
				} else {
					System.out.println("failed to bind!");
				}
			});
		}).start();
	}

}
