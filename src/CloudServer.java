import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class CloudServer {

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080")
        );

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port),
                0
        );

        server.createContext("/", new SchedulerHandler());

        server.setExecutor(null);

        System.out.println("Cloud Scheduling Server started on port " + port);

        server.start();
    }

    static class SchedulerHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            PrintStream originalOut = System.out;

            try {

                System.setOut(
                        new PrintStream(output, true)
                );

                CloudMain.main(new String[0]);

            } catch (Exception e) {

                e.printStackTrace();

            } finally {

                System.setOut(originalOut);
            }

            String result = output.toString(
                    StandardCharsets.UTF_8.name()
            );

            String html =
                    "<html>" +
                    "<head>" +
                    "<title>Cloud Scheduling</title>" +
                    "<style>" +
                    "body{font-family:Arial;margin:40px;}" +
                    "h1{color:#333;}" +
                    "pre{background:#f4f4f4;padding:20px;" +
                    "overflow:auto;}" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<h1>Cloud Scheduling System</h1>" +
                    "<p>FCFS, SJF and MORAS Scheduling Results</p>" +
                    "<pre>" +
                    escapeHtml(result) +
                    "</pre>" +
                    "</body>" +
                    "</html>";

            byte[] response =
                    html.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "text/html; charset=UTF-8"
            );

            exchange.sendResponseHeaders(
                    200,
                    response.length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response);
            os.close();
        }
    }

    private static String escapeHtml(String text) {

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}