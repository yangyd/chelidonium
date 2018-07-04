package yangyd.chelidonium.aliyun;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

class AliyunBucketDummy implements AliyunBucket {

  private final String name;
  private final File dir;

  AliyunBucketDummy(String name, File dir) {
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException("must be a directory: " + dir);
    }
    this.name = name;
    this.dir = dir;
  }

  @Override
  public Map<String, Long> files() {
    Map<String, Long> ret = new HashMap<>();
    for (File f : dir.listFiles()) {
      if (f.isFile()) {
        ret.put(f.getName(), f.length());
      }
    }
    return ret;
  }

  @Override
  public InputStream readFile(String file, long start) {
    File f = new File(dir, file);
    if (f.isFile()) {
      if (start >= 0 && start < f.length()) {
        try {
          InputStream in = new FileInputStream(f);
          skip(in, start);
          return in;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        throw new IllegalArgumentException("invalid read start: " + start);
      }
    } else {
      throw new IllegalArgumentException("File not exist");
    }
  }

  private void skip(InputStream in, long n) throws IOException {
    long skipped = 0;
    while (skipped < n) {
      skipped += in.skip(n - skipped);
    }
  }

  @Override
  public String bucketName() {
    return name;
  }
}
