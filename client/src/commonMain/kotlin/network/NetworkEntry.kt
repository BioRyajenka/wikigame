package network

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object NetworkEntry {
    suspend operator fun invoke()
}
