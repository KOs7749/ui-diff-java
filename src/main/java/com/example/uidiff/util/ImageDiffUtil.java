package com.example.uidiff.util;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;

import java.io.File;

public class ImageDiffUtil {

    /** Tạo ảnh nhị phân vùng khác biệt + vẽ bbox để dễ nhìn */
    public static void diffWithBoxes(String img1, String img2, String outPath) {
        Mat a = opencv_imgcodecs.imread(img1);
        Mat b = opencv_imgcodecs.imread(img2);
        if (a.empty() || b.empty())
            throw new RuntimeException("Cannot read images: " + img1 + " / " + img2);

        if (a.size().width() != b.size().width() || a.size().height() != b.size().height()) {
            opencv_imgproc.resize(b, b, a.size());
        }

        Mat diff = new Mat();
        opencv_core.absdiff(a, b, diff);

        Mat gray = new Mat();
        opencv_imgproc.cvtColor(diff, gray, opencv_imgproc.COLOR_BGR2GRAY);

        Mat thresh = new Mat();
        opencv_imgproc.threshold(gray, thresh, 30, 255, opencv_imgproc.THRESH_BINARY);

        // Tìm contour và vẽ khung đỏ lên ảnh V2 để người xem dễ nhận biết
        Mat contours = new Mat();
        MatVector contourList = new MatVector();
        opencv_imgproc.findContours(thresh.clone(), contourList, contours,
                opencv_imgproc.RETR_EXTERNAL, opencv_imgproc.CHAIN_APPROX_SIMPLE);

        for (long i = 0; i < contourList.size(); i++) {
            Rect rect = opencv_imgproc.boundingRect(contourList.get(i));
            opencv_imgproc.rectangle(b, rect, new Scalar(0, 0, 255, 0), 2, 8, 0); // RED
        }

        // Lưu ảnh diff (vẽ trên ảnh V2)
        new File(outPath).getParentFile().mkdirs();
        opencv_imgcodecs.imwrite(outPath, b);
    }
}
