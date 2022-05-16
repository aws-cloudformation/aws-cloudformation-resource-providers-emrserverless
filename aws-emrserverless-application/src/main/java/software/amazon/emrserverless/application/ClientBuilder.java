package software.amazon.emrserverless.application;

import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.cloudformation.LambdaWrapper;

/**
 * Creates the Client for EMR Serverless SDK.
 */
public class ClientBuilder {

  /**
   * Creates static EMR Serverless Client to call API.
   * @return EmrServerless client
   */
  public static EmrServerlessClient getClient() {
    return EmrServerlessClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
  }

}
