package dev.viandox.ffm;

public class Config {
    private Config() {}
    /**
     * describes if textshadow should render
     * true -> always
     * false -> never
     * null -> vanila behaviour
     */
    public static Boolean TextShadow = null;
    /** how big should the lerp step be, or how fast it should interpolate */
    public static float ToolTipLerpStepSize = 10;
    /** the max distance at which the tooltip will interpolate, if further, the tooltip is just teleported */
    public static float ToolTipMaxLerpDistance = 500;
    /** the size of the tooltip margin, too big value might cause visual glitches */
    public static float ToolTipMarginSize = 8;
    /** the opacity of the tooltip body */
    public static float ToolTipBodyOpactity = 0.9f;
    /** the default color (when original text is full white) to set the background to */
    public static int ToolTipDefaultColor = 0xff888888;
    /** the max brightness (0 - 100) for the background, set lower if the tooltip is hard to read on certain colors  */
    public static float ToolTipColorMaxBrightness = 48;
    public static float[] ColorTristimulus = ColorConverter.CIE10_D65;
    /** revert to the (almost) original tooltip when single lined */
    public static boolean invertedSingleLineToolTip = false;
}
