import XCTest
@testable import SideScreen

final class PenProtocolTests: XCTestCase {
    func testDecodesPressureStrokePacket() throws {
        var packet = Data([15, 1, 0])
        for value: Float in [0.25, 0.75, 0.5] {
            var littleEndian = value.bitPattern.littleEndian
            withUnsafeBytes(of: &littleEndian) { packet.append(contentsOf: $0) }
        }

        let event = try XCTUnwrap(PenProtocol.decode(packet))

        XCTAssertEqual(event.action, .move)
        XCTAssertEqual(event.x, 0.25)
        XCTAssertEqual(event.y, 0.75)
        XCTAssertEqual(event.pressure, 0.5)
    }

    func testRejectsNonFiniteCoordinates() {
        var packet = Data([15, 0, 0])
        for value: Float in [.nan, 0.5, 0.5] {
            var littleEndian = value.bitPattern.littleEndian
            withUnsafeBytes(of: &littleEndian) { packet.append(contentsOf: $0) }
        }

        XCTAssertNil(PenProtocol.decode(packet))
    }

    func testRejectsMalformedPacket() {
        XCTAssertNil(PenProtocol.decode(Data([15, 0, 0])))

        var packet = Data(repeating: 0, count: PenProtocol.packetSize)
        packet[0] = PenProtocol.eventMessage
        packet[1] = 99
        XCTAssertNil(PenProtocol.decode(packet))
    }
}
