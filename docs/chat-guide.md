# Huong dan chat theo thiet ke hien tai cua project

## Kien truc dang dung
- Realtime: SignalR hub `/hubs/chat` (token query param). Xu ly trong [app/src/main/java/com/example/saleapp/core/realtime/ChatHubManager.kt](../app/src/main/java/com/example/saleapp/core/realtime/ChatHubManager.kt).
- REST fallback: Retrofit service [app/src/main/java/com/example/saleapp/core/network/ChatApiService.kt](../app/src/main/java/com/example/saleapp/core/network/ChatApiService.kt) cho load conversation/messages, send message, mark read, close conversation, unread count.
- Repository: [app/src/main/java/com/example/saleapp/data/repository/ChatRepository.kt](../app/src/main/java/com/example/saleapp/data/repository/ChatRepository.kt) gom ca REST + SignalR, expose listener callbacks cho UI.
- UI: [app/src/main/java/com/example/saleapp/ui/chat/ChatFragment.kt](../app/src/main/java/com/example/saleapp/ui/chat/ChatFragment.kt) + [ChatViewModel](../app/src/main/java/com/example/saleapp/ui/chat/ChatViewModel.kt) + [ChatMessageAdapter](../app/src/main/java/com/example/saleapp/ui/chat/ChatMessageAdapter.kt).

## Thu vien su dung
- Microsoft SignalR client for Android (HubConnectionBuilder, callbacks).
- Retrofit + converter cho REST chat endpoints.
- Hilt DI, Coroutines/Flow, RecyclerView ListAdapter cho UI.

## Mo hinh du lieu
- Message DTO: `chatMessageId`, `conversationId`, `senderType` ("User"|"Shop"), `message`, `sentAt`, `readAt` (nullable). Xem [app/src/main/java/com/example/saleapp/data/model/response/ChatMessageDto.kt](../app/src/main/java/com/example/saleapp/data/model/response/ChatMessageDto.kt).
- Conversation DTO: `conversationId`, `userId`, `username`, `status`, `lastMessageAt`, `lastMessage`, `unreadCount`. Xem [app/src/main/java/com/example/saleapp/data/model/response/ChatConversationDto.kt](../app/src/main/java/com/example/saleapp/data/model/response/ChatConversationDto.kt).

## Dong chay chinh
- Ket noi: `ChatViewModel.connectToChat()` goi `ChatRepository.connectToChat()` -> `ChatHubManager.connect()`; cap nhat state Flow `connectionState` (Connecting/Connected/Disconnected).
- Khoi tao: `ChatFragment` goi `createOrGetConversation()` (REST) -> load messages qua REST; sau do lang nghe realtime.
- Nhan tin nhan realtime: SignalR events `ReceiveMessage` (tin den), `MessageSent` (tin cua user da duoc server xac nhan), `MessageRead`, `ShopTyping`.
- Gui tin nhan:
  - Neu hub da ket noi: optimistic UI tao message temp id am, append list; goi `ChatHubManager.sendMessage`. Khi server tra ve `MessageSent`, thay the temp bang message that, set state Success; neu loi, remove temp va hien error.
  - Neu khong ket noi: fallback REST `ChatRepository.sendMessageRest`, append message tra ve.
- Danh dau da doc: neu connected goi SignalR `MarkAsRead`, neu khong thi REST `PUT Chat/messages/{id}/read`.
- Typing: `sendTypingIndicator` qua hub; hub phat `ShopTyping` de hien indicator 3s tren UI.

## Cac trang thai UI (Flow)
- `conversationState`: Idle/Loading/Success/Error khi tao/lay conversation.
- `messagesState`: trang thai load danh sach messages ban dau.
- `messages`: list message hien thi.
- `newMessage`: thong bao message moi (de auto scroll).
- `connectionState`: string Connected/Connecting.../Disconnected de hien banner.
- `isTyping`: bool de hien typing indicator.
- `sendMessageState`: Loading/Success/Error de enable/disable nut send va hien toast loi.

## Luu y ky thuat
- SignalR URL dung token query param; dam bao token hop le truoc khi goi `connect()`.
- Quan ly lifecycle: disconnect trong `ChatFragment.onDestroyView` va `ChatViewModel.onCleared` de tranh rò ro ket noi.
- Temp message id giam dan am (`tempMessageId--`) de tranh trung voi id that tu server.
- Format thoi gian hien thi trong adapter bang `hh:mm a` (SimpleDateFormat locale default).

## Phat trien tiep
- Bo sung push notification (FCM) neu muon thong bao khi app nen nen.
- Them status read/delivered hien thi UI (hien tai chi xu ly su kien MessageRead nhung chua thay doi field). 
- Thao tac reconnect/backoff SignalR neu muon on dinh hon (hien tai chi connect khi mo fragment, disconnect khi dong).
