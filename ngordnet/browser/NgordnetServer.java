// NgordnetServer.java

package ngordnet.browser;

import static spark.Spark.*;

public class NgordnetServer {

    public void register(String URL, NgordnetQueryHandler nqh) {
        get("/" + URL, nqh);
    }



    public void startUp() {

        String portStr = System.getenv("PORT");
        int portNum = (portStr == null || portStr.isBlank()) ? 4567 : Integer.parseInt(portStr);
        port(portNum);

        staticFiles.location("/static");

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });

        init();
        awaitInitialization();
    }


}