// NgordnetServer.java


package ngordnet.browser;

import static spark.Spark.*;

public class NgordnetServer {

    public void configure() {
        String portStr = System.getenv("PORT");
        int portNum = (portStr == null || portStr.isBlank()) ? 4567 : Integer.parseInt(portStr);

        System.out.println("NgordnetServer.configure called");

        System.out.println("About to call port...");
        port(portNum);
        System.out.println("Finished port");

        // 必须尽量早设置
        initExceptionHandler(e -> {
            System.err.println("Spark init failed:");
            e.printStackTrace();
        });

        staticFiles.location("/static");

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });
    }

    public void register(String URL, NgordnetQueryHandler nqh) {
        get("/" + URL, nqh);
    }

    public void start() {
        System.out.println("About to call init()...");
        init();

        System.out.println("About to call awaitInitialization()...");
        awaitInitialization();

        System.out.println("Spark initialization finished");
    }
}