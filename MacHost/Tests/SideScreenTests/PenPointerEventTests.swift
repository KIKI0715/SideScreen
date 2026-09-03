import CoreGraphics
import XCTest
@testable import SideScreen

final class PenPointerEventTests: XCTestCase {
    func testProximityEnterCreatesNativePenDeviceEvent() throws {
        let event = try XCTUnwrap(
            PenPointerEvent.makeProximity(source: nil, entering: true)
        )
        let cocoaEvent = try XCTUnwrap(NSEvent(cgEvent: event))

        XCTAssertEqual(event.type, .tabletProximity)
        XCTAssertTrue(cocoaEvent.isEnteringProximity)
        XCTAssertEqual(cocoaEvent.pointingDeviceType, .pen)
        XCTAssertEqual(cocoaEvent.deviceID, Int(PenPointerEvent.deviceID))
    }

    func testProximityAdvertisesPressureCapableTablet() throws {
        let event = try XCTUnwrap(
            PenPointerEvent.makeProximity(source: nil, entering: true)
        )
        let cocoaEvent = try XCTUnwrap(NSEvent(cgEvent: event))

        XCTAssertEqual(
            cocoaEvent.capabilityMask,
            PenPointerEvent.requiredCapabilityMask
        )
    }

    func testStrokeDownEntersProximityBeforeMouseDown() throws {
        let events = PenPointerEvent.makeStrokeEvents(
            source: nil,
            type: .leftMouseDown,
            point: CGPoint(x: 320, y: 240),
            pressure: 0.42
        )

        XCTAssertEqual(events.map(\.type), [.tabletProximity, .leftMouseDown])
        XCTAssertTrue(try XCTUnwrap(NSEvent(cgEvent: events[0])).isEnteringProximity)
    }

    func testStrokeUpLeavesProximityAfterMouseUp() throws {
        let events = PenPointerEvent.makeStrokeEvents(
            source: nil,
            type: .leftMouseUp,
            point: CGPoint(x: 320, y: 240),
            pressure: 0
        )

        XCTAssertEqual(events.map(\.type), [.leftMouseUp, .tabletProximity])
        XCTAssertFalse(try XCTUnwrap(NSEvent(cgEvent: events[1])).isEnteringProximity)
    }

    func testDragIsTabletPointWithPressure() throws {
        let event = try XCTUnwrap(
            PenPointerEvent.make(
                source: nil,
                type: .leftMouseDragged,
                point: CGPoint(x: 320, y: 240),
                pressure: 0.73
            )
        )

        XCTAssertEqual(event.type, .leftMouseDragged)
        XCTAssertEqual(
            event.getIntegerValueField(.mouseEventSubtype),
            Int64(CGEventMouseSubtype.tabletPoint.rawValue)
        )
        XCTAssertEqual(
            event.getDoubleValueField(.tabletEventPointPressure),
            0.73,
            accuracy: 0.001
        )
        XCTAssertEqual(
            event.getIntegerValueField(.tabletEventDeviceID),
            PenPointerEvent.deviceID
        )
    }

    func testDownCarriesPositionPressureAndContact() throws {
        let event = try XCTUnwrap(
            PenPointerEvent.make(
                source: nil,
                type: .leftMouseDown,
                point: CGPoint(x: 320, y: 240),
                pressure: 0.42
            )
        )

        XCTAssertEqual(
            event.getDoubleValueField(.mouseEventPressure),
            0.42,
            accuracy: 0.005
        )
        XCTAssertEqual(event.getIntegerValueField(.tabletEventPointX), 320)
        XCTAssertEqual(event.getIntegerValueField(.tabletEventPointY), 240)
        XCTAssertEqual(event.getIntegerValueField(.tabletEventPointButtons), 1)
        XCTAssertEqual(event.getIntegerValueField(.mouseEventClickState), 1)
    }

    func testUpReleasesTabletPressureAndContact() throws {
        let event = try XCTUnwrap(
            PenPointerEvent.make(
                source: nil,
                type: .leftMouseUp,
                point: CGPoint(x: 320, y: 240),
                pressure: 0
            )
        )

        XCTAssertEqual(
            event.getIntegerValueField(.mouseEventSubtype),
            Int64(CGEventMouseSubtype.tabletPoint.rawValue)
        )
        XCTAssertEqual(event.getDoubleValueField(.mouseEventPressure), 0, accuracy: 0.001)
        XCTAssertEqual(event.getDoubleValueField(.tabletEventPointPressure), 0, accuracy: 0.001)
        XCTAssertEqual(event.getIntegerValueField(.tabletEventPointButtons), 0)
        XCTAssertEqual(event.getIntegerValueField(.tabletEventDeviceID), PenPointerEvent.deviceID)
    }
}
