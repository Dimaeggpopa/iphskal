#!/usr/bin/env bash
set -euo pipefail

echo "=== Сборка Мой гараж для iPhone (iOS .ipa) ==="
cd "$(dirname "$0")"

if command -v xcodegen >/dev/null 2>&1; then
  echo "[1/3] Генерация Xcode-проекта MyGarageIOS.xcodeproj через XcodeGen..."
  xcodegen generate
else
  echo "Подсказка: Установите XcodeGen (brew install xcodegen) для автогенерации .xcodeproj или откройте Package.swift напрямую в Xcode 16+."
fi

if [ -d "MyGarageIOS.xcodeproj" ]; then
  echo "[2/3] Архивация iOS-приложения..."
  xcodebuild archive \
    -project MyGarageIOS.xcodeproj \
    -scheme MyGarageIOS \
    -configuration Release \
    -archivePath ./build/MyGarageIOS.xcarchive \
    -destination "generic/platform=iOS" \
    CODE_SIGNING_ALLOWED=NO

  echo "[3/3] Упаковка Payload в MyGarageIOS.ipa..."
  mkdir -p ./build/ipa/Payload
  cp -R ./build/MyGarageIOS.xcarchive/Products/Applications/MyGarageIOS.app ./build/ipa/Payload/
  (cd ./build/ipa && zip -qr ../MyGarageIOS.ipa Payload)
  echo "Готово! Файл для установки на iPhone создан: ios/build/MyGarageIOS.ipa"
fi
