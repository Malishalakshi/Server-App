package lk.ijse.dep13.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class ServerApp {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(80);
        System.out.println("Server started on port 80");
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Socket connected from " + socket.getRemoteSocketAddress());
            new Thread(() -> {
                try {
                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    String commandLine = br.readLine();
                    if (commandLine == null) return;
                    String[] commands = commandLine.split(" ");
                    String command = commands[0];
                    String resourcePath = commands[1];
                    System.out.println(command + " " + resourcePath);

                    String host = null;
                    String line;
                    while ((line = br.readLine()) != null && !line.isBlank()){
                        String key = line.split(":")[0].strip();
                        String value = line.substring( line.indexOf( ":" )+1 ).strip();
                        if(key.equalsIgnoreCase( "host" )){
                            host = value;
                        }
                    }
                    System.out.println(command + " " + host);

                    OutputStream os = socket.getOutputStream();
                    if(!command.equalsIgnoreCase( "GET" )){
                        String httpRequest = """
                                HTTP/1.1 405 Method Not Allowed
                                Server: dep.server
                                Date: %s
                                Content-Type: text/html
                                
                                """.formatted( LocalDateTime.now() );
                        os.write( httpRequest.getBytes() );
                        os.flush();
                        String responseBody = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                <meta charset="UTF-8">
                                <title>Method Not Allowed</title>
                                </head>
                                <body>
                                <h1>405 Not Support To Method</h1>
                                </body>
                                </html>
                                
                                """.formatted( LocalDateTime.now() );
                        os.write( responseBody.getBytes() );
                        os.flush();
                    } else if (host == null) {
                        String responseHeader = """
                                HTTP/1.1 400 Bad Request
                                Server: dep.server
                                Date: %s
                                Content-Type: text/html
                                
                                """.formatted( LocalDateTime.now() );
                        os.write( responseHeader.getBytes() );
                        os.flush();
                        String responseBody = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                <meta charset="UTF-8">
                                <title>Bad request</title>
                                </head>
                                <body>
                                <h1>404 Bad Request</h1>
                                </body>
                                </html>
                                
                                """.formatted( LocalDateTime.now() );
                        os.write( responseBody.getBytes() );
                        os.flush();
                    } else {

                        Path path = Path.of("http",host,resourcePath);
                        if(Files.exists( path )){
                            String responseHeader = """
                                    HTTP/1.1 404 Not Found
                                    Server: dep.server
                                    Date: %s
                                    Content-Type: text/html
                                    
                                    """.formatted( LocalDateTime.now() );
                            os.write( responseHeader.getBytes() );
                            os.flush();
                            String responseBody = """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>   
                                        <meta charset="UTF-8">
                                        <title>Not found</title>
                                    </head>
                                    <body>
                                        <h1>404 Not Found</h1>
                                        </body>
                                    </html>
                                    """;

                        }else {
                            String responseHeader = """
                                    HTTP/1.1 200 OK
                                    Server: dep.server
                                    Date: %s
                                    Content-Type: text/html
                                    
                                    """.formatted( LocalDateTime.now() );
                            os.write( responseHeader.getBytes() );
                            os.flush();
                                FileChannel fc = FileChannel.open( path );
                            ByteBuffer bf = ByteBuffer.allocate( 1024 );
                            while ( fc.read( bf ) != -1 ){
                                bf.flip();
                                os.write( bf.array(), 0, bf.limit() );
                            }
                            os.close();
                            fc.close();
                            socket.close();
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException( e );
                }
            }).start();

        }

    }
}
