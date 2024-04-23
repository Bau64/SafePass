package pfc.safepass.app

data class Pass_Item (
    val id: Int,
    val nickname: String,
<<<<<<< Updated upstream
    val user: String,
    val password: String,
    val link: String,
    var icon: ByteArray
=======
    val user: String?,
    val password: String,
    val link: String?,
    var icon: ByteArray?
>>>>>>> Stashed changes
)
