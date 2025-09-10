package com.portalname;

import net.runelite.client.config.*;

import java.awt.*;


@ConfigGroup("PortalName")
public interface PortalNameConfig extends Config {

    // Enum list for choosing active color style
    public enum ColorStyle {
        SINGLE,
        MULTI
    }

    // Position of the label relative to the portal
    enum TextPosition
    {
        TOP,
        MIDDLE,
        BOTTOM
    }

    @ConfigItem(
            keyName = "colorStyle",
            name = "Color Style",
            description = "How colors should be displayed"
    )
    default ColorStyle colorStyle()
    {
        return ColorStyle.SINGLE;   // Default to single color
    }

    @ConfigItem(
            keyName = "textPosition",
            name = "Text Position",
            description = "Where labels are drawn relative to the portal"
    )
    default TextPosition textPosition()
    {
        return TextPosition.MIDDLE;
    }


    // SINGLE STYLE SECTION
    @ConfigSection(
            name = "Single Style",
            description = "One color for all labels or Multiple = Different colors per portal",
            position = 0
    )
    String singleStyle = "singleStyle";

    @ConfigItem(
            keyName = "singleColor",
            name = "Color",
            description = "Single color for all labels",
            section = singleStyle
    )
    default Color singleColor()
    {
        return Color.GREEN; // default color green
    }


    // MULTI STYLE
    @ConfigSection(
            name = "Multi Style",
            description = "Different colors per portal",
            position = 1
    )
    String multiStyle = "multiStyle";

    // REQUIRED TO DO SPACING IN LABELS
    public enum ColorSelection
    {
        PORTAL_COLORS("Portal Colors"),
        UNIQUE_COLORS("Unique Colors");

        private final String label;

        ColorSelection(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return label;
        }
    }

    @ConfigItem(
            keyName = "colorSelection",
            name = "Color Selection",
            description = "Use portal colors or set unique colors per destination",
            section = "multiStyle"
    )
    default ColorSelection colorSelection() {
        return ColorSelection.PORTAL_COLORS;
    }


    // UNIQUE PORTAL COLOR SECTION
    @ConfigSection(
            name = "Unique Portal Colors",
            description = "Only used when 'Unique Colors' is selected above",
            position = 2
    )
    String uniqueColors = "uniqueColors";

    // UNIQUE PORTAL COLORS
    @ConfigItem(
            keyName = "annakarlColor",
            name = "Annakarl",
            description = "Color for Annakarl Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color  annakarlColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "apeAtollColor",
            name = "Ape Atoll Dungeon",
            description = "Color for Ape Atoll Dungeon Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color apeAtollColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "arceuusLibraryColor",
            name = "Arceuus Library",
            description = "Color for Arceuus Library Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color arceuusLibraryColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "ardougneColor",
            name = "Ardougne",
            description = "Color for Ardougne Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color ardougneColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "barrowsColor",
            name = "Barrows",
            description = "Color for Barrows Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color barrowsColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "battlefrontColor",
            name = "Battlefront",
            description = "Color for Battlefront Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color battlefrontColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "camelotColor",
            name = "Camelot",
            description = "Color for Camelot Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color camelotColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "carrallangerColor",
            name = "Carrallanger",
            description = "Color for Carrallanger Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color carrallangerColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "catherbyColor",
            name = "Catherby",
            description = "Color for Catherby Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color catherbyColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "cemeteryColor",
            name = "Cemetery",
            description = "Color for Cemetery Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color cemeteryColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "civitasColor",
            name = "Civitas illa Fortis",
            description = "Color for Civitas illa Fortis Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color civitasColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "draynorColor",
            name = "Draynor Manor",
            description = "Color for Draynor Manor Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color draynorColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "faladorColor",
            name = "Falador",
            description = "Color for Falador Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color faladorColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "fenkenstrainColor",
            name = "Fenkenstrain's Castle",
            description = "Color for Fenkenstrain's Castle Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color fenkenstrainColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "fishingGuildColor",
            name = "Fishing Guild",
            description = "Color for Fishing Guild Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color fishingGuildColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "ghorrockColor",
            name = "Ghorrock",
            description = "Color for Ghorrock Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color ghorrockColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "grandExchangeColor",
            name = "Grand Exchange",
            description = "Color for Grand Exchange Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color grandExchangeColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "harmonyIslandColor",
            name = "Harmony Island",
            description = "Color for Harmony Island Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color harmonyIslandColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "kharyrllColor",
            name = "Kharyrll",
            description = "Color for Kharyrll Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color kharyrllColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "kourendColor",
            name = "Kourend",
            description = "Color for Kourend Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color kourendColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "lumbridgeColor",
            name = "Lumbridge",
            description = "Color for Lumbridge Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color lumbridgeColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "lunarIsleColor",
            name = "Lunar Isle",
            description = "Color for Lunar Isle Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color lunarIsleColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "marimColor",
            name = "Marim",
            description = "Color for Marim Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color marimColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "mindAltarColor",
            name = "Mind Altar",
            description = "Color for Mind Altar Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color mindAltarColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "salveGraveyardColor",
            name = "Salve Graveyard",
            description = "Color for Salve Graveyard Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color salveGraveyardColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "seersVillageColor",
            name = "Seers' Village",
            description = "Color for Seers' Village Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color seersVillageColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "senntistenColor",
            name = "Senntisten",
            description = "Color for Senntisten Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color senntistenColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "trollStrongholdColor",
            name = "Troll Stronghold",
            description = "Color for Troll Stronghold Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color trollStrongholdColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "varrockColor",
            name = "Varrock",
            description = "Color for Varrock Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color varrockColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "waterbirthColor",
            name = "Waterbirth Island",
            description = "Color for Waterbirth Island Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color waterbirthColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "weissColor",
            name = "Weiss",
            description = "Color for Weiss Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color weissColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "westArdougneColor",
            name = "West Ardougne",
            description = "Color for West Ardougne Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color westArdougneColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "yanilleColor",
            name = "Yanille",
            description = "Color for Yanille Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color yanilleColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "yanilleWatchtowerColor",
            name = "Yanille Watchtower",
            description = "Color for Yanille Watchtower Portal",
            section = "uniqueColors"
    )
    @Alpha
    default Color yanilleWatchtowerColor() { return Color.GREEN; }

    // CUSTOM NAME OVERRIDES SECTION
    @ConfigSection(
            name = "Custom Portal Names",
            description = "Override default portal names with custom names",
            position = 3
    )
    String customNames = "customNames";

    @ConfigItem(
            keyName = "enableCustomNames",
            name = "Enable Custom Names",
            description = "Enable custom portal name overrides",
            section = "customNames"
    )
    default boolean enableCustomNames() { return false; }

    @ConfigItem(
            keyName = "customNamesList",
            name = "Custom Names",
            description = "Custom portal names (format: OriginalName=CustomName, one per line)<br>Example: Kharyrll=Canifis",
            section = "customNames"
    )
    default String customNamesList() { 
        return "Kharyrll=Canifis\nSenntisten=Digsite\nKourend=Great Kourend"; 
    }

}
