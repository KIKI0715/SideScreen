// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "SideScreen",
    platforms: [
        // Floor is ScreenCaptureKit basics (12.3) + OSAllocatedUnfairLock /
        // SCStreamConfiguration.capturesAudio (13.0). CGVirtualDisplay is a
        // private API present well before 13 — it does NOT require 14.
        .macOS(.v13)
    ],
    products: [
        .executable(
            name: "SideScreen",
            targets: ["SideScreen"]),
        .executable(
            name: "SideScreenVirtualPenProbe",
            targets: ["SideScreenVirtualPenProbe"])
    ],
    targets: [
        .target(
            name: "VirtualPenSupport",
            dependencies: [],
            path: "VirtualPenSupport/Sources",
            linkerSettings: [
                .linkedFramework("IOKit")
            ]),
        .executableTarget(
            name: "SideScreen",
            dependencies: [],
            path: "Sources",
            cSettings: [
                .unsafeFlags(["-I", "Sources"])
            ],
            swiftSettings: [
                .unsafeFlags(["-Xcc", "-fmodule-map-file=Sources/module.modulemap"])
            ]),
        .testTarget(
            name: "SideScreenTests",
            dependencies: ["SideScreen", "VirtualPenSupport"],
            path: "Tests/SideScreenTests",
            cSettings: [
                .unsafeFlags(["-I", "Sources"])
            ],
            swiftSettings: [
                .unsafeFlags(["-Xcc", "-fmodule-map-file=Sources/module.modulemap"])
            ]
        ),
        .executableTarget(
            name: "SideScreenVirtualPenProbe",
            dependencies: ["VirtualPenSupport"],
            path: "VirtualPenProbe/Sources",
            linkerSettings: [
                .linkedFramework("CoreHID")
            ]),
        .testTarget(
            name: "VirtualPenSupportTests",
            dependencies: ["VirtualPenSupport"],
            path: "VirtualPenSupport/Tests")
    ]
)
