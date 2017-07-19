package org.luoyh.tunnel;

import org.luoyh.tunnel.utils.JsonUtils;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

/**
 *
 * @author luoyh(Roy) - Jul 19, 2017
 */
public class Local {
	private static final String LINE = System.lineSeparator();
	
	public static String EMPTY = "HTTP/1.1 200" + LINE + 
								"Vary: Origin" + LINE +
								"Content-Length: 0" + LINE + 
								"Date: Tue, 18 Jul 2017 02:05:29 GMT" + LINE + LINE;
	
	private static Vertx vertx = Vertx.factory.vertx();
	private static NetSocket client = null;
	private static NetSocket httpClient = null;
	
	private static void localConn(final String host, final int port) {
		NetClient nc = vertx.createNetClient();
		nc.connect(port, host, (r) -> {
			if (r.succeeded()) {
				NetSocket c = r.result();
				
				c.handler(buf -> {
					System.out.println("local reveice!!!");
					System.out.println(buf);
					if (null == client) {
						return;
					}
					client.write(buf);
				});
				c.closeHandler(v -> {
					System.err.println("closed!");
					localConn(host, port);
				});
				httpClient = c;
			} else {
				
			}
		});
	}
	
	public static void main(String[] args) {
		System.out.println(JsonUtils.toJson(args));
		final String serverHost = "119.23.32.198";
		final int serverPort = 22222;
		final String localHost = "localhost";
		final int localPort = 12345;
		new Thread(() -> {
			localConn(localHost, localPort);
		}).start();
		
		new Thread(() -> {
			NetClient nc = vertx.createNetClient();
			
			nc.connect(serverPort, serverHost, r -> {
				if (r.succeeded()) {
					client = r.result();
					client.handler(buf -> {
						System.out.println("22222 reveice:::");
						System.out.println(buf);
						if (null == httpClient) {
							return;
						}
						httpClient.write(buf);
					});
				} else {
					
				}
			});
		}).start();
	}


}
