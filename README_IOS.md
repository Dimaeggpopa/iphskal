# Мой гараж — Нативная версия для iPhone (iOS / SwiftUI / .ipa)

В папке `/ios` находится **полный нативный проект приложения «Мой гараж» для iPhone (iOS 16+ / iOS 18)** на языке **Swift (SwiftUI)** со всеми подключёнными экранами, облачной базой **Firebase Cloud Firestore**, авторизацией **Google Sign-In**, сканером QR/штрихкодов, фото-подсказками полок и публикацией в 1 клик на Авито, Юлу, ВК Маркет и Яндекс.Объявления.

---

## Состав файлов для iPhone (`/ios`)

1. **`/ios/project.yml`** — конфигурация проекта Xcode (`XcodeGen`) с таргетом `MyGarageIOS`, правами доступа к камере и фотогалерее, URL-схемами Google Sign-In и зависимостями Swift Package Manager.
2. **`/ios/Package.swift`** — манифест Swift Package Manager (`FirebaseAuth`, `FirebaseFirestore`, `GoogleSignIn`, `GoogleSignInSwift`).
3. **`/ios/MyGarageIOS/Info.plist`** — системный манифест iOS-приложения (`CFBundleDisplayName = Мой гараж`, разрешения камеры `NSCameraUsageDescription`, `CFBundleURLTypes`).
4. **`/ios/MyGarageIOS/GoogleService-Info.plist`** — готовый конфигурационный файл Firebase для iOS, подключённый к вашей облачной базе данных (`ai-studio-android-118a4e2a-5c69-4018-9cc3-1ff1d7dac084`).
5. **`/ios/MyGarageIOS/MyGarage.entitlements`** и **`ExportOptions.plist`** — настройки подписи и экспорта `.ipa`.
6. **`/ios/MyGarageIOS/MyGarageApp.swift`** — точка входа `@main` SwiftUI-приложения.
7. **`/ios/MyGarageIOS/Models/GarageModels.swift`** — модели данных (`GarageItem`, `PlatformConnection`, `BuyerDialog`, `StorageZone`, `PlatformType`).
8. **`/ios/MyGarageIOS/Services/GarageRepository.swift`** — сервис работы с Firebase Firestore в реальном времени.
9. **`/ios/MyGarageIOS/ViewModels/GarageViewModel.swift`** — логика публикации в 1 клик на 4 площадки, генерации QR/штрихкодов и управления складом.
10. **`/ios/MyGarageIOS/Views/`** — все экраны на SwiftUI:
    - `AuthView.swift` — вход через Google на iPhone
    - `MainTabView.swift` — нижняя панель вкладок iOS (`TabView`) и модальные шторки
    - `HomeView.swift` — главный дашборд и кнопка «Опубликовать всё в 1 клик»
    - `WarehouseView.swift` — склад, фильтр зон, поиск по QR и проверка инвентаризации
    - `PublishView.swift` — мульти-выгрузка на Авито, Юлу, ВК Маркет и Яндекс
    - `DialogsView.swift` — единый центр сообщений покупателей с быстрыми ответами и карточкой полки
    - `MoreView.swift` — управление темами iOS, экстренный радар полок, экспорт через AirDrop и конструктор стеллажей
    - `GarageModals.swift` — шторки добавления товара, фото-подсказки полки, привязки API площадок и QR-сканера
11. **`/ios/build_ipa.sh`** — скрипт автоматической сборки файла `MyGarageIOS.ipa`.

---

## Как установить на iPhone (3 способа)

### Способ 1. Сборка `.ipa` или запуск напрямую через Xcode (macOS)
1. Скачайте архив проекта из AI Studio (**Export ZIP** в меню настроек).
2. На Mac откройте терминал в папке `ios/` и выполните:
   ```bash
   chmod +x build_ipa.sh
   ./build_ipa.sh
   ```
   Либо откройте папку `ios/` напрямую в **Xcode 16+**, выберите свой iPhone в списке устройств и нажмите **Run (⌘R)**.

### Способ 2. Установка `.ipa` через AltStore / Sideloadly / Scarlet
1. Соберите `MyGarageIOS.ipa` скриптом `./build_ipa.sh` (или через GitHub Actions на базе `macos-latest`).
2. Передайте `MyGarageIOS.ipa` на iPhone и установите через **AltStore**, **Sideloadly** или **TestFlight**.

### Способ 3. Единая облачная база данных
Так как `GoogleService-Info.plist` (iOS) и `google-services.json` подключены к одной и той же базе Firestore (`ai-studio-android-118a4e2a-5c69-4018-9cc3-1ff1d7dac084`), все ваши товары, QR-коды и фото-подсказки полок мгновенно синхронизируются при входе под одним Google-аккаунтом.
