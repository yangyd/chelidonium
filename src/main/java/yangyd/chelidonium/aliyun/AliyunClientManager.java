package yangyd.chelidonium.aliyun;

import com.aliyun.oss.OSSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "chelidonium.aliyun")
class AliyunClientManager {
  private final static Logger logger = LoggerFactory.getLogger(AliyunClientManager.class);

  private Map<String, OSSClient> regions;

  private Map<String, String> endpoints = new HashMap<>();
  @NotNull
  private String accessKey;
  @NotNull
  private String secretKey;

  // Caveat of ConfigurationProperties
  // Setters are required for String value
  // Getters are required for List/Map structure

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public Map<String, String> getEndpoints() {
    return endpoints;
  }

  @PostConstruct
  private void init() {
    Map<String, OSSClient> cache = new HashMap<>();
    for (String region : endpoints.keySet()) {
      String endpoint = endpoints.get(region);
      logger.info("Configuring Aliyun Client for region {} ({})", region, endpoint);
      cache.put(region, new OSSClient(endpoint, accessKey, secretKey));
    }
    regions = Collections.unmodifiableMap(cache);
  }

  @PreDestroy
  private void dispose() {
    logger.info("Closing clients...");
    regions.values().forEach(OSSClient::shutdown);
    regions = Collections.emptyMap();
  }

  OSSClient getClient(String region) {
    if (regions.containsKey(region)) {
      return regions.get(region);
    } else {
      throw new IllegalArgumentException("Region not defined: " + region);
    }
  }

}
