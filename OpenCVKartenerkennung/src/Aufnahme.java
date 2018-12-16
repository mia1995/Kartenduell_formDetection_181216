//import org.opencv.bgsegm.BackgroundSubtractorMOG;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.opencv.core.Core.countNonZero;
import static org.opencv.imgproc.Imgproc.*;

public class Aufnahme extends JFrame {

    private BufferedImagePanel imgPanel1;
    private BufferedImagePanel imgPanel2;
    private BufferedImagePanel imgPanel3;
    Mat frame;
    private Detection dt = new Detection();

    public Aufnahme() {
        creatLayout();
    }

    public void creatLayout() {

        setTitle("Video stream");

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new FlowLayout());



        imgPanel1 = new BufferedImagePanel();
        contentPane.add(imgPanel1);
        imgPanel2 = new BufferedImagePanel();
        contentPane.add(imgPanel2);
        /*imgPanel3 = new BufferedImagePanel();
        contentPane.add(imgPanel3);*/
        ActionEvent event = null;
        pack();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        videoProcess();
    }

    public void videoProcess() {

        String filenameHerzCascade ="D:\\Benutzer\\Dokumente\\HSHL\\3. Semester\\VisualComputing\\Projekt\\OpenCVKartenerkennung\\herz_cascade.xml";
        String filenameKaroCascade = "D:\\Benutzer\\Dokumente\\HSHL\\3. Semester\\VisualComputing\\Projekt\\OpenCVKartenerkennung\\karo_cascade.xml";
        String filenamePikCascade = "D:\\Benutzer\\Dokumente\\HSHL\\3. Semester\\VisualComputing\\Projekt\\OpenCVKartenerkennung\\pik_cascade.xml";
        String filenameKreuzCascade = "D:\\Benutzer\\Dokumente\\HSHL\\3. Semester\\VisualComputing\\Projekt\\OpenCVKartenerkennung\\kreuz_cascade.xml";

        CascadeClassifier herzCascade = new CascadeClassifier();
        CascadeClassifier karoCascade = new CascadeClassifier();
        CascadeClassifier pikCascade = new CascadeClassifier();
        CascadeClassifier kreuzCascade = new CascadeClassifier();

        if (!herzCascade.load(filenameHerzCascade)){
            System.err.println("--Error loading herz cascade: " + filenameHerzCascade);
            System.exit(0);
        }
        if (!karoCascade.load(filenameKaroCascade)){
            System.err.println("--Error loading karo cascade: " + filenameKaroCascade);
            System.exit(0);
        }
        if (!pikCascade.load(filenamePikCascade)){
            System.err.println("--Error loading pik cascade: " + filenamePikCascade);
            System.exit(0);
        }
        if (!kreuzCascade.load(filenameKreuzCascade)){
            System.err.println("--Error loading kreuz cascade: " + filenameKreuzCascade);
            System.exit(0);
        }

        VideoCapture capture = new VideoCapture(0);
        frame = new Mat();

        if (!capture.isOpened())
            throw new CvException("The Video File or the Camera could not be opened!");
        capture.read(frame);

        while (capture.read(frame)) {

            //*****Hier werden die Methoden ausgeführt.*****

            dt.formDetect(frame, herzCascade, karoCascade, pikCascade, kreuzCascade);

            imgPanel1.setImage(Mat2BufferedImage(pruefen(frame)));
            imgPanel2.setImage(Mat2BufferedImage(karteErkennen(frame)));
            //imgPanel3.setImage(Mat2BufferedImage((frame)));
            pack();
            /*if(frame.height()<720 && frame.height()>300 && frame.width()<480 && frame.width()>200) {
                break;
            }*/
            if (!capture.read(frame)) {
                break;
            }
        }
        capture.release();
    }


    public BufferedImage Mat2BufferedImage(Mat imgMat) {
        int bufferedImageType = 0;
        switch (imgMat.channels()) {
            case 1:
                bufferedImageType = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                bufferedImageType = BufferedImage.TYPE_3BYTE_BGR;
                break;
            default:
                throw new IllegalArgumentException("Unknown matrix type. Only one byte per pixel (one channel) or three bytes pre pixel (three channels) are allowed.");
        }
        BufferedImage bufferedImage = new BufferedImage(imgMat.cols(), imgMat.rows(), bufferedImageType);
        //Was ist bufferedImage?
        final byte[] bufferedImageBuffer = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        imgMat.get(0, 0, bufferedImageBuffer);
        return bufferedImage;

    }

    public Mat weichZeichnenFilter(Mat img) {

        Mat grayMat = new Mat();
        Mat hierarchy = new Mat();
        Mat lines = new Mat();
        Mat kernal = new Mat();
        Mat dilateImg = new Mat();
        Mat img2 = new Mat();
        Mat imageThreshold = new Mat();
        Mat cannyEdges = new Mat();
        Mat imageBlur = new Mat();
        Mat imageContrast = new Mat();
        Size size = new Size(5, 5);

        ArrayList<MatOfPoint> contourListe = new ArrayList<>();

        Imgproc.cvtColor(img, grayMat, COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayMat, img2);
        Imgproc.GaussianBlur(img2, imageBlur, size, 5, 5);
        //Imgproc.dilate(grayMat, dilateImg, kernal);
        imageBlur.convertTo(imageContrast, -1, 2.5, 60);
        Imgproc.adaptiveThreshold(imageBlur, imageThreshold, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 45, 15); //hier liegt vielleicht das problem!
        Imgproc.Canny(imageBlur, cannyEdges, 100, 100); //hier liegt vielleicht das problem!

        Imgproc.findContours(cannyEdges, contourListe, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat contours = new Mat();
        contours.create(cannyEdges.rows(), cannyEdges.cols(), CvType.CV_8UC3);
        for (int i = 0; i < contourListe.size(); i++) {
            Imgproc.drawContours(contours, contourListe, i, new Scalar((255), (255), (255)), -1, 1);
        }

        Rect auswahl = new Rect(160,120,320,450);
        Mat imageAusgabe = new Mat (imageThreshold,auswahl);

        int TotalNumberOfPixels = imageAusgabe.rows() * imageAusgabe.cols();
        int zeroPixels = TotalNumberOfPixels - countNonZero(imageAusgabe);
        System.out.println(zeroPixels);

        return imageAusgabe;
    }

    public Mat karteErkennen(Mat img){

        Mat grayImg = new Mat();
        Mat blurImg = new Mat();
        Mat thresholdImg = new Mat();
        Size size = new Size(2, 2);
        Mat cannyEdges = new Mat();

        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grayImg, blurImg,size);
        Imgproc.threshold(blurImg, thresholdImg, 170, 255,Imgproc.THRESH_BINARY); //thresh!
        Imgproc.Canny(thresholdImg, cannyEdges, 50, 200, 3, false);

        ArrayList<MatOfPoint> contours= new ArrayList<>();

        Imgproc.findContours(cannyEdges,contours, new Mat(), Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        Mat imageAusgabe = img;

        if(contours.size()>0) {
            Imgproc.drawContours(img, contours, 1, new Scalar(0, 0, 255));

            MatOfPoint points = contours.get(1);
            MatOfPoint2f contours2D = new MatOfPoint2f(points.toArray());
            RotatedRect rect2 = Imgproc.minAreaRect(contours2D);

            Mat rotiert = new Mat();
            Mat cropped = new Mat();

            float angle = (float) rect2.angle;

            Size rect_size = rect2.size;

            if (rect2.angle < -45.) {
                angle += 90.0;
                rect2.size.height = rect_size.width;
                rect2.size.width = rect_size.height;
            }

            Mat m = Imgproc.getRotationMatrix2D(rect2.center, angle, 1.0);
            Imgproc.warpAffine(img, rotiert, m, img.size(), INTER_CUBIC);
            Imgproc.getRectSubPix(rotiert, rect2.size, rect2.center, cropped);

            if(cropped.width()>=21 && cropped.height()>=21) {
                Rect auswahl = new Rect(10, 10, cropped.width() - 20, cropped.height() - 20);
                imageAusgabe = new Mat(cropped, auswahl);
            }
        }

        return imageAusgabe;
    }

    public Mat karteErkennen2(Mat img) {

        //das geht theoretisch

        Mat grayImg = new Mat();
        Mat blurImg = new Mat();
        Mat histImg = new Mat();
        Mat ImgContrast = new Mat();
        Mat thresholdImg = new Mat();
        Mat cannyEdges = new Mat();
        Size size = new Size(7, 7);

        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
        //Imgproc.blur(grayImg, blurImg, size);
        //Imgproc.equalizeHist(grayImg,histImg); //test
        Imgproc.GaussianBlur(grayImg, blurImg, size, 10, 10);
        blurImg.convertTo(ImgContrast, -1, 3.0, 80);
        Imgproc.threshold(blurImg, thresholdImg, 225, 255, Imgproc.THRESH_BINARY);//thresh!
        //Imgproc.adaptiveThreshold(blurImg, thresholdImg, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 45, 15);
        //Imgproc.Canny(thresholdImg, cannyEdges, 100, 100);
        Imgproc.Canny(thresholdImg, cannyEdges, 50, 200, 3, false);

        ArrayList<MatOfPoint> contours= new ArrayList<>();

        Imgproc.findContours(cannyEdges,contours, new Mat(), Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        System.out.println(contours.size());

        if(contours.size()>0) {

            //Imgproc.drawContours(img, contours, contours.size()-1, new Scalar(0, 0, 255));
            //Imgproc.drawContours(img, contours, 1, new Scalar(0, 0, 255));

            System.out.println(contours.size());
            MatOfPoint points = contours.get(0);
            //MatOfPoint points = contours.get(0);
            MatOfPoint2f contours2D = new MatOfPoint2f(points.toArray());
            RotatedRect rect2 = Imgproc.minAreaRect(contours2D);

            Mat rotiert = new Mat();
            Mat cropped = new Mat();

            float angle = (float) rect2.angle;

            Size rect_size = rect2.size;

            if (rect2.angle < -45.) {
                angle += 90.0;
                rect2.size.height = rect_size.width;
                rect2.size.width = rect_size.height;
            }

            Mat m = Imgproc.getRotationMatrix2D(rect2.center, angle, 1.0);
            Imgproc.warpAffine(img, rotiert, m, img.size(), INTER_CUBIC);
            Imgproc.getRectSubPix(rotiert, rect2.size, rect2.center, cropped);

            if(cropped.width()>=21 && cropped.height()>=21) {
                Rect auswahl = new Rect(10, 10, cropped.width() - 20, cropped.height() - 20);
                Mat ausgabe = new Mat(cropped, auswahl);
                frame = ausgabe;
            }
        }

        return frame;
    }

    public Mat pruefen(Mat img) {

        Mat grayImg = new Mat();
        Mat blurImg = new Mat();
        Mat outputImg = img;
        Mat thresholdImg = new Mat();
        Mat cannyEdges = new Mat();
        Size size = new Size(5, 5);

        /*Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(grayImg, blurImg, size, 5, 5);
        Imgproc.threshold(blurImg, thresholdImg, 100, 255, Imgproc.THRESH_BINARY);
        Imgproc.Canny(thresholdImg, cannyEdges, 50, 200, 5, false);*/

        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grayImg, blurImg,size);
        Imgproc.threshold(blurImg, thresholdImg, 170, 255,Imgproc.THRESH_BINARY); //thresh!
        Imgproc.Canny(thresholdImg, cannyEdges, 50, 200, 3, false);

        return cannyEdges;

    }

    public Mat karteFreistellen(Mat img) {

        Mat grayImg = new Mat();
        Mat blurImg = new Mat();
        Mat outputImg = img;
        Mat thresholdImg = new Mat();
        Mat cannyEdges = new Mat();
        Mat lines = new Mat();
        Size size = new Size(5, 5);
        ArrayList<MatOfPoint> contours = new ArrayList<>();

        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(grayImg, blurImg, size, 5, 5);
        Imgproc.threshold(blurImg, thresholdImg, 100, 255, Imgproc.THRESH_BINARY);
        Imgproc.Canny(thresholdImg, cannyEdges, 150, 200, 5, false);

        //Imgproc.findContours(cannyEdges, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        //Imgproc.drawContours(outputImg, contours, -1, new Scalar(0, 0, 255));

        /*for(int i = 0; contours.size() > i; i++){
            contours.get(i);
            do
        }*/

        /*double maxVal = 0;
        int maxValIdx = 0;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea) {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }*/

        //Imgproc.drawContours(outputImg, contours, maxValIdx, new Scalar(0,255,0), 5);

        return outputImg;
    }

    public Mat karteFreistellenHoughlines (Mat img){

        Mat grayImg = new Mat();
        Mat cannyEdges = new Mat();
        Mat blurImg = new Mat();
        Mat thresholdImg = new Mat();
        Size size = new Size(5, 5);

        Mat lines = new Mat();

        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(grayImg, blurImg, size, 5, 5);
        Imgproc.threshold(blurImg, thresholdImg, 100, 255, Imgproc.THRESH_BINARY);
        Imgproc.Canny(thresholdImg, cannyEdges, 50, 200, 3, false);

        Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI/180, 50, 50, 10);
        System.out.println(lines.size());

        for (int x = 0; x < lines.rows(); x++) {
            double[] l = lines.get(x, 0);
            Imgproc.line(img, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        }

        return img;
    }

    public Mat backgroundsubtraction (Mat img){

        Mat output = new Mat();
        Mat grayMat = new Mat();
        Mat accumulatedBackground = new Mat();
        Mat backImage = new Mat();
        Mat foreground = new Mat();


        /*Imgproc.cvtColor(img, grayMat, COLOR_BGR2GRAY);

        Core.absdiff(img,grayMat,output);

        return output;*/

        return output;
    }

    public Mat karteErkennenPruefen(Mat img) {

        Mat grayImg = new Mat();
        Mat blurImg = new Mat();
        Mat thresholdImg = new Mat();
        Mat cannyEdges = new Mat();
        Size size = new Size(2, 2);

        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grayImg, blurImg, size);
        Imgproc.threshold(blurImg, thresholdImg, 108, 255, Imgproc.THRESH_BINARY);//thresh!
        Imgproc.Canny(thresholdImg, cannyEdges, 50, 200, 3, false);

        ArrayList<MatOfPoint> contours= new ArrayList<>();

        Imgproc.findContours(cannyEdges,contours, new Mat(), Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        if(contours.size()>0) {
            Imgproc.drawContours(img, contours, 1, new Scalar(0,0,255));
        }

        return img;
    }

}

//k next neighbour
//houghlines ausrichtung
//pixelzählen