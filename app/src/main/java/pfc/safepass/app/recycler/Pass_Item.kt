package pfc.safepass.app.recycler

data class Pass_Item (
    val id: Int,
    val nickname: String,
    val user: String?,
    val password: String,
    val notes: String?,
    var icon: ByteArray?,
    val date: String
)
