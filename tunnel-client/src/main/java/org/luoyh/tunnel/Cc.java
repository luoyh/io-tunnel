package org.luoyh.tunnel;

import java.util.concurrent.SynchronousQueue;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

/**
 *
 * @author luoyh(Roy) - Jul 18, 2017
 */
public class Cc {
	private static final String LINE = System.lineSeparator();
	
	private static String EMPTY = "HTTP/1.1 200" + LINE + 
								"Vary: Origin" + LINE +
								"Content-Length: 0" + LINE + 
								"Date: Tue, 18 Jul 2017 02:05:29 GMT" + LINE + LINE;
	
	private static Vertx vertx = Vertx.factory.vertx();
	private static SynchronousQueue<String> msgQueue = new SynchronousQueue<>();
	
	public static void main(String[] args) {
		new Thread(() -> {
			NetClient nc = vertx.createNetClient();
			
			nc.connect(12345, "localhost", (r) -> {
				if (r.succeeded()) {
					NetSocket c = r.result();

					c.write("GET /api/table/load HTTP/1.1"+LINE+
							"Host: 192.168.2.239"+LINE+
							"Skw-Authorization: 111"+LINE+
							"Cache-Control: no-cache"+LINE+
							"Postman-Token: 51610e19-3eed-1ff9-6cdd-18cc526c0b5f"+LINE+LINE+"");
					
					c.handler(buf -> {
						System.out.println("12345 reveice!!!");
						System.out.println(buf.toString());
					});
				} else {
					
				}
			});
		}).start();
		
	}


}
