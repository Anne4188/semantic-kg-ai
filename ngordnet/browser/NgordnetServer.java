// NgordnetServer.java


package ngordnet.browser;

import spark.Service;

public class NgordnetServer {

    private final Service http;

    public NgordnetServer() {
        this.http = Service.ignite();
    }

    public void configure() {
        String portStr = System.getenv("PORT");
        int portNum = (portStr == null || portStr.isBlank()) ? 4567 : Integer.parseInt(portStr);

        System.out.println("NgordnetServer.configure called");

        System.out.println("About to call port...");
        http.port(portNum);
        System.out.println("Finished port");

        http.initExceptionHandler(e -> {
            System.err.println("Spark init failed:");
            e.printStackTrace();
        });

        http.staticFiles.location("/static");

        http.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });
    }

    public void register(String URL, NgordnetQueryHandler nqh) {
        System.out.println("Registering route: /" + URL);
        http.get("/" + URL, nqh);
    }

    public void start() {
        System.out.println("About to call init()...");
        http.init();

        System.out.println("About to call awaitInitialization()...");
        http.awaitInitialization();

        System.out.println("Spark initialization finished");
    }
}