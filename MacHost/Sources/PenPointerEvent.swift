import CoreGraphics

/// Builds the tablet-point mouse events expected by pressure-aware Mac apps.
enum PenPointerEvent {
    /// Stable non-zero identifier for SideScreen's virtual pen.
    static let deviceID: Int64 = 0x5353
    /// Device ID + absolute X/Y + tip button + continuous pressure.
    static let requiredCapabilityMask = 0x0447

    static func makeProximity(
        source: CGEventSource?,
        entering: Bool
    ) -> CGEvent? {
        guard let event = CGEvent(source: source) else { return nil }
        event.type = .tabletProximity
        event.setIntegerValueField(.tabletProximityEventVendorID, value: deviceID)
        event.setIntegerValueField(.tabletProximityEventTabletID, value: 1)
        event.setIntegerValueField(.tabletProximityEventPointerID, value: 1)
        event.setIntegerValueField(.tabletProximityEventDeviceID, value: deviceID)
        event.setIntegerValueField(.tabletProximityEventSystemTabletID, value: deviceID)
        event.setIntegerValueField(.tabletProximityEventVendorPointerType, value: 1)
        event.setIntegerValueField(.tabletProximityEventVendorPointerSerialNumber, value: 1)
        event.setIntegerValueField(.tabletProximityEventVendorUniqueID, value: deviceID)
        event.setIntegerValueField(
            .tabletProximityEventCapabilityMask,
            value: Int64(requiredCapabilityMask)
        )
        event.setIntegerValueField(.tabletProximityEventPointerType, value: 1)
        event.setIntegerValueField(.tabletProximityEventEnterProximity, value: entering ? 1 : 0)
        return event
    }

    static func makeStrokeEvents(
        source: CGEventSource?,
        type: CGEventType,
        point: CGPoint,
        pressure: Float
    ) -> [CGEvent] {
        guard let pointerEvent = make(
            source: source,
            type: type,
            point: point,
            pressure: pressure
        ) else { return [] }

        if type == .leftMouseDown {
            return [makeProximity(source: source, entering: true), pointerEvent].compactMap { $0 }
        }
        if type == .leftMouseUp {
            return [pointerEvent, makeProximity(source: source, entering: false)].compactMap { $0 }
        }
        return [pointerEvent]
    }

    static func make(
        source: CGEventSource?,
        type: CGEventType,
        point: CGPoint,
        pressure: Float
    ) -> CGEvent? {
        let clampedPressure = min(1, max(0, pressure))
        guard let event = CGEvent(
            mouseEventSource: source,
            mouseType: type,
            mouseCursorPosition: point,
            mouseButton: .left
        ) else { return nil }

        event.setIntegerValueField(
            .mouseEventSubtype,
            value: Int64(CGEventMouseSubtype.tabletPoint.rawValue)
        )
        event.setDoubleValueField(.mouseEventPressure, value: Double(clampedPressure))
        event.setIntegerValueField(.tabletEventPointX, value: Int64(point.x.rounded()))
        event.setIntegerValueField(.tabletEventPointY, value: Int64(point.y.rounded()))
        event.setIntegerValueField(
            .tabletEventPointButtons,
            value: type == .leftMouseUp ? 0 : 1
        )
        event.setDoubleValueField(.tabletEventPointPressure, value: Double(clampedPressure))
        event.setIntegerValueField(.tabletEventDeviceID, value: deviceID)
        if type == .leftMouseDown {
            event.setIntegerValueField(.mouseEventClickState, value: 1)
        }
        return event
    }
}
