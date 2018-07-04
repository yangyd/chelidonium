package yangyd.chelidonium.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.File;

@Component
public class DirService {

  @NotNull @Value("${chelidonium.baseDir}")
  private String baseDir_s;

  private File baseDir;

  @PostConstruct
  public void setup() {
    this.baseDir = new File(baseDir_s);
    if (!(baseDir.isDirectory() && baseDir.canWrite())) {
      throw new IllegalArgumentException("must be a writable directory: " + baseDir);
    }
  }

  public File mkdir(String dirname) {
    File dir = new File(baseDir, dirname);
    if (dir.isDirectory() || dir.mkdir()) {
      return dir;
    } else {
      throw new IllegalArgumentException("Unable to create directory: " + dirname);
    }
  }

}
