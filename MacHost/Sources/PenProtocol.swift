import Foundation

enum PenAction: UInt8, Equatable {
    case down = 0
    case move = 1
    case up = 2
}

struct PenEvent: Equatable {
    let action: PenAction
    let x: Float
    let y: Float
    let pressure: Float
}

enum PenProtocol {
    static let capabilityMessage: UInt8 = 12
    static let eventMessage: UInt8 = 15
    static let packetSize = 15

    static func decode(_ data: Data) -> PenEvent? {
        guard data.count == packetSize,
              data[data.startIndex] == eventMessage,
              let action = PenAction(rawValue: data[data.startIndex + 1]) else {
            return nil
        }

        let x = readLittleEndianFloat(data, offset: 3)
        let y = readLittleEndianFloat(data, offset: 7)
        let pressure = readLittleEndianFloat(data, offset: 11)
        guard x.isFinite, y.isFinite, pressure.isFinite else { return nil }

        return PenEvent(
            action: action,
            x: min(1, max(0, x)),
            y: min(1, max(0, y)),
            pressure: min(1, max(0, pressure))
        )
    }

    private static func readLittleEndianFloat(_ data: Data, offset: Int) -> Float {
        let bits = data.withUnsafeBytes {
            $0.loadUnaligned(fromByteOffset: offset, as: UInt32.self)
        }
        return Float(bitPattern: UInt32(littleEndian: bits))
    }
}
