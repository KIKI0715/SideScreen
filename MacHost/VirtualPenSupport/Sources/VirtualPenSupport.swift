public enum VirtualPenReport {
    public static let reportID: UInt8 = 1
    public static let coordinateMaximum: UInt16 = .max
    public static let pressureMaximum: UInt16 = 1023

    public static func contact(x: Double, y: Double, pressure: Double) -> [UInt8] {
        makeReport(x: x, y: y, pressure: pressure, flags: 0b0000_0011)
    }

    public static func hover(x: Double, y: Double) -> [UInt8] {
        makeReport(x: x, y: y, pressure: 0, flags: 0b0000_0010)
    }

    public static func outOfRange(x: Double, y: Double) -> [UInt8] {
        makeReport(x: x, y: y, pressure: 0, flags: 0)
    }

    private static func makeReport(
        x: Double,
        y: Double,
        pressure: Double,
        flags: UInt8
    ) -> [UInt8] {
        let encodedX = encode(x, maximum: coordinateMaximum)
        let encodedY = encode(y, maximum: coordinateMaximum)
        let encodedPressure = encode(pressure, maximum: pressureMaximum)

        return [
            reportID,
            flags,
            UInt8(truncatingIfNeeded: encodedX),
            UInt8(truncatingIfNeeded: encodedX >> 8),
            UInt8(truncatingIfNeeded: encodedY),
            UInt8(truncatingIfNeeded: encodedY >> 8),
            UInt8(truncatingIfNeeded: encodedPressure),
            UInt8(truncatingIfNeeded: encodedPressure >> 8)
        ]
    }

    private static func encode(_ value: Double, maximum: UInt16) -> UInt16 {
        let clamped = min(1, max(0, value))
        return UInt16((clamped * Double(maximum)).rounded())
    }
}
