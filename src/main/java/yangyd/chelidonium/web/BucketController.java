package yangyd.chelidonium.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yangyd.chelidonium.aliyun.AliyunBucket;
import yangyd.chelidonium.aliyun.AliyunService;
import yangyd.chelidonium.aliyun.DownloadService;
import yangyd.chelidonium.web.request.SubmitTaskRequest;

import java.util.*;

@RestController
@RequestMapping("/{region}/{bucketName}")
public class BucketController {
  private final AliyunService aliyun;
  private final DownloadService downloadService;

  @Autowired
  public BucketController(AliyunService aliyun, DownloadService downloadService) {
    this.aliyun = aliyun;
    this.downloadService = downloadService;
  }

  @RequestMapping("/request")
  public SubmitTaskRequest generateRequest(@PathVariable String region, @PathVariable String bucketName) {
    List<String> files = new LinkedList<>(aliyun.getBucket(region, bucketName).files().keySet());
    Collections.sort(files);

    SubmitTaskRequest request = new SubmitTaskRequest();
    request.setDir("taskDownload");
    request.setFiles(files);
    return request;
  }

  @RequestMapping("/")
  public Map<String, Long> listBucketFile(@PathVariable String region, @PathVariable String bucketName) {
    return aliyun.getBucket(region, bucketName).files();
  }

  @RequestMapping(method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void submitDownloadTask(
      @PathVariable String region,
      @PathVariable String bucketName,
      @RequestBody SubmitTaskRequest request) {
    AliyunBucket bucket = aliyun.getBucket(region, bucketName);
    Map<String, Object> taskFiles = new HashMap<>();
    Map<String, Long> bucketFiles = bucket.files();

    for (String file : request.getFiles()) {
      if (bucketFiles.containsKey(file)) {
        taskFiles.put(file, bucketFiles.get(file));
      }
    }
    downloadService.download(request.getDir(), bucket, taskFiles);
  }

}
