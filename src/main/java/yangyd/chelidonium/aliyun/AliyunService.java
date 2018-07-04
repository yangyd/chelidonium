package yangyd.chelidonium.aliyun;

import java.util.List;
import java.util.Map;

public interface AliyunService {
  Map<String, String> availableRegions();

  List<String> listBucket(String region);

  AliyunBucket getBucket(String region, String bucket);

}
