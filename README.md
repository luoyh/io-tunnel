# io-tunnel
IO form DOTA one hero!!

> Build and install. Require JDK1.8 or later .

### server

```
cd /yourpath/tunnel-server
mvn clean package -Dmaven.test.skip=true
// run server
java -jar target/tunnel-server-1.0.jar 11111(request port) 22222(remote listening port)
```

### client

```
cd /yourpath/tunnel-client
mvn clean package
// run client
java -jar target/tunnel-client-1.0.jar 1.2.3.4(server host) 22222(server port, same as above) localhost 8080(local server port)
```
