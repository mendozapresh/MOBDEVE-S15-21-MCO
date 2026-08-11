# Implementation Plan - Fix and Standardize Project Colors

The goal is to eliminate hardcoded colors across the project, specifically targeting the Insights screen and profile sections, and ensuring consistency by using the defined color palette in `colors.xml` and the `MoodPalette` utility.

## Proposed Changes

### [Component Name] UI Resources

#### [MODIFY] [colors.xml](file:///C:/Users/Precious/Downloads/MOBDEVE-S15-21-MCO/MOBDEVE-S15-21-MCO/MOBDEVE-S15-21-MCO/app/src/main/res/values/colors.xml)
- Add missing base colors like `white`, `gray_600`, `gray_300`.
- Add brand-specific colors for stability states (Green/Orange) to match the existing `insight_*` palette.

#### [MODIFY] [fragment_insights.xml](file:///C:/Users/Precious/Downloads/MOBDEVE-S15-21-MCO/MOBDEVE-S15-21-MCO/MOBDEVE-S15-21-MCO/app/src/main/res/layout/fragment_insights.xml)
- Replace all hardcoded hex values (e.g., `#F5F5F5`, `#FFFFFF`, `#666666`, `#4CAF50`) with references to `@color/`.

#### [MODIFY] [fragment_profile.xml](file:///C:/Users/Precious/Downloads/MOBDEVE-S15-21-MCO/MOBDEVE-S15-21-MCO/MOBDEVE-S15-21-MCO/app/src/main/res/layout/fragment_profile.xml)
- Standardize colors using the resource palette.

### [Component Name] UI Logic

#### [MODIFY] [InsightsFragment.java](file:///C:/Users/Precious/Downloads/MOBDEVE-S15-21-MCO/MOBDEVE-S15-21-MCO/MOBDEVE-S15-21-MCO/app/src/main/java/com/steadyme/app/ui/InsightsFragment.java)
- Use `ContextCompat.getColor()` for all dynamic color settings.
- Leverage `MoodPalette` for the Pie Chart colors to ensure they match the mood indicators used in the Home and History screens.
- Replace `Color.parseColor()` calls with resource references.

## Verification Plan

### Automated Tests
- I will run `analyze_file` on the modified files to ensure no syntax errors were introduced.

### Manual Verification
- The user can verify that the Insights screen now looks consistent with the rest of the app and respects the brand's green/pale color scheme.
