import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer implements AutoCloseable {
    private final int port;
    private final ExecutorService pool = Executors.newFixedThreadPool(10);

    public SocketServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port 8081...");

            while (true) {
                
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected!");

                
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            
            System.out.println("Server error: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        pool.shutdownNow();
    }
}
