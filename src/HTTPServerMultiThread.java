import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/*
Mit HTTP Version 1.0 baut jeder Browser bei jedem HTTP-Request eine
 eigene TCP-Verbindung zum Webserver auf. Das heißt, jedes einzelne
 Element (Bild, CSS, Java Script, etc.) wird einzeln angefordert.
 Mit HTTP Version 1.1 lässt sich eine TCP-Verbindung für mehrere
 sequenzielle Requests nutzen.
 */
public class HTTPServerMultiThread {
    private static int port;
    private static Path documentRoot;
    private static volatile boolean serverRunning = true;
    // ExecutorService fürs Multithreading (Client-Anfragen parallel bearbeiten)
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    // BlockingQueue fürs Logging der Anfragen vom Client
    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("java HTTPServerMultiThread <port> <document_root>");
            return;
        }

        port = Integer.parseInt(args[0]);
        documentRoot = Paths.get(args[1]).toAbsolutePath();

        if (!Files.exists(documentRoot)) {
            System.out.println(documentRoot + " existiert nicht");
            return;
        }

        // Logging-Thread starten
        Thread loggerThread = new Thread(new Logger());
        loggerThread.start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Starte Server mit Port " + port + "\n");

            while (serverRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    // hier "entstehen" die neuen Threads -> bei jeder neuen Verbindung
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (IOException ex) {
                    if (serverRunning) {
                        System.err.println("Fehler: " + ex.getMessage());
                    }
                }
            }
        } finally {
            // wenn der Server fertig ist
            threadPool.shutdown();
            serverRunning = false;
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 OutputStream outToClient = clientSocket.getOutputStream()) {
                //System.out.println("inFromClient: " + inFromClient + "\n");

                String clientRequest = inFromClient.readLine();
                if (clientRequest == null) return;
                System.out.println("clientRequest: " + clientRequest + "\n");

                String[] requestParts = clientRequest.split(" ");
                String method = requestParts[0];
                String filePath = requestParts.length > 1 ? requestParts[1] : "/";
                String httpType = requestParts.length > 2 ? requestParts[2] : "HTTP/1.0";

                logRequest(method, filePath, clientSocket);  // Log-Nachricht zur Queue hinzufügen

                switch (method) {
                    case "GET":
                        handleGet(filePath, outToClient, httpType);
                        break;
                    case "POST":
                        handlePost(outToClient, httpType);
                        break;
                    case "HEAD":
                        handleHead(filePath, outToClient, httpType);
                        break;
                    case "PUT":
                        handlePut(outToClient, httpType);
                        break;
                    case "DELETE":
                        handleDelete(outToClient, httpType);
                        break;
                    case "LINK":
                        handleLink(outToClient, httpType);
                        break;
                    case "UNLINK":
                        handleUnlink(outToClient, httpType);
                        break;
                    default:
                        sendResponse(outToClient, httpType, "400 Bad Request", "<h1>400 - Bad Request</h1>");
                }

                // bei HTTP/1.0 schließt die Verbindung automatisch nach dem Senden
                // 1.1 läuft einfach weiter im besten Fall
                if ("HTTP/1.0".equals(httpType)) {
                    clientSocket.close();
                }
            } catch (IOException ex) {
                System.err.println("Fehler: " + ex.getMessage());
            }
        }

        private void logRequest(String method, String filePath, Socket clientSocket) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String logEntry = String.format("%s %s %s %s %d", timestamp, method, filePath, clientSocket.getInetAddress(), clientSocket.getPort());
            //System.out.println("logEntry: " + logEntry + "\n");
            try {
                logQueue.put(logEntry);  // Log-Nachricht zur Queue hinzufügen
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void handleGet(String filePath, OutputStream outToClient, String httpType) throws IOException {
            Path requestedFilePath = getFilePath(filePath);
            System.out.println("requestedFilePath: " + requestedFilePath + "\n");
            File file = documentRoot.toFile();
            if (file.isDirectory()) {
                if (Files.exists(requestedFilePath)) {
                    System.out.println("index.html im Verzeichnis gefunden -> send to Client");
                    sendFileToClient(requestedFilePath, outToClient, httpType);
                } else {
                    sendResponse(outToClient, httpType, "404 Not Found", "<h1>404 - File Not Found</h1>");
                }
            }
        }
        // (zB zum Empfangen von Formulardaten)
        private void handlePost(OutputStream outToClient, String protocol) throws IOException {
            sendResponse(outToClient, protocol, "200 OK", "<h1>POST - Data received</h1>");
        }
        private void handleHead(String filePath, OutputStream outToClient, String httpType) throws IOException {
            sendResponse(outToClient, httpType, "200 OK", "");
        }
        // (zB zum Speichern von Daten)
        private void handlePut(OutputStream outToClient, String httpType) throws IOException {
            sendResponse(outToClient, httpType, "200 OK", "<h1>PUT - Data saved</h1>");
        }
        private void handleDelete(OutputStream outToClient, String httpType) throws IOException {
            sendResponse(outToClient, httpType, "200 OK", "<h1>DELETE - Resource deleted</h1>");
        }
        private void handleLink(OutputStream outToClient, String httpType) throws IOException {
            sendResponse(outToClient, httpType, "200 OK", "<h1>LINK - Relationship established</h1>");
        }
        private void handleUnlink(OutputStream outToClient, String httpType) throws IOException {
            sendResponse(outToClient, httpType, "200 OK", "<h1>UNLINK - Relationship removed</h1>");
        }
        // sendet dem Client den Status und Inhalt der HTML und was für ein
        private void sendResponse(OutputStream outToClient, String httpType, String status, String content) throws IOException {
            String response = httpType + " " + status + "\r\nContent-Type: text/html\r\n\r\n" + "<html><body>" + content + "</body></html>";
            outToClient.write(response.getBytes());
            outToClient.flush();
        }

        private void sendFileToClient(Path file, OutputStream outToClient, String httpType) throws IOException {
            String responseHeader = httpType + " 200 OK\r\nContent-Type: text/html\r\n\r\n";
            outToClient.write(responseHeader.getBytes());

            try (InputStream fileStream = Files.newInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileStream.read(buffer)) != -1) {
                    outToClient.write(buffer, 0, bytesRead);
                }
                outToClient.flush();
            }
        }

        private Path getFilePath(String filePath) {
            if ("/".equals(filePath)) {
                filePath = "/index.html";
            }
            return documentRoot.resolve(filePath.substring(1)).normalize();
        }
    }

    // Logger-Thread, das alle 5 Sekunden die Log-Nachrichten ausgibt
    private static class Logger implements Runnable {
        @Override
        public void run() {
            while (serverRunning) {
                try {
                    Thread.sleep(5000);  // 5 Sekunden warten
                    List<String> logs = new ArrayList<>();
                    logQueue.drainTo(logs);  // entleert die Queue und nimmt alle Log-Einträge

                    if (!logs.isEmpty()) {
                        logs.forEach(System.out::println);  // gibt die Logs aus
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
