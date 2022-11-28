package cn.jinnyu.base.bar;

import cn.jinnyu.base.lang.LangKit;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-10-26
 */
public enum BarcodeKit {

    ;

    // --------------------------------------------------

    public static final int UNIT_PX                  = 100;
    /**
     * 边框距画布距离 (px)
     */
    public static final int DEFAULT_BOUND_BOX_MARGIN = 5;

    // --------------------------------------------------

    public static final BarcodeConfig DEFAULT_QRCODE_CONFIG             = new BarcodeConfig();
    public static final BarcodeConfig DEFAULT_CODE_128_CONFIG           = new BarcodeConfig();
    public static final BarcodeConfig DEFAULT_CODE_128_WITH_TEXT_CONFIG = new BarcodeConfig();

    static {
        DEFAULT_QRCODE_CONFIG.setWidth(UNIT_PX * 4);
        DEFAULT_QRCODE_CONFIG.setHeight(UNIT_PX * 4);
        DEFAULT_QRCODE_CONFIG.setBarcodeFormat(BarcodeFormat.QR_CODE);
        DEFAULT_QRCODE_CONFIG.setCharset(StandardCharsets.UTF_8);
        DEFAULT_QRCODE_CONFIG.setCorrectionLevel(ErrorCorrectionLevel.H);
        DEFAULT_QRCODE_CONFIG.setEdgeMarginPx(1);

        DEFAULT_CODE_128_CONFIG.setWidth(UNIT_PX * 4);
        DEFAULT_CODE_128_CONFIG.setHeight(UNIT_PX);
        DEFAULT_CODE_128_CONFIG.setBarcodeFormat(BarcodeFormat.CODE_128);
        DEFAULT_CODE_128_CONFIG.setEdgeMarginPx(1);

        DEFAULT_CODE_128_WITH_TEXT_CONFIG.setWidth(UNIT_PX * 4);
        DEFAULT_CODE_128_WITH_TEXT_CONFIG.setHeight(UNIT_PX * 3);
        DEFAULT_CODE_128_WITH_TEXT_CONFIG.setBarcodeFormat(BarcodeFormat.CODE_128);
        DEFAULT_CODE_128_WITH_TEXT_CONFIG.setEdgeMarginPx(1);

    }

    // --------------------------------------------------

    @Data
    public static class BarcodeConfig {
        /**
         * 画布宽度 (条形码宽高比推荐 4:3 / 二维码 需保持宽高一直)
         */
        private int                  width;
        /**
         * 画布高度 (条形码宽高比推荐 4:3 / 二维码 需保持宽高一直)
         */
        private int                  height;
        /**
         * 码类型
         */
        private BarcodeFormat        barcodeFormat;
        /**
         * 输出文件格式 默认: png
         */
        private String               fileFormat = "png";
        /**
         * 二维码使用 - 内容默认编码
         */
        private Charset              charset;
        /**
         * 二维码使用 - 容错等级 L、M、Q、H (L 为最低, H 为最高)
         */
        private ErrorCorrectionLevel correctionLevel;
        /**
         * 条形码/二维码使用 码内容距边缘距离
         */
        private int                  edgeMarginPx;
    }

    public enum TextPosition {
        LEFT_UP, LEFT_DOWN_1, LEFT_DOWN_2, RIGHT_UP, RIGHT_DOWN_1, RIGHT_DOWN_2,
    }

    // --------------------------------------------------

    private static Map<EncodeHintType, ?> hintTypeMap(BarcodeConfig config) {
        Map<EncodeHintType, Object> map = new HashMap<>();
        if (BarcodeFormat.QR_CODE.equals(config.getBarcodeFormat())) {
            // 内容编码
            map.put(EncodeHintType.CHARACTER_SET, config.getCharset().name());
            // 容错等级 L、M、Q、H 其中 L 为最低, H 为最高
            map.put(EncodeHintType.ERROR_CORRECTION, config.getCorrectionLevel());
        }
        // 图片边距
        map.put(EncodeHintType.MARGIN, config.getEdgeMarginPx());
        return map;
    }

    // --------------------------------------------------

    public static void writeToFile(BufferedImage image, String format, String path) throws IOException {
        ImageIO.write(image, format, new File(path));
    }

    public static void writeToStream(BufferedImage image, String format, OutputStream out) throws IOException {
        ImageIO.write(image, format, out);
    }

    // --------------------------------------------------

    public static BufferedImage createQrcode(String content) throws WriterException {
        return createQrcode(content, DEFAULT_QRCODE_CONFIG);
    }

