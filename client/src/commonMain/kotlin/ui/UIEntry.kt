package ui

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object UIEntry {
    suspend operator fun invoke()
}
