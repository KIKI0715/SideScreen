import XCTest
@testable import VirtualPenSupport

final class VirtualPenReportTests: XCTestCase {
    func testContactReportEncodesCoordinatesAndPressure() {
        let report = VirtualPenReport.contact(
            x: 0.25,
            y: 0.75,
            pressure: 0.5
        )

        XCTAssertEqual(report, [
            0x01,       // report ID
            0b0000_0011, // tip switch + in range
            0x00, 0x40, // x = 16384
            0xFF, 0xBF, // y = 49151
            0x00, 0x02  // pressure = 512 / 1023
        ])
    }

    func testReleaseAndLeaveClearTipBeforeLeavingRange() {
        let release = VirtualPenReport.hover(x: 0.25, y: 0.75)
        let leave = VirtualPenReport.outOfRange(x: 0.25, y: 0.75)

        XCTAssertEqual(release[1], 0b0000_0010)
        XCTAssertEqual(leave[1], 0)
        XCTAssertEqual(Array(release[6...7]), [0, 0])
        XCTAssertEqual(Array(leave[6...7]), [0, 0])
    }
}
