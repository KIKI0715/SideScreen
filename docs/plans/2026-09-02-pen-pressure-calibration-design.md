# Android Pen Pressure Calibration

## Scope

Add tablet-side controls for minimum pressure, maximum pressure, and response curve while preserving the existing 15-byte pen packet and macOS v0.12.0 compatibility. The macOS app is not rebuilt or replaced.

## User interface

The USB Advanced Settings section gains a Pen Pressure card with three Material sliders:

- Minimum pressure: `0.00...0.20`, default `0.02`.
- Maximum pressure: `0.50...1.00`, default `0.95`.
- Response curve: `0.50...2.00`, default `1.00`.

Values update labels and persist immediately. Minimum and maximum controls enforce a safe positive range by constraining invalid stored or incoming values. A Reset button restores the defaults. Curve values below `1.0` make pressure rise sooner; values above `1.0` require firmer pressure.

## Data flow

`PreferencesManager` stores a validated `PenPressureCalibration`. `MainActivity` loads it into the sliders and passes the current calibration with every stylus event. `StreamClient` forwards it to `PenProtocol.encode`. `PenProtocol` maps raw pressure with:

1. `normalized = clamp((raw - min) / (max - min), 0, 1)`
2. `output = normalized ^ gamma`

Only the pressure float changes; capability negotiation, message IDs, coordinates, actions, and packet length remain unchanged.

## Validation and failure handling

Calibration construction clamps all values to supported bounds and guarantees `max > min`. Corrupt or obsolete preferences fall back to validated defaults. All encoded pressure remains finite and within `0...1`.

## Tests

Tests exercise behavior through `PenProtocol.encode`: minimum maps to zero, maximum maps to one, gamma changes the midpoint predictably, output remains monotonic and clamped, and the packet format remains compatible. Preference validation is tested independently without Android UI dependencies. The final gate is the complete Android unit suite, Release APK build, installation on SM-T970, and live capability/stream verification.
