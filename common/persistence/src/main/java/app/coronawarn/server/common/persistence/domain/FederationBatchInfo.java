package app.coronawarn.server.common.persistence.domain;

import java.time.LocalDate;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;

/**
 * Information about federation batches with their status.
 */
public class FederationBatchInfo {

  @Id
  private final String batchTag;
  private final LocalDate date;
  private FederationBatchStatus status;
  private FederationBatchSourceSystem sourceSystem;

  /**
   * Creates a FederationBatchInfo and sets its status to {@link FederationBatchStatus#UNPROCESSED}.
   *
   * @param batchTag     id of the batch.
   * @param date         date the batch was created.
   * @param sourceSystem the target system to use for download.
   */
  public FederationBatchInfo(String batchTag, LocalDate date, FederationBatchSourceSystem sourceSystem) {
    this(batchTag, date, FederationBatchStatus.UNPROCESSED, sourceSystem);
  }

  /**
   * Creates a FederationBatchInfo.
   *
   * @param batchTag id of the batch
   * @param date     date the batch was created
   * @param status   status stored as {@link FederationBatchStatus}
   */
  @PersistenceCreator
  public FederationBatchInfo(String batchTag, LocalDate date, FederationBatchStatus status,
      FederationBatchSourceSystem sourceSystem) {
    this.batchTag = batchTag;
    this.date = date;
    this.status = status;
    this.sourceSystem = sourceSystem;
  }

  public String getBatchTag() {
    return batchTag;
  }

  public LocalDate getDate() {
    return date;
  }

  public FederationBatchStatus getStatus() {
    return status;
  }

  public FederationBatchSourceSystem getSourceSystem() {
    return sourceSystem;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FederationBatchInfo that = (FederationBatchInfo) o;
    return Objects.equals(batchTag, that.batchTag)
        && Objects.equals(date, that.date)
        && status == that.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(batchTag, date, status);
  }
}
