package yangyd.chelidonium.aliyun;

import com.aliyun.oss.model.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
@Profile("chelidonium")
public class AliyunServiceImpl implements AliyunService {

  private final AliyunClientManager clientManager;
  private final ConcurrentMap<String, AliyunBucket> buckets = new ConcurrentHashMap<>();

  @Autowired
  public AliyunServiceImpl(AliyunClientManager clientManager) {
    this.clientManager = clientManager;
  }

  @Override
  public Map<String, String> availableRegions() {
    return Collections.unmodifiableMap(clientManager.getEndpoints());
  }

  @Override
  public List<String> listBucket(String region) {
    return clientManager.getClient(region).listBuckets().stream()
        .map(Bucket::getName).collect(Collectors.toList());
  }

  @Override
  public AliyunBucket getBucket(String region, String bucketName) {
    String key = bucketKey(region, bucketName);
    AliyunBucket bucket;
    if ((bucket = buckets.get(key)) == null) {
      AliyunBucketImpl b = new AliyunBucketImpl(clientManager.getClient(region), bucketName);
      buckets.putIfAbsent(key, b);
      bucket = buckets.get(key);
    }
    return bucket;
  }

  private String bucketKey(String region, String bucket) {
    return region + ":" + bucket;
  }
}
