package network

object NetworkEntry {
    suspend operator fun invoke() {
        println("hi")
    }
}
