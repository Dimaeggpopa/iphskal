import SwiftUI

struct MoreView: View {
    @ObservedObject var viewModel: GarageViewModel
    let userEmail: String?
    let onOpenPhotoHint: (GarageItem) -> Void
    let onSignOut: () -> Void

    @State private var newZoneName: String = ""
    @State private var newZoneCapacity: String = "50"
    @State private var newQuickReply: String = ""
    @State private var showEmergencyRadar = false

    var totalWorth: Int64 {
        viewModel.items.reduce(0) { $0 + ($1.price * $1.quantity) }
    }

    var exportRegistryText: String {
        var lines: [String] = [
            "=== РЕЕСТР СКЛАДА «МОЙ ГАРАЖ» (iOS / iPhone) ===",
            "Позиций: \(viewModel.items.count) | Сумма: \(formatRubles(totalWorth))",
            "----------------------------------------------"
        ]
        for (idx, item) in viewModel.items.enumerated() {
            lines.append("\(idx + 1). \(item.title) — \(formatRubles(item.price)) (\(item.quantity) \(item.unitLabel))")
            lines.append("   Полка: \(item.zone), \(item.shelfLocation) | QR: \(item.qrCode)")
            if !item.hintNote.isEmpty {
                lines.append("   Подсказка: \(item.hintNote)")
            }
        }
        return lines.joined(separator: "\n")
    }

    var body: some View {
        Form {
            Section {
                HStack(spacing: 12) {
                    Image(systemName: "checkmark.seal.fill")
                        .font(.title2)
                        .foregroundColor(.green)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Мой гараж • iOS Native Edition")
                            .font(.subheadline.bold())
                        Text("Полная бесплатная версия для iPhone без подписок: QR-склад, фото-подсказки полок и выгрузка в 1 клик")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }

            Section("Управление темами (Оформление iOS)") {
                Picker("Режим темы", selection: $viewModel.themeModeRaw) {
                    ForEach(AppThemeMode.allCases) { mode in
                        Text(mode.label).tag(mode.rawValue)
                    }
                }
                .pickerStyle(.segmented)

                Picker("Цветовой акцент", selection: $viewModel.accentPaletteRaw) {
                    ForEach(AppAccentPalette.allCases) { palette in
                        Text(palette.label).tag(palette.rawValue)
                    }
                }
            }

            Section("Экстренный радар полок и фото-подсказок") {
                Toggle("Показать все места хранения", isOn: $showEmergencyRadar)
                if showEmergencyRadar {
                    ForEach(viewModel.items) { item in
                        Button {
                            onOpenPhotoHint(item)
                        } label: {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(item.title)
                                        .font(.subheadline.bold())
                                        .foregroundColor(.primary)
                                    Text("📍 \(item.zone) → \(item.shelfLocation) (\(item.qrCode))")
                                        .font(.caption)
                                        .foregroundColor(.orange)
                                }
                                Spacer()
                                Image(systemName: "camera.viewfinder")
                                    .foregroundColor(.orange)
                            }
                        }
                    }
                }
            }

            Section("Финансовая сводка и экспорт") {
                HStack {
                    Text("Общая стоимость склада")
                    Spacer()
                    Text(formatRubles(totalWorth))
                        .fontWeight(.bold)
                        .foregroundColor(.orange)
                }
                ShareLink(item: exportRegistryText) {
                    Label("Поделиться реестром склада (AirDrop / Файлы iOS)", systemImage: "square.and.arrow.up")
                }
            }

            Section("Конструктор мест и стеллажей") {
                HStack {
                    TextField("Новое место (напр. Бокс 2)", text: $newZoneName)
                    TextField("Мест", text: $newZoneCapacity)
                        .keyboardType(.numberPad)
                        .frame(width: 60)
                    Button("Добавить") {
                        let cap = Int64(newZoneCapacity) ?? 50
                        guard !newZoneName.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                        viewModel.saveStorageZone(name: newZoneName, capacity: cap, description: "Зона iOS")
                        newZoneName = ""
                    }
                }
                ForEach(viewModel.zones) { zone in
                    HStack {
                        Text("\(zone.name) • \(zone.capacity) мест")
                        Spacer()
                        Button(role: .destructive) {
                            viewModel.deleteStorageZone(zoneId: zone.id)
                        } label: {
                            Image(systemName: "trash")
                        }
                    }
                }
            }

            Section("Шаблоны быстрых ответов") {
                HStack {
                    TextField("Новая фраза для покупателей", text: $newQuickReply)
                    Button("Добавить") {
                        viewModel.addQuickReplyTemplate(newQuickReply)
                        newQuickReply = ""
                    }
                }
                ForEach(viewModel.quickReplyTemplates, id: \.self) { tpl in
                    HStack {
                        Text(tpl)
                            .font(.caption)
                        Spacer()
                        Button(role: .destructive) {
                            viewModel.removeQuickReplyTemplate(tpl)
                        } label: {
                            Image(systemName: "trash")
                        }
                    }
                }
            }

            Section("Аккаунт облачной синхронизации") {
                HStack {
                    Text("Профиль Google")
                    Spacer()
                    Text(userEmail ?? "Синхронизирован")
                        .foregroundColor(.secondary)
                }
                Button(role: .destructive, action: onSignOut) {
                    Label("Выйти из аккаунта", systemImage: "rectangle.portrait.and.arrow.right")
                }
            }
        }
        .navigationTitle("Ещё и настройки")
    }
}
