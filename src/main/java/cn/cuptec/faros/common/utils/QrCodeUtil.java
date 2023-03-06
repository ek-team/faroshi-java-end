package cn.cuptec.faros.common.utils;


import cn.hutool.extra.qrcode.BufferedImageLuminanceSource;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * @ Author     ：Rason Miao
 * @ Date       ：Created in 18:17 2018/9/4
 * @ Description：
 */
public class QrCodeUtil {

    /**
     * 条形码编码
     *
     * @param contents
     * @param width
     * @param height
     */
    public static BufferedImage encode(String contents, int width, int height) {
        int codeWidth = 3 + // start guard
                (7 * 6) + // left bars
                5 + // middle guard
                (7 * 6) + // right bars
                3; // end guard
        codeWidth = Math.max(codeWidth, width);
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.EAN_13, codeWidth, height, null);

            BufferedImage image = new BufferedImage(bitMatrix.getWidth(), bitMatrix.getHeight(), BufferedImage.TYPE_INT_RGB);
            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, bitMatrix.getWidth(), bitMatrix.getHeight());
            // 使用比特矩阵画并保存图像
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < bitMatrix.getWidth(); i++) {
                for (int j = 0; j < bitMatrix.getWidth(); j++) {
                    if (bitMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 条形码解码
     *
     * @param imgPath
     * @return String
     */
    public static String decode(String imgPath) {
        BufferedImage image = null;
        Result result = null;
        try {
            image = ImageIO.read(new File(imgPath));
            if (image == null) {
                System.out.println("the decode image may be not exit.");
            }
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            result = new MultiFormatReader().decode(bitmap, null);
            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建二维码
     *
     * @param outputStream
     * @param imageFormat
     * @param content
     * @param qrCodeSize
     * @return
     * @throws WriterException
     * @throws IOException
     */
    public static boolean createQrCode(OutputStream outputStream, String imageFormat, String content, int qrCodeSize) throws WriterException, IOException {
        //设置二维码的纠错级别
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); // 矫错级别

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        //创建比特矩阵(位矩阵)的QR码编码的字符串
        BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
        // 使BufferedImage勾画QRCode (matrixWidth 是行二维码像素点)
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // 使用比特矩阵画并保存图像
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        if (imageFormat == null) {
            imageFormat = "png";
        }
        return ImageIO.write(image, imageFormat, outputStream);
    }

    public static BufferedImage createQrCode(String content, int qrCodeSize) throws WriterException, IOException {
        //设置二维码的纠错级别
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); // 矫错级别

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        //创建比特矩阵(位矩阵)的QR码编码的字符串
        BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
        // 使BufferedImage勾画QRCode (matrixWidth 是行二维码像素点)
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // 使用比特矩阵画并保存图像
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        return image;
    }


    static BASE64Encoder encoder = new sun.misc.BASE64Encoder();
    static BASE64Decoder decoder = new sun.misc.BASE64Decoder();

    /**
     * 将图片转换成二进制
     *
     * @return
     */
    static String getImageBinary(String path) {
        File f = new File(path);
        BufferedImage bi;
        try {
            bi = ImageIO.read(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baos);  //经测试转换的图片是格式这里就什么格式，否则会失真
            byte[] bytes = baos.toByteArray();

            return encoder.encodeBuffer(bytes).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static final int QRCOLOR = 0xFF000000; // 默认是黑色
    private static final int BGWHITE = 0xFFFFFFFF; // 背景颜色
    private static final int WIDTH = 400; // 二维码宽
    private static final int HEIGHT = 400; // 二维码高
    // 用于设置QR二维码参数
    private static HashMap<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>() {
        private static final long serialVersionUID = 1L;

        {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);// 设置QR二维码的纠错级别（H为最高级别）具体级别信息
            put(EncodeHintType.CHARACTER_SET, "utf-8");// 设置编码方式
            put(EncodeHintType.MARGIN, 0);
        }
    };

    // 生成带logo的二维码图片
    public static BufferedImage drawLogoQRCode(OutputStream outputStream, String imageFormat, String logoFileUrl, String qrUrl, String note, int qrCodeSize, String note3, int type) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            BitMatrix bm = multiFormatWriter.encode(qrUrl, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);
            BufferedImage image = new BufferedImage(qrCodeSize, qrCodeSize, BufferedImage.TYPE_INT_RGB);

            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < qrCodeSize; x++) {
                for (int y = 0; y < qrCodeSize; y++) {
                    image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
                }
            }

            int width = image.getWidth();
            int height = image.getHeight();
            System.out.println(width);
            System.out.println(height);
            // 构建绘图对象
            Graphics2D g = image.createGraphics();
            if (!StringUtils.isEmpty(logoFileUrl)) {
                // 读取Logo图片
                BufferedImage logo = ImageIO.read(new URL(logoFileUrl));
                // 开始绘制logo图片
                g.drawImage(logo, width * 2 / 5, height * 2 / 5, width * 2 / 10, height * 2 / 10, null);
                g.dispose();
                logo.flush();
            }


            // 自定义文本描述
            if (note != null) {
                // 新的图片，把带logo的二维码下面加上文字
                BufferedImage outImage = new BufferedImage(qrCodeSize, qrCodeSize + 45, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D outg = outImage.createGraphics();
                // 画二维码到新的面板
                outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
                // 画文字到新的面板
                outg.setColor(Color.BLACK);
                outg.setFont(new Font("宋体", Font.BOLD, 22)); // 字体、字型、字号
                int strWidth = outg.getFontMetrics().stringWidth(note);

                int strWidth3 = outg.getFontMetrics().stringWidth(note3);

                if (strWidth > qrCodeSize) {
                    // //长度过长就截取前面部分
                    // 长度过长就换行
                    String note1 = note.substring(0, note.length() / 2);
                    String note2 = note.substring(note.length() / 2, note.length());
                    int strWidth1 = outg.getFontMetrics().stringWidth(note1);
                    int strWidth2 = outg.getFontMetrics().stringWidth(note2);
                    outg.drawString(note1, qrCodeSize / 2 - strWidth1 / 2, height + (outImage.getHeight() - height) / 2 + 12);
                    BufferedImage outImage2 = new BufferedImage(qrCodeSize, qrCodeSize + 85, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D outg2 = outImage2.createGraphics();
                    outg2.drawImage(outImage, 0, 0, outImage.getWidth(), outImage.getHeight(), null);
                    outg2.setColor(Color.BLACK);
                    outg2.setFont(new Font("宋体", Font.BOLD, 22)); // 字体、字型、字号
                    outg2.drawString(note2, qrCodeSize / 2 - strWidth2 / 2, outImage.getHeight() + (outImage2.getHeight() - outImage.getHeight()) / 2 + 5);

                    outg2.dispose();


                    outImage2.flush();
                    outImage = outImage2;
                    //增加第三行
                    BufferedImage outImage3 = new BufferedImage(qrCodeSize, qrCodeSize + 85, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D outg3 = outImage3.createGraphics();
                    outg3.drawImage(outImage, 0, 0, outImage.getWidth(), outImage.getHeight(), null);
                    outg3.setColor(Color.BLACK);
                    outg3.setFont(new Font("宋体", Font.BOLD, 22)); // 字体、字型、字号
                    outg3.drawString(note3, qrCodeSize / 2 - strWidth3 / 2, outImage.getHeight() + (outImage3.getHeight() - outImage.getHeight()) / 2 + 5);

                    outg3.dispose();


                    outImage3.flush();
                    outImage = outImage3;

                } else {
                    if (type != 3) {
                        outg.drawString(note, qrCodeSize / 2 - strWidth / 2, height + (outImage.getHeight() - height) / 2 + 12); // 画文字

                    }
                    //增加第三行
                    BufferedImage outImage3 = new BufferedImage(qrCodeSize, qrCodeSize + 85, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D outg3 = outImage3.createGraphics();
                    outg3.drawImage(outImage, 0, 0, outImage.getWidth(), outImage.getHeight(), null);
                    outg3.setColor(Color.BLACK);
                    outg3.setFont(new Font("宋体", Font.BOLD, 20)); // 字体、字型、字号
                    if (type != 3) {
                        outg3.drawString(note3, 15, height + 60);

                    }
                    //增加第一行标题


                    BufferedImage outImage1 = new BufferedImage(343, qrCodeSize + 130, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D outg1 = outImage1.createGraphics();
                    outg1.drawImage(outImage3, 20, 40, outImage3.getWidth(), outImage3.getHeight(), null);
//                    outg1.setColor(Color.BLACK);
//                    outg1.setFont(new Font("宋体", Font.BOLD, 25)); // 字体、字型、字号
//                    outg1.drawString("康  复  护  航   有  医  相  伴", 0, 30);

                    BufferedImage outImage4 = new BufferedImage(387, qrCodeSize + 183, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D outg4 = outImage4.createGraphics();

                    outg4.drawImage(outImage1, 30, 30, outImage1.getWidth(), outImage1.getHeight(), null);
                    if (type != 2) {
                        outg4.setColor(Color.BLACK);
                        outg4.setFont(new Font("宋体", Font.BOLD, 26)); // 字体、字型、字号
                        outg4.drawString("微 信 公 众 号：易 网 健", 59, 469);
                    }

                    if (type == 1 || type == 3) {
                        //添加字体图片
                        String url = "https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/000000.png";
                        // 读取Logo图片
                        BufferedImage logo = ImageIO.read(new URL(url));
                        // 开始绘制logo图片
                        outg4.drawImage(logo, 0, 0, null);
                        logo.flush();
                    }


                    outg4.dispose();


                    outImage4.flush();
                    outImage = outImage4;
                }
                outg.dispose();
                outImage.flush();
                image = outImage;
            }

            image.flush();
            ImageIO.write(image, "png", outputStream); // TODO
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage drawLogoQRCodeHospital1(OutputStream outputStream, String imageFormat, String logoFileUrl, String qrUrl, String note, int qrCodeSize, String note3, int type) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            BitMatrix bm = multiFormatWriter.encode(qrUrl, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);
            BufferedImage image = new BufferedImage(qrCodeSize, qrCodeSize, BufferedImage.TYPE_INT_RGB);

            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < qrCodeSize; x++) {
                for (int y = 0; y < qrCodeSize; y++) {
                    image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
                }
            }

            int width = image.getWidth();
            int height = image.getHeight();
            System.out.println(width);
            System.out.println(height);
            // 构建绘图对象
            Graphics2D g = image.createGraphics();
            if (!StringUtils.isEmpty(logoFileUrl)) {
                // 读取Logo图片
                BufferedImage logo = ImageIO.read(new URL(logoFileUrl));
                // 开始绘制logo图片
                g.drawImage(logo, width * 2 / 5, height * 2 / 5, width * 2 / 10, height * 2 / 10, null);
                g.dispose();
                logo.flush();
            }
            // 新的图片，把带logo的二维码下面加上文字
            BufferedImage outImage = new BufferedImage(qrCodeSize, qrCodeSize + 45, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D outg = outImage.createGraphics();
            // 画二维码到新的面板
            outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
            // 画文字到新的面板
            outg.setColor(Color.BLACK);
            outg.setFont(new Font("宋体", Font.BOLD, 22)); // 字体、字型、字号


            BufferedImage outImage1 = new BufferedImage(387, qrCodeSize + 183, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D outg1 = outImage1.createGraphics();
            outg1.drawImage(outImage, 50, 70, outImage.getWidth(), outImage.getHeight(), null);
            outg1.setColor(Color.BLACK);
            if (type != 1) {
                outg1.setFont(new Font("宋体", Font.BOLD, 26)); // 字体、字型、字号
                outg1.drawString("售后服务及回收请扫码", 70, 409);
                outg1.setFont(new Font("宋体", Font.BOLD, 25)); // 字体、字型、字号
                outg1.drawString("咨询热线:400-900-1022", 50, 439);
                outg1.setFont(new Font("宋体", Font.BOLD, 26)); // 字体、字型、字号
                outg1.drawString("微 信 公 众 号 ：易 网 健", 50, 469);
            } else {
                outg1.setFont(new Font("宋体", Font.BOLD, 26)); // 字体、字型、字号
                outg1.drawString("微 信 公 众 号：易 网 健", 59, 409);

            }


            //添加字体图片
            String url = "https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/000000.png";
            // 读取Logo图片
            BufferedImage logo = ImageIO.read(new URL(url));
            // 开始绘制logo图片
            outg1.drawImage(logo, 0, 0, null);
            logo.flush();


            outg1.dispose();
            outImage1.flush();
            outImage = outImage1;
            outg.dispose();
            outImage.flush();
            image = outImage;
            image.flush();
            ImageIO.write(image, "png", outputStream); // TODO
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage drawLogoQRCodeHospital2(OutputStream outputStream, String imageFormat, String logoFileUrl, String qrUrl, String note, int qrCodeSize, String note3, int type) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            BitMatrix bm = multiFormatWriter.encode(qrUrl, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);
            BufferedImage image = new BufferedImage(qrCodeSize, qrCodeSize, BufferedImage.TYPE_INT_RGB);

            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < qrCodeSize; x++) {
                for (int y = 0; y < qrCodeSize; y++) {
                    image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
                }
            }

            int width = image.getWidth();
            int height = image.getHeight();
            System.out.println(width);
            System.out.println(height);
            // 构建绘图对象
            Graphics2D g = image.createGraphics();
            if (!StringUtils.isEmpty(logoFileUrl)) {
                // 读取Logo图片
                BufferedImage logo = ImageIO.read(new URL(logoFileUrl));
                // 开始绘制logo图片
                g.drawImage(logo, width * 2 / 5, height * 2 / 5, width * 2 / 10, height * 2 / 10, null);
                g.dispose();
                logo.flush();
            }
            // 新的图片，把带logo的二维码下面加上文字
            BufferedImage outImage = new BufferedImage(qrCodeSize, qrCodeSize + 45, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D outg = outImage.createGraphics();
            // 画二维码到新的面板
            outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
            // 画文字到新的面板
            outg.setColor(Color.BLACK);
            outg.setFont(new Font("宋体", Font.BOLD, 22)); // 字体、字型、字号


            BufferedImage outImage1 = new BufferedImage(387, qrCodeSize + 183, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D outg1 = outImage1.createGraphics();
            outg1.drawImage(outImage, 50, 70, outImage.getWidth(), outImage.getHeight(), null);
            outg1.setColor(Color.BLACK);
            if (type == 1) {
                outg1.setFont(new Font("宋体", Font.BOLD, 26)); // 字体、字型、字号
                outg1.drawString("售后服务及回收请扫码", 70, 409);
                outg1.setFont(new Font("宋体", Font.BOLD, 25)); // 字体、字型、字号
                outg1.drawString("咨询热线:400-618-9886", 50, 439);

            } else {
                outg1.setFont(new Font("宋体", Font.BOLD, 26)); // 字体、字型、字号
                outg1.drawString("售后服务及回收请扫码", 70, 409);
                outg1.setFont(new Font("宋体", Font.BOLD, 25)); // 字体、字型、字号
                outg1.drawString("咨询热线:400-900-1022", 50, 439);
            }

            outg1.dispose();
            outImage1.flush();
            outImage = outImage1;
            outg.dispose();
            outImage.flush();
            image = outImage;
            image.flush();
            ImageIO.write(image, "png", outputStream); // TODO
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static BufferedImage orderImage(OutputStream outputStream,  String logoFileUrl, String qrUrl, int qrCodeSize) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            BitMatrix bm = multiFormatWriter.encode(qrUrl, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);
            BufferedImage image = new BufferedImage(qrCodeSize, qrCodeSize, BufferedImage.TYPE_INT_RGB);

            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < qrCodeSize; x++) {
                for (int y = 0; y < qrCodeSize; y++) {
                    image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
                }
            }

            int width = image.getWidth();
            int height = image.getHeight();
            System.out.println(width);
            System.out.println(height);
            // 构建绘图对象
            Graphics2D g = image.createGraphics();
            if (!StringUtils.isEmpty(logoFileUrl)) {
                // 读取Logo图片
                BufferedImage logo = ImageIO.read(new URL(logoFileUrl));
                // 开始绘制logo图片
                g.drawImage(logo, width * 2 / 5, height * 2 / 5, width * 2 / 10, height * 2 / 10, null);
                g.dispose();
                logo.flush();
            }
            // 新的图片，把带logo的二维码下面加上文字
            BufferedImage outImage = new BufferedImage(qrCodeSize, qrCodeSize + 45, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D outg = outImage.createGraphics();
            // 画二维码到新的面板
            outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
            // 画文字到新的面板
            outg.setColor(Color.BLACK);
            outg.setFont(new Font("宋体", Font.BOLD, 22)); // 字体、字型、字号


            BufferedImage outImage1 = new BufferedImage(387, qrCodeSize + 183, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D outg1 = outImage1.createGraphics();
            outg1.drawImage(outImage, 50, 70, outImage.getWidth(), outImage.getHeight(), null);
            outg1.setColor(Color.BLACK);
                outg1.setFont(new Font("宋体", Font.BOLD, 26)); // 字体、字型、字号
                outg1.drawString("识别二维码帮我代付", 70, 409);




            outg1.dispose();
            outImage1.flush();
            outImage = outImage1;
            outg.dispose();
            outImage.flush();
            image = outImage;
            image.flush();
            //ImageIO.write(image, "png", outputStream); // TODO
            return outImage1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static BufferedImage doctorImage(OutputStream outputStream,  String logoFileUrl, String qrUrl, int qrCodeSize) {
        logoFileUrl="http://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20191129103524.jpg";
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            BitMatrix bm = multiFormatWriter.encode(qrUrl, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);
            BufferedImage image = new BufferedImage(qrCodeSize, qrCodeSize, BufferedImage.TYPE_INT_RGB);

            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < qrCodeSize; x++) {
                for (int y = 0; y < qrCodeSize; y++) {
                    image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
                }
            }

            int width = image.getWidth();
            int height = image.getHeight();
            System.out.println(width);
            System.out.println(height);
            // 构建绘图对象
            Graphics2D g = image.createGraphics();
            if (!StringUtils.isEmpty(logoFileUrl)) {
                // 读取Logo图片
                BufferedImage logo = ImageIO.read(new URL(logoFileUrl));
                // 开始绘制logo图片
                g.drawImage(logo, width * 2 / 5, height * 2 / 5, width * 2 / 10, height * 2 / 10, null);
                g.dispose();
                logo.flush();
            }
            // 新的图片，把带logo的二维码下面加上文字
            BufferedImage outImage = new BufferedImage(qrCodeSize, qrCodeSize + 45, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D outg = outImage.createGraphics();
            // 画二维码到新的面板
            outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
            // 画文字到新的面板
            outg.setColor(Color.BLACK);
            outg.setFont(new Font("宋体", Font.BOLD, 22)); // 字体、字型、字号


//            BufferedImage outImage1 = new BufferedImage(387, qrCodeSize + 183, BufferedImage.TYPE_4BYTE_ABGR);
//            Graphics2D outg1 = outImage1.createGraphics();
//            outg1.drawImage(outImage, 50, 70, outImage.getWidth(), outImage.getHeight(), null);
//            outg1.setColor(Color.BLACK);
//            outg1.setFont(new Font("宋体", Font.BOLD, 26)); // 字体、字型、字号
//
//            outg1.dispose();
//            outImage1.flush();
//            outImage = outImage1;
            outg.dispose();
            outImage.flush();
            image = outImage;
            image.flush();
            //ImageIO.write(image, "png", outputStream); // TODO
            return outImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
