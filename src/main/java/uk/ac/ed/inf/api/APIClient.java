package uk.ac.ed.inf.api;

import uk.ac.ed.inf.CustomLogger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Generic API Client that connects to a server and sends GET requests
 */
public class APIClient {
    private final String baseApiUrl;

    public APIClient(String apiUrl) {
        this.baseApiUrl = apiUrl;
    }

    protected String requestGET(String apiEndpoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(apiEndpoint))
                    .GET()
                    .build();

            // Send a new message to server
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (!isSuccessCode(response.statusCode())) {
                CustomLogger.getLogger().error("The server responded with code " + response.statusCode() + " when requesting from " + apiEndpoint);
            }
            return response.body();
        }
        catch (URISyntaxException e) {
            CustomLogger.getLogger().error("Invalid API endpoint syntax: " + apiEndpoint);
            return null;
        }
        catch (IOException e) {
            CustomLogger.getLogger().error("An IO Exception when requesting from: " + apiEndpoint);
            return null;
        } catch (InterruptedException e) {
            CustomLogger.getLogger().error("Network error! The request was interrupted when requesting from: " + apiEndpoint);
            return null;
        }
    }

    private boolean isSuccessCode(int code) {
        return code / 100 == 2;
    }

    protected String createEndpoint(String... endpointParts) {
        return this.baseApiUrl + "/" + String.join("/", endpointParts);
    }
}
