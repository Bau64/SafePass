@file:Suppress("ArrayInDataClass")

package pfc.safepass.app.recycler

data class PassItem (
    val id: Int,
    val nickname: String,
    val user: String?,
    val password: String,
    val notes: String?,
    var icon: ByteArray?,
    val date: String
)
