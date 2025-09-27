package com.example.uidiff.model;

public class DiffResult {
  private String id;
  private String v1Shot;
  private String v2Shot;
  private String imgDiff;
  private String domDiff;

  // getter & setter
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getV1Shot() { return v1Shot; }
  public void setV1Shot(String v1Shot) { this.v1Shot = v1Shot; }

  public String getV2Shot() { return v2Shot; }
  public void setV2Shot(String v2Shot) { this.v2Shot = v2Shot; }

  public String getImgDiff() { return imgDiff; }
  public void setImgDiff(String imgDiff) { this.imgDiff = imgDiff; }

  public String getDomDiff() { return domDiff; }
  public void setDomDiff(String domDiff) { this.domDiff = domDiff; }
}
