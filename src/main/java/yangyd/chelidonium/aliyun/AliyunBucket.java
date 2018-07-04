package yangyd.chelidonium.aliyun;

import java.io.InputStream;
import java.util.Map;

public interface AliyunBucket {
  Map<String, Long> files();

  /**
   * Open an input stream that reads from the file, starting from the given position (inclusive).
   * <strong>The caller is responsible of disposing the stream.</strong>
   */
  InputStream readFile(String file, long start);

  String bucketName();
}
