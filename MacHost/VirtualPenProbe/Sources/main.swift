import CoreHID
import Foundation
import VirtualPenSupport

if CommandLine.arguments.contains("--corehid") {
    if #available(macOS 15, *) {
        let properties = HIDVirtualDevice.Properties(
            descriptor: VirtualPenDevice.reportDescriptor,
            vendorID: 0x1209,
            productID: 0x0715,
            product: "SideScreen Virtual Pen",
            manufacturer: "SideScreen",
            versionNumber: 1,
            serialNumber: "SIDESCREEN-PEN-1"
        )
        guard HIDVirtualDevice(properties: properties) != nil else {
            fputs("ERROR: CoreHID denied creation of the virtual pen device.\n", stderr)
            exit(1)
        }
        print("READY: CoreHID created the SideScreen Virtual Pen.")
        exit(0)
    } else {
        fputs("ERROR: CoreHID virtual devices require macOS 15 or newer.\n", stderr)
        exit(1)
    }
}

do {
    let device = try VirtualPenDevice()
    try device.send(VirtualPenReport.outOfRange(x: 0.5, y: 0.5))
    print("READY: SideScreen Virtual Pen was created and accepted an input report.")
} catch {
    fputs("ERROR: \(error.localizedDescription)\n", stderr)
    exit(1)
}
