package tn.esprit.Services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.scene.image.Image;

public class EmojiService {

    public static class Emoji {
        private final String unicode;
        private final String sentiment;
        private final String imageUrl;

        public Emoji(String unicode, String sentiment, String imageUrl) {
            this.unicode = unicode;
            this.sentiment = sentiment;
            this.imageUrl = imageUrl;
        }

        public String getUnicode() { return unicode; }
        public String getSentiment() { return sentiment; }
        public String getImageUrl() { return imageUrl; }
    }

    private static final String API_KEY = "22b221fd734cfd73f62238bc32fa072f636bc63b";
    private static final String EMOJI_API_URL = "https://emoji-api.com/emojis?access_key=" + API_KEY;
    private static final String TWEMOJI_BASE_URL = "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/";
    private static final List<String> RELEVANT_GROUPS = Arrays.asList("smileys-emotion", "people-body", "activities", "food-drink", "travel-places", "objects", "animals-nature");
    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);
    private static volatile Map<String, List<Emoji>> cachedEmojis = null;
    private static final Object lock = new Object();
    private static final Map<String, CompletableFuture<Image>> imageCache = new HashMap<>();

    static {
        preloadEmojis();
    }

    private static void preloadEmojis() {
        fetchEmojis().thenAccept(emojis -> {
            if (emojis != null) {
                emojis.forEach((sentiment, emojiList) ->
                        emojiList.forEach(emoji -> loadImageAsync(emoji.getImageUrl())));
                System.out.println("Emojis preloaded successfully.");
            } else {
                System.err.println("Preloading returned null emojis.");
            }
        }).exceptionally(throwable -> {
            System.err.println("Preloading failed: " + throwable.getMessage());
            return null;
        });
    }

    public static CompletableFuture<Map<String, List<Emoji>>> fetchEmojis() {
        synchronized (lock) {
            if (cachedEmojis != null) {
                System.out.println("Returning cached emojis.");
                return CompletableFuture.completedFuture(cachedEmojis);
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, List<Emoji>> categorizedEmojis = new HashMap<>();
                categorizedEmojis.put("positive", new ArrayList<>());
                categorizedEmojis.put("negative", new ArrayList<>());
                categorizedEmojis.put("neutral", new ArrayList<>());

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(EMOJI_API_URL))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("API Response Status: " + response.statusCode());
                if (response.statusCode() != 200) {
                    System.err.println("Emoji API request failed with status: " + response.statusCode() + ". Falling back to defaults.");
                    return getDefaultEmojis();
                }

                Gson gson = new Gson();
                JsonArray emojiArray = gson.fromJson(response.body(), JsonArray.class);
                if (emojiArray == null || emojiArray.isEmpty()) {
                    System.err.println("API returned empty or invalid data. Falling back to defaults.");
                    return getDefaultEmojis();
                }

                List<String> positiveGroups = Arrays.asList("smileys-emotion", "people-body", "activities", "food-drink");
                List<String> neutralGroups = Arrays.asList("travel-places", "objects", "animals-nature");
                List<String> negativeGroups = Arrays.asList("smileys-emotion", "people-body");

                for (int i = 0; i < Math.min(emojiArray.size(), 100); i++) {
                    JsonObject emojiJson = emojiArray.get(i).getAsJsonObject();
                    String group = emojiJson.get("group").getAsString();

                    if (!RELEVANT_GROUPS.contains(group)) continue;

                    String unicode = emojiJson.get("character").getAsString();
                    String unicodeHex = convertUnicodeToHex(unicode);
                    String imageUrl = TWEMOJI_BASE_URL + unicodeHex + ".png";
                    String slug = emojiJson.get("slug").getAsString().toLowerCase();

                    if (positiveGroups.contains(group) || slug.contains("smile") || slug.contains("happy") || slug.contains("love") ||
                            slug.contains("laugh") || slug.contains("party") || slug.contains("cool")) {
                        categorizedEmojis.get("positive").add(new Emoji(unicode, "positive", imageUrl));
                    } else if (negativeGroups.contains(group) && (slug.contains("sad") || slug.contains("cry") || slug.contains("angry") ||
                            slug.contains("mad") || slug.contains("frown") || slug.contains("disappoint") || slug.contains("frustrated") ||
                            slug.contains("broken") || slug.contains("rage") || slug.contains("pout") || slug.contains("pensive") ||
                            slug.contains("weary") || slug.contains("symbols-on-mouth") || slug.contains("tired") || slug.contains("fearful") ||
                            slug.contains("scream") || slug.contains("confounded") || slug.contains("grimace") || slug.contains("no") ||
                            slug.contains("stop") || slug.contains("loudly-crying") || slug.contains("pouting") || slug.contains("red") ||
                            slug.contains("angrily") || slug.contains("crying-face") || slug.contains("angry-face") || slug.contains("disappointed-face") ||
                            slug.contains("worried-face") || slug.contains("face-with-steam"))) {
                        categorizedEmojis.get("negative").add(new Emoji(unicode, "negative", imageUrl));
                    } else if (neutralGroups.contains(group) || slug.contains("think") || slug.contains("shrug") || slug.contains("face") ||
                            slug.contains("neutral") || slug.contains("roll")) {
                        categorizedEmojis.get("neutral").add(new Emoji(unicode, "neutral", imageUrl));
                    }
                }


                trimToMax(categorizedEmojis, 20);
                ensureMinimumEmojis(categorizedEmojis);
                synchronized (lock) { cachedEmojis = categorizedEmojis; }
                System.out.println("Fetched emojis - Positive: " + categorizedEmojis.get("positive").size() +
                        ", Negative: " + categorizedEmojis.get("negative").size() +
                        ", Neutral: " + categorizedEmojis.get("neutral").size());
                return categorizedEmojis;

            } catch (Exception e) {
                System.err.println("Failed to fetch emojis from API: " + e.getMessage());
                return getDefaultEmojis();
            }
        }, executorService);
    }

    private static void trimToMax(Map<String, List<Emoji>> categorizedEmojis, int maxSize) {
        categorizedEmojis.forEach((k, v) -> {
            if (v.size() > maxSize) v.subList(maxSize, v.size()).clear();
        });
    }

    private static void ensureMinimumEmojis(Map<String, List<Emoji>> categorizedEmojis) {
        Map<String, List<Emoji>> defaults = getDefaultEmojis();
        if (categorizedEmojis.get("positive").isEmpty()) {
            categorizedEmojis.put("positive", defaults.get("positive"));
        }
        if (categorizedEmojis.get("negative").isEmpty()) {
            categorizedEmojis.put("negative", defaults.get("negative"));
        }
        if (categorizedEmojis.get("neutral").isEmpty()) {
            categorizedEmojis.put("neutral", defaults.get("neutral"));
        }
    }

    private static String convertUnicodeToHex(String unicode) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < unicode.length(); i++) {
            int codePoint = unicode.codePointAt(i);
            if (Character.isSupplementaryCodePoint(codePoint)) i++;
            hex.append(String.format("%x", codePoint));
            if (i < unicode.length() - 1) hex.append("-");
        }
        return hex.toString().toLowerCase();
    }

    private static Map<String, List<Emoji>> getDefaultEmojis() {
        Map<String, List<Emoji>> categorizedEmojis = new HashMap<>();
        categorizedEmojis.put("positive", Arrays.asList(
                new Emoji("👍", "positive", TWEMOJI_BASE_URL + "1f44d.png"),
                new Emoji("😊", "positive", TWEMOJI_BASE_URL + "1f60a.png"),
                new Emoji("😂", "positive", TWEMOJI_BASE_URL + "1f602.png"),
                new Emoji("❤️", "positive", TWEMOJI_BASE_URL + "2764.png"),
                new Emoji("🎉", "positive", TWEMOJI_BASE_URL + "1f389.png"),
                new Emoji("😍", "positive", TWEMOJI_BASE_URL + "1f60d.png"),
                new Emoji("👏", "positive", TWEMOJI_BASE_URL + "1f44f.png"),
                new Emoji("🌟", "positive", TWEMOJI_BASE_URL + "1f31f.png"),
                new Emoji("😎", "positive", TWEMOJI_BASE_URL + "1f60e.png"),
                new Emoji("💪", "positive", TWEMOJI_BASE_URL + "1f4aa.png")
        ));
        categorizedEmojis.put("negative", Arrays.asList(
                new Emoji("👎", "negative", TWEMOJI_BASE_URL + "1f44e.png"),
                new Emoji("😢", "negative", TWEMOJI_BASE_URL + "1f622.png"),
                new Emoji("😡", "negative", TWEMOJI_BASE_URL + "1f621.png"),
                new Emoji("💔", "negative", TWEMOJI_BASE_URL + "1f494.png"),
                new Emoji("😤", "negative", TWEMOJI_BASE_URL + "1f624.png"),
                new Emoji("😞", "negative", TWEMOJI_BASE_URL + "1f61e.png"),
                new Emoji("🤬", "negative", TWEMOJI_BASE_URL + "1f92c.png"),
                new Emoji("😣", "negative", TWEMOJI_BASE_URL + "1f623.png"),
                new Emoji("💢", "negative", TWEMOJI_BASE_URL + "1f4a2.png"),
                new Emoji("😠", "negative", TWEMOJI_BASE_URL + "1f620.png")
        ));
        categorizedEmojis.put("neutral", Arrays.asList(
                new Emoji("🤔", "neutral", TWEMOJI_BASE_URL + "1f914.png"),
                new Emoji("😐", "neutral", TWEMOJI_BASE_URL + "1f610.png"),
                new Emoji("🙂", "neutral", TWEMOJI_BASE_URL + "1f642.png"),
                new Emoji("👀", "neutral", TWEMOJI_BASE_URL + "1f440.png"),
                new Emoji("🤷", "neutral", TWEMOJI_BASE_URL + "1f937.png"),
                new Emoji("😶", "neutral", TWEMOJI_BASE_URL + "1f636.png"),
                new Emoji("🤝", "neutral", TWEMOJI_BASE_URL + "1f91d.png"),
                new Emoji("🙄", "neutral", TWEMOJI_BASE_URL + "1f644.png"),
                new Emoji("😴", "neutral", TWEMOJI_BASE_URL + "1f634.png"),
                new Emoji("🤓", "neutral", TWEMOJI_BASE_URL + "1f913.png")
        ));
        synchronized (lock) { cachedEmojis = categorizedEmojis; }
        System.out.println("Using default emojis. Positive: 10, Negative: 10, Neutral: 10");
        return categorizedEmojis;
    }

    public static void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    public static void clearCache() {
        synchronized (lock) { cachedEmojis = null; }
        imageCache.clear();
        System.out.println("Emoji cache cleared.");
    }

    public static CompletableFuture<Image> loadImageAsync(String url) {
        return imageCache.computeIfAbsent(url, k -> CompletableFuture.supplyAsync(() -> {
            try {
                Image image = new Image(url, 24, 24, true, true);
                if (image.isError()) {
                    System.err.println("Failed to load image from " + url + ": " + image.getException().getMessage());
                    return loadPlaceholderImage();
                }
                System.out.println("Successfully loaded image from " + url);
                return image;
            } catch (Exception e) {
                System.err.println("Exception while loading image from " + url + ": " + e.getMessage());
                return loadPlaceholderImage();
            }
        }, executorService));
    }

    private static Image loadPlaceholderImage() {
        try {
            Image placeholder = new Image(EmojiService.class.getResourceAsStream("/forumUI/icons/emoji_placeholder.png"), 24, 24, true, true);
            if (placeholder.isError()) {
                System.err.println("Failed to load placeholder image: " + placeholder.getException().getMessage());
            }
            return placeholder;
        } catch (Exception e) {
            System.err.println("Exception while loading placeholder image: " + e.getMessage());
            return new Image("https://via.placeholder.com/24", 24, 24, true, true); // Fallback to an online placeholder
        }
    }
}