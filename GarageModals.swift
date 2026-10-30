import SwiftUI

struct AddOrEditItemSheet: View {
    let initialItem: GarageItem?
    let availableZones: [String]
    let onSave: (GarageItem) -> Void
    let onDelete: (String) -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var title: String = ""
    @State private var description: String = ""
    @State private var priceText: String = ""
    @State private var quantityText: String = "1"
    @State private var zone: String = "Гараж"
    @State private var shelfLocation: String = "Стеллаж А1, полка 2"
    @State private var qrCode: String = GarageRepository.generateQrCode()
    @State private var barcode: String = GarageRepository.generateBarcode()
    @State private var serialCode: String = ""
    @State private var hintNote: String = ""
    @State private var photoUri: String = ""
    @State private var hintPhotoUri: String = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Основная информация") {
                    TextField("Название товара (напр. Компрессор 50л)", text: $title)
                    TextField("Цена в рублях (₽)", text: $priceText)
                        .keyboardType(.numberPad)
                    TextField("Количество на складе", text: $quantityText)
                        .keyboardType(.numberPad)
                    TextField("Описание для Авито / Юлы / ВК / Яндекса", text: $description, axis: .vertical)
                        .lineLimit(3...6)
                }

                Section("Точное место в гараже и фото-подсказка") {
                    TextField("Зона (Гараж, Склад, Бокс, Балкон)", text: $zone)
                    TextField("Стеллаж / полка / ящик", text: $shelfLocation)
                    TextField("Текстовый ориентир (чтобы не запутаться)", text: $hintNote)
                    TextField("URL или путь фото-подсказки полки", text: $hintPhotoUri)
                }

                Section("Коды учёта (QR, штрихкод, серийный номер)") {
                    HStack {
                        TextField("QR-код", text: $qrCode)
                        Button("Сгенерировать") {
                            qrCode = GarageRepository.generateQrCode()
                        }
                        .font(.caption.bold())
                    }
                    HStack {
                        TextField("Штрихкод EAN-13", text: $barcode)
                        Button("Новый ШК") {
                            barcode = GarageRepository.generateBarcode()
                        }
                        .font(.caption.bold())
                    }
                    TextField("Серийный номер / Артикул", text: $serialCode)
                }

                if let initialItem, !initialItem.id.isEmpty {
                    Section {
                        Button(role: .destructive) {
                            onDelete(initialItem.id)
                        } label: {
                            Label("Удалить товар со склада", systemImage: "trash")
                        }
                    }
                }
            }
            .navigationTitle(initialItem == nil ? "Новый товар" : "Редактировать")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Отмена") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Сохранить") {
                        var item = initialItem ?? GarageItem()
                        item.title = title.isEmpty ? "Товар со склада" : title
                        item.description = description
                        item.price = Int64(priceText) ?? 0
                        item.quantity = max(1, Int64(quantityText) ?? 1)
                        item.zone = zone.isEmpty ? "Гараж" : zone
                        item.shelfLocation = shelfLocation.isEmpty ? "Стеллаж А1" : shelfLocation
                        item.qrCode = qrCode.isEmpty ? GarageRepository.generateQrCode() : qrCode
                        item.barcode = barcode
                        item.serialCode = serialCode
                        item.hintNote = hintNote
                        item.photoUri = photoUri
                        item.hintPhotoUri = hintPhotoUri
                        onSave(item)
                    }
                    .fontWeight(.bold)
                }
            }
            .onAppear {
                if let item = initialItem {
                    title = item.title
                    description = item.description
                    priceText = "\(item.price)"
                    quantityText = "\(item.quantity)"
                    zone = item.zone
                    shelfLocation = item.shelfLocation
                    qrCode = item.qrCode
                    barcode = item.barcode
                    serialCode = item.serialCode
                    hintNote = item.hintNote
                    photoUri = item.photoUri
                    hintPhotoUri = item.hintPhotoUri
                }
            }
        }
    }
}

struct PhotoHintSheet: View {
    let item: GarageItem
    let onSaveHint: (String, String) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var noteText: String = ""
    @State private var photoUrlText: String = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Где лежит «\(item.title)»") {
                    LabeledContent("Зона хранения", value: item.zone)
                    LabeledContent("Полка / стеллаж", value: item.shelfLocation)
                    LabeledContent("QR-метка", value: item.qrCode)
                    if !item.barcode.isEmpty {
                        LabeledContent("Штрихкод", value: item.barcode)
                    }
                }

                Section("Фото-подсказка и ориентир для быстрого поиска") {
                    TextField("Подробный ориентир (напр. синий контейнер слева у ворот)", text: $noteText, axis: .vertical)
                        .lineLimit(2...5)
                    TextField("Ссылка / URI фотографии полки", text: $photoUrlText)
                }
            }
            .navigationTitle("Фото-подсказка полки")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Закрыть") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Обновить") {
                        onSaveHint(photoUrlText, noteText)
                    }
                    .fontWeight(.bold)
                }
            }
            .onAppear {
                noteText = item.hintNote
                photoUrlText = item.hintPhotoUri
            }
        }
    }
}

