package com.jessy.booking_project.upload;

import java.util.Arrays;
import java.util.Optional;

/**
 * 允許上傳的圖片格式，以及每種格式檔案開頭的固定簽名（magic bytes）。
 *
 * <p>簽名是檔案格式規範定的，改副檔名或 Content-Type 都改不掉，所以拿它來判斷才可靠。
 */
public enum ImageType {

    /** FF D8 FF */
    JPEG("jpg", new Signature(0, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})),

    /** 89 'P' 'N' 'G' 0D 0A 1A 0A */
    PNG("png", new Signature(0, new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A})),

    /** "RIFF" + 4 bytes 檔案大小（每張不同，跳過）+ "WEBP"，所以是兩段。 */
    WEBP("webp",
            new Signature(0, new byte[]{'R', 'I', 'F', 'F'}),
            new Signature(8, new byte[]{'W', 'E', 'B', 'P'}));

    /** 判斷格式最少需要讀的 bytes 數（WEBP 的 8 + 4）。 */
    public static final int HEADER_LENGTH = 12;

    private final String extension;
    private final Signature[] signatures;

    ImageType(String extension, Signature... signatures) {
        this.extension = extension;
        this.signatures = signatures;
    }

    public String extension() {
        return extension;
    }

    /** 從檔案開頭的 bytes 判斷格式；都不符合回 empty。 */
    public static Optional<ImageType> detect(byte[] head) {
        return Arrays.stream(values())
                .filter(type -> type.matches(head))
                .findFirst();
    }

    private boolean matches(byte[] head) {
        return Arrays.stream(signatures).allMatch(s -> s.matches(head));
    }

    /** 「從第 offset 個 byte 起，必須是這串 bytes」。 */
    private record Signature(int offset, byte[] bytes) {

        boolean matches(byte[] head) {
            if (head.length < offset + bytes.length) {
                return false;
            }
            // Java 的 byte 有號（-128 ~ 127），但 == 比較不受影響，直接比即可
            for (int i = 0; i < bytes.length; i++) {
                if (head[offset + i] != bytes[i]) {
                    return false;
                }
            }
            return true;
        }
    }
}
