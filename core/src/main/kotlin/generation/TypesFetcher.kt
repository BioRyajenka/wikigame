package generation

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ConfigurationBuilder
import java.net.URL
import kotlin.properties.Delegates
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

data class PropertyDesc(
    val type: KType,
    val name: String
)

data class TypeDesc(
    val clazz: Class<*>,
    val constructorArguments: List<PropertyDesc>,
    val fields: List<PropertyDesc>,
) {
    var isOpen by Delegates.notNull<Boolean>()
    var parent: TypeDesc? = null
}

fun <T: Annotation> getAllAnnotatedTypes(annotation: Class<T>, vararg urls: URL) : List<TypeDesc> {
    return getAllAnnotatedTypes(annotation, *urls)
}

fun <T: Annotation> getAllAnnotatedTypes(annotation: Class<T>, urls: Collection<URL>) : List<TypeDesc> {
    require(urls.isNotEmpty())

    val reflections =
        Reflections(
            ConfigurationBuilder()
//                .setUrls(ClasspathHelper.forPackage(""))
                .setUrls(urls)
                .setScanners(
                    SubTypesScanner(false),
                    TypeAnnotationsScanner()
                )
        )

    val types = reflections.getTypesAnnotatedWith(annotation)

    return types.map { type ->
        val constructorArguments = type.kotlin.primaryConstructor!!.parameters.map {
            PropertyDesc(it.type, it.name!!)
        }

        val fields = type.kotlin.declaredMemberProperties.map { field ->
            PropertyDesc(field.returnType, field.name)
        }

        TypeDesc(type, constructorArguments, fields)
    }
}
