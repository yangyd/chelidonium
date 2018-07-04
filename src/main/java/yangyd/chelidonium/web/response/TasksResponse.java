package yangyd.chelidonium.web.response;

import yangyd.chelidonium.aliyun.actor.DownloadManager;

import java.util.List;

public class TasksResponse {
  private final List<DownloadManager.Started> startedTasks;
  private final List<DownloadManager.Rejected> rejectedTasks;
  private final List<DownloadManager.BucketTask> completedTasks;

  public TasksResponse(List<DownloadManager.Started> startedTasks, List<DownloadManager.Rejected> rejectedTasks, List<DownloadManager.BucketTask> completedTasks) {
    this.startedTasks = startedTasks;
    this.rejectedTasks = rejectedTasks;
    this.completedTasks = completedTasks;
  }

  public List<DownloadManager.Started> getStartedTasks() {
    return startedTasks;
  }

  public List<DownloadManager.Rejected> getRejectedTasks() {
    return rejectedTasks;
  }

  public List<DownloadManager.BucketTask> getCompletedTasks() {
    return completedTasks;
  }
}