    /**
     * 创建二维码
     *
     * @param content 内容
     * @param config  二维码配置
     * @return 图片
     * @throws WriterException 内容无法编码时抛出本异常
     */
    public static BufferedImage createQrcode(String content, BarcodeConfig config) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, config.getBarcodeFormat(), config.getWidth(), config.getHeight(), hintTypeMap((config)));
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    // --------------------------------------------------

    /**
     * 400 * 100, 边距1px
     *
     * @param code 条形码内容
     * @return 图片
     */
    public static BufferedImage createBarcode(String code) {
        return createBarcode(code, DEFAULT_CODE_128_CONFIG);
    }

    /**
     * 创建条形码
     *
     * @param code   条形码
     * @param config 条形码配置
     * @return 图片
     */
    public static BufferedImage createBarcode(String code, BarcodeConfig config) {
        BitMatrix bitMatrix = new Code128Writer().encode(code, BarcodeFormat.CODE_128, config.getWidth(), config.getHeight(), hintTypeMap(config));
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public static BufferedImage createBarcodeWithTexts(String code, Map<String, TextPosition> texts) {
        return createBarcodeWithTexts(code, DEFAULT_CODE_128_WITH_TEXT_CONFIG, texts);
    }

    /**
     * 创建一个基于 给定画布 3/4宽 1/4高 的 条形码图片<br>可以在 左上 右上 左下(2行) 右下(2行) 设置文字的图片
     *
     * @param code   条形码
     * @param config 条形码配置
     * @param texts  文本内容
     * @return 图片
     */
    public static BufferedImage createBarcodeWithTexts(String code, BarcodeConfig config, Map<String, TextPosition> texts) {
        int           bgWidth   = config.getWidth();
        int           bgHeight  = config.getHeight();
        BarcodeConfig barConfig = new BarcodeConfig();
        barConfig.setWidth(bgWidth / 4 * 3);
        barConfig.setHeight(bgHeight / 4);
        BufferedImage image  = createBarcode(code, barConfig);
        BufferedImage output = new BufferedImage(bgWidth, bgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D    g2d    = output.createGraphics();
        // 抗锯齿
        setGraphics2D(g2d);
        // 设置背景白色
        drawBackground(g2d, bgWidth, bgHeight);
        // 设置边框
        drawBoundBox(g2d, bgWidth, bgHeight);
        // 将条形码打印新的画布
        drawBarcodeImage(g2d, image, bgWidth, bgHeight);
        // 将条形码文字打印到新的画布
        drawBarcodeText(g2d, image, bgWidth, bgHeight, code);
        // 将文字打印到画布
        if (!LangKit.isEmpty(texts)) {
            for (Map.Entry<String, TextPosition> entry : texts.entrySet()) {
                drawWords(g2d, bgWidth, bgHeight, entry.getKey(), entry.getValue());
            }
        }
        g2d.dispose();
        output.flush();
        return output;
    }

    /**
     * 设置 Graphics2D 属性（抗锯齿）
     *
     * @param g2d Graphics2D提供对几何形状、坐标转换、颜色管理和文本布局更为复杂的控制
     */
    private static void setGraphics2D(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        Stroke s = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        g2d.setStroke(s);
    }

    /**
     * 设置背景为白色
     *
     * @param g2d Graphics2D提供对几何形状、坐标转换、颜色管理和文本布局更为复杂的控制
     */
    private static void drawBackground(Graphics2D g2d, int width, int height) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
    }

    /**
     * 设置边框
     *
     * @param g2d Graphics2D提供对几何形状、坐标转换、颜色管理和文本布局更为复杂的控制
     */
    private static void drawBoundBox(Graphics2D g2d, int width, int height) {
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(DEFAULT_BOUND_BOX_MARGIN, DEFAULT_BOUND_BOX_MARGIN, width - (DEFAULT_BOUND_BOX_MARGIN * 2), height - (DEFAULT_BOUND_BOX_MARGIN * 2));
    }

    private static void drawBarcodeImage(Graphics2D g2d, BufferedImage image, int bgWidth, int bgHeight) {
        g2d.drawImage(image, (bgWidth - image.getWidth()) / 2, (bgHeight - image.getHeight()) / 2 - DEFAULT_BOUND_BOX_MARGIN * 2, image.getWidth(), image.getHeight(), null);
    }

    private static void drawBarcodeText(Graphics2D g2d, BufferedImage barcodeImage, int bgWidth, int bgHeight, String code) {
        // 将文字到新的面板
        Color color = new Color(0, 0, 0);
        g2d.setColor(color);
        // 字体、字型、字号
        g2d.setFont(new Font("宋体", Font.PLAIN, 14));
        // 文字长度
        code = code.replace("", " ").trim();
        int wordsWidth = g2d.getFontMetrics().stringWidth(code);
        int wordStartX = (bgWidth - wordsWidth) / 2;
        int wordStartY = (bgHeight + barcodeImage.getHeight()) / 2 + DEFAULT_BOUND_BOX_MARGIN * 2;
        g2d.drawString(code, wordStartX, wordStartY);
    }

    private static void drawWords(Graphics2D g2d, int width, int height, String words, TextPosition tp) {
        int fontWidth = g2d.getFontMetrics().stringWidth(words);
        int x         = 0;
        int y         = 0;
        switch (tp) {
            case LEFT_UP: {
                x = DEFAULT_BOUND_BOX_MARGIN * 4;
                y = DEFAULT_BOUND_BOX_MARGIN * 6;
                break;
            }
            case RIGHT_UP: {
                x = width - DEFAULT_BOUND_BOX_MARGIN * 4 - fontWidth;
                y = DEFAULT_BOUND_BOX_MARGIN * 6;
                break;
            }
            case LEFT_DOWN_1: {
                x = DEFAULT_BOUND_BOX_MARGIN * 4;
                y = height - DEFAULT_BOUND_BOX_MARGIN * 10;
                break;
            }
            case LEFT_DOWN_2: {
                x = DEFAULT_BOUND_BOX_MARGIN * 4;
                y = height - DEFAULT_BOUND_BOX_MARGIN * 6;
                break;
            }
            case RIGHT_DOWN_1: {
                x = width - DEFAULT_BOUND_BOX_MARGIN * 4 - fontWidth;
                y = height - DEFAULT_BOUND_BOX_MARGIN * 10;
                break;
            }
            case RIGHT_DOWN_2: {
                x = width - DEFAULT_BOUND_BOX_MARGIN * 4 - fontWidth;
                y = height - DEFAULT_BOUND_BOX_MARGIN * 6;
                break;
            }
        }
        g2d.drawString(words, x, y);
    }

}
