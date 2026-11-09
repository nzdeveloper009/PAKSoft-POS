import ComposeApp
import SwiftUI

@main
struct iOSApp: App {
    init() {
        KoinInitIosKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
