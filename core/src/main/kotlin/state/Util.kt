package core.state

// util

class VariableWithEmptyValue<T> private constructor(private var value: T? = null) {
    // null means default value

    companion object {
        private val EMPTY = VariableWithEmptyValue<Any>(null)

        @Suppress("UNCHECKED_CAST")
        fun <T> empty() = EMPTY as VariableWithEmptyValue<T>
        fun <T> ofValue(value: T) = VariableWithEmptyValue(value)
    }
}
