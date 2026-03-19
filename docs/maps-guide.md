# Huong dan map theo thiet ke hien tai cua project

## Cach lam dang dung
- Khong dung Google Maps SDK. Map duoc render bang WebView va HTML tu sinh trong [app/src/main/java/com/example/saleapp/ui/store/StoreMapFragment.kt](../app/src/main/java/com/example/saleapp/ui/store/StoreMapFragment.kt).
- HTML nhung iframe Google Maps embed va fallback sang OpenStreetMap embed neu nguon dau tien khong tai duoc. Map chi la anh/iframe, khong co SDK native.
- JS bridge `AndroidBridge` xu ly cac hanh dong:
  - `onStoreMarkerClick(locationId)`: mo bottom sheet chi tiet chi nhanh.
  - `openExternalMap(lat, lon, label)`: mo Google Maps app qua intent `geo:` (fallback URL https://maps.google.com/... neu khong co app).
  - `reportMapLoadFailure()`: goi chan doan ket noi tu Kotlin (HTTP GET toi maps.google.com, openstreetmap.org...) va hien trang thai tren trang HTML.
- Danh sach store lay tu `StoreMapViewModel` (UiState). Khi co du lieu se goi `window.addStores(payload)` trong WebView de ve danh sach va cap iframe.
- API key Google Maps trong manifest dang placeholder `YOUR_MAPS_API_KEY` va khong duoc su dung vi khong dung SDK.

## Dong chay man hinh StoreMapFragment
1) Khoi tao WebView: bat JavaScript + DOM storage, cam file/content access. Load HTML tu `buildNoKeyMapHtml()` (nhung iframe, list store, JS bridge).
2) Lang nghe state tu ViewModel:
   - Loading: hien progress.
   - Success: luu danh sach store, render vao WebView (hoac cho WebView onPageFinished roi moi render).
   - Error: thong bao toast.
3) Tu WebView: khi nhan nut "Mo Google Maps" se mo intent ngoai; khi click store se goi bridge -> mo bottom sheet chi tiet.

## Cac file lien quan
- Fragment va HTML sinh: [app/src/main/java/com/example/saleapp/ui/store/StoreMapFragment.kt](../app/src/main/java/com/example/saleapp/ui/store/StoreMapFragment.kt)
- Bottom sheet chi tiet store: [app/src/main/java/com/example/saleapp/ui/store/StoreDetailBottomSheetFragment.kt](../app/src/main/java/com/example/saleapp/ui/store/StoreDetailBottomSheetFragment.kt)
- Manifest meta-data API key: [app/src/main/AndroidManifest.xml](../app/src/main/AndroidManifest.xml)

## Ghi chu ky thuat
- Vi khong dung SDK, khong can quyen location; map chi hien toa do tu backend. Neu sau nay muon hien vi tri hien tai thi moi xin quyen va truyen vao HTML.
- Embed Google Maps co the bi gioi han tren mot so emulator/khong co Play Services; da co fallback OpenStreetMap.
- Neu muon doi sang Google Maps SDK: can them phu thuoc Play Services Maps, xin quyen location, tao `SupportMapFragment`, dung API key that; hien tai code khong lam dieu nay.