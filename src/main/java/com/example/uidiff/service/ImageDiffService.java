package com.example.uidiff.service;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;

@Service
public class ImageDiffService {

    /** So sánh ảnh V1/V2, vẽ bbox màu đỏ lên V2 → lưu outPath */
    public void diff(String v1Png, String v2Png, String outPath) {
        Mat a = opencv_imgcodecs.imread(v1Png);
        Mat b = opencv_imgcodecs.imread(v2Png);
        if (a.empty() || b.empty())
            throw new RuntimeException("Cannot read images: " + v1Png + " / " + v2Png);

        if (a.size().width() != b.size().width() || a.size().height() != b.size().height())
            opencv_imgproc.resize(b, b, a.size());

        Mat diff = new Mat(); opencv_core.absdiff(a, b, diff);
        Mat gray = new Mat(); opencv_imgproc.cvtColor(diff, gray, opencv_imgproc.COLOR_BGR2GRAY);
        Mat thresh = new Mat(); opencv_imgproc.threshold(gray, thresh, 30, 255, opencv_imgproc.THRESH_BINARY);

        Mat contours = new Mat();
        MatVector contourList = new MatVector();
        opencv_imgproc.findContours(thresh.clone(), contourList, contours,
                opencv_imgproc.RETR_EXTERNAL, opencv_imgproc.CHAIN_APPROX_SIMPLE);

        for (long i = 0; i < contourList.size(); i++) {
            Rect rect = opencv_imgproc.boundingRect(contourList.get(i));
            opencv_imgproc.rectangle(b, rect, new Scalar(0, 0, 255, 0), 2, 8, 0);
        }
        File f = Path.of(outPath).toFile();
        f.getParentFile().mkdirs();
        opencv_imgcodecs.imwrite(outPath, b);
    }
}
