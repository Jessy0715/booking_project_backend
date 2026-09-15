package com.jessy.booking_project.upload;

import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 上傳時縮圖：長邊超過 1280px 就等比例縮小，並統一輸出 JPEG（品質 0.85）。
 * 一張 4 MB 的手機照片處理後約 150–300 KB。
 *
 * <p>順帶的效果：重新編碼會把 EXIF（拍攝地點、機型等）整個丟掉，對使用者隱私是好事。
 *
 * <p>已知限制：
 * <ul>
 *   <li>Java 內建 ImageIO 不認 WebP，WebP 原檔直接存，不縮。</li>
 *   <li>EXIF 丟掉也包含「方向」資訊，手機直拍的照片可能會變橫的。要修得另外讀 EXIF orientation。</li>
 *   <li>PNG 的透明區域會鋪成白色（轉 JPEG 沒有透明）。</li>
 * </ul>
 */
@Slf4j
@Component
public class ImageResizer {

    /** 場地照片在網頁上最大就顯示這麼寬，存更大只是浪費空間。 */
    private static final int MAX_SIDE = 1280;

    /** 0.85 是肉眼幾乎看不出差別、檔案卻小很多的常用值。 */
    private static final float JPEG_QUALITY = 0.85f;

    /** 處理結果：內容 + 實際輸出的格式（可能跟輸入不同，例如 PNG 進 JPEG 出）。 */
    public record Result(byte[] content, ImageType type) {
    }

    public Result resize(byte[] input, ImageType type) {
        // ImageIO 預設不支援 WebP，先原樣放行
        if (type == ImageType.WEBP) {
            return new Result(input, type);
        }

        BufferedImage source = decode(input);
        BufferedImage scaled = scaleToFit(source);
        byte[] jpeg = encodeJpeg(scaled);

        log.debug("縮圖：{}x{} → {}x{}，{} KB → {} KB",
                source.getWidth(), source.getHeight(), scaled.getWidth(), scaled.getHeight(),
                input.length / 1024, jpeg.length / 1024);

        return new Result(jpeg, ImageType.JPEG);
    }

    private BufferedImage decode(byte[] input) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(input));
            // 簽名對但內容壞掉（例如被截斷的檔案）會回 null，不是丟例外
            if (image == null) {
                throw new BusinessException(ErrorCode.UPLOAD_TYPE_NOT_ALLOWED);
            }
            return image;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        }
    }

    /** 等比例縮到長邊 ≤ MAX_SIDE；本來就夠小的不放大。 */
    private BufferedImage scaleToFit(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        double scale = Math.min(1.0, (double) MAX_SIDE / Math.max(width, height));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));

        // TYPE_INT_RGB 沒有 alpha 通道，才能寫成 JPEG；先鋪白底，PNG 的透明區才不會變黑
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, targetWidth, targetHeight);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g.dispose();
        }
        return target;
    }

    /** ImageIO.write() 不能指定品質，要用 ImageWriter 才行。 */
    private byte[] encodeJpeg(BufferedImage image) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(JPEG_QUALITY);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }
}
