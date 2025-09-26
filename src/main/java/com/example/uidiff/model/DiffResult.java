package com.example.uidiff.model;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class DiffResult {
    private String id;
    private String v1Shot;
    private String v2Shot;
    private String imgDiff;
    private String domDiff;
}
