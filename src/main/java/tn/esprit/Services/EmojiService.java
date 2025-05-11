package tn.esprit.Services;

import java.util.*;

public class EmojiService {

    public static class Emoji {
        private final String unicode;
        private final String sentiment;

        public Emoji(String unicode, String sentiment) {
            this.unicode = unicode;
            this.sentiment = sentiment;
        }

        public String getUnicode() {
            return unicode;
        }

        public String getSentiment() {
            return sentiment;
        }
    }

    public static Map<String, List<Emoji>> fetchEmojis() {
        Map<String, List<Emoji>> categorizedEmojis = new HashMap<>();

        // Positive emojis
        List<Emoji> positive = Arrays.asList(
                new Emoji("👍", "positive"), new Emoji("😊", "positive"), new Emoji("😄", "positive"),
                new Emoji("🎉", "positive"), new Emoji("✨", "positive"), new Emoji("💪", "positive"),
                new Emoji("🌟", "positive"), new Emoji("❤️", "positive"), new Emoji("😍", "positive"),
                new Emoji("👏", "positive"), new Emoji("🎈", "positive"), new Emoji("🥳", "positive"),
                new Emoji("🚀", "positive"), new Emoji("🏆", "positive"), new Emoji("🎁", "positive")
        );

        // Negative emojis
        List<Emoji> negative = Arrays.asList(
                new Emoji("👎", "negative"), new Emoji("😢", "negative"), new Emoji("💔", "negative"),
                new Emoji("😡", "negative"), new Emoji("😞", "negative"), new Emoji("😠", "negative"),
                new Emoji("🤬", "negative"), new Emoji("😭", "negative"), new Emoji("😓", "negative"),
                new Emoji("💩", "negative"), new Emoji("🤮", "negative"), new Emoji("😖", "negative")
        );

        // Neutral emojis
        List<Emoji> neutral = Arrays.asList(
                new Emoji("🤔", "neutral"), new Emoji("😐", "neutral"), new Emoji("😶", "neutral"),
                new Emoji("🤷", "neutral"), new Emoji("🙄", "neutral"), new Emoji("😑", "neutral"),
                new Emoji("🤨", "neutral"), new Emoji("😕", "neutral"), new Emoji("🤩", "neutral"),
                new Emoji("😎", "neutral"), new Emoji("🧐", "neutral"), new Emoji("🤓", "neutral")
        );

        categorizedEmojis.put("positive", positive);
        categorizedEmojis.put("negative", negative);
        categorizedEmojis.put("neutral", neutral);

        return categorizedEmojis;
    }
}