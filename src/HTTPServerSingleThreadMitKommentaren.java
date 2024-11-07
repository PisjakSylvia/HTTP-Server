import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HTTPServerSingleThreadMitKommentaren {
    private static int port;
    private static Path documentRoot;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java HTTPServerSingleThread <port> <document_root>");
            return;
        }

        port = Integer.parseInt(args[0]);
        documentRoot = Paths.get(args[1]).toAbsolutePath();

        System.out.println("documentRoot: " + documentRoot);



        if (!Files.exists(documentRoot)) {
            System.out.println( documentRoot + " existiert nicht");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Starte Server mit Port " + port);

            while (true) {
                try (Socket client = serverSocket.accept()) {
                    handleClient(client);
                }
            }
        }
    }

    private static void handleClient(Socket client) throws IOException {
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
        OutputStream outToClient = client.getOutputStream();

        // clientSentence ist die erste Zeile, und in dieser MUSS GET stehen wegen des HTTP 0.9
        String clientSentence = inFromClient.readLine();
        if (clientSentence == null || !clientSentence.startsWith("GET ")) {
            System.out.println("Ungültige Anfrage: " + clientSentence);
            return;
        }

        // Extrahiert den angeforderten Dateipfad [1]
        // zB images/TechnikErleben.png
        String filePath = clientSentence.split(" ")[1];
        System.out.println("Client fordert filePath: " + filePath);

        // requestedFilePath ist der komplette Pfad
        // zB C: ... /documentRoot/images/TechnikErleben.png
        Path requestedFilePath = getFilePath(filePath);
        System.out.println("\n" + "requestedFilePath: " + requestedFilePath + "\n");

        // ist auch ein Pfad, aber nur bis zur documentRoot
        File file = documentRoot.toFile();
        System.out.println("Verzeichnis: " + file);
        System.out.println("ist 'Verzeichnis' ein Verzeichnis?: " + file.isDirectory()+ "\n");

        // schaun, ob es documentRoot ein Child hat, welches die index Datei ist
//        // Liste mit den Children erstellen und ausgeben
//        String[] contents = file.list();
//        for (String fileName : contents) {
//            if ( fileName == "index.html") {
//            }
//            System.out.println(fileName+ "\n");
//        }

        // Wenn das angeforderte Dokument ein Verzeichnis ist, versuche "index.html" zu laden
        if (file.isDirectory()) {
            System.out.println("ist ein Verzeichnis"+ "\n");
            // requestedFilePath = requestedFilePath.resolve("index.html");
            // Überprüfen, ob "index.html" existiert
            System.out.println("requestedFilePath: " + requestedFilePath + "\n");
            if (Files.exists(requestedFilePath)) {
                System.out.println("index.html im Verzeichnis gefunden -> send to Client");
                sendFileToClient(requestedFilePath, outToClient);
            } else {
                System.out.println("Kein index.html im Verzeichnis gefunden");
            }
        }

        // Wenn die Datei existiert und kein Verzeichnis ist -> senden an Client
//        if (Files.exists(requestedFilePath) && !Files.isDirectory(requestedFilePath)) {
//            System.out.println("File gefunden, sende "+ requestedFilePath + " an Client");
//            sendFileToClient(requestedFilePath, outToClient);
//        }
    }
    private static void sendFileToClient(Path file, OutputStream outToClient) throws IOException {
        // HTTP 0.9 Antwort-Header für Textdateien
        outToClient.write("HTTP/0.9 200 OK\r\nContent-Type: text/html\r\n\r\n".getBytes());

        try (InputStream fileStream = Files.newInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                outToClient.write(buffer, 0, bytesRead);
            }
            outToClient.flush();
        }
    }
    private static Path getFilePath(String filePath) {
        if ("/".equals(filePath)) {
            filePath = "/index.html";
        }

        // Verwende das dynamische documentRoot-Verzeichnis
        return documentRoot.resolve(filePath.substring(1)).normalize();
    }
}
