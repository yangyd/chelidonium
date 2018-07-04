package yangyd.chelidonium.web.request;

import java.util.List;

public class SubmitTaskRequest {
  private String dir;
  private List<String> files;

  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }

  public List<String> getFiles() {
    return files;
  }

  public void setFiles(List<String> files) {
    this.files = files;
  }
}
