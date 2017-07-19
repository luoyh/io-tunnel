package org.luoyh.tunnel;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

/**
 * Hello world!
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
	private static NetSocket client = null;
	private static NetSocket netClient = null;
	//private static SynchronousQueue<String> msgQueue = new SynchronousQueue<>();
	
	public static void main(String[] args) {
		final int netPort = Integer.parseInt(args[0]);
		final int remotePort = Integer.parseInt(args[1]);
		
		
		new Thread(() -> {
			NetServer ns = vertx.createNetServer();
			
			ns.connectHandler(s -> {
				System.err.println("new connect!!");
				netClient = s;
				s.handler(buf -> {
					if (null == client) {
						s.write(EMPTY);
						return;
					}
					client.write(buf);
				});
			});
			
			ns.listen(netPort, "0.0.0.0", r -> {
				if (r.succeeded()) {
					System.out.println("server is listening!");
				} else {
					System.out.println("failed to bind!");
				}
			});
		}).start();
		
		new Thread(() -> {
			NetServer ns = vertx.createNetServer();
			
			ns.connectHandler(s -> {
				client = s;
				s.handler(buf -> {
					if (null == netClient) {
						return ;
					}
					netClient.write(buf);
				});
			});
			
			ns.listen(remotePort, "0.0.0.0", r -> {
				if (r.succeeded()) {
					System.out.println("server is listening!");
				} else {
					System.out.println("failed to bind!");
				}
			});
		}).start();
	}
}
