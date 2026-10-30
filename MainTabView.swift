import SwiftUI
import FirebaseAuth
import GoogleSignIn

enum GarageTabIOS: Int, CaseIterable {
    case home = 0
    case warehouse = 1
    case publish = 2
    case dialogs = 3
    case more = 4
}

struct MainTabView: View {
    let user: User
    @StateObject private var viewModel: GarageViewModel
    @State private var selectedTab: GarageTabIOS = .home

    @State private var editingItem: GarageItem? = nil
    @State private var showAddItemSheet = false
    @State private var photoHintItem: GarageItem? = nil
    @State private var selectedPlatformForConfig: PlatformConnection? = nil
    @State private var showQrScannerSheet = false
    @State private var showCreateDialogSheet = false

    init(user: User) {
        self.user = user
        _viewModel = StateObject(wrappedValue: GarageViewModel(userId: user.uid))
    }

    var unreadTotal: Int {
        Int(viewModel.dialogs.reduce(0) { $0 + $1.unreadCount })
    }

    var body: some View {
        ZStack(alignment: .top) {
            TabView(selection: $selectedTab) {
                NavigationStack {
                    HomeView(
                        viewModel: viewModel,
                        onAddItem: {
                            editingItem = nil
                            showAddItemSheet = true
                        },
                        onEditItem: { item in
                            editingItem = item
                            showAddItemSheet = true
                        },
                        onOpenPhotoHint: { item in
                            photoHintItem = item
                        },
                        onConfigurePlatform: { conn in
                            selectedPlatformForConfig = conn
                        },
                        onOpenScanner: {
                            showQrScannerSheet = true
                        },
                        onOpenDialogs: {
                            selectedTab = .dialogs
                        }
                    )
                }
                .tabItem {
                    Label("Главная", systemImage: "house.fill")
                }
                .tag(GarageTabIOS.home)

                NavigationStack {
                    WarehouseView(
                        viewModel: viewModel,
                        onAddItem: {
                            editingItem = nil
                            showAddItemSheet = true
                        },
                        onEditItem: { item in
                            editingItem = item
                            showAddItemSheet = true
                        },
                        onOpenPhotoHint: { item in
                            photoHintItem = item
                        },
                        onOpenScanner: {
                            showQrScannerSheet = true
                        }
                    )
                }
                .tabItem {
                    Label("Склад", systemImage: "shippingbox.fill")
                }
                .tag(GarageTabIOS.warehouse)

                NavigationStack {
                    PublishView(
                        viewModel: viewModel,
                        onAddItem: {
                            editingItem = nil
                            showAddItemSheet = true
                        },
                        onEditItem: { item in
                            editingItem = item
                            showAddItemSheet = true
                        },
                        onOpenPhotoHint: { item in
                            photoHintItem = item
                        },
                        onConfigurePlatform: { conn in
                            selectedPlatformForConfig = conn
                        }
                    )
                }
                .tabItem {
                    Label("Публикация", systemImage: "plus.circle.fill")
                }
                .tag(GarageTabIOS.publish)

                NavigationStack {
                    DialogsView(
                        viewModel: viewModel,
                        onCreateDialog: {
                            showCreateDialogSheet = true
                        },
                        onOpenPhotoHint: { item in
                            photoHintItem = item
                        }
                    )
                }
                .tabItem {
                    Label("Диалоги", systemImage: "bubble.left.and.bubble.right.fill")
                }
                .badge(unreadTotal > 0 ? unreadTotal : 0)
                .tag(GarageTabIOS.dialogs)

                NavigationStack {
                    MoreView(
                        viewModel: viewModel,
                        userEmail: user.email,
                        onOpenPhotoHint: { item in
                            photoHintItem = item
                        },
                        onSignOut: {
                            GIDSignIn.sharedInstance.signOut()
                            try? Auth.auth().signOut()
                        }
                    )
                }
                .tabItem {
                    Label("Ещё", systemImage: "ellipsis.circle.fill")
                }
                .tag(GarageTabIOS.more)
            }
            .tint(viewModel.accentPalette.primaryColor)

            if let banner = viewModel.publishProgress.completedBannerMessage {
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                    Text(banner)
                        .font(.caption.bold())
                        .foregroundColor(.white)
                    Spacer()
                    Button {
                        viewModel.publishProgress.completedBannerMessage = nil
                    } label: {
                        Image(systemName: "xmark")
                            .font(.caption)
                            .foregroundColor(.white)
                    }
                }
                .padding(12)
                .background(Color.green.opacity(0.25))
                .background(.ultraThinMaterial)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .padding(.horizontal, 16)
                .padding(.top, 8)
            }
        }
        .sheet(isPresented: $showAddItemSheet) {
            AddOrEditItemSheet(
                initialItem: editingItem,
                availableZones: viewModel.zones.map(\.name),
                onSave: { item in
                    viewModel.saveItem(item) {
                        showAddItemSheet = false
                    }
                },
                onDelete: { id in
                    viewModel.deleteItem(itemId: id)
                    showAddItemSheet = false
                }
            )
        }
        .sheet(item: $photoHintItem) { item in
            PhotoHintSheet(
                item: item,
                onSaveHint: { newUri, newNote in
                    var updated = item
                    updated.hintPhotoUri = newUri
                    updated.hintNote = newNote
                    viewModel.saveItem(updated) {
                        photoHintItem = nil
                    }
                }
            )
        }
        .sheet(item: $selectedPlatformForConfig) { conn in
            PlatformConnectionSheet(
                connection: conn,
                onSave: { updated in
                    viewModel.savePlatform(updated) {
                        selectedPlatformForConfig = nil
                    }
                }
            )
        }
        .sheet(isPresented: $showQrScannerSheet) {
            QrScannerSheet(
                items: viewModel.items,
                onSelectItem: { found in
                    showQrScannerSheet = false
                    photoHintItem = found
                }
            )
        }
        .sheet(isPresented: $showCreateDialogSheet) {
            CreateBuyerDialogSheet(
                items: viewModel.items,
                onCreate: { dlg in
                    viewModel.createDialog(dlg) {
                        showCreateDialogSheet = false
                    }
                }
            )
        }
    }
}
