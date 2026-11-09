package pk.paksoft.pos.core.datastore

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRetain
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Stores one token per shop as a generic password (service = [service], account = shop id).
 * A failed read is treated as "no token"; a failed write throws (the caller must not believe it is logged in).
 * Token values are never logged.
 */
@OptIn(ExperimentalForeignApi::class)
class KeychainTokenStore(private val service: String = "pk.paksoft.pos.token") : TokenStore {

    override suspend fun read(shopId: String): String? = memScoped {
        val query = query(shopId)
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)
        if (status != errSecSuccess) return@memScoped null
        val data: CFDataRef = result.value?.reinterpret() ?: return@memScoped null
        try {
            val bytes = CFDataGetBytePtr(data) ?: return@memScoped null
            bytes.readBytes(CFDataGetLength(data).toInt()).decodeToString()
        } finally {
            CFRelease(data)
        }
    }

    override suspend fun write(shopId: String, token: String) {
        clear(shopId)
        val bytes = token.encodeToByteArray()
        val data = bytes.usePinned {
            CFDataCreate(kCFAllocatorDefault, it.addressOf(0).reinterpret(), bytes.size.toLong())
        } ?: error("Keychain: could not allocate token data")
        val query = query(shopId)
        CFDictionaryAddValue(query, kSecValueData, data)
        CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
        val status = SecItemAdd(query, null)
        CFRelease(query)
        CFRelease(data)
        check(status == errSecSuccess) { "Keychain write failed (status $status)" }
    }

    override suspend fun clear(shopId: String) {
        val query = query(shopId)
        SecItemDelete(query)
        CFRelease(query)
    }

    private fun query(shopId: String): CFMutableDictionaryRef {
        val dict = CFDictionaryCreateMutable(null, 0, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr)
            ?: error("Keychain: could not allocate query")
        CFDictionaryAddValue(dict, kSecClass, kSecClassGenericPassword)
        // The dictionary retains its values; release our +1 from the bridge afterwards.
        val svc = CFBridgingRetain(service)
        val acct = CFBridgingRetain(shopId)
        CFDictionaryAddValue(dict, kSecAttrService, svc)
        CFDictionaryAddValue(dict, kSecAttrAccount, acct)
        CFRelease(svc)
        CFRelease(acct)
        return dict
    }
}
