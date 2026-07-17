# MorphingEngine — Dynamic Island-style Animation Engine

## Overview

A singleton `ViewGroup` overlay that any UI element can call to produce iOS Dynamic Island-style fluid morphing animations. The engine handles all spring physics, path morphing, and choreography internally — callers only specify an anchor view and content.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│  WindowManager (above all, touches pass through)        │
│  ┌───────────────────────────────────────────────────┐  │
│  │  MorphingEngine (ViewGroup, fills screen)         │  │
│  │                                                    │  │
│  │  States: IDLE → MORPHING → EXPANDED → MORPHING → IDLE │
│  │                                                    │  │
│  │  ┌────────────────┐  ┌────────────────────────┐  │  │
│  │  │  SpringPhysics  │  │  ShapeMorpher          │  │  │
│  │  │  (damped HO)    │  │  (Path keyframe anim)  │  │  │
│  │  └────────────────┘  └────────────────────────┘  │  │
│  └───────────────────────────────────────────────────┘  │
│                                                          │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Launcher UI (LinearLayout)                       │  │
│  │  [Notification Bar] [Today]                        │  │
│  │  [Recent Apps Grid] [Status Bar] [Dock]           │  │
│  │                                                    │  │
│  │  Any view here can call engine.expand(this, ...)   │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## Files

### 1. `animation/MorphingEngine.kt` (~250 lines)

The core engine — a transparent `ViewGroup` added to the window.

#### Public API

```kotlin
class MorphingEngine(context: Context) : ViewGroup(context) {

    // Entry point. anchorView = the UI element the user tapped.
    // contentView = what to show when expanded (provided by caller).
    // config = animation tuning (spring, duration, shape).
    fun expand(
        anchorView: View,
        contentView: View,
        config: MorphConfig = MorphConfig()
    )

    // Animate back to anchor position and hide.
    fun collapse()

    // True while expanded or mid-animation.
    val isExpanded: Boolean
}
```

#### Internal Flow (expand)

```
1. Capture anchor global rect + screenshot (for seamless look)
2. Elevate engine above launcher window
3. Phase 1 — Spring to anchor size+position (looks like pill grows from anchor)
   - SpringAnimation on translationX, translationY, scaleX, scaleY
4. Phase 2 — Crossfade anchor screenshot → real content
5. Phase 3 — If content has variable size, spring to content height
```

#### Internal Flow (collapse)

```
1. Crossfade content → anchor screenshot
2. Phase 2 — Spring back to anchor's current position/size
3. Remove engine from window
```

### 2. `animation/SpringInterpolator.kt` (~40 lines)

Damped harmonic oscillator — no external dependencies.

```kotlin
class SpringInterpolator(
    stiffness: Float = 300f,   // N/m  (higher = snappier)
    dampingRatio: Float = 0.7f // 0=bouncy, 1=critically damped
) : Interpolator {
    override fun getInterpolation(input: Float): Float
    // Solves mx'' + cx' + kx = 0 using closed-form damped SHO
}
```

Values tuned to match iOS feel: `stiffness=300f`, `dampingRatio=0.65f`.

### 3. `animation/MorphConfig.kt` (~30 lines)

Per-call configuration — callers can override defaults.

```kotlin
data class MorphConfig(
    val stiffness: Float = 300f,
    val dampingRatio: Float = 0.65f,
    val startRadius: Float = 28f,      // pill corner radius when collapsed
    val endRadius: Float = 20f,         // corner radius when expanded
    val maxWidthFraction: Float = 0.5f, // max width as fraction of screen
    val durationMs: Long = 400L
)
```

## Shape Morphing Strategy

Android lacks `CAShapeLayer`, so we use **`ViewOutlineProvider`** to animate the background shape:

```kotlin
// During animation, interpolate between pill and rounded-rect paths
val path = Path()
val progress = currentAnimationProgress  // 0..1 from spring

// At progress=0: pill shape matching anchorView bounds
// At progress=0.3: expanding pill
// At progress=1.0: full expanded rounded rect
path.addRoundRect(interpolatedBounds, interpolatedRadii, ...)
outline.setConvexPath(path)
```

A `ValueAnimator` driven by the spring interpolator updates the path each frame.

## Rendering (Layer Approach)

The engine has three visual layers, drawn in order:

| Layer | Purpose | When visible |
|-------|---------|-------------|
| Backdrop | Dim/glassmorphism scrim | Expanded only |
| Content container | The caller's contentView | Expanded + morphing out |
| Anchor ghost | Screenshot of anchorView | Morphing phases only |

## Edge Cases

| Case | Handling |
|------|----------|
| Anchor disappears (app launched) | Cancel animation, treat as collapse |
| Second expand while animating | Queue or interrupt (configurable) |
| Screen rotation | Remove engine, abort any animation |
| Content taller than screen | Clamp to max height, make scrollable |
| Multiple anchors simultaneously | Not supported — single island at a time |

## Integration Checklist

- [ ] Add `MorphingEngine` as an overlay via `WindowManager` (needs `SYSTEM_ALERT_WINDOW` or launcher carries `android.permission.SYSTEM_ALERT_WINDOW`)
- [ ] OR simpler: add as top child of `activity_main.xml` with `android:importantForAccessibility="no"` and click-through when idle (touch passthrough via `onTouchEvent` returning `false` when not expanded)
- [ ] Inject engine into `MainActivity` — `MorphingEngine(this)` as a field
- [ ] Expose as `val morphingEngine: MorphingEngine` on Activity for any UI element to access

## Caller Examples

```kotlin
// From a dock icon click
dockIcon.setOnClickListener {
    val musicContent = layoutInflater.inflate(R.layout.island_music, null)
    morphingEngine.expand(it, musicContent)
}

// From a notification icon
notifIcon.setOnClickListener {
    val notifContent = createNotificationCard(notification)
    morphingEngine.expand(it, notifContent)
}

// From settings gear
settingsIcon.setOnClickListener {
    val quickSettings = layoutInflater.inflate(R.layout.island_quicksettings, null)
    morphingEngine.expand(it, quickSettings)
}
```

## Future Considerations

- **Split island**: Two engine instances side by side (like two live activities)
- **Persistent island**: Long-running content (music player) stays after collapse as a small pill
- **Gesture dismiss**: Swipe up/down to collapse with follow-on spring physics
