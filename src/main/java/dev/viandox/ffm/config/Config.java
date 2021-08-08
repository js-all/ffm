package dev.viandox.ffm.config;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import com.google.gson.*;
import dev.viandox.ffm.gui.*;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {
    private static List<String> list(String[] v) {
        ArrayList<String> r = new ArrayList<>();
        Collections.addAll(r, v);
        return r;
    }
    private Config() {}

    public static FFMCyclingOption<String> TextShadow =
            new FFMCyclingOption<String>("text_shadow", Text.of("text shadow"), Config.list(new String[] {"always", "default", "never"}), false);
    /** how big should the lerp step be, or how fast it should interpolate */
    public static FFMSliderOption ToolTipLerpStepSize =
            new FFMSliderOption("tooltip_lerp_step_size", 10, Text.of("tooltip transition speed"), 1, 20);
    /** the max distance at which the tooltip will interpolate, if further, the tooltip is just teleported */
    public static float ToolTipMaxLerpDistance = 500;
    /** the size of the tooltip margin, too big value might cause visual glitches */
    public static float ToolTipMarginSize = 8;
    /** the opacity of the tooltip body */
    public static FFMColorOption ToolTipBodyColor =
            new FFMColorOption("tooltip_body_color", 0xE6000000,Text.of("tooltip background color"), true);
    /** the default color (when original text is full white) to set the background to */
    public static FFMColorOption ToolTipDefaultColor =
            new FFMColorOption("tooltip_default_color", 0xff888888, Text.of("default tooltip header color"), false);
    /** if the tooltip header color is changed to make text more readable */
    public static FFMBooleanOption ColorCorrectToolTip =
            new FFMBooleanOption("tooltip_color_correct", true, Text.of("color correct tooltip"), false);
    /** the max brightness (0 - 100) for the background, set lower if the tooltip is hard to read on certain colors  */
    public static FFMSliderOption ColorCorrectToolTipLightness =
            new FFMSliderOption("tooltip_color_correct_lightness", 60, Text.of("tooltip header lightness"), 0, 100);
    /** revert to the (almost) original tooltip when single lined */
    public static FFMBooleanOption invertedSingleLineToolTip =
            new FFMBooleanOption("tooltip_invert_single_line", false, Text.of("invert tooltip colors when single line"), false);
    /** the minimum delay between two scrapping of the player list, minimum because the scrapping is attempted every update, and dropped if under the delay */
    public static Duration minDelayBetweenPlayerListScrapping = Duration.of(1, ChronoUnit.SECONDS);
    /** is the vanilla armor indicator hidden*/
    public static FFMBooleanOption hideArmorBar =
            new FFMBooleanOption("hide_armor_bar", true, Text.of("hide armor bar"), false);
    public static FFMColorOption accentColor =
            new FFMColorOption("accent_color", 0xfffa3947, Text.of("accent color"), false);
    public static FFMColorOption accentColorBright =
        new FFMColorOption("accent_color_bright", 0xffff707b, Text.of("accent color bright"), false);
    public static Duration transitionDuration = Duration.of(100, ChronoUnit.MILLIS);
    public static Duration playerAbilityDisplayDuration = Duration.of(3, ChronoUnit.SECONDS);
    public static double interpolationPoint1 = 0;
    public static double interpolationPoint2 = 1;
    public static float localMapScale = 0.9f;
    public static float globalMapScale = 1.25f;
    public static Vector3f globalMapTranslate = new Vector3f(10, 10, 0);
    public static Duration mapChangeInterval = Duration.of(250, ChronoUnit.MILLIS);
    public static float mapArrowButtHeight = 3;
    public static float mapArrowHeight = 11;
    public static float mapArrowWidth = 8;
    public static float mapArrowOutlineWidth = 1.5f;
    public static int mapArrowOutlineColor = 0x44000000;
    public static Map<String, String> fastMenuEntries = new HashMap<>();

    public static List<FFMOption<?>> options = null;

    static {
        fastMenuEntries.put("pets", "/pets");
        fastMenuEntries.put("auctions", "/ah");
        fastMenuEntries.put("bazaar", "/bz");
        fastMenuEntries.put("wardrobe", "/wardrobe");
        fastMenuEntries.put("storage", "/storage");
        fastMenuEntries.put("menu", "/sbmenu");
        // get every field, map them to null if they're not FFMOptions and filter nulls out
        options = Arrays.stream(Config.class.getDeclaredFields()).map(field -> {
            try {
                return FFMOption.class.isAssignableFrom(field.getType()) ?
                        (FFMOption<?>) field.get(null) : null; // field.get(null) because they are static
            } catch (IllegalAccessException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        try {
            deserialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void serialize() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject config = new JsonObject();
        for (FFMOption<?> option : options) {
            option.serialize(config);
        }
        FileWriter writer = new FileWriter("config/ffm.json");
        gson.toJson(config, writer);
        writer.flush();
        writer.close();
    }

    public static void deserialize() throws IOException {
        JsonObject config;
        JsonParser jsonParser = new JsonParser();
        FileReader reader = new FileReader("config/ffm.json");
        JsonElement jsonElement = jsonParser.parse(reader);
        reader.close();
        config = jsonElement.getAsJsonObject();

        for (FFMOption<?> option : options) {
            option.deserialize(config);
        }
    }
}
