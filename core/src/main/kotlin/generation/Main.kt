package generation

import com.squareup.kotlinpoet.FileSpec
import generation.generator.generateDiffClass
import generation.generator.generateImmutableClass
import generation.generator.generateNetworkTransferFunctions
import generation.generator.generateStateClass
import org.reflections.util.ClasspathHelper
import java.lang.reflect.Modifier
import java.nio.file.Path
import java.nio.file.Paths

/*
что генерится:
    minus
    State plus Diff
    decode
    encode
    Immutable

про мап:
    если в стейте, то MutableMap<String, String>
    если в диффе, то MutableMap<String, String?>
    если в иммутабле, то Map<String, String>

правила для дифа:
	если null, то нет изменений
	иначе - изменения есть, причем переприсваивания только если поле не Map и не другой стейт
	не nullable только Map и id
	если в Map (k,v) v == null, значит надо удалить. иначе - поменять значение на v
 */

// https://stackoverflow.com/questions/46640670/how-to-configure-gradle-for-code-generation-so-that-intellij-recognises-generate

const val STATE_DEF_SUFFIX = "StateDef"
const val RESULT_PACKAGE_NAME = "state.gen"

fun main() {
    val saveDir = Paths.get("C:\\work\\gamedev\\wikigame\\wikigame2\\core\\src\\main\\kotlin")

    val stateDefs = loadTypeDescsByAnnotatiton(StateDef::class.java)

    stateDefs.forEach { typeDef ->
        check(typeDef.clazz.simpleName.endsWith(STATE_DEF_SUFFIX)) {
            "${typeDef.clazz.simpleName} should end with ...$STATE_DEF_SUFFIX"
        }
    }

    saveToFile("StateDiff", saveDir, stateDefs.map(::generateDiffClass)) { builder, it ->
        builder.addType(it)
    }

    saveToFile("ImmutableState", saveDir, stateDefs.map(::generateImmutableClass)) { builder, it ->
        builder.addType(it)
    }

    saveToFile("State", saveDir, stateDefs.map(::generateStateClass)) { builder, it ->
        builder.addType(it)
    }

//     TODO: check что все parent у event тоже transferable
    val transferables = loadTypeDescsByAnnotatiton(TransferableViaNetwork::class.java)
    val transferableFuncs = transferables.flatMap(::generateNetworkTransferFunctions)
    saveToFile("Transferable", saveDir, transferableFuncs) { builder, it ->
        builder.addFunction(it)
    }
}

private fun <T : Annotation> loadTypeDescsByAnnotatiton(annotation: Class<T>): List<TypeDesc> {
    val stateDefs = getAllAnnotatedTypes(
        annotation,
        ClasspathHelper.forPackage("state")
//        Paths.get("C:\\work\\gamedev\\wikigame\\codegeneration-test\\src\\main\\kotlin\\test").toUri().toURL()
//        Paths.get("C:\\work\\gamedev\\wikigame\\codegeneration-test\\build\\classes\\kotlin\\main").toUri().toURL()
    )

    stateDefs.forEach { typeDef ->
        val superclass = typeDef.clazz.superclass
        if (superclass != Object::class.java) {
            val parent = stateDefs.find { it.clazz == superclass }

            check(parent != null || Modifier.isAbstract(superclass.modifiers)) {
                "Parent ${superclass.simpleName} of ${typeDef.clazz.simpleName} should be annotated"
            }
            typeDef.parent = parent
        }
    }

    stateDefs.forEach { typeDef ->
        typeDef.isOpen = stateDefs.any { it.parent === typeDef }
    }
    return stateDefs
}

private fun <T> saveToFile(fileName: String, saveDir: Path, specs: List<T>, appl: (FileSpec.Builder, T) -> Unit) {
    val file = FileSpec.builder(RESULT_PACKAGE_NAME, fileName)
        .also { builder ->
            specs.forEach { appl(builder, it) }
        }
        .indent("    ")
        .build()
//    file.writeTo(System.out)
    file.writeTo(saveDir)
}
