import SwiftUI

struct WarehouseView: View {
    @ObservedObject var viewModel: GarageViewModel
    let onAddItem: () -> Void
    let onEditItem: (GarageItem) -> Void
    let onOpenPhotoHint: (GarageItem) -> Void
    let onOpenScanner: () -> Void

    @State private var searchQuery: String = ""
    @State private var selectedZone: String = "Все"

    var allZones: [String] {
        let base = ["Все", "Гараж", "Склад", "Балкон", "Подвал", "Бокс"]
        let custom = viewModel.zones.map(\.name)
        return Array(NSOrderedSet(array: base + custom)).compactMap { $0 as? String }
    }

    var filteredItems: [GarageItem] {
        viewModel.items.filter { item in
            let matchesZone = selectedZone == "Все" || item.zone.caseInsensitiveCompare(selectedZone) == .orderedSame
            let q = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            let matchesSearch = q.isEmpty ||
                item.title.lowercased().contains(q) ||
                item.qrCode.lowercased().contains(q) ||
                item.barcode.lowercased().contains(q) ||
                item.shelfLocation.lowercased().contains(q)
            return matchesZone && matchesSearch
        }
    }

    var body: some View {
        VStack(spacing: 12) {
            HStack(spacing: 10) {
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.secondary)
                    TextField("Поиск по названию, полке, QR или штрихкоду...", text: $searchQuery)
                        .font(.subheadline)
                }
                .padding(10)
                .background(Color(red: 0.11, green: 0.13, blue: 0.18))
                .clipShape(RoundedRectangle(cornerRadius: 12))

                Button(action: onOpenScanner) {
                    Image(systemName: "qrcode.viewfinder")
                        .font(.title3)
                        .padding(10)
                        .background(Color.orange)
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding(.horizontal, 16)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(allZones, id: \.self) { zone in
                        Button {
                            selectedZone = zone
                        } label: {
                            Text(zone)
                                .font(.caption.bold())
                                .padding(.horizontal, 14)
                                .padding(.vertical, 8)
                                .background(selectedZone == zone ? Color.orange : Color(red: 0.11, green: 0.13, blue: 0.18))
                                .foregroundColor(.white)
                                .clipShape(Capsule())
                        }
                    }
                }
                .padding(.horizontal, 16)
            }

            List {
                ForEach(filteredItems) { item in
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(item.title)
                                .font(.headline)
                            Spacer()
                            Text(formatRubles(item.price))
                                .font(.subheadline.bold())
                                .foregroundColor(.orange)
                        }

                        Text("📍 \(item.zone) • \(item.shelfLocation) • \(item.quantity) \(item.unitLabel)")
                            .font(.caption)
                            .foregroundColor(.secondary)

                        HStack(spacing: 8) {
                            Text("QR: \(item.qrCode)")
                                .font(.caption2.monospaced())
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.blue.opacity(0.2))
                                .clipShape(Capsule())

                            if !item.barcode.isEmpty {
                                Text("ШК: \(item.barcode)")
                                    .font(.caption2.monospaced())
                                    .foregroundColor(.secondary)
                            }

                            Spacer()

                            Button {
                                viewModel.toggleInventoryVerification(item)
                            } label: {
                                Label(
                                    item.isVerifiedInInventory ? "Проверено" : "Сверить",
                                    systemImage: item.isVerifiedInInventory ? "checkmark.seal.fill" : "checkmark.seal"
                                )
                                .font(.caption2.bold())
                                .foregroundColor(item.isVerifiedInInventory ? .green : .secondary)
                            }
                            .buttonStyle(.plain)

                            Button {
                                onOpenPhotoHint(item)
                            } label: {
                                Label("Фото полки", systemImage: "camera.fill")
                                    .font(.caption2.bold())
                                    .foregroundColor(.orange)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.vertical, 4)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        onEditItem(item)
                    }
                }
            }
            .listStyle(.plain)
        }
        .navigationTitle("Склад и стеллажи")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: onAddItem) {
                    Label("Добавить", systemImage: "plus")
                }
            }
        }
    }
}
