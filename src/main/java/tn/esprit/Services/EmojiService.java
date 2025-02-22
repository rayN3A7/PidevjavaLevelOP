package tn.esprit.Services;

import javafx.scene.image.Image;

import java.util.Arrays;
import java.util.List;

public class EmojiService {
    private static final String TWEEMOJI_BASE_URL = "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/"; // Updated to jsDelivr

    public static List<Image> fetchEmojis() {
        // List of Unicode hexcodes for a variety of social-media-like and gaming-oriented emojis
        String[] emojiHexcodes = {
                "1f44d", // Thumbs Up (Like)
                "2764",  // Red Heart (Love)
                "1f602", // Face with Tears of Joy (Haha)
                "1f62e", // Disappointed Face (Sad)
                "1f620", // Angry Face (Angry)
                "1f60d", // Smiling Face with Heart-Eyes (Wow)
                "1f44f", // Clapping Hands (Applause, gaming-friendly)
                "1f525", // Fire (Popular gaming reaction)
                "1f4af", // Hundred Points (Gaming success)
                "1f389", // Party Popper (Celebration)
                "1f44c", // OK Hand (Gaming approval)
                "1f499", // Blue Heart (Additional social media)
                "1f60a", // Smiling Face with Sunglasses (Cool, gaming-friendly)
                "1f4a9", // Poop (Fun, social media)
                "1f680", // Rocket (Gaming adventure)
                "1f3c6", // Trophy (Gaming achievement)
                "1f381", // Gift (Celebration)
                "1f3ae", // Video Game (Gaming theme)
                "1f3b2", // Game Die (Gaming fun)
                "1f4a5", // Collision (Exciting, gaming)
                "1f64f", // Praying Hands (Gratitude, gaming-friendly)
                "1f3c3", // Runner (Gaming effort)
                "1f451", // Crown (Gaming victory)
                "1f3b0"  // Slot Machine (Gaming fun)
        };

        return Arrays.stream(emojiHexcodes)
                .map(hexcode -> {
                    Image image = new Image(TWEEMOJI_BASE_URL + hexcode + ".png", 48, 48, true, true); // Load as 48x48 PNG
                    if (image.isError()) {
                        System.err.println("Failed to load emoji for hexcode " + hexcode + ": " + image.getException());
                    }
                    return image;
                })
                .filter(image -> !image.isError()) // Filter out invalid images
                .toList();
    }
}

/*package tn.esprit.Services;

import javafx.scene.image.Image;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EmojiService {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "https://api.emojisworld.fr/v1/?categories=1&limit=10"; // Category 1 = "Smileys & People"

    public static List<Object> fetchEmojis() throws Exception {
        List<Object> emojis = new ArrayList<>(); // List to hold both Unicode emojis and Image objects

        // Fetch Unicode emojis from Emojis World API
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Failed to fetch emojis: " + response.code());
            }

            String jsonData = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray results = jsonObject.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject emojiObj = results.getJSONObject(i);
                String emoji = emojiObj.getString("emoji"); // Get the Unicode emoji
                emojis.add(emoji);
            }
        }

        // Add custom gaming emotes as Image objects (load from local resources or external URLs)
        String[] gameEmotes = {
                "/forumUI/icons/valorant.png", // Example Valorant emote path
                "/forumUI/icons/sucessalert.png",      // Example League of Legends emote path
                "/forumUI/icons/fortnite.png" // Example Overwatch emote path
        };

        for (String emotePath : gameEmotes) {
            try {
                Image emoteImage = new Image(EmojiService.class.getResourceAsStream(emotePath));
                if (!emoteImage.isError()) {
                    emojis.add(emoteImage);
                }
            } catch (Exception e) {
                System.err.println("Failed to load gaming emote from: " + emotePath + " - " + e.getMessage());
            }
        }

        return emojis;
    }
}*/