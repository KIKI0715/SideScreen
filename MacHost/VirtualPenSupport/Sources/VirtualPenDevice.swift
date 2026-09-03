import Foundation
import IOKit.hid

public enum VirtualPenDeviceError: Error, LocalizedError {
    case creationDenied
    case reportRejected(IOReturn)

    public var errorDescription: String? {
        switch self {
        case .creationDenied:
            return "macOS denied creation of the virtual pen device. The signed executable needs the com.apple.developer.hid.virtual.device entitlement."
        case .reportRejected(let code):
            return "macOS rejected a virtual pen input report (IOReturn \(code))."
        }
    }
}

public final class VirtualPenDevice {
    public static let reportDescriptor = Data([
        0x05, 0x0D,       // Usage Page (Digitizers)
        0x09, 0x01,       // Usage (Digitizer)
        0xA1, 0x01,       // Collection (Application)
        0x85, 0x01,       // Report ID (1)
        0x09, 0x20,       // Usage (Stylus)
        0xA1, 0x00,       // Collection (Physical)
        0x15, 0x00,       // Logical Minimum (0)
        0x25, 0x01,       // Logical Maximum (1)
        0x75, 0x01,       // Report Size (1)
        0x09, 0x42,       // Usage (Tip Switch)
        0x09, 0x32,       // Usage (In Range)
        0x95, 0x02,       // Report Count (2)
        0x81, 0x02,       // Input (Data, Variable, Absolute)
        0x75, 0x06,       // Report Size (6)
        0x95, 0x01,       // Report Count (1)
        0x81, 0x03,       // Input (Constant)
        0x05, 0x01,       // Usage Page (Generic Desktop)
        0x09, 0x30,       // Usage (X)
        0x09, 0x31,       // Usage (Y)
        0x15, 0x00,       // Logical Minimum (0)
        0x26, 0xFF, 0xFF, // Logical Maximum (65535)
        0x75, 0x10,       // Report Size (16)
        0x95, 0x02,       // Report Count (2)
        0x81, 0x02,       // Input (Data, Variable, Absolute)
        0x05, 0x0D,       // Usage Page (Digitizers)
        0x09, 0x30,       // Usage (Tip Pressure)
        0x15, 0x00,       // Logical Minimum (0)
        0x26, 0xFF, 0x03, // Logical Maximum (1023)
        0x75, 0x10,       // Report Size (16)
        0x95, 0x01,       // Report Count (1)
        0x81, 0x02,       // Input (Data, Variable, Absolute)
        0xC0,             // End Collection
        0xC0              // End Collection
    ])

    private let device: IOHIDUserDevice

    public init() throws {
        let properties: [String: Any] = [
            kIOHIDReportDescriptorKey: Self.reportDescriptor,
            kIOHIDTransportKey: "Virtual",
            kIOHIDVendorIDKey: 0x1209,
            kIOHIDProductIDKey: 0x0715,
            kIOHIDVersionNumberKey: 1,
            kIOHIDManufacturerKey: "SideScreen",
            kIOHIDProductKey: "SideScreen Virtual Pen",
            kIOHIDSerialNumberKey: "SIDESCREEN-PEN-1",
            kIOHIDPrimaryUsagePageKey: 0x0D,
            kIOHIDPrimaryUsageKey: 0x01,
            kIOHIDBuiltInKey: false
        ]

        guard let device = IOHIDUserDeviceCreateWithProperties(
            kCFAllocatorDefault,
            properties as CFDictionary,
            0
        ) else {
            throw VirtualPenDeviceError.creationDenied
        }
        self.device = device
    }

    public func send(_ report: [UInt8]) throws {
        let result = report.withUnsafeBytes { bytes in
            IOHIDUserDeviceHandleReportWithTimeStamp(
                device,
                mach_absolute_time(),
                bytes.bindMemory(to: UInt8.self).baseAddress!,
                report.count
            )
        }
        guard result == kIOReturnSuccess else {
            throw VirtualPenDeviceError.reportRejected(result)
        }
    }
}
