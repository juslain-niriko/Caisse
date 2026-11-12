package mg.caisse.ui.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val STUDENTS = "students"
    const val TUITION = "tuition"
    const val REPORTS = "reports"
    const val ENTRIES = "entries"
    const val ACTIVITIES = "activities"
    const val INVESTMENTS = "investments"
    const val SETTINGS = "settings"

    const val STUDENT_DETAIL = "student/{id}"
    const val FORM = "form/{mode}/{txId}?studentId={studentId}"

    fun studentDetail(id: Long) = "student/$id"

    fun form(mode: String, txId: Long = 0L, studentId: Long = -1L) =
        "form/$mode/$txId?studentId=$studentId"
}
