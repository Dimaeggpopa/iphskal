import SwiftUI

struct PublishView: View {
    @ObservedObject var viewModel: GarageViewModel
    let onAddItem: () -> Void
    let onEditItem: (GarageItem) -> Void
    let onOpenPhotoHint: (GarageItem) -> Void
    let onConfigurePlatform: (PlatformConnection) -> Void

    var selectedItemsCount: Int {
        viewModel.items.filter(\.isSelectedForPublish).count
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Мульти-выгрузка в 1 клик")
                        .font(.title3.bold())
                    Text("Выберите площадки и отметьте товары со склада — приложение автоматически сформирует фиды и опубликует объявления на Авито, Юлу, ВК Маркет и Яндекс.")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    ForEach(viewModel.platforms) { conn in
                        HStack {
                            Button {
                                viewModel.togglePlatformForUpload(conn)
                            } label: {
                                Image(systemName: conn.isSelectedForUpload ? "checkmark.square.fill" : "square")
                                    .foregroundColor(conn.isSelectedForUpload ? .orange : .secondary)
                                    .font(.title3)
                            }

                            VStack(alignment: .leading, spacing: 2) {
                                Text(conn.platformName)
                                    .font(.subheadline.bold())
                                Text(conn.lastSyncStatus)
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                            Spacer()
                            Button("Настроить API") {
                                onConfigurePlatform(conn)
                            }
                            .font(.caption.bold())
                            .foregroundColor(.blue)
                        }
                        .padding(10)
                        .background(Color(red: 0.11, green: 0.13, blue: 0.18))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    Button {
                        viewModel.publishAllInOneClick(onlySelected: true)
                    } label: {
                        HStack {
                            Image(systemName: "paperplane.fill")
                            Text("Выгрузить выбранные (\(selectedItemsCount)) в 1 клик")
                                .fontWeight(.bold)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color.orange)
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                    }
                    .disabled(viewModel.publishProgress.isPublishing)
                }
                .padding(16)
                .background(Color(red: 0.09, green: 0.11, blue: 0.15))
                .clipShape(RoundedRectangle(cornerRadius: 18))

                HStack {
                    Text("Товары к публикации")
                        .font(.headline)
                    Spacer()
                    Button("Добавить товар", action: onAddItem)
                        .font(.caption.bold())
                }

                ForEach(viewModel.items) { item in
                    HStack(spacing: 12) {
                        Button {
                            viewModel.toggleItemSelectionForPublish(item)
                        } label: {
                            Image(systemName: item.isSelectedForPublish ? "checkmark.circle.fill" : "circle")
                                .font(.title3)
                                .foregroundColor(item.isSelectedForPublish ? .orange : .secondary)
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.title)
                                .font(.subheadline.bold())
                            Text("\(formatRubles(item.price)) • 📍 \(item.zone), \(item.shelfLocation)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            if !item.publishedPlatforms.isEmpty {
                                Text("Опубликовано: \(item.publishedPlatforms.joined(separator: ", ").uppercased())")
                                    .font(.caption2.bold())
                                    .foregroundColor(.green)
                            }
                        }
                        Spacer()
                        Button {
                            onOpenPhotoHint(item)
                        } label: {
                            Image(systemName: "camera.viewfinder")
                                .foregroundColor(.orange)
                        }
                    }
                    .padding(12)
                    .background(Color(red: 0.11, green: 0.13, blue: 0.18))
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .onTapGesture {
                        onEditItem(item)
                    }
                }
            }
            .padding(16)
        }
        .navigationTitle("Публикация")
    }
}