struct PlatformConnectionSheet: View {
    let connection: PlatformConnection
    let onSave: (PlatformConnection) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var isConnected: Bool = true
    @State private var accountIdentifier: String = ""
    @State private var apiClientId: String = ""
    @State private var defaultPhone: String = ""
    @State private var defaultAddress: String = ""
    @State private var autoSync: Bool = true
    @State private var autoPromote: Bool = false

    var body: some View {
        NavigationStack {
            Form {
                Section("Статус интеграции \(connection.platformName)") {
                    Toggle("Аккаунт привязан и активен", isOn: $isConnected)
                    TextField("ID профиля / Магазина / Группы ВК", text: $accountIdentifier)
                    TextField("API Ключ / Токен автозагрузки", text: $apiClientId)
                }

                Section("Контактные данные для объявлений") {
                    TextField("Телефон продавца", text: $defaultPhone)
                    TextField("Город / адрес гаража для самовывоза", text: $defaultAddress)
                    Toggle("Авто-синхронизация остатков", isOn: $autoSync)
                    Toggle("Авто-поднятие просмотров", isOn: $autoPromote)
                }
            }
            .navigationTitle(connection.platformName)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Отмена") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Привязать") {
                        var copy = connection
                        copy.isConnected = isConnected
                        copy.accountIdentifier = accountIdentifier
                        copy.apiClientId = apiClientId
                        copy.defaultContactPhone = defaultPhone
                        copy.defaultCityAddress = defaultAddress
                        copy.autoSync = autoSync
                        copy.autoPromote = autoPromote
                        copy.lastSyncStatus = isConnected ? "Аккаунт привязан • Готов к выгрузке" : "Отключено"
                        onSave(copy)
                    }
                    .fontWeight(.bold)
                }
            }
            .onAppear {
                isConnected = connection.isConnected || true
                accountIdentifier = connection.accountIdentifier
                apiClientId = connection.apiClientId
                defaultPhone = connection.defaultContactPhone
                defaultAddress = connection.defaultCityAddress
                autoSync = connection.autoSync
                autoPromote = connection.autoPromote
            }
        }
    }
}

struct QrScannerSheet: View {
    let items: [GarageItem]
    let onSelectItem: (GarageItem) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var codeQuery: String = ""

    var matchedItems: [GarageItem] {
        let q = codeQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if q.isEmpty { return items }
        return items.filter {
            $0.qrCode.lowercased().contains(q) ||
            $0.barcode.lowercased().contains(q) ||
            $0.serialCode.lowercased().contains(q) ||
            $0.title.lowercased().contains(q)
        }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                TextField("Введите или отсканируйте QR (#GAR-...) / Штрихкод", text: $codeQuery)
                    .textFieldStyle(.roundedBorder)
                    .padding(.horizontal, 16)

                List(matchedItems) { item in
                    Button {
                        onSelectItem(item)
                    } label: {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.title)
                                .font(.subheadline.bold())
                            Text("📍 \(item.zone) → \(item.shelfLocation)")
                                .font(.caption)
                                .foregroundColor(.orange)
                            Text("QR: \(item.qrCode) • ШК: \(item.barcode)")
                                .font(.caption2.monospaced())
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
            .navigationTitle("Сканер QR и Штрихкодов")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Закрыть") { dismiss() }
                }
            }
        }
    }
}

struct CreateBuyerDialogSheet: View {
    let items: [GarageItem]
    let onCreate: (BuyerDialog) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var buyerName: String = ""
    @State private var platformKey: String = PlatformType.avito.rawValue
    @State private var selectedItemId: String = ""
    @State private var initialQuestion: String = "Здравствуйте! Товар ещё в наличии? Можно сегодня забрать?"

    var body: some View {
        NavigationStack {
            Form {
                Section("Покупатель и площадка") {
                    TextField("Имя покупателя", text: $buyerName)
                    Picker("Площадка", selection: $platformKey) {
                        ForEach(PlatformType.allCases) { p in
                            Text(p.displayName).tag(p.rawValue)
                        }
                    }
                }

                Section("Товар со склада") {
                    Picker("Товар", selection: $selectedItemId) {
                        Text("Выберите товар").tag("")
                        ForEach(items) { item in
                            Text("\(item.title) (\(item.shelfLocation))").tag(item.id)
                        }
                    }
                    TextField("Сообщение покупателя", text: $initialQuestion, axis: .vertical)
                }
            }
            .navigationTitle("Новый диалог")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Отмена") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Создать") {
                        let matched = items.first(where: { $0.id == selectedItemId }) ?? items.first
                        let dlg = BuyerDialog(
                            buyerName: buyerName.isEmpty ? "Алексей (Покупатель)" : buyerName,
                            platformKey: platformKey,
                            itemId: matched?.id ?? "",
                            itemTitle: matched?.title ?? "Товар из гаража",
                            itemPrice: matched?.price ?? 0,
                            itemShelfLocation: matched?.shelfLocation ?? "Стеллаж А1",
                            itemQrCode: matched?.qrCode ?? "#GAR-101-I",
                            lastMessage: initialQuestion,
                            unreadCount: 1
                        )
                        onCreate(dlg)
                    }
                    .fontWeight(.bold)
                }
            }
        }
    }
}
