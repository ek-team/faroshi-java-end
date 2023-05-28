package cn.cuptec.faros.util;

import cn.cuptec.faros.common.utils.http.ServletUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QrCodeUtils {
    /**
     * 生成不带白边的二维码
     *
     * @param content 二维码内容(目标url)
     * @param qrCodePath 生成的二维码地址(最终保存地址)
     * @throws Exception 异常
     */
    public static BufferedImage generatorQrCode(String content, String qrCodePath) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();

        hints.put(EncodeHintType.MARGIN, 0);
        BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 256, 256, hints);

        // 去白边
        int[] rec = bitMatrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;
        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (bitMatrix.get(i + rec[0], j + rec[1])) {
                    resMatrix.set(i, j);
                }
            }
        }

        int width = resMatrix.getWidth();
        int height = resMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (resMatrix.get(x, y)) {
                    //                    image.setRGB(x, y, Color.BLACK.getRGB());
                    image.setRGB(x, y, -16777216);
                } else {
                    //                    image.setRGB(x, y, Color.WHITE.getRGB());
                    image.setRGB(x, y, -1);
                }
            }
        }
        return image;
    }
}

