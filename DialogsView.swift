import SwiftUI

struct DialogsView: View {
    @ObservedObject var viewModel: GarageViewModel
    let onCreateDialog: () -> Void
    let onOpenPhotoHint: (GarageItem) -> Void

    @State private var selectedDialog: BuyerDialog? = nil
    @State private var draftMessage: String = ""

    var body: some View {
        List {
            if viewModel.dialogs.isEmpty {
                Section {
                    VStack(spacing: 10) {
                        Image(systemName: "bubble.left.and.bubble.right")
                            .font(.largeTitle)
                            .foregroundColor(.orange)
                        Text("Единый центр сообщений (Авито, Юла, ВК, Яндекс)")
                            .font(.subheadline.bold())
                            .multilineTextAlignment(.center)
                        Text("Все вопросы покупателей с привязкой к полке и QR-коду товара отображаются здесь.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                        Button("Создать диалог с покупателем", action: onCreateDialog)
                            .buttonStyle(.borderedProminent)
                            .tint(.orange)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                }
            } else {
                ForEach(viewModel.dialogs) { dlg in
                    let pType = PlatformType.fromKey(dlg.platformKey)
                    Button {
                        viewModel.markDialogRead(dlg)
                        selectedDialog = dlg
                    } label: {
                        HStack(spacing: 12) {
                            Circle()
                                .fill(pType.brandColor.opacity(0.25))
                                .frame(width: 44, height: 44)
                                .overlay(
                                    Text(String(dlg.buyerName.prefix(1)).uppercased())
                                        .font(.headline)
                                        .foregroundColor(pType.brandColor)
                                )

                            VStack(alignment: .leading, spacing: 4) {
                                HStack {
                                    Text(dlg.buyerName)
                                        .font(.subheadline.bold())
                                        .foregroundColor(.white)
                                    Text("• \(pType.shortName)")
                                        .font(.caption.bold())
                                        .foregroundColor(pType.brandColor)
                                    Spacer()
                                    Text(dlg.timeLabel)
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                }

                                Text("\(dlg.itemTitle) • \(formatRubles(dlg.itemPrice))")
                                    .font(.caption)
                                    .foregroundColor(.orange)
                                    .lineLimit(1)

                                Text(dlg.lastMessage)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                    .lineLimit(1)
                            }

                            if dlg.unreadCount > 0 {
                                Text("\(dlg.unreadCount)")
                                    .font(.caption2.bold())
                                    .foregroundColor(.white)
                                    .padding(6)
                                    .background(Color.red)
                                    .clipShape(Circle())
                            }
                        }
                    }
                    .swipeActions {
                        Button(role: .destructive) {
                            viewModel.deleteDialog(dialogId: dlg.id)
                        } label: {
                            Label("Удалить", systemImage: "trash")
                        }
                    }
                }
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("Диалоги")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: onCreateDialog) {
                    Label("Новый чат", systemImage: "plus.bubble.fill")
                }
            }
        }
        .sheet(item: $selectedDialog) { dialog in
            NavigationStack {
                VStack(spacing: 0) {
                    // Item & Shelf Quick Bar
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(dialog.itemTitle)
                                .font(.subheadline.bold())
                            Text("📍 \(dialog.itemShelfLocation.isEmpty ? "Стеллаж А1" : dialog.itemShelfLocation) • QR: \(dialog.itemQrCode)")
                                .font(.caption)
                                .foregroundColor(.orange)
                        }
                        Spacer()
                        if let linkedItem = viewModel.items.first(where: { $0.id == dialog.itemId || $0.qrCode == dialog.itemQrCode }) {
                            Button {
                                selectedDialog = nil
                                onOpenPhotoHint(linkedItem)
                            } label: {
                                Label("Фото полки", systemImage: "camera.fill")
                                    .font(.caption.bold())
                            }
                            .buttonStyle(.borderedProminent)
                            .tint(.orange)
                        }
                    }
                    .padding(12)
                    .background(Color(red: 0.11, green: 0.13, blue: 0.18))

                    ScrollView {
                        VStack(spacing: 10) {
                            let liveDialog = viewModel.dialogs.first(where: { $0.id == dialog.id }) ?? dialog
                            ForEach(liveDialog.parsedMessages()) { msg in
                                HStack {
                                    if msg.isOutgoing { Spacer() }
                                    VStack(alignment: msg.isOutgoing ? .trailing : .leading, spacing: 4) {
                                        Text(msg.text)
                                            .font(.subheadline)
                                            .padding(10)
                                            .background(msg.isOutgoing ? Color.orange : Color(red: 0.16, green: 0.18, blue: 0.24))
                                            .foregroundColor(.white)
                                            .clipShape(RoundedRectangle(cornerRadius: 14))
                                        Text(msg.time)
                                            .font(.caption2)
                                            .foregroundColor(.secondary)
                                    }
                                    if !msg.isOutgoing { Spacer() }
                                }
                            }
                        }
                        .padding(16)
                    }

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(viewModel.quickReplyTemplates, id: \.self) { tpl in
                                Button {
                                    let liveDialog = viewModel.dialogs.first(where: { $0.id == dialog.id }) ?? dialog
                                    viewModel.sendMessage(dialog: liveDialog, text: tpl, isFromMe: true)
                                } label: {
                                    Text(tpl)
                                        .font(.caption)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 6)
                                        .background(Color.orange.opacity(0.18))
                                        .foregroundColor(.orange)
                                        .clipShape(Capsule())
                                }
                            }
                        }
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                    }

                    HStack(spacing: 8) {
                        TextField("Написать ответ покупателю...", text: $draftMessage)
                            .textFieldStyle(.roundedBorder)
                        Button {
                            let liveDialog = viewModel.dialogs.first(where: { $0.id == dialog.id }) ?? dialog
                            viewModel.sendMessage(dialog: liveDialog, text: draftMessage, isFromMe: true)
                            draftMessage = ""
                        } label: {
                            Image(systemName: "paperplane.fill")
                                .padding(10)
                                .background(Color.orange)
                                .foregroundColor(.white)
                                .clipShape(Circle())
                        }
                    }
                    .padding(12)
                }
                .navigationTitle(dialog.buyerName)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button("Готово") {
                            selectedDialog = nil
                        }
                    }
                }
            }
        }
    }
}
