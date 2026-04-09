package ngordnet.browser;

import static spark.Spark.*;

public class NgordnetServer {
    public void register(String URL, NgordnetQueryHandler nqh) {
        get("/" + URL, nqh);
    }

    public void startUp() {
        port(4567);

        String staticPath = "/Users/anne/Desktop/semantic-kg-ai/semantic-kg-ai/src/main/resources/static";
        System.out.println("Static path = " + staticPath);

        staticFiles.externalLocation(staticPath);

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });

        init();
        awaitInitialization();
    }
}