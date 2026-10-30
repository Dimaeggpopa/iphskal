import SwiftUI

struct HomeView: View {
    @ObservedObject var viewModel: GarageViewModel
    let onAddItem: () -> Void
    let onEditItem: (GarageItem) -> Void
    let onOpenPhotoHint: (GarageItem) -> Void
    let onConfigurePlatform: (PlatformConnection) -> Void
    let onOpenScanner: () -> Void
    let onOpenDialogs: () -> Void

    var totalWorth: Int64 {
        viewModel.items.reduce(0) { $0 + ($1.price * $1.quantity) }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                // Hero 1-Click Publish Card
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("МОЙ ГАРАЖ • iOS")
                                .font(.caption.bold())
                                .foregroundColor(.orange)
                            Text("Товаров: \(viewModel.items.count) • \(formatRubles(totalWorth))")
                                .font(.title3.bold())
                                .foregroundColor(.white)
                        }
                        Spacer()
                        Button(action: onOpenScanner) {
                            HStack(spacing: 6) {
                                Image(systemName: "qrcode.viewfinder")
                                Text("Сканер")
                                    .font(.caption.bold())
                            }
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .background(Color.white.opacity(0.14))
                            .foregroundColor(.white)
                            .clipShape(Capsule())
                        }
                    }

                    if viewModel.publishProgress.isPublishing {
                        VStack(alignment: .leading, spacing: 6) {
                            Text(viewModel.publishProgress.currentStepText)
                                .font(.caption)
                                .foregroundColor(.orange)
                            ProgressView(value: viewModel.publishProgress.progressFraction)
                                .tint(.orange)
                        }
                    }

                    Button {
                        viewModel.publishAllInOneClick(onlySelected: false)
                    } label: {
                        HStack {
                            Image(systemName: "bolt.fill")
                            Text("Опубликовать всё в 1 клик (4 площадки)")
                                .fontWeight(.bold)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(
                            LinearGradient(
                                colors: [Color.orange, Color.blue],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                    }
                    .disabled(viewModel.publishProgress.isPublishing)
                }
                .padding(16)
                .background(Color(red: 0.11, green: 0.13, blue: 0.18))
                .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))

                // Platforms Grid
                VStack(alignment: .leading, spacing: 10) {
                    Text("Площадки выгрузки (нажмите для настройки API)")
                        .font(.subheadline.bold())
                        .foregroundColor(.secondary)

                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                        ForEach(viewModel.platforms) { conn in
                            let pType = PlatformType.fromKey(conn.platformKey)
                            Button {
                                onConfigurePlatform(conn)
                            } label: {
                                VStack(alignment: .leading, spacing: 6) {
                                    HStack {
                                        Circle()
                                            .fill(pType.brandColor)
                                            .frame(width: 10, height: 10)
                                        Text(conn.platformName)
                                            .font(.subheadline.bold())
                                            .foregroundColor(.white)
                                        Spacer()
                                        Image(systemName: conn.isConnected ? "checkmark.circle.fill" : "link.badge.plus")
                                            .foregroundColor(conn.isConnected ? .green : .orange)
                                    }
                                    Text(conn.lastSyncStatus)
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                        .lineLimit(1)
                                }
                                .padding(12)
                                .background(Color(red: 0.11, green: 0.13, blue: 0.18))
                                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                            }
                        }
                    }
                }

                // Quick Actions
                HStack(spacing: 10) {
                    Button(action: onAddItem) {
                        Label("Новый товар", systemImage: "plus.circle.fill")
                            .font(.subheadline.bold())
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.orange.opacity(0.2))
                            .foregroundColor(.orange)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    Button(action: onOpenDialogs) {
                        Label("Диалоги (\(viewModel.dialogs.count))", systemImage: "message.fill")
                            .font(.subheadline.bold())
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.blue.opacity(0.2))
                            .foregroundColor(.blue)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }

                // Recent Items with Photo Hint button
                VStack(alignment: .leading, spacing: 10) {
                    Text("Склад и места хранения")
                        .font(.headline)

                    if viewModel.items.isEmpty {
                        Text("На складе пока нет товаров. Нажмите «Новый товар», чтобы добавить позицию с QR-кодом и фото полки.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .padding()
                    } else {
                        ForEach(viewModel.items.prefix(8)) { item in
                            HStack(spacing: 12) {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(item.title)
                                        .font(.subheadline.bold())
                                        .foregroundColor(.white)
                                    Text("\(formatRubles(item.price)) • 📍 \(item.zone), \(item.shelfLocation)")
                                        .font(.caption)
                                        .foregroundColor(.orange)
                                    Text("QR: \(item.qrCode) • Выгружено: \(item.publishedPlatforms.count)/4")
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                Button {
                                    onOpenPhotoHint(item)
                                } label: {
                                    Image(systemName: "camera.viewfinder")
                                        .font(.title3)
                                        .foregroundColor(.orange)
                                        .padding(8)
                                        .background(Color.orange.opacity(0.15))
                                        .clipShape(Circle())
                                }
                            }
                            .padding(12)
                            .background(Color(red: 0.11, green: 0.13, blue: 0.18))
                            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                            .onTapGesture {
                                onEditItem(item)
                            }
                        }
                    }
                }
            }
            .padding(16)
        }
        .navigationTitle("Мой гараж")
        .navigationBarTitleDisplayMode(.inline)
    }
}
