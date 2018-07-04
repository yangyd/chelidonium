package yangyd.chelidonium.aliyun;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObjectSummary;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

class AliyunBucketImpl implements AliyunBucket {
  private final OSSClient client;
  private final String bucket;

  AliyunBucketImpl(OSSClient client, String bucket) {
    this.client = client;
    this.bucket = bucket;
  }

  @Override
  public Map<String, Long> files() {
    Map<String, Long> map = new HashMap<>();
    for (OSSObjectSummary summary : client.listObjects(bucket).getObjectSummaries()) {
      map.put(summary.getKey(), summary.getSize());
    }
    return map;
  }

  @Override
  public InputStream readFile(String file, long start) {
    if (start != 0) {
      long size = sizeOf(file);
      if (start < 0 || start > size - 1) {
        throw new IllegalStateException(String.format("Invalid start position of file: %d of %d", start, size));
      }
    }

    GetObjectRequest request = new GetObjectRequest(bucket, file);
    request.setRange(start, -1);
    return client.getObject(request).getObjectContent();
  }

  @Override
  public String bucketName() {
    return bucket;
  }

  private long sizeOf(String file) {
    return client.getSimplifiedObjectMeta(bucket, file).getSize();
  }
}
