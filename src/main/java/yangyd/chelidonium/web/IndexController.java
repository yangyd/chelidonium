package yangyd.chelidonium.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import yangyd.chelidonium.aliyun.AliyunService;
import yangyd.chelidonium.aliyun.DownloadService;
import yangyd.chelidonium.web.response.TasksResponse;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/")
public class IndexController {

  private final AliyunService aliyunService;
  private final DownloadService downloadService;

  @Autowired
  public IndexController(AliyunService aliyunService, DownloadService downloadService) {
    this.aliyunService = aliyunService;
    this.downloadService = downloadService;
  }

  @RequestMapping
  Callable<ResponseEntity<StreamingResponseBody>> index() {
    SimpleResponse<StreamingResponseBody> response = new SimpleResponse<>();
    response.fullfill(o -> {
      PrintWriter out = new PrintWriter(o);
      out.println("Available Regions:");
      for (Map.Entry<String, String> entry : aliyunService.availableRegions().entrySet()) {
        out.printf("\t%s \t%s\r\n", entry.getKey(), entry.getValue());
      }
      out.flush();
    });
    return response.handle;
  }

  @RequestMapping("/tasks")
  public TasksResponse listTasks() {
    return new TasksResponse(
        downloadService.startedTasks(),
        downloadService.rejectedTasks(),
        downloadService.completedTasks());
  }

  @RequestMapping("/{region}")
  List<String> buckets(@PathVariable String region) {
    return aliyunService.listBucket(region);
  }
}
