package tn.esprit.utils;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ProfanityChecker {
    private static final String SIGHTENGINE_API_URL = "https://api.sightengine.com/1.0/text/check.json";
    private static final String API_USER = "1787381229";
    private static final String API_SECRET = "SBjmvEWgFEJBXSDXAi3L4Yct3KwRMYtw";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Logger logger = Logger.getLogger(ProfanityChecker.class.getName());
    private static final List<String> LANGUAGES = Arrays.asList("en", "fr");
    private static final List<String> ARABIC_PROFANITIES = Arrays.asList("كلب", "حمار", "حرام");
    public static boolean containsProfanity(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            logger.warning("Text is null or empty, skipping profanity check.");
            return false;
        }

        for (String profanity : ARABIC_PROFANITIES) {
            if (text.contains(profanity)) {
                logger.info("Profanity detected in local Arabic check for text: \"" + text + "\"");
                return true;
            }
        }

        for (String lang : LANGUAGES) {
            if (checkProfanityInLanguage(text, lang)) {
                logger.info("Profanity detected in " + lang + " for text: \"" + text + "\"");
                return true;
            }
        }
        logger.info("No profanity detected in supported languages for text: \"" + text + "\"");
        return false;
    }

    private static boolean checkProfanityInLanguage(String text, String lang) throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("text", text)
                .add("lang", lang)
                .add("mode", "standard")
                .add("api_user", API_USER)
                .add("api_secret", API_SECRET)
                .build();

        Request request = new Request.Builder()
                .url(SIGHTENGINE_API_URL)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                logger.fine("API Response for " + lang + ": " + jsonResponse);
                JSONObject json = new JSONObject(jsonResponse);

                int profanityMatches = json.getJSONObject("profanity").getJSONArray("matches").length();
                if (profanityMatches > 0) {
                    logger.info("Profanity found in " + lang + ": " + profanityMatches + " matches for text: \"" + text + "\"");
                    return true;
                }
                return false;
            } else {
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                logger.warning("SightEngine API returned unsuccessful response for " + lang + ": " + response.code() + " - " + response.message());
                logger.warning("Response body: " + responseBody);
                throw new IOException("Failed to get response from SightEngine API for " + lang + ": HTTP " + response.code());
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error contacting SightEngine API for " + lang + " with text: \"" + text + "\"", e);
            return false;
        }
    }
}