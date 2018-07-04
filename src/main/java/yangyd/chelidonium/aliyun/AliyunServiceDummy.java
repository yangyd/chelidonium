package yangyd.chelidonium.aliyun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Profile("dev")
public class AliyunServiceDummy implements AliyunService {

  @Autowired
  private AliyunClientManager clientManager;

  @Override
  public Map<String, String> availableRegions() {
    return Collections.unmodifiableMap(clientManager.getEndpoints());
  }

  @Override
  public List<String> listBucket(String region) {
    return Arrays.asList("yangyd", "ettouy");
  }

  @Override
  public AliyunBucket getBucket(String region, String bucket) {
    String dir = String.join(System.getProperty("file.separator"), System.getProperty("user.home"), "Downloads", "forms");
    File d = new File(dir);

    if (!d.isDirectory()) {
      throw new IllegalStateException("data dir not exist: " + d);
    }

    return new AliyunBucketDummy(bucket, d);
  }

}
