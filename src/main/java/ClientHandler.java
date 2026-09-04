import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket; 
    private final RequestHandler requestHandler; 

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.requestHandler = new RequestHandler();
    }

    public void run() {
        try {
            
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream())
            );
            
            PrintWriter writer = new PrintWriter(
                    clientSocket.getOutputStream(), true
            );

            
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                
                String response = requestHandler.handleRequest(message);
                
                writer.println(response);
                System.out.println("Sent: " + response);
            }
        } catch (IOException e) {
            
            System.out.println("Client error: " + e.getMessage());
        } finally {
            try {
                
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}
