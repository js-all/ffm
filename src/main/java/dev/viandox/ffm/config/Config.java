package dev.viandox.ffm.config;

import net.minecraft.client.util.math.Vector3f;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;

public class Config {

    private Config() {}
    /**
     * describes if textshadow should render
     * true -> always
     * false -> never
     * null -> vanila behaviour
     */
    public static Boolean TextShadow = false;
    /** how big should the lerp step be, or how fast it should interpolate */
    public static float ToolTipLerpStepSize = 10;
    /** the max distance at which the tooltip will interpolate, if further, the tooltip is just teleported */
    public static float ToolTipMaxLerpDistance = 500;
    /** the size of the tooltip margin, too big value might cause visual glitches */
    public static float ToolTipMarginSize = 8;
    /** the opacity of the tooltip body */
    public static float ToolTipBodyOpacity = 0.9f;
    /** the default color (when original text is full white) to set the background to */
    public static int ToolTipDefaultColor = 0xff888888;
    /** if the tooltip header color is changed to make text more readable */
    public static boolean ColorCorrectToolTip = true;
    /** the max brightness (0 - 100) for the background, set lower if the tooltip is hard to read on certain colors  */
    public static float ColorCorrectToolTipLightness = 60;
    /** revert to the (almost) original tooltip when single lined */
    public static boolean invertedSingleLineToolTip = false;
    /** the minimum delay between two scrapping of the player list, minimum because the scrapping is attempted every update, and dropped if under the delay */
    public static Duration minDelayBetweenPlayerListScrapping = Duration.of(1, ChronoUnit.SECONDS);
    /** is the vanilla armor indicator hidden*/
    public static boolean hideArmorBar = true;
    public static int accentColor = 0xfffa3947;
    public static int accentColorBright = 0xffff707b;
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

    static {
        fastMenuEntries.put("pets", "/pets");
        fastMenuEntries.put("auctions", "/ah");
        fastMenuEntries.put("bazaar", "/bz");
        fastMenuEntries.put("wardrobe", "/wardrobe");
        fastMenuEntries.put("storage", "/storage");
        fastMenuEntries.put("menu", "/sbmenu");
    }
}
