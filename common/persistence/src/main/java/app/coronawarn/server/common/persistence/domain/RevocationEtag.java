package app.coronawarn.server.common.persistence.domain;

import org.springframework.data.annotation.Id;

public class RevocationEtag {

  @Id
  private String path;

  private String etag;

  public RevocationEtag() {
  }

  public RevocationEtag(final String path, final String etag) {
    this.path = path;
    this.etag = etag;
  }

  public String getEtag() {
    return etag;
  }

  public String getPath() {
    return path;
  }

  public void setEtag(final String etag) {
    this.etag = etag;
  }

  public void setPath(final String path) {
    this.path = path;
  }
}
