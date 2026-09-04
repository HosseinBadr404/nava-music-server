public final class NavaServer {
    private NavaServer() {
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("NAVA_PORT", "8081"));
        try (SocketServer server = new SocketServer(port)) {
            server.start();
        }
    }
}
