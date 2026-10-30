// swift-tools-version: 5.10
import PackageDescription

let package = Package(
    name: "MyGarageIOS",
    defaultLocalization: "ru",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "MyGarageIOS",
            targets: ["MyGarageIOS"]
        )
    ],
    dependencies: [
        .package(url: "https://github.com/firebase/firebase-ios-sdk.git", from: "11.4.0"),
        .package(url: "https://github.com/google/GoogleSignIn-iOS.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "MyGarageIOS",
            dependencies: [
                .product(name: "FirebaseAuth", package: "firebase-ios-sdk"),
                .product(name: "FirebaseFirestore", package: "firebase-ios-sdk"),
                .product(name: "GoogleSignIn", package: "GoogleSignIn-iOS"),
                .product(name: "GoogleSignInSwift", package: "GoogleSignIn-iOS")
            ],
            path: "MyGarageIOS",
            resources: [
                .process("GoogleService-Info.plist")
            ]
        )
    ]
)
