package software.amazon.logs.metricfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {

  public static CloudWatchLogsClient getClient() {
    return CloudWatchLogsClient.builder()
              .httpClient(LambdaWrapper.HTTP_CLIENT)
              .build();
  }
}
