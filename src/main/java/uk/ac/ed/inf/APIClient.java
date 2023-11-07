package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class APIClient {
    private final String baseApiUrl;

    public APIClient(String apiUrl) {
        this.baseApiUrl = apiUrl;
    }

    protected String requestGET(String apiEndpoint) {
        CustomLogger logger = CustomLogger.getLogger();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(apiEndpoint))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                logger.error("The server responded with code " + response.statusCode() + " when requesting from " + apiEndpoint);
            }
            return response.body();
        }
        catch (URISyntaxException e) {
            logger.error("Invalid API endpoint syntax: " + apiEndpoint);
            return null;
        }
        catch (IOException e) {
            logger.error("An IO Exception when requesting from: " + apiEndpoint);
            return null;
        } catch (InterruptedException e) {
            logger.error("Network error! The request was interrupted when requesting from: " + apiEndpoint);
            return null;
        }
    }

    protected String createEndpoint(String endpoint) {
        return this.baseApiUrl + "/" + endpoint;
    }
}
