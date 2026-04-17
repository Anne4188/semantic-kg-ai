
//AnswerHandler.java

package ngordnet.main;

import ngordnet.browser.NgordnetQuery;
import ngordnet.browser.NgordnetQueryHandler;
import ngordnet.ngrams.NGramMap;
import spark.Request;
import spark.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnswerHandler extends NgordnetQueryHandler {
    private final NGramMap ngm;

    public AnswerHandler(NGramMap ngm) {
        this.ngm = ngm;
    
    }

    @Override
    public String handle(NgordnetQuery q) {

        //-------key for switch mode--------------

        //Default template mode
        //return handleWithMode(q, false);

        //Default llm mode
        return handleWithMode(q, true);


    }

    private String handleWithMode(NgordnetQuery q, boolean useLLM) {
        List<String> words = q.words();
        if (words == null || words.isEmpty()) {
            return "No query word provided.";
        }

        String query = words.get(0);

        try {
            return callPythonAnswerGenerator(query, useLLM);
        } catch (IOException | InterruptedException e) {
            return "Error generating answer: " + e.getMessage();
        }
    }

    private String callPythonAnswerGenerator(String query, boolean useLLM)
            throws IOException, InterruptedException {
        String projectRoot = Paths.get("").toAbsolutePath().toString();

        List<String> command = new ArrayList<>();
        
        //command.add("ai-service/.venv/bin/python3");

        command.add("/opt/venv/bin/python3");
        command.add("ai-service/llm_answer.py");
        command.add(query);

        if (useLLM) {
            command.add("--use-llm");
        }

        System.out.println("DEBUG callPythonAnswerGenerator: useLLM = " + useLLM);
        System.out.println("DEBUG Java command = " + command);


        ProcessBuilder pb = new ProcessBuilder(command);
        String baseUrl = System.getenv("LLM_BASE_URL");
        String apiKey = System.getenv("LLM_API_KEY");
        String model = System.getenv("LLM_MODEL");

        if (baseUrl != null) {
            pb.environment().put("LLM_BASE_URL", baseUrl);
        }
        if (apiKey != null) {
            pb.environment().put("LLM_API_KEY", apiKey);
        }
        if (model != null) {
            pb.environment().put("LLM_MODEL", model);
        }

        pb.directory(Paths.get(projectRoot).toFile());
        pb.redirectErrorStream(true);

        //--------------------------------------------------

        Process process = pb.start();

        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            return "Python answer generator failed with exit code "
                    + exitCode + ". Output:\n" + output;
        }

        return output.trim();
    }


    @Override
    public Object processQuery(Request request, Response response, NGramMap ngm) {
        String wordsParam = request.queryParams("words");
        String startYearParam = request.queryParams("startYear");
        String endYearParam = request.queryParams("endYear");
        String kParam = request.queryParams("k");
        String useLLMParam = request.queryParams("useLLM");

        List<String> words = (wordsParam == null || wordsParam.isBlank())
                ? List.of()
                : Arrays.asList(wordsParam.split(","));

        int startYear = (startYearParam == null || startYearParam.isBlank())
                ? 1900 : Integer.parseInt(startYearParam);

        int endYear = (endYearParam == null || endYearParam.isBlank())
                ? 2020 : Integer.parseInt(endYearParam);

        int k = (kParam == null || kParam.isBlank())
                ? 5 : Integer.parseInt(kParam);

        boolean useLLM = useLLMParam != null && useLLMParam.equalsIgnoreCase("true");

        System.out.println("DEBUG processQuery(Request): useLLMParam = " + useLLMParam);
        System.out.println("DEBUG processQuery(Request): useLLM = " + useLLM);

        NgordnetQuery query = new NgordnetQuery(words, startYear, endYear, k);

        return handleWithMode(query, useLLM);
    }

    @Override
    public Object processQuery(NgordnetQuery query, NGramMap ngm) {
        System.out.println("DEBUG processQuery(NgordnetQuery): fallback called");
        return handleWithMode(query, false);
    }
}