package com.sayales.app



import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.PropertyResolver
import org.springframework.stereotype.Component
import org.telegram.telegrambots.ApiContextInitializer
import java.io.UnsupportedEncodingException
import java.lang.reflect.Field
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.stream.Stream
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


@SpringBootConfiguration
@ComponentScan(basePackages = arrayOf("com.sayales.*"))
class App

@Configuration
class Config

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class SecuredValue(val propName: String, val envName: String)

@Component
class SecurePropertyBeanPostProcessor(@Autowired val propertyResolver: PropertyResolver) : BeanPostProcessor {


    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val declaredFields = bean.javaClass.declaredFields
        Stream.of(*declaredFields).filter { Objects.nonNull(it.getAnnotation(SecuredValue::class.java))}.peek{it.isAccessible = true}.forEach { setSecuredValue(it, bean) }
        return bean
    }

    private fun setSecuredValue(field: Field, bean: Any) {
        val securedValue = field.getAnnotation(SecuredValue::class.java)
        val propertyEncrypted = propertyResolver.resolvePlaceholders(securedValue.propName)
        val propertyDecrypted = AES.decrypt(propertyEncrypted ?: "", System.getenv(securedValue.envName))
        field.set(bean, propertyDecrypted)
    }
}


public fun main(args: Array<String>) {
    ApiContextInitializer.init()
    SpringApplication.run(App::class.java, *args);
}


private object AES {

    private var secretKey: SecretKeySpec? = null
    private var key: ByteArray? = null

    fun setKey(myKey: String) {
        val sha: MessageDigest?
        try {
            key = myKey.toByteArray(charset("UTF-8"))
            sha = MessageDigest.getInstance("SHA-1")
            key = sha!!.digest(key)
            key = (key!!).copyOf(16)
            secretKey = SecretKeySpec(key!!, "AES")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

    }

    fun decrypt(strToDecrypt: String, secret: String): String? {
        try {
            setKey(secret)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            return String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)))
        } catch (e: Exception) {
            println("Error while decrypting: $e")
        }

        return null
    }
}