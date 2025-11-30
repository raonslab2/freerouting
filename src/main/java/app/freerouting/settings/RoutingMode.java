package app.freerouting.settings;

import com.google.gson.annotations.SerializedName;

/**
 * High-level autorouter presets to balance speed vs. quality.
 */
public enum RoutingMode
{
  @SerializedName("fast")
  FAST,
  @SerializedName("balanced")
  BALANCED,
  @SerializedName("quality")
  QUALITY;

  public static RoutingMode fromString(String value)
  {
    if (value == null)
    {
      return null;
    }
    String normalized = value
        .trim()
        .toLowerCase();
    switch (normalized)
    {
      case "fast":
        return FAST;
      case "quality":
      case "slow":
        return QUALITY;
      case "balanced":
      default:
        return BALANCED;
    }
  }
}
