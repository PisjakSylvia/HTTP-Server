Ü 3.4 	Java Sockets: HTTP-Server (single-thread) 
	Implementieren Sie einen HTTP-Server, der Anfragen von Clients in einem einzigen Thread behandelt. Der Einfachheit halber soll sich die Funktionalität des Servers auf HTTP-Version 0.9 (http://www.w3.org/Protocols/HTTP/AsImplemented.html) stützen, die auf das Ausliefern einzelner Dateien beschränkt ist.  
Der Server soll über Kommandozeilenparameter konfiguriert werden. Dabei müssen sowohl TCP-Port als auch Basisverzeichnis (document root) des HTTP-Servers beim Start angegeben werden. 
Hinweise: 
▪	Beachten Sie, dass diese ursprüngliche HTTP-Version keine Möglichkeit bietet, Fehlermeldungen an den Client zu übertragen. Signalisieren Sie daher einen Fehler einfach dadurch, dass der Server nichts liefert und die Verbindung schließt. 
▪	Sollte ein angegebenes Dokument nicht existieren, so schließt der Server einfach die Verbindung, ohne Daten auszuliefern. 
▪	Sollte ein Verzeichnis angefordert werden, so soll der Server die in diesem Verzeichnis befindliche Datei index.hml zurückgeben. Ist diese nicht vorhanden, so wird wiederum nichts an den Client retourniert. 
▪	Die Beschreibung (http://www.w3.org/Protocols/HTTP/AsImplemented.html) geht davon aus, dass nur Text zurückgeliefert wird. Die aktuellen Browser unterstützen aber auch, dass binäre Daten wie z.B. Bilder ausgeliefert werden. 
Im Moodle-Kurs befinden sich ein paar Testdaten (documentRoot.zip), mit denen Sie Ihren Server mit einem Browser Ihrer Wahl testen können. 


	https://stackoverflow.com/questions/2443098/java-sockets-can-i-write-a-tcp-server-with-one-thread 
	https://stackoverflow.com/questions/31162754/was-http-request-received-over-tcp-or-udp
	https://serverfault.com/questions/98951/does-https-use-tcp-or-udp 
	Man muss TCP verwenden
	https://stackoverflow.com/questions/13133346/request-sent-with-version-http-0-9 
	https://dev.to/mateuszjarzyna/build-your-own-http-server-in-java-in-less-than-one-hour-only-get-method-2k02  


1.	Man geht in Git Bash mit cd bis ins „src“ des Projektes
2.	$ javac HTTPServerSingleThread.java -> das kompiliert das Programm
3.	Dann gibt man einen Port und die root an
•	$ java HTTPServerSingleThread 8080 "C:/Users/Sylvia/Desktop/UNI 5. Semester/Rechnernetze/ueb3/documentRoot"
4.	Punkt 3 startet den Server auf dem gewählten port
5.	Jetzt muss man http://localhost:8080/index.html im Browser öffnen
Aufpassen: wenn man documentRoot entzippt, ist die index.html in documentRoot/documentRoot -> großes Problem
 
Ü 3.5 	Java Sockets: HTTP-Server (multi-thread) 
	Erweitern Sie die Funktionalität des Servers aus Ü 3.4 wie folgt. Anstatt die Anfragen von Clients seriell in einem Thread zu behandeln, soll jede Anfrage durch einen neuen Thread behandelt
werden. Erweitern Sie den Funktionsumfang Ihres Servers, dass dieser HTTP-Version 1.0 unterstützt (1.0: https://tools.ietf.org/html/rfc1945, 1.1:
https://datatracker.ietf.org/doc/html/rfc9112/). Des Weiteren soll ihr Server persistente Verbindungen unterstützen (siehe hierzu das RFC zu HTTP 1.1). 
Weiters ist der Server durch eine Logging-Komponente zu erweitern. Jeder Zugriff auf den Server (Zeitpunkt, Request, IP und Port des Clients) soll dabei mitprotokolliert werden. Dieses
Logging soll zentral durch einen eigenen Thread realisiert werden, der in Abständen von 5 Sekunden das Zugriffsprotokoll auf die Konsole schreibt. Die Zugriffe müssen daher mit geeigneten Mechanismen von den behandelnden Threads an diesen Logging-Thread kommuniziert werden. Sorgen Sie für eine geeignete Synchronisation der Threads. 
Beispiel-Ausgabe: 
2021-09-26 15:40:10 GET /index.html 127.0.0.1 44239 
2021-09-26 15:40:10 GET /images/logo.gif 127.0.0.1 44240 
2021-09-26 15:40:10 GET /images/TechnikErleben.png 127.0.0.1 44241 
	https://www.geeksforgeeks.org/multithreaded-servers-in-java/ 
	 https://stackoverflow.com/questions/14729475/can-i-make-a-java-httpserver-threaded-process-requests-in-parallel
	https://www.tutorialspoint.com/javaexamples/net_multisoc.htm 
	https://gist.github.com/declank/8fce5a02e4b6bb572e7a
	https://www.geeksforgeeks.org/multithreading-in-java/
	https://www.geeksforgeeks.org/multithreading-in-java/
	https://stackoverflow.com/questions/23068676/how-to-get-current-timestamp-in-string-format-in-java-yyyy-mm-dd-hh-mm-ss 
	https://stackoverflow.com/questions/565893/best-practices-for-java-logging-from-multiple-threads
	https://www.baeldung.com/log4j2-print-thread-info
	https://kb.sos-berlin.com/pages/viewpage.action?pageId=3638224
	https://www.elektronik-kompendium.de/sites/net/2606281.htm#:~:text=Mit%20HTTP%20Version%201.0%20baut,f%C3%BCr%20mehrere%20sequenzielle%20Requests%20nutzen.
	https://stackoverflow.com/questions/23412463/spring-framework-http-options-returning-all-methods-get-put-post-delete-tr
	https://medium.com/codenx/understanding-the-lesser-known-http-methods-head-options-trace-and-connect-af4311e63781
	https://www.javacodegeeks.com/2023/07/get-post-put-and-delete-and-their-limitations-for-building-robust-apis.html 
	https://www.digitalocean.com/community/tutorials/logger-in-java-logging-example
	https://signoz.io/guides/java-log/
	https://stackoverflow.com/questions/12222424/logger-getloggerclass-name-class-getname-leads-to-nullpointerexception
