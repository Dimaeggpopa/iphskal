import SwiftUI
import FirebaseCore
import FirebaseAuth
import GoogleSignIn

final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        return true
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }
}

@main
struct MyGarageApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @StateObject private var authObserver = AuthStateObserver()

    var body: some Scene {
        WindowGroup {
            Group {
                if let user = authObserver.currentUser {
                    MainTabView(user: user)
                } else {
                    AuthView()
                }
            }
            .preferredColorScheme(.dark)
        }
    }
}

@MainActor
final class AuthStateObserver: ObservableObject {
    @Published var currentUser: User?
    private var handle: AuthStateDidChangeListenerHandle?

    init() {
        self.currentUser = Auth.auth().currentUser
        self.handle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            Task { @MainActor in
                self?.currentUser = user
            }
        }
    }

    deinit {
        if let handle {
            Auth.auth().removeStateDidChangeListener(handle)
        }
    }
}
