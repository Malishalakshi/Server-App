package lk.ijse.dep13.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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



                } catch (IOException e) {
                    throw new RuntimeException( e );
                }
            }).start();

        }

    }
}
