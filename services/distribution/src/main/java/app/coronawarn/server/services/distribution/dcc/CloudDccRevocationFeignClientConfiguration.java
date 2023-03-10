package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import feign.Client;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableFeignClients
@Profile("!fake-dcc-revocation & revocation")
public class CloudDccRevocationFeignClientConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(CloudDccRevocationFeignClientConfiguration.class);

  private final CloudDccRevocationFeignHttpClientProvider feignDccRevocationClientProvider;

  private final DistributionServiceConfig.Client clientConfig;

  /**
   * Create an instance.
   */
  public CloudDccRevocationFeignClientConfiguration(
      CloudDccRevocationFeignHttpClientProvider feignDccRevocationClientProvider,
      DistributionServiceConfig distributionServiceConfig) {
    logger.debug("Creating Cloud DCC Revocation Feign Client Configuration");
    this.feignDccRevocationClientProvider = feignDccRevocationClientProvider;
    this.clientConfig = distributionServiceConfig.getDccRevocation().getClient();
  }

  @Bean
  public Client dccRevocationFeignClient() {
    return feignDccRevocationClientProvider.createDccRevocationFeignClient();
  }

  /**
   * Retrier configuration for Feign DCC Revocation client.
   */
  @Bean
  public Retryer dccRevocationRetryer() {
    long retryPeriod = TimeUnit.SECONDS.toMillis(clientConfig.getRetryPeriod());
    long maxRetryPeriod = TimeUnit.SECONDS.toMillis(clientConfig.getMaxRetryPeriod());
    int maxAttempts = clientConfig.getMaxRetryAttempts();
    return new Retryer.Default(retryPeriod, maxRetryPeriod, maxAttempts);
  }
}
