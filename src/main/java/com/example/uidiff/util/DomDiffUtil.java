package com.example.uidiff.util;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DomDiffUtil {
    public static void writeDiff(String dom1, String dom2, Path outFile) {
        try {
            List<String> a = List.of(dom1.split("\\R"));
            List<String> b = List.of(dom2.split("\\R"));
            var patch = DiffUtils.diff(a, b);

            StringBuilder sb = new StringBuilder();
            for (AbstractDelta<String> d : patch.getDeltas()) {
                sb.append(d).append(System.lineSeparator());
            }
            Files.createDirectories(outFile.getParent());
            Files.writeString(outFile, sb.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
