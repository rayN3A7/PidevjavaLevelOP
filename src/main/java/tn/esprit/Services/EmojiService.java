package tn.esprit.Services;

import javafx.scene.image.Image;

import java.util.Arrays;
import java.util.List;

public class EmojiService {
    private static final String TWEEMOJI_BASE_URL = "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/"; // Updated to jsDelivr

    public static List<Image> fetchEmojis() {
        String[] emojiHexcodes = {
                "1f44d",
                "2764",
                "1f602",
                "1f62e",
                "1f620",
                "1f60d",
                "1f44f",
                "1f525",
                "1f4af",
                "1f389",
                "1f44c",
                "1f499",
                "1f60a",
                "1f4a9",
                "1f680",
                "1f3c6",
                "1f381",
                "1f3ae",
                "1f3b2",
                "1f4a5",
                "1f64f",
                "1f3c3",
                "1f451",
                "1f3b0"
        };

        return Arrays.stream(emojiHexcodes)
                .map(hexcode -> {
                    Image image = new Image(TWEEMOJI_BASE_URL + hexcode + ".png", 48, 48, true, true); // Load as 48x48 PNG
                    if (image.isError()) {
                        System.err.println("Failed to load emoji for hexcode " + hexcode + ": " + image.getException());
                    }
                    return image;
                })
                .filter(image -> !image.isError())
                .toList();
    }
}

