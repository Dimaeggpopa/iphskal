import SwiftUI
import FirebaseCore
import FirebaseAuth
import GoogleSignIn

struct AuthView: View {
    @State private var isLoading = false
    @State private var errorMessage: String? = nil

    var body: some View {
        ZStack {
            Color(red: 0.05, green: 0.06, blue: 0.09)
                .ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer(minLength: 12)

                VStack(spacing: 14) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 24, style: .continuous)
                            .fill(Color(red: 0.11, green: 0.13, blue: 0.18))
                            .frame(width: 88, height: 88)
                            .overlay(
                                RoundedRectangle(cornerRadius: 24, style: .continuous)
                                    .stroke(Color.orange.opacity(0.6), lineWidth: 1.5)
                            )

                        Image(systemName: "shippingbox.and.arrow.backward.fill")
                            .font(.system(size: 38, weight: .bold))
                            .foregroundStyle(
                                LinearGradient(
                                    colors: [.orange, .blue],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                    }

                    Text("Мой гараж")
                        .font(.system(size: 30, weight: .heavy, design: .rounded))
                        .foregroundColor(.white)

                    Text("Умный склад на iPhone, учёт по QR и штрихкодам, фото-подсказки полок и публикация в 1 клик на все площадки")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 12)

                    HStack(spacing: 18) {
                        ForEach(PlatformType.allCases) { platform in
                            VStack(spacing: 6) {
                                Circle()
                                    .fill(platform.brandColor.opacity(0.2))
                                    .frame(width: 44, height: 44)
                                    .overlay(
                                        Text(String(platform.shortName.prefix(2)).uppercased())
                                            .font(.caption.bold())
                                            .foregroundColor(platform.brandColor)
                                    )
                                Text(platform.shortName)
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                    .padding(.top, 4)
                }

                VStack(spacing: 12) {
                    FeatureRowIOS(
                        icon: "bolt.fill",
                        title: "Публикация в 1 клик",
                        subtitle: "Размещение сразу всех товаров на Авито, Юлу, ВК и Яндекс без сбоев"
                    )
                    FeatureRowIOS(
                        icon: "qrcode.viewfinder",
                        title: "QR-коды, штрихкоды и фото-подсказки",
                        subtitle: "Мгновенный поиск нужной полки и фото места хранения с камеры iPhone"
                    )
                    FeatureRowIOS(
                        icon: "checkmark.seal.fill",
                        title: "Полностью бесплатно • Без подписок",
                        subtitle: "Все функции склада, инвентаризации и единых диалогов открыты сразу"
                    )
                }

                Spacer()

                VStack(spacing: 10) {
                    if let errorMessage {
                        Text(errorMessage)
                            .font(.caption)
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)
                            .padding(10)
                            .frame(maxWidth: .infinity)
                            .background(Color.red.opacity(0.15))
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    Button(action: signInWithGoogle) {
                        HStack(spacing: 10) {
                            if isLoading {
                                ProgressView()
                                    .tint(.white)
                            } else {
                                Image(systemName: "person.badge.shield.checkmark.fill")
                                Text("Войти через Google")
                                    .fontWeight(.bold)
                            }
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 54)
                        .background(
                            LinearGradient(
                                colors: [Color.orange, Color.blue],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                    }
                    .disabled(isLoading)

                    Text("Облачная синхронизация вашего склада между iPhone и другими устройствами")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
            }
            .padding(24)
        }
    }

    private func signInWithGoogle() {
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            errorMessage = "Отсутствует CLIENT_ID в GoogleService-Info.plist"
            return
        }
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController else {
            errorMessage = "Не удалось открыть окно авторизации iOS"
            return
        }

        isLoading = true
        errorMessage = nil

        let config = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.configuration = config

        GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
            if let error {
                isLoading = false
                errorMessage = error.localizedDescription
                return
            }

            guard let user = result?.user,
                  let idToken = user.idToken?.tokenString else {
                isLoading = false
                errorMessage = "Не удалось получить токен Google"
                return
            }

            let credential = GoogleAuthProvider.credential(
                withIDToken: idToken,
                accessToken: user.accessToken.tokenString
            )

            Auth.auth().signIn(with: credential) { _, authError in
                isLoading = false
                if let authError {
                    errorMessage = authError.localizedDescription
                }
            }
        }
    }
}

private struct FeatureRowIOS: View {
    let icon: String
    let title: String
    let subtitle: String

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(Color.orange.opacity(0.18))
                    .frame(width: 40, height: 40)
                Image(systemName: icon)
                    .foregroundColor(.orange)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.subheadline.bold())
                    .foregroundColor(.white)
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Spacer()
        }
        .padding(12)
        .background(Color(red: 0.11, green: 0.13, blue: 0.18))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}
